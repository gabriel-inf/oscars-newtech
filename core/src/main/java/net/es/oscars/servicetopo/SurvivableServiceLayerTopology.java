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
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.pce.BhandariPCE;
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
public class SurvivableServiceLayerTopology
{
    @Autowired
    private PruningService pruningService;

    @Autowired
    private BhandariPCE bhandariPCE;

    private Set<TopoVertex> serviceLayerDevices;    // ETHERNET and VIRTUAL devices
    private Set<TopoVertex> serviceLayerPorts;      // ETHERNET and VIRTUAL ports
    private Set<TopoEdge> serviceLayerLinks;        // ETHERNET and LOGICAL edges

    private Set<TopoVertex> mplsLayerDevices;       // MPLS devices
    private Set<TopoVertex> mplsLayerPorts;         // MPLS ports
    private Set<TopoEdge> mplsLayerLinks;           // MPLS edges

    private Set<TopoVertex> nonAdjacentPorts;       // Service-layer ports which connect to MPLS-layer ports
    private Set<SurvivableLogicalEdge> logicalLinks;          // Abstraction of survivable path between two nonadjacent ports
    private Set<SurvivableLogicalEdge> llBackup;              // Copy of logicalLinks, used for resetting, etc.

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

        log.info("decomposing topology into Service-Layer and MPLS-Layer.");
        buildServiceLayerTopo();
        buildMplsLayerTopo();

        log.info("building logical edges for Service-Layer topology.");
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
                .filter(d -> d.getVertexType().equals(VertexType.SWITCH))
                .collect(Collectors.toSet());


        // Parse the Ports
        Set<TopoVertex> allEthernetPorts = ethernetVertices.stream()
                .filter(p -> p.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());


        // Parse the INTERNAL Edges
        Set<TopoEdge> allInternalEthernetEdges = allInternalEdges.stream()
                .filter(e -> !e.getA().getVertexType().equals(VertexType.ROUTER) && !e.getZ().getVertexType().equals(VertexType.ROUTER))
                .collect(Collectors.toSet());


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
                .filter(d -> d.getVertexType().equals(VertexType.ROUTER))
                .collect(Collectors.toSet());


        // Parse the Ports
        Set<TopoVertex> allMplsPorts = mplsVertices.stream()
                .filter(p -> p.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());


        // Parse the INTERNAL Edges
        Set<TopoEdge> allInternalMPLSEdges = allInternalEdges.stream()
                .filter(e -> !e.getA().getVertexType().equals(VertexType.SWITCH) && !e.getZ().getVertexType().equals(VertexType.SWITCH))
                .collect(Collectors.toSet());


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
            TopoVertex portA = serviceLink.getA();
            TopoVertex portZ = serviceLink.getZ();

            // Skip INTERNAL edges
            if(serviceLink.getLayer().equals(Layer.INTERNAL))
            {
                continue;
            }

            // ETHERNET -> MPLS edge
            if(serviceLayerPorts.contains(portA) && !serviceLayerPorts.contains(portZ))
            {
                nonAdjacentPorts.add(portA);
            }

