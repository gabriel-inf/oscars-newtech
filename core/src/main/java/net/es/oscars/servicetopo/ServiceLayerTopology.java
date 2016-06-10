package net.es.oscars.servicetopo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.topo.*;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.DeviceType;
import net.es.oscars.topo.enums.IfceType;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceLayerTopology
{
    Set<TopoVertex> serviceLayerDevices;
    Set<TopoVertex> serviceLayerPorts;
    List<TopoEdge> serviceLayerLinks;

    Set<TopoVertex> mplsLayerDevices;
    Set<TopoVertex> mplsLayerPorts;
    List<TopoEdge> mplsLayerLinks;

    List<TopoVertex> logicalSrcDst;
    List<TopoVertex> logicalSrcdstPort;
    List<TopoEdge> logicalLinks;

    @Autowired
    TopoService topoService;

    public void createMultilayerTopology()
    {
        buildServiceLayerTopo();
        buildMplsLayerTopo();
        buildLogicalLayerTopo();
    }


    private void buildServiceLayerTopo()
    {
        Topology ethernetTopo = topoService.layer(Layer.ETHERNET);
        Topology internalTopo = topoService.layer(Layer.INTERNAL);

        Set<TopoVertex> ethernetVertices = ethernetTopo.getVertices();
        Set<TopoVertex> internalVertices = internalTopo.getVertices();


        Set<TopoEdge> allEthernetEdges = ethernetTopo.getEdges();
        Set<TopoEdge> allInternalEdges = internalTopo.getEdges();

        assert(internalVertices.isEmpty());     // Only edges should be INTERNAL

        // Parse the Devices
        Set<TopoVertex> allEthernetDevices = ethernetVertices.stream()
                .filter(d -> d.getVertexType().equals(VertexType.SWITCH))
                .collect(Collectors.toList());


        // Parse the Ports
        Set<TopoVertex> allEthernetPorts = ethernetVertices.stream()
                .filter(p -> p.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toList());


        // Parse the INTERNAL Edges
        Set<TopoEdge> allInternalEthernetEdges = allInternalEdges.stream()
                .filter(e -> !e.getA().getVertexType().equals(VertexType.ROUTER) && !e.getZ().getVertexType().equals(VertexType.ROUTER))
                .collect(Collectors.toList());


        // Compose Service-Layer
        serviceLayerDevices.addAll(allEthernetDevices);
        serviceLayerPorts.addAll(allEthernetPorts);
        serviceLayerLinks.addAll(allEthernetEdges);
        serviceLayerLinks.addAll(allInternalEthernetEdges);
    }


    private void buildMplsLayerTopo()
    {
        Topology mplsTopo = topoService.layer(Layer.MPLS);
        Topology internalTopo = topoService.layer(Layer.INTERNAL);

        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoVertex> internalVertices = internalTopo.getVertices();

        Set<TopoEdge> allMplsEdges = mplsTopo.getEdges();
        Set<TopoEdge> allInternalEdges = internalTopo.getEdges();

        assert(internalVertices.isEmpty());     // Only edges should be INTERNAL

        // Parse the Devices
        Set<TopoVertex> allMplsDevices = mplsVertices.stream()
                .filter(d -> d.getVertexType().equals(VertexType.ROUTER))
                .collect(Collectors.toList());


        // Parse the Ports
        Set<TopoVertex> allMplsPorts = mplsVertices.stream()
                .filter(p -> p.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toList());


        // Parse the INTERNAL Edges
        Set<TopoEdge> allInternalMPLSEdges = allInternalEdges.stream()
                .filter(e -> !e.getA().getVertexType().equals(VertexType.SWITCH) && !e.getZ().getVertexType().equals(VertexType.SWITCH))
                .collect(Collectors.toList());


        //Compose MPLS Mesh Layer
        mplsLayerDevices.addAll(allMplsDevices);
        mplsLayerPorts.addAll(allMplsPorts);
        mplsLayerLinks.addAll(allMplsEdges);
        mplsLayerLinks.addAll(allInternalMPLSEdges);
    }


    public void buildLogicalLayerTopo()
    {
        ;
    }
}
