package net.es.oscars.servicetopo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.DijkstraPCE;
import net.es.oscars.pce.PruningService;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.resv.ent.ReservedVlanE;
import net.es.oscars.resv.ent.ScheduleSpecificationE;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.VertexType;
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

                LogicalEdge azLogicalEdge = new LogicalEdge(nonAdjacentA,nonAdjacentZ, 0L, 0L, 0L, Layer.LOGICAL, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                LogicalEdge zaLogicalEdge = new LogicalEdge(nonAdjacentZ,nonAdjacentA, 0L, 0L, 0L, Layer.LOGICAL, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

                logicalLinks.add(azLogicalEdge);
                logicalLinks.add(zaLogicalEdge);
            }
        }

        log.info("logical edges added to Service-Layer Topology.");

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
     * @param requestedSchedule - Request schedule
     * @param urnList - List of URNs in the network; Necessary for passing to PruningService methods
     * @param rsvBwList - List of currently reserved Bandwidth elements (during request schedule)
     * @param rsvVlanList - List of currently reserved VLAN elements (during request schedule)
     */
    public void calculateLogicalLinkWeights(RequestedVlanPipeE requestedVlanPipe, ScheduleSpecificationE requestedSchedule, List<UrnE> urnList, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList)
    {
        //if(requestedVlanPipe.getAzMbps() == requestedVlanPipe.getZaMbps())
        //    this.calculateLogicalLinkWeightsSymmetric(requestedVlanPipe, requestedSchedule, urnList, rsvBwList, rsvVlanList);
        //else
            this.calculateLogicalLinkWeightsAsymmetric(requestedVlanPipe, requestedSchedule, urnList, rsvBwList, rsvVlanList);
    }


    /**
     * Calls PruningService and DijkstraPCE methods to compute shortest MPLS-layer paths, calculates the combined weight of each path, and maps them to the appropriate logical links.
     * This method may no longer be worth keeping since it was too naive to handle a number of general cases.
     * @param requestedVlanPipe - Request pipe
     * @param requestedSchedule - Request schedule
     * @param urnList - List of URNs in the network; Necessary for passing to PruningService methods
     * @param rsvBwList - List of currently reserved Bandwidth elements (during request schedule)
     * @param rsvVlanList - List of currently reserved VLAN elements (during request schedule)
     */
    private void calculateLogicalLinkWeightsSymmetric(RequestedVlanPipeE requestedVlanPipe, ScheduleSpecificationE requestedSchedule, List<UrnE> urnList, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList)
    {
        log.info("Performing Symmetric routing on MPLS-Layer topology to assign weights to Service-Layer logical links.");
        Set<LogicalEdge> logicalLinksToRemoveFromServiceLayer = new HashSet<>();

        Topology mplsLayerTopo = new Topology();
        mplsLayerTopo.getVertices().addAll(mplsLayerDevices);
        mplsLayerTopo.getVertices().addAll(mplsLayerPorts);
        mplsLayerTopo.getEdges().addAll(mplsLayerLinks);

        // Step 1: Prune MPLS-Layer topology once before considering any logical links.
        log.info("step 1: pruning MPLS-layer by bandwidth and vlan availability.");
        Topology prunedMPLSTopo = pruningService.pruneWithPipe(mplsLayerTopo, requestedVlanPipe, requestedSchedule, urnList, rsvBwList, rsvVlanList);
        log.info("step 1 COMPLETE.");

        for(LogicalEdge oneLogicalLink : logicalLinks)
        {
            TopoVertex srcEthPort = oneLogicalLink.getA();      //Ethernet-source of logical link
            TopoVertex dstEthPort = oneLogicalLink.getZ();      //Ethernet-dest of logical link
            TopoVertex mplsSrc;                                 //MPLS port adjacent to srcEthPort
            TopoVertex mplsDst;                                 //MPLS port adjacent to dstEthPort

            TopoEdge physEdgeAtoMpls = null;
            TopoEdge physEdgeZtoMpls = null;
            TopoEdge physEdgeMplstoA = null;
            TopoEdge physEdgeMplstoZ = null;

            Set<TopoVertex> adaptationPorts = new HashSet<>();
            Set<TopoEdge> adaptationEdges = new HashSet<>();

            Topology adaptationTopo = new Topology();

            long weightMetric = 0;

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

            // Step 2: Prune the adaptation (Ethernet-MPLS) edges and ports to ensure this logical link is worth building.
            log.info("step 2: pruning adaptation (Ethernet-MPLS) edges and ports to ensure logical link is worth building.");
            mplsSrc = physEdgeAtoMpls.getZ();
            mplsDst = physEdgeMplstoZ.getA();

            adaptationPorts.add(srcEthPort);
            adaptationPorts.add(dstEthPort);
            adaptationPorts.add(mplsSrc);
            adaptationPorts.add(mplsDst);

            adaptationEdges.add(physEdgeAtoMpls);
            adaptationEdges.add(physEdgeZtoMpls);
            adaptationEdges.add(physEdgeMplstoA);
            adaptationEdges.add(physEdgeMplstoZ);

            adaptationTopo.setVertices(adaptationPorts);
            adaptationTopo.setEdges(adaptationEdges);

            Topology prunedAdaptationTopo = pruningService.pruneWithPipe(adaptationTopo, requestedVlanPipe, requestedSchedule, rsvBwList, rsvVlanList);

            if(!prunedAdaptationTopo.equals(adaptationTopo))
            {
                log.info("cannot assign weight to logical edge: adaptation ports/links do not support demand.");
                log.info("step 2 FAILED.");
                log.info("removing logical link from Service-Layer topology.");

                logicalLinksToRemoveFromServiceLayer.add(oneLogicalLink);
                continue;
            }
            log.info("step 2 COMPLETE.");

            for(TopoEdge tedge : mplsLayerTopo.getEdges())
            {
                log.info("MPLS Edge: (" + tedge.getA().getUrn() + "," + tedge.getZ().getUrn() + ")");
            }

            for(TopoEdge tedge : prunedMPLSTopo.getEdges())
            {
                log.info("MPLS After Edge: (" + tedge.getA().getUrn() + "," + tedge.getZ().getUrn() + ")");
            }

            log.info("MPLS edge SIZE = " + mplsLayerTopo.getEdges().size());
            log.info("MPLS after SIZE = " + prunedMPLSTopo.getEdges().size());

            // Step 3: Perform routing on MPLS layer to construct physical routes corresponding to the logical link.
            log.info("step 3: performing MPLS-Layer routing.");

            List<TopoEdge> path = dijkstraPCE.computeShortestPathEdges(prunedMPLSTopo, mplsSrc, mplsDst);

            if(path.isEmpty())
            {
                log.error("no path found for logical link.");
                log.info("step 3 FAILED.");
                log.info("removing logical link from Service-Layer topology.");
                logicalLinksToRemoveFromServiceLayer.add(oneLogicalLink);
                continue;
            }

            log.info("step 3 COMPLETE.");

            // Step 4: Calculate total cost-metric for logical link.
            log.info("step 4: compute logical link-weights.");
            for(TopoEdge pathEdge : path)
            {
                weightMetric += pathEdge.getMetric();
            }

            // Add *uni-directional* cost of adaptation links to Logical Link weight since these are ETHERNET links, but will implicitly be used in BOTH directions across two logical links
            weightMetric += physEdgeAtoMpls.getMetric();
            weightMetric += physEdgeMplstoZ.getMetric();

            oneLogicalLink.setMetric(weightMetric);

            log.info("step 4 COMPLETE.");

            // Step 5: Store the physical route corresponding to this logical link
            path.add(0, physEdgeAtoMpls);
            path.add(physEdgeMplstoZ);

            oneLogicalLink.setCorrespondingTopoEdges(path);
        }

        // Step 6: If any logical links cannot be built, remove them from the Service-Layer Topology for this request.
        logicalLinks.removeAll(logicalLinksToRemoveFromServiceLayer);
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
     * @param requestedSchedule - Request schedule
     * @param urnList - List of URNs in the network; Necessary for passing to PruningService methods
     * @param rsvBwList - List of currently reserved Bandwidth elements (during request schedule)
     * @param rsvVlanList - List of currently reserved VLAN elements (during request schedule)
     */
    private void calculateLogicalLinkWeightsAsymmetric(RequestedVlanPipeE requestedVlanPipe, ScheduleSpecificationE requestedSchedule, List<UrnE> urnList, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList)
    {
        log.info("Performing Asymmetric routing on MPLS-Layer topology to assign weights to Service-Layer logical links.");
        Set<LogicalEdge> logicalLinksToRemoveFromServiceLayer = new HashSet<>();

        Topology mplsLayerTopo = new Topology();
        mplsLayerTopo.getVertices().addAll(mplsLayerDevices);
        mplsLayerTopo.getVertices().addAll(mplsLayerPorts);
        mplsLayerTopo.getEdges().addAll(mplsLayerLinks);

        // Step 1: Prune MPLS-Layer topology once before considering any logical links.
        log.info("step 1: pruning MPLS-layer by bandwidth and vlan availability for each direction.");
        Topology prunedMPLSTopoAZ = pruningService.pruneWithPipeAZ(mplsLayerTopo, requestedVlanPipe, requestedSchedule, urnList, rsvBwList, rsvVlanList);
        Topology prunedMPLSTopoZA = pruningService.pruneWithPipeZA(mplsLayerTopo, requestedVlanPipe, requestedSchedule, urnList, rsvBwList, rsvVlanList);
        log.info("step 1 COMPLETE.");

        for(LogicalEdge oneLogicalLink : logicalLinks)
        {
            TopoVertex srcEthPort = oneLogicalLink.getA();      //Ethernet-source of logical link
            TopoVertex dstEthPort = oneLogicalLink.getZ();      //Ethernet-dest of logical link
            TopoVertex mplsSrc;                                 //MPLS port adjacent to srcEthPort
            TopoVertex mplsDst;                                 //MPLS port adjacent to dstEthPort

            TopoEdge physEdgeAtoMpls = null;
            TopoEdge physEdgeZtoMpls = null;
            TopoEdge physEdgeMplstoA = null;
            TopoEdge physEdgeMplstoZ = null;

            Set<TopoVertex> adaptationPorts = new HashSet<>();
            Set<TopoEdge> adaptationEdges = new HashSet<>();

            Topology adaptationTopo = new Topology();

            long weightMetricAZ = 0;
            long weightMetricZA = 0;

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

            // Step 2: Prune the adaptation (Ethernet-MPLS) edges and ports to ensure this logical link is worth building.
            log.info("step 2: pruning adaptation (Ethernet-MPLS) edges and ports to ensure logical link is worth building.");
            mplsSrc = physEdgeAtoMpls.getZ();
            mplsDst = physEdgeMplstoZ.getA();

            adaptationPorts.add(srcEthPort);
            adaptationPorts.add(dstEthPort);
            adaptationPorts.add(mplsSrc);
            adaptationPorts.add(mplsDst);

            adaptationEdges.add(physEdgeAtoMpls);
            adaptationEdges.add(physEdgeZtoMpls);
            adaptationEdges.add(physEdgeMplstoA);
            adaptationEdges.add(physEdgeMplstoZ);

            adaptationTopo.setVertices(adaptationPorts);
            adaptationTopo.setEdges(adaptationEdges);

            Topology prunedAdaptationTopo = pruningService.pruneWithPipe(adaptationTopo, requestedVlanPipe, requestedSchedule, rsvBwList, rsvVlanList);

            if(!prunedAdaptationTopo.equals(adaptationTopo))
            {
                log.info("cannot assign weight to logical edge: adaptation ports/links do not support demand.");
                log.info("step 2 FAILED.");
                log.info("removing logical link from Service-Layer topology.");

                logicalLinksToRemoveFromServiceLayer.add(oneLogicalLink);
                continue;
            }
            log.info("step 2 COMPLETE.");


            // Step 3: Perform routing on MPLS layer to construct physical routes corresponding to the logical link.
            log.info("step 3: performing MPLS-Layer routing.");

            List<TopoEdge> pathAZ = dijkstraPCE.computeShortestPathEdges(prunedMPLSTopoAZ, mplsSrc, mplsDst);
            List<TopoEdge> pathZA = dijkstraPCE.computeShortestPathEdges(prunedMPLSTopoZA, mplsSrc, mplsDst);

            if(pathAZ.isEmpty() && pathZA.isEmpty())
            {
                log.error("no path found for logical link.");
                log.info("step 3 FAILED.");
                log.info("removing logical link from Service-Layer topology.");
                logicalLinksToRemoveFromServiceLayer.add(oneLogicalLink);
                continue;
            }

            log.info("step 3 COMPLETE.");

            // Step 4: Calculate total cost-metric for logical link.
            log.info("step 4: compute logical link-weights.");
            for(TopoEdge pathEdge : pathAZ)
            {
                weightMetricAZ += pathEdge.getMetric();
            }

            for(TopoEdge pathEdge : pathZA)
            {
                weightMetricZA += pathEdge.getMetric();
            }

            // Add *uni-directional* cost of adaptation links to Logical Link weight since these are ETHERNET links, but will implicitly be used in BOTH directions across two logical links
            weightMetricAZ += physEdgeAtoMpls.getMetric();
            weightMetricAZ += physEdgeMplstoZ.getMetric();

            weightMetricZA += physEdgeAtoMpls.getMetric();
            weightMetricZA += physEdgeMplstoZ.getMetric();

            oneLogicalLink.setMetricAZ(weightMetricAZ);
            oneLogicalLink.setMetricZA(weightMetricZA);

            oneLogicalLink.setMetric(weightMetricAZ);   // The calling function expects metric to be set. Pathfinding is done in the forward direction, so we use that value here

            log.info("step 4 COMPLETE.");

            // Step 5: Store the physical route corresponding to this logical link
            pathAZ.add(0, physEdgeAtoMpls);
            pathAZ.add(physEdgeMplstoZ);

            pathZA.add(0, physEdgeAtoMpls);
            pathZA.add(physEdgeMplstoZ);

            oneLogicalLink.setCorrespondingAZTopoEdges(pathAZ);
            oneLogicalLink.setCorrespondingZATopoEdges(pathZA);

            oneLogicalLink.setCorrespondingTopoEdges(pathAZ);   // Palindromic calling functions expect correspondingTopoEdges to be set. Pathfinding is done in the forward direction, so we use that value here.
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
        log.info("determining if source is already represented on Service-Layer topology.");
        if(srcDevice.getVertexType().equals(VertexType.SWITCH))
        {
            log.info("it is.");
            return;
        }

        log.info("representing MPLS source on Service-Layer topology as VIRTUAL node.");

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
        log.info("determining if destination is already represented on Service-Layer topology.");
        if(dstDevice.getVertexType().equals(VertexType.SWITCH))
        {
            log.info("it is.");
            return;
        }

        log.info("representing MPLS destination on Service-Layer topology as VIRTUAL node.");

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
