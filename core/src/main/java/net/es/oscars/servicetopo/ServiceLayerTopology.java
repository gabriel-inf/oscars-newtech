package net.es.oscars.servicetopo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.DijkstraPCE;
import net.es.oscars.pce.PruningService;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.ent.ScheduleSpecificationE;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.*;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
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
    TopoService topoService;

    @Autowired
    PruningService pruningService;

    @Autowired
    DijkstraPCE dijkstraPCE;

    Set<TopoVertex> serviceLayerDevices;
    Set<TopoVertex> serviceLayerPorts;
    Set<TopoEdge> serviceLayerLinks;

    Set<TopoVertex> mplsLayerDevices;
    Set<TopoVertex> mplsLayerPorts;
    Set<TopoEdge> mplsLayerLinks;

    Set<TopoVertex> nonAdjacentPorts;
    Set<LogicalEdge> logicalLinks;
    Set<LogicalEdge> llBackup;

    Set<TopoVertex> logicalSrcNodes = null;
    Set<TopoVertex> logicalDstNodes = null;

    Topology ethernetTopology;
    Topology mplsTopology;
    Topology internalTopology;

    // This method should be called whenever the physical topology is updated
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


        //Compose MPLS Mesh Layer
        mplsLayerDevices.addAll(allMplsDevices);
        mplsLayerPorts.addAll(allMplsPorts);
        mplsLayerLinks.addAll(allMplsEdges);
        mplsLayerLinks.addAll(allInternalMPLSEdges);
    }


    private void buildLogicalLayerTopo()
    {
        identifyNonAdjacentSLPorts();
        buildLogicalLayerLinks();
    }


    //Identify switch ports which have links connected to router ports. All of these srvice-layer ports will have logical edges to each other.
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


    //Establish a logical edge between non-adjacent service-layer ports. Weights will be assigned later.
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

                LogicalEdge azLogicalEdge = new LogicalEdge(nonAdjacentA,nonAdjacentZ, 0L, Layer.LOGICAL, new ArrayList<>());
                LogicalEdge zaLogicalEdge = new LogicalEdge(nonAdjacentZ,nonAdjacentA, 0L, Layer.LOGICAL, new ArrayList<>());

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

    //Calls Pruner and Dijkstra to compute shortest MPLS-layer paths, calculates weights of those paths, and maps them to the appropriate logical links
    public void calculateLogicalLinkWeights(RequestedVlanPipeE requestedVlanPipe, ScheduleSpecificationE requestedSchedule, List<UrnE> urnList)
    {
        log.info("Performing routing on MPLS-Layer topology to assign weights to Service-Layer logical links.");
        Set<LogicalEdge> logicalLinksToRemoveFromServiceLayer = new HashSet<>();

        Topology mplsLayerTopo = new Topology();
        mplsLayerTopo.getVertices().addAll(mplsLayerDevices);
        mplsLayerTopo.getVertices().addAll(mplsLayerPorts);
        mplsLayerTopo.getEdges().addAll(mplsLayerLinks);

        // Step 1: Prune MPLS-Layer topology once before considering any logical links.
        log.info("step 1: pruning MPLS-layer by bandwidth and vlan availability.");
        Topology prunedMPLSTopo = pruningService.pruneWithPipe(mplsLayerTopo, requestedVlanPipe, urnList, requestedSchedule);

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
                assert(false);
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

            Topology prunedAdaptationTopo = pruningService.pruneWithPipe(adaptationTopo, requestedVlanPipe, requestedSchedule);

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

    // Doesn't destroy logical links, but resets cost metrics to 0, and clears the correspondingTopoEdges lists. This needs to be done, for example, prior to every call to calculateLogicalLinkWeights().
    public void resetLogicalLinks()
    {
        llBackup.stream()
                .forEach(ll -> {
                    ll.getCorrespondingTopoEdges().clear();
                    ll.setMetric(0L);
                });

        logicalLinks.clear();
        logicalLinks.addAll(llBackup);
    }


    // Should only be called if Source Device is MPLS
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

    // Should only be called if Source Device is MPLS
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
            assert(false);
        }
    }


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
}
