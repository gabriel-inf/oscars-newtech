package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.dto.topo.enums.PortLayer;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.servicetopo.SurvivableServiceLayerTopology;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeremy on 7/27/16.
 */
@Slf4j
@Component
public class SurvivabilityPCE
{
    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private SurvivableServiceLayerTopology serviceLayerTopology;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    @Autowired
    private BhandariPCE bhandariPCE;

    /**
     * Depends on BhandariPCE to construct the survivable physical-layer EROs for a request after pruning the topology based on requested parameters
     *
     * @param requestPipe Requested pipe with required reservation parameters
     * @param bwAvailMap
     * @return A four- element Map containing both the primary and secondary link-disjoint forward-direction EROs and the primary and secondary link-disjoint reverse-direction EROs
     * @throws PCEException
     */
    public Map<String, List<TopoEdge>> computeSurvivableERO(RequestedVlanPipeE requestPipe,
                                                            Map<String, Map<String, Integer>> bwAvailMap,
                                                            List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        if(requestPipe.getEroSurvivability().equals(SurvivabilityType.SURVIVABILITY_TOTAL))
        {
            return computeSurvivableEroComplete(requestPipe, bwAvailMap, rsvVlanList);
        }
        else if(requestPipe.getEroSurvivability().equals(SurvivabilityType.SURVIVABILITY_PARTIAL))
        {
            return computeSurvivableEroPartial(requestPipe, bwAvailMap, rsvVlanList);
        }
        else
        {
            throw new PCEException("Unsupported SurvivabilityType");
        }
    }


    private Map<String, List<TopoEdge>> computeSurvivableEroComplete(RequestedVlanPipeE requestPipe,
                                                                     Map<String, Map<String, Integer>> bwAvailMap,
                                                                     List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        String srcDeviceURN = requestPipe.getAJunction().getDeviceUrn();
        String dstDeviceURN = requestPipe.getZJunction().getDeviceUrn();

        UrnE srcDeviceURN_e = urnRepo.findByUrn(requestPipe.getAJunction().getDeviceUrn()).orElseThrow(NoSuchElementException::new);
        UrnE dstDeviceURN_e = urnRepo.findByUrn(requestPipe.getZJunction().getDeviceUrn()).orElseThrow(NoSuchElementException::new);


        VertexType srcType = topoService.getVertexTypeFromDeviceType(srcDeviceURN_e.getDeviceType());
        VertexType dstType = topoService.getVertexTypeFromDeviceType(dstDeviceURN_e.getDeviceType());

        TopoVertex srcDevice = new TopoVertex(srcDeviceURN, srcType);
        TopoVertex dstDevice = new TopoVertex(dstDeviceURN, dstType);

        Topology multiLayerTopo = topoService.getMultilayerTopology();

        // Identify src/dst ports for disjoint routing



        // Bandwidth and Vlan pruning
        Topology prunedTopo = pruningService.pruneWithPipeAZ(multiLayerTopo, requestPipe, bwAvailMap, rsvVlanList);

        // Disjoint shortest-path routing
        List<List<TopoEdge>> azPathSet = bhandariPCE.computeDisjointPaths(prunedTopo, srcDevice, dstDevice, requestPipe.getNumPaths());

        log.info(azPathSet.toString());

        if(azPathSet.isEmpty())
        {
            throw new PCEException("Empty path-set in Survivability PCE");
        }
        else if(azPathSet.size() != requestPipe.getNumPaths())
        {
            throw new PCEException(requestPipe.getNumPaths() + " disjoint paths could not be found in Survivability PCE");
        }


        // Get palindromic paths in reverse-direction //
        List<List<TopoEdge>> zaPathSet = new ArrayList<>();

        // 1. Reverse the links
        for(List<TopoEdge> azERO : azPathSet)
        {
            List<TopoEdge> zaERO = new ArrayList<>();

            for(TopoEdge azEdge : azERO)
            {
                Optional<TopoEdge> reverseEdge = prunedTopo.getEdges().stream()
                        .filter(r -> r.getA().equals(azEdge.getZ()))
                        .filter(r -> r.getZ().equals(azEdge.getA()))
                        .findFirst();

                reverseEdge.ifPresent(zaERO::add);
            }

            zaPathSet.add(zaERO);
        }

        // 2. Reverse the order
        for(List<TopoEdge> zaERO : zaPathSet)
        {
            Collections.reverse(zaERO);
        }

        log.info(zaPathSet.toString());

        assert(azPathSet.size() == requestPipe.getNumPaths());
        assert(azPathSet.size() == zaPathSet.size());


        for(int p = 0; p < azPathSet.size(); p++)
        {
            List<TopoEdge> azERO = azPathSet.get(p);
            List<TopoEdge> zaERO = zaPathSet.get(p);
            assert(azERO.size() == zaERO.size());
        }

        Map<String, List<TopoEdge>> theMap = new HashMap<>();
        Integer numPathsSoFar = 0;
        for(List<TopoEdge> azPath : azPathSet){
            numPathsSoFar += 1;
            theMap.put("az" + numPathsSoFar, azPath);
        }
        numPathsSoFar = 0;
        for(List<TopoEdge> zaPath : zaPathSet){
            numPathsSoFar += 1;
            theMap.put("za" + numPathsSoFar, zaPath);
        }
        return theMap;
    }


