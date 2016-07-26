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
        log.info("Entering EroPCE.");

        UrnE srcPortURN = requestPipe.getAJunction().getFixtures().iterator().next().getPortUrn();
        UrnE dstPortURN = requestPipe.getZJunction().getFixtures().iterator().next().getPortUrn();
        TopoVertex srcPort = new TopoVertex(srcPortURN.getUrn(), VertexType.PORT);
        TopoVertex dstPort = new TopoVertex(dstPortURN.getUrn(), VertexType.PORT);
        String srcPortName = srcPortURN.getUrn();
        String dstPortName = dstPortURN.getUrn();

        Topology multiLayerTopoAzDirection = topoService.getMultilayerTopology();
        Topology multiLayerTopoZaDirection = topoService.getMultilayerTopology();   // Same as AZ initially

        List<String> requestedAzERO = requestPipe.getAzERO().stream().collect(Collectors.toList());
        List<String> requestedZaERO = requestPipe.getZaERO().stream().collect(Collectors.toList());

        if(!requestedAzERO.get(0).equals(requestPipe.getAJunction().getDeviceUrn().getUrn()) || !requestedAzERO.get(requestedAzERO.size()-1).equals(requestPipe.getZJunction().getDeviceUrn().getUrn()))
        {
            throw new PCEException("Requested ERO must begin at source-device URN, and terminate at destination-device URN.");
        }

        if(!requestedZaERO.get(0).equals(requestPipe.getZJunction().getDeviceUrn().getUrn()) || !requestedZaERO.get(requestedZaERO.size()-1).equals(requestPipe.getAJunction().getDeviceUrn().getUrn()))
        {
            throw new PCEException("Requested ERO must begin at source-device URN, and terminate at destination-device URN.");
        }

        if(requestedAzERO.isEmpty() || requestedZaERO.isEmpty())
            throw new PCEException("Requested ERO must include at least one URN.");

        requestedAzERO.add(0, srcPortName);
        requestedAzERO.add(dstPortName);
        requestedZaERO.add(0, dstPortName);
        requestedZaERO.add(srcPortName);

        Set<TopoVertex> azURNs = multiLayerTopoAzDirection.getVertices().stream()
                .filter(v -> requestedAzERO.contains(v.getUrn()))
                .collect(Collectors.toSet());

        Set<TopoVertex> zaURNs = multiLayerTopoZaDirection.getVertices().stream()
                .filter(v -> requestedZaERO.contains(v.getUrn()))
                .collect(Collectors.toSet());

        Set<TopoEdge> edgestoKeepAz = multiLayerTopoAzDirection.getEdges().stream()
                .filter(e -> (azURNs.contains(e.getA()) && azURNs.contains(e.getZ())) || (azURNs.contains(e.getZ()) && azURNs.contains(e.getA())))
                .collect(Collectors.toSet());

        Set<TopoEdge> edgestoKeepZa = multiLayerTopoZaDirection.getEdges().stream()
                .filter(e -> (zaURNs.contains(e.getA()) && zaURNs.contains(e.getZ())) || (zaURNs.contains(e.getZ()) && zaURNs.contains(e.getA())))
                .collect(Collectors.toSet());

        // Prune all URNs from topology not matching specified EROs
        multiLayerTopoAzDirection.getVertices().retainAll(azURNs);
        multiLayerTopoZaDirection.getVertices().retainAll(zaURNs);

        // Prune all Edges from topology not beginning/ending at specified ERO URNs
        multiLayerTopoAzDirection.getEdges().retainAll(edgestoKeepAz);
        multiLayerTopoZaDirection.getEdges().retainAll(edgestoKeepZa);

        for(TopoEdge badEdge : multiLayerTopoAzDirection.getEdges())
        {
            if(badEdge.getA().getVertexType().equals(VertexType.SWITCH) || badEdge.getZ().getVertexType().equals(VertexType.SWITCH))
            {
                if(!multiLayerTopoZaDirection.getVertices().contains(badEdge.getA()) && !multiLayerTopoZaDirection.getVertices().contains(badEdge.getZ()))
                {
                    throw new PCEException("All ETHERNET-layer devices and ports must be represented in both the forward-direction and reverse-direction EROs");
                }
            }
        }

        for(TopoEdge badEdge : multiLayerTopoZaDirection.getEdges())
        {
            if(badEdge.getA().getVertexType().equals(VertexType.SWITCH) || badEdge.getZ().getVertexType().equals(VertexType.SWITCH))
            {
                if(!multiLayerTopoAzDirection.getVertices().contains(badEdge.getA()) && !multiLayerTopoAzDirection.getVertices().contains(badEdge.getZ()))
                {
                    throw new PCEException("All ETHERNET-layer devices and ports must be represented in both the forward-direction and reverse-direction EROs");
                }
            }
        }

        // Bandwidth and Vlan pruning
        Topology prunedTopoAZ = pruningService.pruneWithPipeAZ(multiLayerTopoAzDirection, requestPipe, requestSched, rsvBwList, rsvVlanList);
        Topology prunedTopoZA = pruningService.pruneWithPipeZA(multiLayerTopoZaDirection, requestPipe, requestSched, rsvBwList, rsvVlanList);


        if(!prunedTopoAZ.equals(multiLayerTopoAzDirection))
            throw new PCEException("Requested AZ ERO unavailable; failed to complete Patch Computation");
        if(!prunedTopoZA.equals(multiLayerTopoZaDirection))
            throw new PCEException("Requested ZA ERO unavailable; failed to complete Patch Computation");

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
