package net.es.oscars.servicetopo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.enums.*;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceLayerTopology
{
    Set<TopoVertex> serviceLayerDevices = new HashSet<>();
    Set<TopoVertex> serviceLayerPorts = new HashSet<>();
    Set<TopoEdge> serviceLayerLinks = new HashSet<>();

    Set<TopoVertex> nonAdjacentPorts;

    Set<TopoVertex> mplsLayerDevices = new HashSet<>();
    Set<TopoVertex> mplsLayerPorts = new HashSet<>();
    Set<TopoEdge> mplsLayerLinks = new HashSet<>();

    Set<TopoVertex> logicalSrcNodes = null;
    Set<TopoVertex> logicalDstNodes = null;

    Set<TopoEdge> logicalLinks;

    @Autowired
    TopoService topoService;


    // these objects are for easily getting/setting topologies for testing only!
    Topology ethernetTopology;
    Topology mplsTopology;
    Topology internalTopology;

    public void createMultilayerTopology()
    {
        buildServiceLayerTopo();
        buildMplsLayerTopo();
        buildLogicalLayerTopo();
    }


    private void buildServiceLayerTopo()
    {
        /* UNCOMMENT THESE LINES AFTER TESTING */
        //Topology ethernetTopo = topoService.layer(Layer.ETHERNET);
        //Topology internalTopo = topoService.layer(Layer.INTERNAL);

        /* DELETE THESE LINES AFTER TESTING */
        Topology ethernetTopo = ethernetTopology;
        Topology internalTopo = internalTopology;

        Set<TopoVertex> ethernetVertices = ethernetTopo.getVertices();
        Set<TopoVertex> internalVertices = internalTopo.getVertices();


        Set<TopoEdge> allEthernetEdges = ethernetTopo.getEdges();
        Set<TopoEdge> allInternalEdges = internalTopo.getEdges();

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
        /* UNCOMMENT THESE LINES AFTER TESTING */
        //Topology mplsTopo = topoService.layer(Layer.MPLS);
        //Topology internalTopo = topoService.layer(Layer.INTERNAL);

        /* DELETE THESE LINES AFTER TESTING */
        Topology mplsTopo = mplsTopology;
        Topology internalTopo = internalTopology;


        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoVertex> internalVertices = internalTopo.getVertices();

        Set<TopoEdge> allMplsEdges = mplsTopo.getEdges();
        Set<TopoEdge> allInternalEdges = internalTopo.getEdges();

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

                TopoEdge azLogicalEdge = new TopoEdge(nonAdjacentA,nonAdjacentZ, 0L, Layer.LOGICAL);
                TopoEdge zaLogicalEdge = new TopoEdge(nonAdjacentZ,nonAdjacentA, 0L, Layer.LOGICAL);

                logicalLinks.add(azLogicalEdge);
                logicalLinks.add(zaLogicalEdge);
            }
        }
    }


    // Should only be called if Source Device is MPLS
    public void buildLogicalLayerSrcNodes(TopoVertex srcDevice, TopoVertex srcInPort)
    {
        TopoVertex virtualSrcDevice = srcDevice;
        TopoVertex virtualSrcPort = new TopoVertex(srcInPort.getUrn() + "dummy", VertexType.VIRTUAL);

        virtualSrcDevice.setVertexType(VertexType.VIRTUAL);

        TopoEdge portToDevice = new TopoEdge(virtualSrcPort, virtualSrcDevice, 0L, Layer.ETHERNET);
        TopoEdge devicetoPort = new TopoEdge(virtualSrcDevice, virtualSrcPort, 0L, Layer.ETHERNET);

        TopoEdge vPortToPort = new TopoEdge(virtualSrcPort, srcInPort, 0L, Layer.ETHERNET);
        TopoEdge portToVPort = new TopoEdge(srcInPort, virtualSrcPort, 0L, Layer.ETHERNET);

        serviceLayerLinks.add(portToDevice);
        serviceLayerLinks.add(devicetoPort);
        serviceLayerLinks.add(vPortToPort);
        serviceLayerLinks.add(portToVPort);

        nonAdjacentPorts.add(virtualSrcPort);

        buildLogicalLayerLinks();               // Should filter out duplicates -- TEST THAT!
    }

    // Should only be called if Source Device is MPLS
    public void buildLogicalLayerDstNodes(TopoVertex dstDevice, TopoVertex dstOutPort)
    {
        TopoVertex virtualDstDevice = dstDevice;
        TopoVertex virtualDstPort = new TopoVertex(dstOutPort.getUrn() + "dummy", VertexType.VIRTUAL);

        virtualDstDevice.setVertexType(VertexType.VIRTUAL);

        TopoEdge portToDevice = new TopoEdge(virtualDstPort, virtualDstDevice, 0L, Layer.ETHERNET);
        TopoEdge devicetoPort = new TopoEdge(virtualDstDevice, virtualDstPort, 0L, Layer.ETHERNET);

        TopoEdge vPortToPort = new TopoEdge(virtualDstPort, dstOutPort, 0L, Layer.ETHERNET);
        TopoEdge portToVPort = new TopoEdge(dstOutPort, virtualDstPort, 0L, Layer.ETHERNET);

        serviceLayerLinks.add(portToDevice);
        serviceLayerLinks.add(devicetoPort);
        serviceLayerLinks.add(vPortToPort);
        serviceLayerLinks.add(portToVPort);

        nonAdjacentPorts.add(virtualDstPort);

        buildLogicalLayerLinks();               // Should filter out duplicates -- TEST THAT!
    }


    // Here for testing only!
    public Topology getTopology(Layer layer)
    {
        if(layer.equals(Layer.ETHERNET))
            return ethernetTopology;
        else if(layer.equals(Layer.MPLS))
            return mplsTopology;
        else
            return internalTopology;
    }

    // Here for testing only!
    public void setTopology(Topology topology)
    {
        Layer layer = topology.getLayer();

        if(layer.equals(Layer.ETHERNET))
            ethernetTopology = topology;
        else if(layer.equals(Layer.MPLS))
            mplsTopology = topology;
        else
            internalTopology = topology;
    }
}
