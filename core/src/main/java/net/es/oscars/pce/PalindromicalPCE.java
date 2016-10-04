package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.resv.ent.ReservedVlanE;
import net.es.oscars.resv.ent.ScheduleSpecificationE;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.dto.topo.VertexType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by jeremy on 6/22/16.
 */
@Slf4j
@Component
public class PalindromicalPCE
{
    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    /**
     * Depends on DijkstraPCE to construct the Physical-Layer EROs for a request
     *
     * @param requestPipe Requested pipe with required reservation parameters
     * @param requestSched Requested schedule parameters
     * @return A two-element Map containing both the forward-direction (A->Z) ERO and the reverse-direction (Z->A) ERO
     * @throws PCEException
     */
    public Map<String, List<TopoEdge>> computePalindromicERO(RequestedVlanPipeE requestPipe, ScheduleSpecificationE requestSched, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        Topology multiLayerTopo = topoService.getMultilayerTopology();

        UrnE srcPortURN = requestPipe.getAJunction().getFixtures().iterator().next().getPortUrn();
        UrnE dstPortURN = requestPipe.getZJunction().getFixtures().iterator().next().getPortUrn();

        TopoVertex srcPort = new TopoVertex(srcPortURN.getUrn(), VertexType.PORT);
        TopoVertex dstPort = new TopoVertex(dstPortURN.getUrn(), VertexType.PORT);

        // Bandwidth and Vlan pruning
        Topology prunedTopo = pruningService.pruneWithPipe(multiLayerTopo, requestPipe, requestSched, rsvBwList, rsvVlanList);

        // Shortest path routing
        List<TopoEdge> azERO = dijkstraPCE.computeShortestPathEdges(prunedTopo, srcPort, dstPort);

        if (azERO.isEmpty())
        {
            throw new PCEException("Empty path from Symmetric PCE");
        }

        // Get symmetric path in reverse-direction
        List<TopoEdge> zaERO = new LinkedList<>();

        // 1. Reverse the links
        for(TopoEdge azEdge : azERO)
        {
            Optional<TopoEdge> reverseEdge = prunedTopo.getEdges().stream()
                    .filter(r -> r.getA().equals(azEdge.getZ()))
                    .filter(r -> r.getZ().equals(azEdge.getA()))
                    .findFirst();

            if(reverseEdge.isPresent())
                zaERO.add(reverseEdge.get());
        }

        // 2. Reverse the order
        Collections.reverse(zaERO);

        assert(azERO.size() == zaERO.size());

        Map<String, List<TopoEdge>> theMap = new HashMap<>();
        theMap.put("az", azERO);
        theMap.put("za", zaERO);

        return theMap;
    }
}
