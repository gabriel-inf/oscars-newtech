package net.es.oscars.servicetopo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.PortLayer;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.pce.DijkstraPCE;
import net.es.oscars.pce.PruningService;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.ent.ReservedVlanE;
import net.es.oscars.topo.ent.UrnE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@Builder
@Component
@AllArgsConstructor
@NoArgsConstructor
public class ServiceLayerTopology
{
    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    private Set<TopoVertex> serviceLayerDevices;    // ETHERNET and VIRTUAL devices
    private Set<TopoVertex> serviceLayerPorts;      // ETHERNET and VIRTUAL ports
    private Set<TopoEdge> serviceLayerLinks;        // ETHERNET and LOGICAL edges

    private Set<TopoVertex> mplsLayerDevices;       // MPLS devices
    private Set<TopoVertex> mplsLayerPorts;         // MPLS ports
    private Set<TopoEdge> mplsLayerLinks;           // MPLS edges

    private Set<TopoVertex> nonAdjacentPorts;       // Service-layer ports which connect to MPLS-layer ports
    private Set<LogicalEdge> logicalLinks;          // Abstraction of path between two nonadjacent ports
    private Set<LogicalEdge> llBackup;              // Copy of logicalLinks, used for resetting, etc.

    // Set by calling object - Refer to NonPalindromicPCE for details //
    private Topology ethernetTopology;
    private Topology mplsTopology;
    private Topology internalTopology;

    /**
     * Managing method in charge of constructing the multi-layer service-topology.
     * Divides physical topology into two layers: MPLS-only layer, and Service-layer:
     * MPLS-only layer contains: all MPLS devices, adjacent ports, INTERNAL links between MPLSdevices-MPLSports, links between MPLS-ports.
     */
    public void createMultilayerTopology()
    {
        serviceLayerDevices = new HashSet<>();
        serviceLayerPorts = new HashSet<>();
        serviceLayerLinks = new HashSet<>();

        mplsLayerDevices = new HashSet<>();
        mplsLayerPorts = new HashSet<>();
        mplsLayerLinks = new HashSet<>();

        buildServiceLayerTopo();
        buildMplsLayerTopo();

        buildLogicalLayerTopo();
    }

    /**
     * Parses physical ethernet and internal topologies to construct service-layer topology.
     */
    private void buildServiceLayerTopo()
    {
        Set<TopoVertex> ethernetVertices = ethernetTopology.getVertices();
        Set<TopoVertex> internalVertices = internalTopology.getVertices();

        Set<TopoEdge> allEthernetEdges = ethernetTopology.getEdges();
        Set<TopoEdge> allInternalEdges = internalTopology.getEdges();

        assert(internalVertices.isEmpty());     // Only edges should be INTERNAL

        // Parse the Devices
        Set<TopoVertex> allEthernetDevices = ethernetVertices.stream()
                .filter(d -> !d.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());


        // Parse the Ports
        Set<TopoVertex> allEthernetPorts = ethernetVertices.stream()
                .filter(p -> p.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());


        // Parse the INTERNAL Edges
        Set<TopoEdge> allInternalEthernetEdges = new HashSet<>();
        for(TopoEdge oneIntEdge : allInternalEdges)
        {
            TopoVertex aVert = oneIntEdge.getA();
            TopoVertex zVert = oneIntEdge.getZ();
            VertexType aType = aVert.getVertexType();
            VertexType zType = zVert.getVertexType();
            PortLayer aLayer = aVert.getPortLayer();
            PortLayer zLayer = zVert.getPortLayer();

            if((aType.equals(VertexType.PORT) && aLayer.equals(PortLayer.ETHERNET)) || (zType.equals(VertexType.PORT) && zLayer.equals(PortLayer.ETHERNET)))
                allInternalEthernetEdges.add(oneIntEdge);
        }

        // Compose Service-Layer
        serviceLayerDevices.addAll(allEthernetDevices);
        serviceLayerPorts.addAll(allEthernetPorts);
        serviceLayerLinks.addAll(allEthernetEdges);
        serviceLayerLinks.addAll(allInternalEthernetEdges);
    }