            // MPLS -> Ethernet edge
            if(!serviceLayerPorts.contains(portA) && serviceLayerPorts.contains(portZ))
            {
                nonAdjacentPorts.add(portZ);
            }
        }
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

                SurvivableLogicalEdge azLogicalEdge = new SurvivableLogicalEdge(nonAdjacentA,nonAdjacentZ, 0L, 0L, Layer.LOGICAL, new ArrayList<>(), new ArrayList<>());
                SurvivableLogicalEdge zaLogicalEdge = new SurvivableLogicalEdge(nonAdjacentZ,nonAdjacentA, 0L, 0L, Layer.LOGICAL, new ArrayList<>(), new ArrayList<>());

                azLogicalEdge.setMetric(0L);
                zaLogicalEdge.setMetric(0L);

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

    /**
     * Calls PruningService and DijkstraPCE methods to compute shortest MPLS-layer path-pair, calculates the combined weight of each path, and maps them to the appropriate logical links.
     * @param requestedVlanPipe - Request pipe
     * @param urnList - List of URNs in the network; Necessary for passing to PruningService methods
     * @param bwAvailMap - A map of available "Ingress" and 'Egress" bandwidth for each URN.
     * @param rsvVlanList - List of currently reserved VLAN elements (during request schedule)
     */
    public void calculateLogicalLinkWeights(RequestedVlanPipeE requestedVlanPipe, List<UrnE> urnList,
                                            Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList){
        Set<SurvivableLogicalEdge> logicalLinksToRemoveFromServiceLayer = new HashSet<>();

        Topology mplsLayerTopo = new Topology();
        mplsLayerTopo.getVertices().addAll(mplsLayerDevices);
        mplsLayerTopo.getVertices().addAll(mplsLayerPorts);
        mplsLayerTopo.getEdges().addAll(mplsLayerLinks);

        // Step 1: Prune MPLS-Layer topology once before considering any logical links.
        Topology prunedMPLSTopo = pruningService.pruneWithPipe(mplsLayerTopo, requestedVlanPipe, urnList, bwAvailMap, rsvVlanList);

        for(SurvivableLogicalEdge oneLogicalLink : logicalLinks)
        {
            TopoVertex srcEthPort = oneLogicalLink.getA();      //Ethernet-source of logical link
            TopoVertex dstEthPort = oneLogicalLink.getZ();      //Ethernet-dest of logical link
            TopoVertex mplsSrcPort;                             //MPLS port adjacent to srcEthPort
            TopoVertex mplsDstPort;                             //MPLS port adjacent to dstEthPort
            TopoVertex mplsSrcDevice = null;                           //MPLS device adjacent to mplsSrcPort
            TopoVertex mplsDstDevice = null;                           //MPLS device adjacent to mplsDstPort

            TopoEdge physEdgeAtoMpls = null;
            TopoEdge physEdgeZtoMpls = null;
            TopoEdge physEdgeMplstoA = null;
            TopoEdge physEdgeMplstoZ = null;

            TopoEdge physEdgeMplsSrcPortToDevice = null;
            TopoEdge physEdgeMplsDstDeviceToPort = null;

            Set<TopoVertex> adaptationPorts = new HashSet<>();
            Set<TopoEdge> adaptationEdges = new HashSet<>();

            Topology adaptationTopo = new Topology();

            long weightMetricPrimary = 0;
            long weightMetricSecondary = 0;

            for(TopoEdge oneSLLink : serviceLayerLinks)
            {
                if(oneSLLink.getA().equals(srcEthPort))
                {
                    if(oneSLLink.getZ().getVertexType().equals(VertexType.PORT))
                    {
                        physEdgeAtoMpls = oneSLLink;
                    }
                }
                else if(oneSLLink.getZ().equals(srcEthPort))
                {
                    if(oneSLLink.getA().getVertexType().equals(VertexType.PORT))
                    {
                        physEdgeMplstoA = oneSLLink;
                    }
                }

                if(oneSLLink.getZ().equals(dstEthPort))
                {
                    if (oneSLLink.getA().getVertexType().equals(VertexType.PORT)) {
                        physEdgeMplstoZ = oneSLLink;
                    }
                }
                else if(oneSLLink.getA().equals(dstEthPort))
                {
                    if(oneSLLink.getZ().getVertexType().equals(VertexType.PORT))
                    {
                        physEdgeZtoMpls = oneSLLink;
                    }
                }
            }

            if(physEdgeAtoMpls == null || physEdgeZtoMpls == null || physEdgeMplstoA == null || physEdgeMplstoZ == null)
            {
                log.error("service-layer topology has incorrectly identified adaptation edges");
                assert false;
            }


            mplsSrcPort = physEdgeAtoMpls.getZ();
            mplsDstPort = physEdgeMplstoZ.getA();

            for(TopoEdge oneMplsLink : mplsLayerLinks)
            {
                if(oneMplsLink.getA().equals(mplsSrcPort))
                {
                    if(oneMplsLink.getZ().getVertexType().equals(VertexType.ROUTER))
                    {
                        mplsSrcDevice = oneMplsLink.getZ();
                        physEdgeMplsSrcPortToDevice = oneMplsLink;
                    }
                }
                else if(oneMplsLink.getZ().equals(mplsDstPort))
                {
                    if(oneMplsLink.getA().getVertexType().equals(VertexType.ROUTER))
                    {
                        mplsDstDevice = oneMplsLink.getA();
                        physEdgeMplsDstDeviceToPort = oneMplsLink;
                    }
                }
            }

            assert(mplsSrcDevice != null);
            assert(mplsDstDevice != null);

            // Step 2: Prune the adaptation (Ethernet-MPLS) edges and ports to ensure this logical link is worth building.
            adaptationPorts.add(srcEthPort);
            adaptationPorts.add(dstEthPort);
            adaptationPorts.add(mplsSrcPort);
            adaptationPorts.add(mplsDstPort);

            adaptationEdges.add(physEdgeAtoMpls);
            adaptationEdges.add(physEdgeZtoMpls);
            adaptationEdges.add(physEdgeMplstoA);
            adaptationEdges.add(physEdgeMplstoZ);

            adaptationTopo.setVertices(adaptationPorts);
            adaptationTopo.setEdges(adaptationEdges);

            Topology prunedAdaptationTopo = pruningService.pruneWithPipe(adaptationTopo, requestedVlanPipe, bwAvailMap, rsvVlanList);

            if(!prunedAdaptationTopo.equals(adaptationTopo))
            {
                logicalLinksToRemoveFromServiceLayer.add(oneLogicalLink);
                continue;
            }

            // Step 3: Perform routing on MPLS layer to construct physical routes corresponding to the logical link.

            List<List<TopoEdge>> pathPair = bhandariPCE.computeDisjointPaths(prunedMPLSTopo, mplsSrcDevice, mplsDstDevice, 2);

            if(pathPair.isEmpty())
            {
                logicalLinksToRemoveFromServiceLayer.add(oneLogicalLink);
                continue;
            }
            else if(pathPair.size() != 2)
            {
                logicalLinksToRemoveFromServiceLayer.add(oneLogicalLink);
                continue;
            }

            // Step 4: Calculate total cost-metric for logical link.

            List<TopoEdge> primaryPath = pathPair.get(0);
            List<TopoEdge> secondaryPath = pathPair.get(1);


            // Add the src/dst ports back onto the paths
            for(List<TopoEdge> path : pathPair)
            {
                path.add(0, physEdgeMplsSrcPortToDevice);
                path.add(physEdgeMplsDstDeviceToPort);
            }

            // Primary
            for(TopoEdge pathEdge : primaryPath)
            {
                weightMetricPrimary += pathEdge.getMetric();
            }

            // Add *uni-directional* cost of adaptation links to Logical Link weight since these are ETHERNET links, but will implicitly be used in BOTH directions across two logical links
            weightMetricPrimary += physEdgeAtoMpls.getMetric();
            weightMetricPrimary += physEdgeMplstoZ.getMetric();

            oneLogicalLink.setMetricPrimary(weightMetricPrimary);

            // Secondary
            for(TopoEdge pathEdge : secondaryPath)
            {
                weightMetricSecondary += pathEdge.getMetric();
            }

            // Add *uni-directional* cost of adaptation links to Logical Link weight since these are ETHERNET links, but will implicitly be used in BOTH directions across two logical links
            weightMetricSecondary += physEdgeAtoMpls.getMetric();
            weightMetricSecondary += physEdgeMplstoZ.getMetric();

            oneLogicalLink.setMetricSecondary(weightMetricSecondary);

            oneLogicalLink.setMetric(weightMetricPrimary);   // The calling function expects metric to be set. Pathfinding is done based on shortest primary path, so we use that value here

            // Step 5: Store the physical route corresponding to this logical link
            for(List<TopoEdge> path : pathPair)
            {
                path.add(0, physEdgeAtoMpls);
                path.add(physEdgeMplstoZ);
            }

            oneLogicalLink.setCorrespondingPrimaryTopoEdges(pathPair.get(0));
            oneLogicalLink.setCorrespondingSecondaryTopoEdges(pathPair.get(1));
        }

        // Step 6: If any logical links cannot be built, remove them from the Service-Layer Topology for this request.
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
                    ll.getCorrespondingPrimaryTopoEdges().clear();
                    ll.getCorrespondingSecondaryTopoEdges().clear();
                    ll.setMetric(0L);
                    ll.setMetricPrimary(0L);
                    ll.setMetricSecondary(0L);
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
        {
            return;
        }

        TopoVertex virtualSrcDevice = new TopoVertex(srcDevice.getUrn() + "-virtual", VertexType.VIRTUAL);
        TopoVertex virtualSrcPort = new TopoVertex(srcInPort.getUrn() + "-virtual", VertexType.VIRTUAL);

        serviceLayerDevices.add(virtualSrcDevice);
        serviceLayerPorts.add(virtualSrcPort);

        TopoEdge portToDevice = new TopoEdge(virtualSrcPort, virtualSrcDevice, 0L, Layer.ETHERNET);
        TopoEdge devicetoPort = new TopoEdge(virtualSrcDevice, virtualSrcPort, 0L, Layer.ETHERNET);

        TopoEdge vPortToPort = new TopoEdge(virtualSrcPort, srcInPort, 0L, Layer.ETHERNET);
        TopoEdge portToVPort = new TopoEdge(srcInPort, virtualSrcPort, 0L, Layer.ETHERNET);

        serviceLayerLinks.add(portToDevice);
        serviceLayerLinks.add(devicetoPort);
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
        {
            return;
        }


        TopoVertex virtualDstDevice = new TopoVertex(dstDevice.getUrn() + "-virtual", VertexType.VIRTUAL);
        TopoVertex virtualDstPort = new TopoVertex(dstOutPort.getUrn() + "-virtual", VertexType.VIRTUAL);

        serviceLayerDevices.add(virtualDstDevice);
        serviceLayerPorts.add(virtualDstPort);

        TopoEdge portToDevice = new TopoEdge(virtualDstPort, virtualDstDevice, 0L, Layer.ETHERNET);
        TopoEdge devicetoPort = new TopoEdge(virtualDstDevice, virtualDstPort, 0L, Layer.ETHERNET);

        TopoEdge vPortToPort = new TopoEdge(virtualDstPort, dstOutPort, 0L, Layer.ETHERNET);
        TopoEdge portToVPort = new TopoEdge(dstOutPort, virtualDstPort, 0L, Layer.ETHERNET);

        serviceLayerLinks.add(portToDevice);
        serviceLayerLinks.add(devicetoPort);
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
     * Looks up a given MPLS-Layer node to find the corresponding VIRTUAL Service-layer node.
     * @param realNode - Physical node for which to find corresponding VIRTUAL node.
     * @return the appropriate VIRTUAL node, or null is no such node exists.
     */
    public TopoVertex getVirtualNode(TopoVertex realNode)
    {
        for(TopoVertex oneNode : serviceLayerDevices)
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
     * Gets MPLS-Layer Primary ERO given a Service-layer ERO, which possibly contains LOGICAL edges
     * @param serviceLayerERO - Service-layer ERO; may contain LOGICAL edges
     * @return Corresponding physical ERO
     */
    public List<TopoEdge> getActualPrimaryERO(List<TopoEdge> serviceLayerERO)
    {
        List<TopoEdge> actualERO = new LinkedList<>();

        for(TopoEdge oneEdge : serviceLayerERO)
        {
            TopoVertex physicalA = oneEdge.getA();
            TopoVertex physicalZ = oneEdge.getZ();

            boolean edgeIsLogical = false;

            for(SurvivableLogicalEdge oneLogical : logicalLinks)
            {
                TopoVertex logicalA = oneLogical.getA();
                TopoVertex logicalZ = oneLogical.getZ();

                // This link is logical - Get corresponding physical ERO
                if(physicalA.equals(logicalA) && physicalZ.equals(logicalZ))
                {
                    actualERO.addAll(oneLogical.getCorrespondingPrimaryTopoEdges());
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
     * Gets MPLS-Layer Secondary ERO given a Service-layer ERO, which possibly contains LOGICAL edges
     * @param serviceLayerERO - Service-layer ERO; may contain LOGICAL edges
     * @return Corresponding physical ERO
     */
    public List<TopoEdge> getActualSecondaryERO(List<TopoEdge> serviceLayerERO)
    {
        List<TopoEdge> actualERO = new LinkedList<>();

        for(TopoEdge oneEdge : serviceLayerERO)
        {
            TopoVertex physicalA = oneEdge.getA();
            TopoVertex physicalZ = oneEdge.getZ();

            boolean edgeIsLogical = false;

            for(SurvivableLogicalEdge oneLogical : logicalLinks)
            {
                TopoVertex logicalA = oneLogical.getA();
                TopoVertex logicalZ = oneLogical.getZ();

                // This link is logical - Get corresponding physical ERO
                if(physicalA.equals(logicalA) && physicalZ.equals(logicalZ))
                {
                    actualERO.addAll(oneLogical.getCorrespondingSecondaryTopoEdges());
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
