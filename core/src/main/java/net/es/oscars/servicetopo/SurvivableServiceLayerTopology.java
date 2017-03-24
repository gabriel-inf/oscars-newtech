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
    private Set<SurvivableLogicalEdge> logicalLinks;          // Abstraction of path between two nonadjacent ports
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

                List<Long> kMetrics = new ArrayList<>();     // Set of metrics for each K-path
                List<List<TopoEdge>> kPaths = new ArrayList<>();   // Set of K-paths

                SurvivableLogicalEdge azLogicalEdge = new SurvivableLogicalEdge(nonAdjacentA,nonAdjacentZ, kMetrics, Layer.LOGICAL, kPaths);

                logicalLinks.add(azLogicalEdge);
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
    public void calculateLogicalLinkWeights(RequestedVlanPipeE requestedVlanPipe, List<UrnE> urnList, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList, int numDisjoint)
    {
        Set<SurvivableLogicalEdge> logicalLinksToRemoveFromServiceLayer = new HashSet<>();

        // Step 1: Construct MPLS-Layer topology
        Topology mplsLayerTopo = new Topology();
        mplsLayerTopo.getVertices().addAll(mplsLayerDevices);
        mplsLayerTopo.getVertices().addAll(mplsLayerPorts);
        mplsLayerTopo.getEdges().addAll(mplsLayerLinks);

        for(SurvivableLogicalEdge oneLogicalLink : logicalLinks)
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

            Set<TopoEdge> adaptationEdgeSet = new HashSet<>();

            // Step 2: Identify adaptation edges between ETHERNET ports and MPLS ports/devices
            for(TopoEdge oneSLLink : serviceLayerLinks)
            {
                TopoVertex slA = oneSLLink.getA();
                TopoVertex slZ = oneSLLink.getZ();

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


            if(!srcIsVirtual)
            {
                if(!srcIsOnSwitch)
                {
                    adaptationEdgeSet.add(physEdgeAtoRouter);
                    adaptationEdgeSet.add(physEdgeRoutertoA);
                }
                else
                {
                    adaptationEdgeSet.add(physEdgeAtoMplsPort);
                    adaptationEdgeSet.add(physEdgeMplsPorttoA);
                }
            }

            if(!dstIsVirtual)
            {
                if(!dstIsOnSwitch)
                {
                    adaptationEdgeSet.add(physEdgeZtoRouter);
                    adaptationEdgeSet.add(physEdgeRoutertoZ);
                }
                else
                {
                    adaptationEdgeSet.add(physEdgeZtoMplsPort);
                    adaptationEdgeSet.add(physEdgeMplsPorttoZ);
                }
            }

            // Only add links to VIRTUAL ports if these ports are end-points of the current logical link
            for(TopoEdge oneSlLink : serviceLayerLinks)
            {
                if(oneSlLink.getA().getVertexType().equals(VertexType.VIRTUAL))
                {
                    if(oneSlLink.getA().equals(srcEthPort) || oneSlLink.getA().equals(dstEthPort))
                        adaptationEdgeSet.add(oneSlLink);
                }

                if(oneSlLink.getZ().getVertexType().equals(VertexType.VIRTUAL))
                {
                    if(oneSlLink.getZ().equals(srcEthPort) || oneSlLink.getZ().equals(dstEthPort))
                        adaptationEdgeSet.add(oneSlLink);
                }
            }

            // Step 3: Add ETHERNET src/dst ports and adaptation edges to MPLS-Layer topology
            mplsLayerTopo.getVertices().add(srcEthPort);
            mplsLayerTopo.getVertices().add(dstEthPort);
            mplsLayerTopo.getEdges().addAll(adaptationEdgeSet);


            // Step 4: Prune updated MPLS-Layer topology before pathfinding.
            Topology prunedMPLSTopo = pruningService.pruneWithPipe(mplsLayerTopo, requestedVlanPipe, urnList, bwAvailMap, rsvVlanList);


            // Step 5: Compute MPLS-Layer routes to construct physical survivable paths corresponding to this logical link. Bhandari will modify the input to perform disjoint routing between MPLS devices (port src/dst would always result in failures).
            List<List<TopoEdge>> pathSet = bhandariPCE.computeDisjointPaths(prunedMPLSTopo, srcEthPort, dstEthPort, numDisjoint, adaptationEdgeSet);

            // Step 6: Delete ETHERNET src/dst ports and adaptation edges from MPLS-Layer topology so they can't be used in pathfinding for unrelated logical links
            mplsLayerTopo.getVertices().remove(srcEthPort);
            mplsLayerTopo.getVertices().remove(dstEthPort);
            mplsLayerTopo.getEdges().removeAll(adaptationEdgeSet);


            if(pathSet.isEmpty() || (pathSet.size() != numDisjoint))
            {
                logicalLinksToRemoveFromServiceLayer.add(oneLogicalLink);
                continue;
            }

            assert(pathSet.size() == numDisjoint);

            // Step 7: Calculate total cost-metric for logical link and Step 8: map the physical path-set to the corresponding logical link.
            for(int p = 0; p < pathSet.size(); p++)
            {
                List<TopoEdge> onePath = pathSet.get(p);
                Long oneMetric = 0L;

                for(TopoEdge oneEdge : onePath)
                    oneMetric += oneEdge.getMetric();

                // The calling function expects metric to be set. Pathfinding is done in consideration of the primary path, so we set that value here.
                if(p == 0)
                    oneLogicalLink.setMetric(oneMetric);

                oneLogicalLink.getKMetrics().add(oneMetric);
                oneLogicalLink.getKCorrespondingTopoEdges().add(onePath);
            }

            assert(oneLogicalLink.getKMetrics().size() == numDisjoint);
            assert(oneLogicalLink.getKCorrespondingTopoEdges().size() == numDisjoint);
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
                    ll.getKCorrespondingTopoEdges().clear();
                    ll.setMetric(0L);
                    ll.getKCorrespondingTopoEdges().clear();
                });

        logicalLinks.clear();
        logicalLinks.addAll(llBackup);
    }

    /**
     * Adds a VIRTUAL port onto the Service-layer to represent a request's starting node which is on the MPLS-layer.
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
     * Adds a VIRTUAL port onto the Service-layer to represent a request's terminating node which is on the MPLS-layer.
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
     * Gets MPLS-Layer A-Z ERO given a Service-layer ERO, which possibly contains LOGICAL edges
     * @param serviceLayerERO - Service-layer ERO; may contain LOGICAL edges
     * @param numDisjoint - Request's K-disjoint path requirement
     * @return Corresponding physical ERO
     */
    public List<List<TopoEdge>> getActualEroList(List<TopoEdge> serviceLayerERO, int numDisjoint)
    {
        List<List<TopoEdge>> actualEroList = new ArrayList<>();

        for(int k = 1; k <= numDisjoint; k++)
        {
            List<TopoEdge> oneActualERO = new ArrayList<>();
            actualEroList.add(oneActualERO);
        }

        for(TopoEdge oneEdge : serviceLayerERO)
        {
            TopoVertex physicalA = oneEdge.getA();
            TopoVertex physicalZ = oneEdge.getZ();

            // This link is physical - Part of actual ERO
            if(!oneEdge.getLayer().equals(Layer.LOGICAL))
            {
                for(int k = 1; k <= numDisjoint; k++)
                {
                    List<TopoEdge> oneActualERO = actualEroList.get(k-1);
                    oneActualERO.add(oneEdge);
                }
            }
            else    // This link is logical - Get corresponding physical EROs
            {
                List<SurvivableLogicalEdge> matchingLogicalEdges = logicalLinks.stream()
                        .filter(ll -> ll.getA().equals(physicalA) && ll.getZ().equals(physicalZ))
                        .collect(Collectors.toList());

                assert(matchingLogicalEdges.size() == 1);
                SurvivableLogicalEdge matchingEdge = matchingLogicalEdges.get(0);

                for(int k = 1; k <= numDisjoint; k++)
                {
                    List<TopoEdge> oneActualERO = actualEroList.get(k-1);
                    oneActualERO.addAll(matchingEdge.getKCorrespondingTopoEdges().get(k-1));
                }
            }
        }

        // Remove any edges containing VIRTUAL nodes - they don't exist in the actual EROs
        for(int k = 1; k <= numDisjoint; k++)
        {
            List<TopoEdge> oneActualERO = actualEroList.get(k-1);
            oneActualERO.removeIf(e -> e.getA().getVertexType().equals(VertexType.VIRTUAL));
            oneActualERO.removeIf(e -> e.getZ().getVertexType().equals(VertexType.VIRTUAL));
        }

        assert(actualEroList.size() == numDisjoint);
        actualEroList.stream().forEach(ero -> {assert(!ero.isEmpty());});

        return  actualEroList;
    }
}