    /**
     * Parses physical mpls and internal topologies to construct MPLS-layer topology.
     */
    private void buildMplsLayerTopo()
    {
        Set<TopoVertex> mplsVertices = mplsTopology.getVertices();
        Set<TopoVertex> internalVertices = internalTopology.getVertices();

        Set<TopoEdge> allMplsEdges = mplsTopology.getEdges();
        Set<TopoEdge> allInternalEdges = internalTopology.getEdges();

        assert(internalVertices.isEmpty());     // Only edges should be INTERNAL

        // Parse the Devices
        Set<TopoVertex> allMplsDevices = mplsVertices.stream()
                .filter(d -> !d.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());


        // Parse the Ports
        Set<TopoVertex> allMplsPorts = mplsVertices.stream()
                .filter(p -> p.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());


        // Parse the INTERNAL Edges
        Set<TopoEdge> allInternalMPLSEdges = new HashSet<>();
        for(TopoEdge oneIntEdge : allInternalEdges)
        {
            TopoVertex aVert = oneIntEdge.getA();
            TopoVertex zVert = oneIntEdge.getZ();
            VertexType aType = aVert.getVertexType();
            VertexType zType = zVert.getVertexType();
            PortLayer aLayer = aVert.getPortLayer();
            PortLayer zLayer = zVert.getPortLayer();

            if((aType.equals(VertexType.PORT) && aLayer.equals(PortLayer.MPLS)) || (zType.equals(VertexType.PORT) && zLayer.equals(PortLayer.MPLS)))
                allInternalMPLSEdges.add(oneIntEdge);
        }


        // Compose MPLS Mesh Layer
        mplsLayerDevices.addAll(allMplsDevices);
        mplsLayerPorts.addAll(allMplsPorts);
        mplsLayerLinks.addAll(allMplsEdges);
        mplsLayerLinks.addAll(allInternalMPLSEdges);
    }

    /**
     * Adds zero-weight logical links between every pair of NonAdjacent Service-layer ports
     */
    private void buildLogicalLayerTopo()
    {
        identifyNonAdjacentSLPorts();
        buildLogicalLayerLinks();
    }


    /**
     * Identifies which Service-layer ports connect to MPLS-layer ports.
     */
    private void identifyNonAdjacentSLPorts()
    {
        nonAdjacentPorts = new HashSet<>();

        for(TopoEdge serviceLink : serviceLayerLinks)
        {
            TopoVertex vertA = serviceLink.getA();
            TopoVertex vertZ = serviceLink.getZ();

            // Skip INTERNAL edges
            if(serviceLink.getLayer().equals(Layer.INTERNAL))
                continue;

            // ETHERNET -> MPLS edge
            if(serviceLayerPorts.contains(vertA) && !serviceLayerPorts.contains(vertZ))
                nonAdjacentPorts.add(vertA);

            // MPLS -> ETHERNET edge
            if(!serviceLayerPorts.contains(vertA) && serviceLayerPorts.contains(vertZ))
                nonAdjacentPorts.add(vertZ);
        }

        // Ports which are not on any ETHERNET edges (edge ports) should be added to set
        serviceLayerPorts.stream()
                .filter(p -> !nonAdjacentPorts.contains(p))
                .forEach(p ->
                {
                    List<TopoEdge> ethLinkscontainingP =  serviceLayerLinks.stream()
                            .filter(l -> l.getLayer().equals(Layer.ETHERNET) && (l.getA().equals(p) || l.getZ().equals(p)))
                            .collect(Collectors.toList());

                    if(ethLinkscontainingP.isEmpty())
                    {
                        List<TopoEdge> linksConnectingSwitchToP = serviceLayerLinks.stream()
                                .filter(l -> l.getLayer().equals(Layer.INTERNAL) && (l.getA().equals(p) || l.getZ().equals(p)))
                                .filter(l -> l.getA().getVertexType().equals(VertexType.SWITCH) || l.getZ().getVertexType().equals(VertexType.SWITCH))
                                .collect(Collectors.toList());

                        if(linksConnectingSwitchToP.isEmpty())
                        {
                            log.info("Adding " + p.getUrn());
                            log.info(ethLinkscontainingP.toString());
                            nonAdjacentPorts.add(p);
                        }
                    }
                });
    }