    // Number of disjoint paths requested specified in the requestPipe
    private Map<String, List<TopoEdge>> computeSurvivableEroPartial(RequestedVlanPipeE requestPipe, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList) throws PCEException
    {
        Topology ethTopo = topoService.layer(Layer.ETHERNET);
        Topology intTopo = topoService.layer(Layer.INTERNAL);
        Topology mplsTopo = topoService.layer(Layer.MPLS);
        Topology prunedFullTopo = pruningService.pruneWithPipe(topoService.getMultilayerTopology(), requestPipe, bwAvailMap, rsvVlanList);

        // Identify URNs of each fixture
        Set<String> fixtureURNs = new HashSet<>();
        requestPipe.getAJunction().getFixtures().stream().forEach(f -> fixtureURNs.add(f.getPortUrn()));
        requestPipe.getZJunction().getFixtures().stream().forEach(f -> fixtureURNs.add(f.getPortUrn()));

        // Remove unneeded edge-ports for simpler service-layer topology construction
        pruningService.pruneTopologyOfEdgePortsExcept(ethTopo, fixtureURNs);
        pruningService.pruneTopologyOfEdgePortsExcept(intTopo, fixtureURNs);
        pruningService.pruneTopologyOfEdgePortsExcept(mplsTopo, fixtureURNs);

        // Filter MPLS-ports and MPLS-devices out of ethTopo
        Set<TopoVertex> portsOnly = ethTopo.getVertices().stream()
                .filter(v -> v.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());

        for (TopoEdge intEdge : intTopo.getEdges())
        {
            TopoVertex vertA = intEdge.getA();
            TopoVertex vertZ = intEdge.getZ();

            if (portsOnly.isEmpty())
            {
                break;
            }

            if(portsOnly.contains(vertA) && !vertA.getPortLayer().equals(PortLayer.MPLS))
                portsOnly.remove(vertA);

            if(portsOnly.contains(vertZ) && !vertZ.getPortLayer().equals(PortLayer.MPLS))
                portsOnly.remove(vertZ);
        }

        ethTopo.getVertices().removeIf(v -> v.getVertexType().equals(VertexType.ROUTER) && !topoService.determineIfRouterHasEthernetPorts(v.getUrn()));
        ethTopo.getVertices().removeAll(portsOnly);

        // Filter Devices and Ports out of intTopo
        intTopo.getVertices().removeAll(intTopo.getVertices());

        /* Initialize Service-Layer Topology */
        serviceLayerTopology.setTopology(ethTopo);
        serviceLayerTopology.setTopology(intTopo);
        serviceLayerTopology.setTopology(mplsTopo);

        serviceLayerTopology.createMultilayerTopology();
        serviceLayerTopology.resetLogicalLinks();

        UrnE srcDeviceURN = topoService.getUrn(requestPipe.getAJunction().getDeviceUrn());
        UrnE dstDeviceURN = topoService.getUrn(requestPipe.getZJunction().getDeviceUrn());
        VertexType srcType = topoService.getVertexTypeFromDeviceType(srcDeviceURN.getDeviceType());
        VertexType dstType = topoService.getVertexTypeFromDeviceType(dstDeviceURN.getDeviceType());
        TopoVertex srcDevice = new TopoVertex(srcDeviceURN.getUrn(), srcType);
        TopoVertex dstDevice = new TopoVertex(dstDeviceURN.getUrn(), dstType);
        Set<RequestedVlanFixtureE> srcFixtures = requestPipe.getAJunction().getFixtures();
        Set<RequestedVlanFixtureE> dstFixtures = requestPipe.getZJunction().getFixtures();
        TopoVertex srcPort;
        TopoVertex dstPort;

        if(srcFixtures.size() > 0)
        {
            RequestedVlanFixtureE srcFix = srcFixtures.iterator().next();
            String srcFixURN = srcFix.getPortUrn();
            PortLayer srcFixLayer = topoService.lookupPortLayer(srcFixURN);

            srcPort = new TopoVertex(srcFixURN, VertexType.PORT, srcFixLayer);
        }
        else // Add a dummy fixture-port to Service Layer Topology.
        {
            if(srcDevice.getVertexType().equals(VertexType.ROUTER))
            {
                srcPort = new TopoVertex("fix" + srcDevice.getUrn(), VertexType.PORT, PortLayer.MPLS);
                addPortToServiceMplsTopology(serviceLayerTopology, srcPort, srcDevice);
            }
            else
            {
                srcPort = new TopoVertex("fix" + srcDevice.getUrn(), VertexType.PORT, PortLayer.ETHERNET);
                addPortToServiceEthernetTopology(serviceLayerTopology, srcPort, srcDevice);
            }
        }

        if(dstFixtures.size() > 0)
        {
            RequestedVlanFixtureE dstFix = dstFixtures.iterator().next();
            String dstFixURN = dstFix.getPortUrn();
            PortLayer dstFixLayer = topoService.lookupPortLayer(dstFixURN);

            dstPort = new TopoVertex(dstFixURN, VertexType.PORT, dstFixLayer);
        }
        else // Add a dummy fixture-port to Service Layer Topology.
        {
            if(dstDevice.getVertexType().equals(VertexType.ROUTER))
            {
                dstPort = new TopoVertex("fix" + dstDevice.getUrn(), VertexType.PORT, PortLayer.MPLS);
                addPortToServiceMplsTopology(serviceLayerTopology, dstPort, dstDevice);
            }
            else
            {
                dstPort = new TopoVertex("fix" + dstDevice.getUrn(), VertexType.PORT, PortLayer.ETHERNET);
                addPortToServiceEthernetTopology(serviceLayerTopology, dstPort, dstDevice);
            }
        }

        // Handle MPLS-layer source/destination devices
        serviceLayerTopology.buildLogicalLayerSrcNodes(srcDevice, srcPort);
        serviceLayerTopology.buildLogicalLayerDstNodes(dstDevice, dstPort);

        // Performs shortest path routing on MPLS-layer to properly assign weights to each logical link on Service-Layer
        serviceLayerTopology.calculateLogicalLinkWeights(requestPipe, urnRepo.findAll(), bwAvailMap, rsvVlanList, requestPipe.getNumPaths());

        Topology slTopo = serviceLayerTopology.getSLTopology();
        Topology prunedSlTopo = pruningService.pruneWithPipe(slTopo, requestPipe, bwAvailMap, rsvVlanList);

        TopoVertex serviceLayerSrcNode;
        TopoVertex serviceLayerDstNode;

        if(srcDevice.getVertexType().equals(VertexType.SWITCH))
            serviceLayerSrcNode = srcPort;
        else
        {
            if(topoService.determineIfRouterHasEthernetPorts(srcDevice.getUrn()))
                serviceLayerSrcNode = srcPort;
            else
                serviceLayerSrcNode = serviceLayerTopology.getVirtualNode(srcPort);
        }

        if(dstDevice.getVertexType().equals(VertexType.SWITCH))
            serviceLayerDstNode = dstPort;
        else
        {
            if(topoService.determineIfRouterHasEthernetPorts(dstDevice.getUrn()))
                serviceLayerDstNode = dstPort;
            else
                serviceLayerDstNode = serviceLayerTopology.getVirtualNode(dstPort);
        }

        assert (serviceLayerSrcNode != null);
        assert (serviceLayerDstNode != null);

        // Shortest path routing on Service-Layer
        List<TopoEdge> azServiceLayerERO = dijkstraPCE.computeShortestPathEdges(prunedSlTopo, serviceLayerSrcNode, serviceLayerDstNode);

        if (azServiceLayerERO.isEmpty())
            throw new PCEException("Empty path Survivability PCE");

        // Get palindromic Service-Layer path in reverse-direction
        List<TopoEdge> zaServiceLayerERO = this.getReverseERO(azServiceLayerERO, prunedSlTopo);


        Map<String, List<TopoEdge>> theMap = new HashMap<>();

        if (azServiceLayerERO.size() != zaServiceLayerERO.size())
            return theMap;

        // Obtain physical ERO from Service-Layer EROs
        List<List<TopoEdge>> azEroList = serviceLayerTopology.getActualEroList(azServiceLayerERO, requestPipe.getNumPaths());

        // Get palindromic Physical path in reverse-direction
        List<List<TopoEdge>> zaEroList = new ArrayList<>();
        for(List<TopoEdge> oneAzERO : azEroList)
        {
            zaEroList.add(this.getReverseERO(oneAzERO, prunedFullTopo));
        }

        if(azEroList.size() != zaEroList.size())
            return theMap;


        // Remove starting and ending ports from all EROs
        for(List<TopoEdge> oneAzEro : azEroList)
        {
            int pathSize = oneAzEro.size();

            if(oneAzEro.get(pathSize-1).getZ().getVertexType().equals(VertexType.PORT))
                oneAzEro.remove(pathSize-1);

            if(oneAzEro.get(0).getA().getVertexType().equals(VertexType.PORT))
                oneAzEro.remove(0);
        }

        for(List<TopoEdge> oneZaEro : zaEroList)
        {
            int pathSize = oneZaEro.size();

            if(oneZaEro.get(pathSize-1).getZ().getVertexType().equals(VertexType.PORT))
                oneZaEro.remove(pathSize-1);

            if(oneZaEro.get(0).getA().getVertexType().equals(VertexType.PORT))
                oneZaEro.remove(0);
        }


        // No logical edges were used in this path - Only a single non-survivable ERO will be returned
        if(azEroList.size() == 1)
        {
            theMap.put("az", azEroList.get(0));
            theMap.put("za", zaEroList.get(0));

            return theMap;
        }

        int numPathsSoFar = 0;

        // Put all EROs into map in order
        for(List<TopoEdge> oneAzEro : azEroList)
        {
            numPathsSoFar += 1;
            theMap.put("az" + numPathsSoFar, oneAzEro);
        }

        numPathsSoFar = 0;
        for(List<TopoEdge> oneZaEro : zaEroList)
        {
            numPathsSoFar += 1;
            theMap.put("za" + numPathsSoFar, oneZaEro);
        }

        return theMap;
    }

