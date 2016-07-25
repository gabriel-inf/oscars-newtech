package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.resv.ent.ReservedVlanE;
import net.es.oscars.resv.ent.ScheduleSpecificationE;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.VertexType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeremy on 7/22/16.
 */
@Slf4j
@Component
public class EroPCE
{
    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    /**
     * Depends on DijkstraPCE to construct the Physical-Layer EROs for a request after pruning the topology based on requested ERO parameters
     *
     * @param requestPipe Requested pipe with required reservation parameters, and non-empty ERO specifications
     * @param requestSched Requested schedule parameters
     * @return A two-element Map containing both the forward-direction (A->Z) ERO and the reverse-direction (Z->A) ERO
     * @throws PCEException
     */
    public Map<String, List<TopoEdge>> computeSpecifiedERO(RequestedVlanPipeE requestPipe, ScheduleSpecificationE requestSched, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        Topology multiLayerTopo = topoService.getMultilayerTopology();
        Topology multiLayerTopoAzDirection = topoService.getMultilayerTopology();
        Topology multiLayerTopoZaDirection = topoService.getMultilayerTopology();

        List<String> requestedAzERO = requestPipe.getAzERO();
        List<String> requestedZaERO = requestPipe.getZaERO();

        Set<TopoVertex> azURNs = multiLayerTopo.getVertices().stream()
                .filter(v -> requestedAzERO.contains(v.getUrn()))
                .collect(Collectors.toSet());

        Set<TopoVertex> zaURNs = multiLayerTopo.getVertices().stream()
                .filter(v -> requestedZaERO.contains(v.getUrn()))
                .collect(Collectors.toSet());

        Set<TopoVertex> bidirectionalURNs = new HashSet<>();        // Only needed for bidirectional pruning

        for(TopoVertex az : azURNs)
        {
            if(zaURNs.contains(az))
            {
                bidirectionalURNs.add(az);
            }
        }

        Set<TopoEdge> edgestoKeepAz = multiLayerTopoAzDirection.getEdges().stream()
                .filter(e -> (azURNs.contains(e.getA()) && azURNs.contains(e.getZ())) || (azURNs.contains(e.getZ()) && azURNs.contains(e.getA())))
                .collect(Collectors.toSet());

        Set<TopoEdge> edgestoKeepZa = multiLayerTopoZaDirection.getEdges().stream()
                .filter(e -> (zaURNs.contains(e.getA()) && zaURNs.contains(e.getZ())) || (zaURNs.contains(e.getZ()) && zaURNs.contains(e.getA())))
                .collect(Collectors.toSet());

        Set<TopoEdge> edgestoKeepBidirectional = multiLayerTopo.getEdges().stream()
                .filter(e -> (bidirectionalURNs.contains(e.getA()) && bidirectionalURNs.contains(e.getZ())) || (bidirectionalURNs.contains(e.getZ()) && bidirectionalURNs.contains(e.getA())))
                .collect(Collectors.toSet());

        // Prune all URNs from topology not matching specified EROs
        multiLayerTopoAzDirection.getVertices().retainAll(azURNs);
        multiLayerTopoZaDirection.getVertices().retainAll(zaURNs);
        multiLayerTopo.getVertices().retainAll(bidirectionalURNs);

        // Prune all Edges from topology not beginning/ending at specified ERO URNs
        multiLayerTopoAzDirection.getEdges().retainAll(edgestoKeepAz);
        multiLayerTopoZaDirection.getEdges().retainAll(edgestoKeepZa);
        multiLayerTopo.getEdges().retainAll(edgestoKeepBidirectional);

        // Bandwidth and Vlan pruning
        Topology prunedTopoAZ = pruningService.pruneWithPipeAZ(multiLayerTopoAzDirection, requestPipe, requestSched, rsvBwList, rsvVlanList);
        Topology prunedTopoZA = pruningService.pruneWithPipeZA(multiLayerTopoZaDirection, requestPipe, requestSched, rsvBwList, rsvVlanList);
        //TODO: make sure the following line prunes based on the combination of both directions!!!!!
        Topology prunedTopo = pruningService.pruneWithPipe(multiLayerTopo, requestPipe, requestSched, rsvBwList, rsvVlanList);

        if(!prunedTopoAZ.equals(multiLayerTopoAzDirection))
            throw new PCEException("Requested AZ ERO unavailable; failed to complete Patch Computation");
        if(!prunedTopoZA.equals(multiLayerTopoZaDirection))
            throw new PCEException("Requested ZA ERO unavailable; failed to complete Patch Computation");
        if(!prunedTopo.equals(multiLayerTopo))
            throw new PCEException("Requested Bidirectionl ERO unavailable; failed to complete Patch Computation");

        UrnE srcPortURN = requestPipe.getAJunction().getFixtures().iterator().next().getPortUrn();
        UrnE dstPortURN = requestPipe.getZJunction().getFixtures().iterator().next().getPortUrn();

        TopoVertex srcPort = new TopoVertex(srcPortURN.getUrn(), VertexType.PORT);
        TopoVertex dstPort = new TopoVertex(dstPortURN.getUrn(), VertexType.PORT);


        // Shortest path routing
        List<TopoEdge> azEroCalculated = dijkstraPCE.computeShortestPathEdges(prunedTopoAZ, srcPort, dstPort);
        List<TopoEdge> zaEroCalculated = dijkstraPCE.computeShortestPathEdges(prunedTopoZA, dstPort, srcPort);

        if (azEroCalculated.isEmpty() || zaEroCalculated.isEmpty())
        {
            throw new PCEException("Empty path from Symmetric PCE");
        }

        List<TopoVertex> azEroVertices = dijkstraPCE.translatePathEdgesToVertices(azEroCalculated);
        List<TopoVertex> zaEroVertices = dijkstraPCE.translatePathEdgesToVertices(zaEroCalculated);
        List<String> azEroStrings = dijkstraPCE.translatePathVerticesToStrings(azEroVertices);
        List<String> zaEroStrings = dijkstraPCE.translatePathVerticesToStrings(zaEroVertices);

        if(!azEroStrings.equals(requestedAzERO) || !zaEroStrings.equals(requestedZaERO))
        {
            throw new PCEException("Requested ERO unavailable; failed to complete Patch Computation");
        }

        Map<String, List<TopoEdge>> theMap = new HashMap<>();
        theMap.put("az", azEroCalculated);
        theMap.put("za", zaEroCalculated);

        return theMap;
    }
}