    /**
     * Establishes a zero-weight LOGICAL edge on the Service-layer topology between every pair of NonAdjacent Service-layer ports.
     */
    private void buildLogicalLayerLinks()
    {
        logicalLinks = new HashSet<>();

        for(TopoVertex nonAdjacentA : nonAdjacentPorts)
        {
            for(TopoVertex nonAdjacentZ : nonAdjacentPorts)
            {
                if(nonAdjacentA.equals(nonAdjacentZ))
                {
                    continue;
                }

                // Do NOT build logical link between physically connected ETHERNET ports
                List<TopoEdge> slEdgeWithTheseEndpoints = serviceLayerLinks.stream()
                        .filter(l -> l.getA().equals(nonAdjacentA) && l.getZ().equals(nonAdjacentZ))
                        .collect(Collectors.toList());

                if(!slEdgeWithTheseEndpoints.isEmpty())
                    continue;

                // Do NOT build logical link between ETHERNET ports on the same device
                List<TopoEdge> internalEdgesFromA = serviceLayerLinks.stream()
                        .filter(l -> l.getA().equals(nonAdjacentA) && l.getLayer().equals(Layer.INTERNAL))
                        .collect(Collectors.toList());

                List<TopoEdge> internalEdgesFromZ = serviceLayerLinks.stream()
                        .filter(l -> l.getA().equals(nonAdjacentZ) && l.getLayer().equals(Layer.INTERNAL))
                        .collect(Collectors.toList());

                assert(internalEdgesFromA.size() == 1 || nonAdjacentA.getVertexType().equals(VertexType.VIRTUAL));
                assert(internalEdgesFromZ.size() == 1 || nonAdjacentZ.getVertexType().equals(VertexType.VIRTUAL));

                if(!nonAdjacentA.getVertexType().equals(VertexType.VIRTUAL) && !nonAdjacentZ.getVertexType().equals(VertexType.VIRTUAL))
                {
                    if(internalEdgesFromA.get(0).getZ().equals(internalEdgesFromZ.get(0).getZ()))
                        continue;
                }

                LogicalEdge azLogicalEdge = new LogicalEdge(nonAdjacentA,nonAdjacentZ, 0L, 0L, 0L, Layer.LOGICAL, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                LogicalEdge zaLogicalEdge = new LogicalEdge(nonAdjacentZ,nonAdjacentA, 0L, 0L, 0L, Layer.LOGICAL, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

                logicalLinks.add(azLogicalEdge);
                logicalLinks.add(zaLogicalEdge);
            }
        }

        // Create a backup of all topology-based Logical Links (excludes links created for a specific request).
        llBackup = new HashSet<>();
        logicalLinks.stream()
                .filter(ll -> !ll.getA().getVertexType().equals(VertexType.VIRTUAL))
                .filter(ll -> !ll.getZ().getVertexType().equals(VertexType.VIRTUAL))
                .forEach(llBackup::add);
    }

    // Determines whether to use symmetric or asymmetric logical link computation
    /**
     *
     */
    /**
     * Managing method - Determines whether to perform logical edge weight computation Symmetrically or Asymmetrically.
     * This method may no longer be necessary, since the Symmetric subroutine was too naive to work in general cases.
     * @param requestedVlanPipe - Request pipe
     * @param urnList - List of URNs in the network; Necessary for passing to PruningService methods
     * @param bwAvailMap - Map of available bandwidth for "Ingress" and "Egress" direction for each URN.
     * @param rsvVlanList - List of currently reserved VLAN elements (during request schedule)
     */
    public void calculateLogicalLinkWeights(RequestedVlanPipeE requestedVlanPipe, List<UrnE> urnList, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList)
    {
        this.calculateLogicalLinkWeightsAsymmetric(requestedVlanPipe, urnList, bwAvailMap, rsvVlanList);
    }



    /**
     * Calls PruningService and DijkstraPCE methods to compute shortest MPLS-layer paths, calculates the combined weight of each path, and maps them to the appropriate logical links.
     * This method has tentatively replacd its Symmetric counterpart and provides the same coverage (with more complexity).
     * Each logical link is considered for pruned MPLS-layer topologies given requested A->Z b/w, and reuqested Z->A b/w.
     * If a link's beginning/terminating ports do not support both of those requested b/w values, the logical link is removed from the network.
     * The DijkstraPCE is called to perform shortest-path routing given both of the requested b/w values.
     * If A->Z b/w =/= Z->A b/w, then the physical MPLS-layer EROs will differ.
     * Weights are set according to sum of weights of the underlying MPLS-layer edges.
     * @param requestedVlanPipe - Request pipe
     * @param urnList - List of URNs in the network; Necessary for passing to PruningService methods
     * @param bwAvailMap - Map of available bandwidth for "Ingress" and "Egress" direction for each URN.
     * @param rsvVlanList - List of currently reserved VLAN elements (during request schedule)
     */
    private void calculateLogicalLinkWeightsAsymmetric(RequestedVlanPipeE requestedVlanPipe,
                                                       List<UrnE> urnList, Map<String, Map<String, Integer>> bwAvailMap,
                                                       List<ReservedVlanE> rsvVlanList)
    {
        Set<LogicalEdge> logicalLinksToRemoveFromServiceLayer = new HashSet<>();

        // Step 1: Construct MPLS-Layer topology
        Topology mplsLayerTopo = new Topology();
        mplsLayerTopo.getVertices().addAll(mplsLayerDevices);
        mplsLayerTopo.getVertices().addAll(mplsLayerPorts);
        mplsLayerTopo.getEdges().addAll(mplsLayerLinks);

        for(LogicalEdge oneLogicalLink : logicalLinks)
        {
            TopoVertex srcEthPort = oneLogicalLink.getA();      //Ethernet-source of logical link
            TopoVertex dstEthPort = oneLogicalLink.getZ();      //Ethernet-dest of logical link

            boolean srcIsOnSwitch = false;
            boolean dstIsOnSwitch = false;
            boolean srcIsVirtual = srcEthPort.getVertexType().equals(VertexType.VIRTUAL);
            boolean dstIsVirtual = dstEthPort.getVertexType().equals(VertexType.VIRTUAL);

            TopoEdge physEdgeAtoRouter = null;      // Edge srcEthPort --> Router
            TopoEdge physEdgeZtoRouter = null;      // Edge dstEthPort --> Router
            TopoEdge physEdgeRoutertoA = null;      // Edge Router --> srcEthPort
            TopoEdge physEdgeRoutertoZ = null;      // Edge Router --> dstEthPort

            TopoEdge physEdgeAtoMplsPort = null;      // Edge srcEthPort --> MPLS-Layer port
            TopoEdge physEdgeZtoMplsPort = null;      // Edge dstEthPort --> MPLS-Layer port
            TopoEdge physEdgeMplsPorttoA = null;      // Edge MPLS-Layer port --> srcEthPort
            TopoEdge physEdgeMplsPorttoZ = null;      // Edge MPLS-Layer port --> dstEthPort

            List<TopoEdge> pathAZ = null;
            List<TopoEdge> pathZA = null;

            long weightMetricAZ = 0;
            long weightMetricZA = 0;

            for(TopoEdge oneSLLink : serviceLayerLinks)
            {
                TopoVertex slA = oneSLLink.getA();
                TopoVertex slZ = oneSLLink.getZ();

                // Step 2: Identify INTERNAL adaptation edges between ETHERNET ports and MPLS devices
                if(slA.equals(srcEthPort) && slZ.getVertexType().equals(VertexType.ROUTER))
                    physEdgeAtoRouter = oneSLLink;
                else if(slZ.equals(srcEthPort) && slA.getVertexType().equals(VertexType.ROUTER))
                    physEdgeRoutertoA = oneSLLink;

                if(slZ.equals(dstEthPort) && slA.getVertexType().equals(VertexType.ROUTER))
                    physEdgeRoutertoZ = oneSLLink;
                else if(slA.equals(dstEthPort) && slZ.getVertexType().equals(VertexType.ROUTER))
                    physEdgeZtoRouter = oneSLLink;

                if((slA.equals(srcEthPort) && slZ.getVertexType().equals(VertexType.SWITCH)) || (slZ.equals(srcEthPort) && slA.getVertexType().equals(VertexType.SWITCH)))
                {
                    srcIsOnSwitch = true;
                    physEdgeAtoMplsPort = serviceLayerLinks.stream().filter(l -> l.getA().equals(srcEthPort) && l.getZ().getVertexType().equals(VertexType.PORT)).findFirst().get();
                    physEdgeMplsPorttoA = serviceLayerLinks.stream().filter(l -> l.getZ().equals(srcEthPort) && l.getA().getVertexType().equals(VertexType.PORT)).findFirst().get();
                }

                if((slA.equals(dstEthPort) && slZ.getVertexType().equals(VertexType.SWITCH)) || (slZ.equals(dstEthPort) && slA.getVertexType().equals(VertexType.SWITCH)))
                {
                    dstIsOnSwitch = true;
                    physEdgeZtoMplsPort = serviceLayerLinks.stream().filter(l -> l.getA().equals(dstEthPort) && l.getZ().getVertexType().equals(VertexType.PORT)).findFirst().get();
                    physEdgeMplsPorttoZ = serviceLayerLinks.stream().filter(l -> l.getZ().equals(dstEthPort) && l.getA().getVertexType().equals(VertexType.PORT)).findFirst().get();
                }
            }

            if(physEdgeAtoRouter == null || physEdgeZtoRouter == null || physEdgeRoutertoA == null || physEdgeRoutertoZ == null)
            {
                if(!srcIsOnSwitch && !dstIsOnSwitch)
                {
                    if(!srcIsVirtual && !dstIsVirtual)
                    {
                        log.error("Service-layer topology has incorrectly identified adaptation edges");
                        assert false;
                    }
                }
            }


            // Step 3: Add ETHERNET src/dst ports and adaptation edges to MPLS-Layer topology
            mplsLayerTopo.getVertices().add(srcEthPort);
            mplsLayerTopo.getVertices().add(dstEthPort);

            log.info("Added " + srcEthPort.getUrn());
            log.info("Added " + dstEthPort.getUrn());

            if(!srcIsVirtual)
            {
                if(!srcIsOnSwitch)
                {
                    mplsLayerTopo.getEdges().add(physEdgeAtoRouter);
                    mplsLayerTopo.getEdges().add(physEdgeRoutertoA);
                    log.info("Added " + physEdgeAtoRouter.toString());
                    log.info("Added " + physEdgeRoutertoA.toString());
                }
                else
                {
                    mplsLayerTopo.getEdges().add(physEdgeAtoMplsPort);
                    mplsLayerTopo.getEdges().add(physEdgeMplsPorttoA);
                    log.info("Added " + physEdgeAtoMplsPort.toString());
                    log.info("Added " + physEdgeMplsPorttoA.toString());
                }
            }

            if(!dstIsVirtual)
            {
                if(!dstIsOnSwitch)
                {
                    mplsLayerTopo.getEdges().add(physEdgeZtoRouter);
                    mplsLayerTopo.getEdges().add(physEdgeRoutertoZ);
                    log.info("Added " + physEdgeZtoRouter.toString());
                    log.info("Added " + physEdgeRoutertoZ.toString());
                }
                else
                {
                    mplsLayerTopo.getEdges().add(physEdgeZtoMplsPort);
                    mplsLayerTopo.getEdges().add(physEdgeMplsPorttoZ);
                    log.info("Added " + physEdgeZtoMplsPort.toString());
                    log.info("Added " + physEdgeMplsPorttoZ.toString());
                }
            }

            serviceLayerLinks.stream()
                    .filter(l -> l.getA().getVertexType().equals(VertexType.VIRTUAL) || l.getZ().getVertexType().equals(VertexType.VIRTUAL))
                   .forEach(l -> mplsLayerTopo.getEdges().add(l));

            // Step 4: Prune updated MPLS-Layer topology before pathfinding. Operation must be done in two directions since requested bandwidth may be asymmetric from A->Z and Z->A
            log.info("Computing MPLS-Layer path-pair between " + srcEthPort + " <---> " + dstEthPort);
            Topology prunedMPLSTopoAZ = pruningService.pruneWithPipeAZ(mplsLayerTopo, requestedVlanPipe, urnList, bwAvailMap, rsvVlanList);
            Topology prunedMPLSTopoZA = pruningService.pruneWithPipeZA(mplsLayerTopo, requestedVlanPipe, urnList, bwAvailMap, rsvVlanList);

            // Step 5: Compute MPLS-Layer routes beginning and ending at ETHERNET src/dst ports to construct physical paths corresponding to this logical link
            pathAZ = dijkstraPCE.computeShortestPathEdges(prunedMPLSTopoAZ, srcEthPort, dstEthPort);
            pathZA = dijkstraPCE.computeShortestPathEdges(prunedMPLSTopoZA, dstEthPort, srcEthPort);

            // Step 6: Delete ETHERNET src/dst ports and adaptation edges from MPLS-Layer topology so they can't be used in pathfinding for unrelated logical links
            mplsLayerTopo.getVertices().remove(srcEthPort);
            mplsLayerTopo.getVertices().remove(dstEthPort);
            mplsLayerTopo.getEdges().remove(physEdgeAtoRouter);
            mplsLayerTopo.getEdges().remove(physEdgeZtoRouter);
            mplsLayerTopo.getEdges().remove(physEdgeRoutertoA);
            mplsLayerTopo.getEdges().remove(physEdgeRoutertoZ);
            mplsLayerTopo.getEdges().remove(physEdgeAtoMplsPort);
            mplsLayerTopo.getEdges().remove(physEdgeZtoMplsPort);
            mplsLayerTopo.getEdges().remove(physEdgeMplsPorttoA);
            mplsLayerTopo.getEdges().remove(physEdgeMplsPorttoZ);
            mplsLayerTopo.getEdges().removeIf(l -> l.getA().getVertexType().equals(VertexType.VIRTUAL) || l.getZ().getVertexType().equals(VertexType.VIRTUAL));

            log.info(pathAZ.toString());
            log.info(pathZA.toString());

            if(pathAZ.isEmpty() && pathZA.isEmpty())
            {
                logicalLinksToRemoveFromServiceLayer.add(oneLogicalLink);
                continue;
            }

            assert(pathAZ.get(0).getZ().equals(pathZA.get(pathZA.size()-1).getA()));
            assert(pathZA.get(0).getZ().equals(pathAZ.get(pathAZ.size()-1).getA()));

            String azPath = "AZ Path: ";
            for(TopoEdge azEdge : pathAZ)
                azPath += azEdge.getA().getUrn() + " --- ";
            azPath += pathAZ.get(pathAZ.size()-1).getZ().getUrn();
            log.info(azPath);

            String zaPath = "ZA Path: ";
            for(TopoEdge zaEdge : pathZA)
                zaPath += zaEdge.getA().getUrn() + " --- ";
            zaPath += pathZA.get(pathZA.size()-1).getZ().getUrn();
            log.info(zaPath);

            // Step 7: Calculate total cost-metric for logical link (in both directions).
            for(TopoEdge pathEdge : pathAZ)
                weightMetricAZ += pathEdge.getMetric();

            for(TopoEdge pathEdge : pathZA)
                weightMetricZA += pathEdge.getMetric();

            oneLogicalLink.setMetricAZ(weightMetricAZ);
            oneLogicalLink.setMetricZA(weightMetricZA);

            oneLogicalLink.setMetric(weightMetricAZ);   // The calling function expects metric to be set. Pathfinding is done in the forward direction, so we use that value here

            log.info("Metric: " + oneLogicalLink.getMetric());


            // Step 8: Map the physical path-pair to the corresponding logical link
            oneLogicalLink.setCorrespondingAZTopoEdges(pathAZ);
            oneLogicalLink.setCorrespondingZATopoEdges(pathZA);

            oneLogicalLink.setCorrespondingTopoEdges(pathAZ);   // Palindromic calling functions expect correspondingTopoEdges to be set. Pathfinding is done in the forward direction, so we use that value here.
        }

        // Step 9: If any logical links cannot be built, remove them from the Service-Layer Topology for this request.
        logicalLinks.removeAll(logicalLinksToRemoveFromServiceLayer);
    }


    /**
     * Doesn't destroy logical links, but resets cost metrics to 0, and clears the corresponding phyical TopoEdges (MPLS-ERO) lists.
     * This needs to be done, for example, prior to every call to calculateLogicalLinkWeights().
     */
    public void resetLogicalLinks()
    {
        llBackup.stream()
                .forEach(ll -> {
                    ll.getCorrespondingAZTopoEdges().clear();
                    ll.getCorrespondingZATopoEdges().clear();
                    ll.setMetric(0L);
                });

        logicalLinks.clear();
        logicalLinks.addAll(llBackup);
    }

    /**
     * Adds a VIRTUAL device and port onto the Service-layer to represent a request's starting node which is on the MPLS-layer.
     * This is necessary since if the request is sourced on the MPLS-layer, it has no foothold on the service-layer; VIRTUAL nodes are dummy hooks.
     * A bidirectional zero-cost link is added between the VIRTUAL port and MPLS-layer srcInPort.
     * If the specified topology nodes are already on the Service-layer, this method does nothing to modify the Service-layer topology.
     * @param srcDevice - Request's source device
     * @param srcInPort - Request's source port
     */
    public void buildLogicalLayerSrcNodes(TopoVertex srcDevice, TopoVertex srcInPort)
    {
        if(srcDevice.getVertexType().equals(VertexType.SWITCH))
            return;

        if(serviceLayerPorts.contains(srcInPort))
            return;

        TopoVertex virtualSrcPort = new TopoVertex(srcInPort.getUrn() + "-virtual", VertexType.VIRTUAL, PortLayer.ETHERNET);

        serviceLayerPorts.add(virtualSrcPort);

        TopoEdge vPortToPort = new TopoEdge(virtualSrcPort, srcInPort, 0L, Layer.ETHERNET);
        TopoEdge portToVPort = new TopoEdge(srcInPort, virtualSrcPort, 0L, Layer.ETHERNET);

        serviceLayerLinks.add(vPortToPort);
        serviceLayerLinks.add(portToVPort);

        nonAdjacentPorts.add(virtualSrcPort);

        buildLogicalLayerLinks();               // Should filter out duplicates
    }

    /**
     * Adds a VIRTUAL device and port onto the Service-layer to represent a request's terminating node which is on the MPLS-layer.
     * This is necessary since if the request is destined on the MPLS-layer, it has no foothold on the service-layer; VIRTUAL nodes are dummy hooks.
     * A bidirectional zero-cost link is added between the VIRTUAL port and MPLS-layer dstOutPort.
     * If the specified topology nodes are already on the Service-layer, this method does nothing to modify the Service-layer topology.
     * @param dstDevice - Request's destination device
     * @param dstOutPort - Request's destination port
     */
    public void buildLogicalLayerDstNodes(TopoVertex dstDevice, TopoVertex dstOutPort)
    {
        if(dstDevice.getVertexType().equals(VertexType.SWITCH))
            return;

        if(serviceLayerPorts.contains(dstOutPort))
            return;

        TopoVertex virtualDstPort = new TopoVertex(dstOutPort.getUrn() + "-virtual", VertexType.VIRTUAL, PortLayer.ETHERNET);

        serviceLayerPorts.add(virtualDstPort);

        TopoEdge vPortToPort = new TopoEdge(virtualDstPort, dstOutPort, 0L, Layer.ETHERNET);
        TopoEdge portToVPort = new TopoEdge(dstOutPort, virtualDstPort, 0L, Layer.ETHERNET);

        serviceLayerLinks.add(vPortToPort);
        serviceLayerLinks.add(portToVPort);

        nonAdjacentPorts.add(virtualDstPort);

        buildLogicalLayerLinks();               // Should filter out duplicates
    }


    /**
     * Assigns the passed in topology to the appropriate layer's global class variable
     * @param topology - Single-layer topology; Pre-managed and altered if necessary
     */
    public void setTopology(Topology topology)
    {
        assert(topology != null);

        Layer layer = topology.getLayer();

        if(layer.equals(Layer.ETHERNET))
            ethernetTopology = topology;
        else if(layer.equals(Layer.MPLS))
            mplsTopology = topology;
        else if(layer.equals(Layer.INTERNAL))
            internalTopology = topology;
        else
        {
            log.error("Topology passed to ServiceLayerTopology class with invalid Layer.");
            assert false;
        }
    }

    /**
     * Get the service-layer topology, including: ETHERNET devices, VIRTUAL devices, ETHERNET ports, VIRTUAL ports, INTERNAL links, ETHERNET links, LOGICAL links
     * @return Service-Layer topology as a combined Topology object
     */
    public Topology getSLTopology()
    {
        Topology topo = new Topology();

        topo.setLayer(Layer.ETHERNET);
        topo.getVertices().addAll(serviceLayerDevices);
        topo.getVertices().addAll(serviceLayerPorts);
        topo.getEdges().addAll(serviceLayerLinks);
        topo.getEdges().addAll(logicalLinks);

        return topo;
    }

    /**
     * Same as getSLTopology, except the logical link metrics and corresponding MPLS-layer EROs are assigned using the requested A->Z b/w constraint.
     * @return Service-Layer topology as a combined Topology object
     */
    public Topology getSLTopologyAZ()
    {
        Topology topo = new Topology();

        topo.setLayer(Layer.ETHERNET);
        topo.getVertices().addAll(serviceLayerDevices);
        topo.getVertices().addAll(serviceLayerPorts);
        topo.getEdges().addAll(serviceLayerLinks);

        Set<LogicalEdge> copyOfLogicalLinks = logicalLinks.stream()
                .collect(Collectors.toSet());

        for(LogicalEdge oneEdge : copyOfLogicalLinks)
            oneEdge.setMetric(oneEdge.getMetricAZ());

        topo.getEdges().addAll(copyOfLogicalLinks);

        return topo;
    }

    /**
     * Same as getSLTopology, except the logical link metrics and corresponding MPLS-layer EROs are assigned using the requested Z->A b/w constraint.
     * @return Service-Layer topology as a combined Topology object
     */
    public Topology getSLTopologyZA()
    {
        Topology topo = new Topology();

        topo.setLayer(Layer.ETHERNET);
        topo.getVertices().addAll(serviceLayerDevices);
        topo.getVertices().addAll(serviceLayerPorts);
        topo.getEdges().addAll(serviceLayerLinks);

        Set<LogicalEdge> copyOfLogicalLinks = logicalLinks.stream()
                .collect(Collectors.toSet());

        for(LogicalEdge oneEdge : copyOfLogicalLinks)
            oneEdge.setMetric(oneEdge.getMetricZA());

        topo.getEdges().addAll(copyOfLogicalLinks);

        return topo;
    }

    /**
     * Looks up a given MPLS-Layer node to find the corresponding VIRTUAL Service-layer node.
     * @param realNode - Physical node for which to find corresponding VIRTUAL node.
     * @return the appropriate VIRTUAL node, or null is no such node exists.
     */
    public TopoVertex getVirtualNode(TopoVertex realNode)
    {
        for(TopoVertex oneNode : serviceLayerPorts)
        {
            if(oneNode.getVertexType().equals(VertexType.VIRTUAL))
            {
                if(oneNode.getUrn().contains(realNode.getUrn()))
                {
                    return oneNode;
                }
            }
        }

        return null;
    }

    /**
     * Gets MPLS-Layer ERO given a Service-layer ERO, which possibly contains LOGICAL edges
     * @param serviceLayerERO - Service-layer ERO; may contain LOGICAL edges
     * @return Corresponding physical ERO
     */
    public List<TopoEdge> getActualERO(List<TopoEdge> serviceLayerERO)
    {
        List<TopoEdge> actualERO = new LinkedList<>();

        for(TopoEdge oneEdge : serviceLayerERO)
        {
            TopoVertex physicalA = oneEdge.getA();
            TopoVertex physicalZ = oneEdge.getZ();

            boolean edgeIsLogical = false;

            for(LogicalEdge oneLogical : logicalLinks)
            {
                TopoVertex logicalA = oneLogical.getA();
                TopoVertex logicalZ = oneLogical.getZ();

                // This link is logical - Get corresponding physical ERO
                if(physicalA.equals(logicalA) && physicalZ.equals(logicalZ))
                {
                    actualERO.addAll(oneLogical.getCorrespondingTopoEdges());
                    edgeIsLogical = true;

                    break;
                }
            }

            // This link is NOT logical - Part of actual ERO
            if(!edgeIsLogical)
            {
                actualERO.add(oneEdge);
            }
        }

        // Remove any edges containing VIRTUAL nodes - they don't exist in the actual ERO
        actualERO.removeIf(e -> e.getA().getVertexType().equals(VertexType.VIRTUAL));
        actualERO.removeIf(e -> e.getZ().getVertexType().equals(VertexType.VIRTUAL));

        return  actualERO;
    }

    /**
     * Same as getActualERO, except returns only the physical ERO as constrained by the requested A->Z b/w.
     * @param serviceLayerERO - Service-layer ERO; may contain LOGICAL edges
     * @return Corresponding physical ERO, constrained by requested A->Z b/w.
     */
    public List<TopoEdge> getActualEROAZ(List<TopoEdge> serviceLayerERO)
    {
        List<TopoEdge> actualERO = new LinkedList<>();

        for(TopoEdge oneEdge : serviceLayerERO)
        {
            TopoVertex physicalA = oneEdge.getA();
            TopoVertex physicalZ = oneEdge.getZ();

            boolean edgeIsLogical = false;

            for(LogicalEdge oneLogical : logicalLinks)
            {
                TopoVertex logicalA = oneLogical.getA();
                TopoVertex logicalZ = oneLogical.getZ();

                // This link is logical - Get corresponding physical ERO
                if(physicalA.equals(logicalA) && physicalZ.equals(logicalZ))
                {
                    actualERO.addAll(oneLogical.getCorrespondingAZTopoEdges());
                    edgeIsLogical = true;

                    break;
                }
            }

            // This link is NOT logical - Part of actual ERO
            if(!edgeIsLogical)
            {
                actualERO.add(oneEdge);
            }
        }

        // Remove any edges containing VIRTUAL nodes - they don't exist in the actual ERO
        actualERO.removeIf(e -> e.getA().getVertexType().equals(VertexType.VIRTUAL));
        actualERO.removeIf(e -> e.getZ().getVertexType().equals(VertexType.VIRTUAL));

        return  actualERO;
    }

    /**
     * Same as getActualERO, except returns only the physical ERO as constrained by the requested Z->A b/w.
     * @param serviceLayerERO - Service-layer ERO; may contain LOGICAL edges
     * @return Corresponding physical ERO, constrained by requested Z->A b/w.
     */
    public List<TopoEdge> getActualEROZA(List<TopoEdge> serviceLayerERO)
    {
        List<TopoEdge> actualERO = new LinkedList<>();

        for(TopoEdge oneEdge : serviceLayerERO)
        {
            TopoVertex physicalA = oneEdge.getA();
            TopoVertex physicalZ = oneEdge.getZ();

            boolean edgeIsLogical = false;

            for(LogicalEdge oneLogical : logicalLinks)
            {
                TopoVertex logicalA = oneLogical.getA();
                TopoVertex logicalZ = oneLogical.getZ();

                // This link is logical - Get corresponding physical ERO
                if(physicalA.equals(logicalA) && physicalZ.equals(logicalZ))
                {
                    actualERO.addAll(oneLogical.getCorrespondingZATopoEdges());
                    edgeIsLogical = true;

                    break;
                }
            }

            // This link is NOT logical - Part of actual ERO
            if(!edgeIsLogical)
            {
                actualERO.add(oneEdge);
            }
        }

        // Remove any edges containing VIRTUAL nodes - they don't exist in the actual ERO
        actualERO.removeIf(e -> e.getA().getVertexType().equals(VertexType.VIRTUAL));
        actualERO.removeIf(e -> e.getZ().getVertexType().equals(VertexType.VIRTUAL));

        return  actualERO;
    }
}