    private void addPortToServiceMplsTopology(SurvivableServiceLayerTopology serviceLayerTopology, TopoVertex port, TopoVertex device)
    {
        serviceLayerTopology.getMplsLayerPorts().add(port);
        TopoEdge portToDeviceEdge = new TopoEdge(port, device, 0L, Layer.MPLS);
        TopoEdge deviceToPortEdge = new TopoEdge(device, port, 0L, Layer.MPLS);
        serviceLayerTopology.getMplsLayerLinks().add(portToDeviceEdge);
        serviceLayerTopology.getMplsLayerLinks().add(deviceToPortEdge);
        serviceLayerTopology.getMplsTopology().getVertices().add(port);
        serviceLayerTopology.getMplsTopology().getEdges().add(portToDeviceEdge);
        serviceLayerTopology.getMplsTopology().getEdges().add(deviceToPortEdge);
    }

    private void addPortToServiceEthernetTopology(SurvivableServiceLayerTopology serviceLayerTopology, TopoVertex port, TopoVertex device)
    {
        serviceLayerTopology.getServiceLayerPorts().add(port);
        TopoEdge portToDeviceEdge = new TopoEdge(port, device, 0L, Layer.ETHERNET);
        TopoEdge deviceToPortEdge = new TopoEdge(device, port, 0L, Layer.ETHERNET);
        serviceLayerTopology.getServiceLayerLinks().add(portToDeviceEdge);
        serviceLayerTopology.getServiceLayerLinks().add(deviceToPortEdge);
        serviceLayerTopology.getEthernetTopology().getVertices().add(port);
        serviceLayerTopology.getEthernetTopology().getEdges().add(portToDeviceEdge);
        serviceLayerTopology.getEthernetTopology().getEdges().add(deviceToPortEdge);
    }

    private List<TopoEdge> getReverseERO(List<TopoEdge> originalERO, Topology topology)
    {
        List<TopoEdge> reverseERO = new ArrayList<>();

        // Reverse each link
        for (TopoEdge originalEdge : originalERO)
        {
            Optional<TopoEdge> reverseEdge = topology.getEdges().stream()
                    .filter(r -> r.getA().equals(originalEdge.getZ()))
                    .filter(r -> r.getZ().equals(originalEdge.getA()))
                    .findFirst();

            reverseEdge.ifPresent(reverseERO::add);
        }

        // Reverse the order
        Collections.reverse(reverseERO);

        return reverseERO;
    }
}
