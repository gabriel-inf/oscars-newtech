package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.*;
import net.es.oscars.servicetopo.ServiceLayerTopology;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.*;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeremy on 6/22/16.
 */
@Slf4j
@Component
public class NonPalindromicalPCE
{
    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ServiceLayerTopology serviceLayerTopology;

    /**
     * Depends on DijkstraPCE and ServiceLayerTopology to construct and build the Service-Layer EROs, and then map them to Physical-Layer EROs
     *
     * @param requestPipe Requested pipe with required reservation parameters
     * @param requestSched Requested schedule parameters
     * @return A two-element Map containing both the forward-direction (A->Z) ERO and the reverse-direction (Z->A) ERO
     * @throws PCEException
     */
    public Map<String, List<TopoEdge>> computeNonPalindromicERO(RequestedVlanPipeE requestPipe, ScheduleSpecificationE requestSched, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        Topology ethTopo = topoService.layer(Layer.ETHERNET);
        Topology intTopo = topoService.layer(Layer.INTERNAL);
        Topology mplsTopo = topoService.layer(Layer.MPLS);

        // Filter MPLS-ports and MPLS-devices out of ethTopo
        Set<TopoVertex> portsOnly = ethTopo.getVertices().stream()
                .filter(v -> v.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());

        for(TopoEdge intEdge : intTopo.getEdges())
        {
            TopoVertex vertA = intEdge.getA();
            TopoVertex vertZ = intEdge.getZ();

            if(portsOnly.isEmpty())
            {
                break;
            }

            if(portsOnly.contains(vertA))
            {
                if(!vertZ.getVertexType().equals(VertexType.ROUTER))
                {
                    portsOnly.remove(vertA);
                }
            }
        }

        ethTopo.getVertices().removeIf(v -> v.getVertexType().equals(VertexType.ROUTER));
        ethTopo.getVertices().removeAll(portsOnly);

        // Filter Devices and Ports out of intTopo
        intTopo.getVertices().removeAll(intTopo.getVertices());

        /* These calls only need to be made once when topology is updated */
        serviceLayerTopology.setTopology(ethTopo);
        serviceLayerTopology.setTopology(intTopo);
        serviceLayerTopology.setTopology(mplsTopo);

        serviceLayerTopology.createMultilayerTopology();
        /* * */

        serviceLayerTopology.resetLogicalLinks();


        UrnE srcDeviceURN = requestPipe.getAJunction().getDeviceUrn();
        UrnE dstDeviceURN = requestPipe.getZJunction().getDeviceUrn();

        VertexType srcType = topoService.getVertexTypeFromDeviceType(srcDeviceURN.getDeviceType());
        VertexType dstType = topoService.getVertexTypeFromDeviceType(dstDeviceURN.getDeviceType());

        TopoVertex srcDevice = new TopoVertex(srcDeviceURN.getUrn(), srcType);
        TopoVertex dstDevice = new TopoVertex(dstDeviceURN.getUrn(), dstType);

        UrnE srcPortURN = requestPipe.getAJunction().getFixtures().iterator().next().getPortUrn();
        UrnE dstPortURN = requestPipe.getZJunction().getFixtures().iterator().next().getPortUrn();

        TopoVertex srcPort = new TopoVertex(srcPortURN.getUrn(), VertexType.PORT);
        TopoVertex dstPort = new TopoVertex(dstPortURN.getUrn(), VertexType.PORT);

        // Handle MPLS-layer source/destination devices
        serviceLayerTopology.buildLogicalLayerSrcNodes(srcDevice, srcPort);
        serviceLayerTopology.buildLogicalLayerDstNodes(dstDevice, dstPort);

        // Performs shortest path routing on MPLS-layer to properly assign weights to each logical link on Service-Layer
        serviceLayerTopology.calculateLogicalLinkWeights(requestPipe, requestSched, urnRepo.findAll(), rsvBwList, rsvVlanList);

        Topology slTopo;

        if(requestPipe.getAzMbps() == requestPipe.getZaMbps())
        {
            slTopo = serviceLayerTopology.getSLTopology();
        }
        else
        {
            slTopo = serviceLayerTopology.getSLTopologyAZ();
        }

        Topology prunedSlTopo = pruningService.pruneWithPipe(slTopo, requestPipe, requestSched, rsvBwList, rsvVlanList);

        TopoVertex serviceLayerSrcNode;
        TopoVertex serviceLayerDstNode;

        if(srcDevice.getVertexType().equals(VertexType.SWITCH))
        {
            serviceLayerSrcNode = srcPort;
        }
        else
        {
            serviceLayerSrcNode = serviceLayerTopology.getVirtualNode(srcDevice);
            assert(serviceLayerSrcNode != null);
        }

        if(dstDevice.getVertexType().equals(VertexType.SWITCH))
        {
            serviceLayerDstNode = dstPort;
        }
        else
        {
            serviceLayerDstNode = serviceLayerTopology.getVirtualNode(dstDevice);
            assert(serviceLayerDstNode != null);
        }


        // Shortest path routing on Service-Layer
        List<TopoEdge> azServiceLayerERO = dijkstraPCE.computeShortestPathEdges(prunedSlTopo, serviceLayerSrcNode, serviceLayerDstNode);

        if (azServiceLayerERO.isEmpty())
        {
            throw new PCEException("Empty path NonPalindromic PCE");
        }

        // Get symmetric Service-Layer path in reverse-direction
        List<TopoEdge> zaServiceLayerERO = new LinkedList<>();

        for(TopoEdge topEdge : prunedSlTopo.getEdges())
                log.info("TOPOLOGY EDGE: (" + topEdge.getA().getUrn() + "," + topEdge.getZ().getUrn() + ")");

        // 1. Reverse the links
        for(TopoEdge azEdge : azServiceLayerERO)
        {
            log.info("FORWARD EDGE: (" + azEdge.getA().getUrn() + "," + azEdge.getZ().getUrn() + ")");
            Optional<TopoEdge> reverseEdge = prunedSlTopo.getEdges().stream()
                    .filter(r -> r.getA().equals(azEdge.getZ()))
                    .filter(r -> r.getZ().equals(azEdge.getA()))
                    .findFirst();

            if(reverseEdge.isPresent())
            {
                log.info("REVERSE EDGE: (" + reverseEdge.get().getA().getUrn() + "," + reverseEdge.get().getZ().getUrn() + ")");
                zaServiceLayerERO.add(reverseEdge.get());
            }
        }

        // 2. Reverse the order
        Collections.reverse(zaServiceLayerERO);

        log.info("AZ ERO");
        for(int i = 0; i < azServiceLayerERO.size(); i++)
            log.info("(" + azServiceLayerERO.get(i).getA().getUrn() + "," + azServiceLayerERO.get(i).getZ().getUrn() + ")");

        log.info("ZA ERO");
        for(int i = 0; i < zaServiceLayerERO.size(); i++)
            log.info("(" + zaServiceLayerERO.get(i).getA().getUrn() + "," + zaServiceLayerERO.get(i).getZ().getUrn() + ")");

        Map<String, List<TopoEdge>> theMap = new HashMap<>();

        if(!(azServiceLayerERO.size() == zaServiceLayerERO.size()))
            return  theMap;

        List<TopoEdge> azERO;
        List<TopoEdge> zaERO;

        // Obtain physical ERO from Service-Layer EROs
        //if(requestPipe.getAzMbps() == requestPipe.getZaMbps())
        //{
            azERO = serviceLayerTopology.getActualERO(azServiceLayerERO);
            zaERO = serviceLayerTopology.getActualERO(zaServiceLayerERO);
        //}
        //else
        //{
            azERO = serviceLayerTopology.getActualEROAZ(azServiceLayerERO);
            zaERO = serviceLayerTopology.getActualEROZA(zaServiceLayerERO);

            String loggingAZ = "azERO: ";
            for(TopoEdge oneURN : azERO)
                loggingAZ = loggingAZ + oneURN.getA().getUrn() + "-";
            loggingAZ = loggingAZ + azERO.get(azERO.size()-1).getZ().getUrn();
            log.info(loggingAZ);

            String loggingZA = "zaERO: ";
            for(TopoEdge oneURN : zaERO)
                loggingZA = loggingZA + oneURN.getA().getUrn() + "-";
            loggingZA = loggingZA + zaERO.get(zaERO.size()-1).getZ().getUrn();
            log.info(loggingZA);
        //}


        theMap.put("az", azERO);
        theMap.put("za", zaERO);

        return theMap;
    }
}
