package net.es.oscars.topo;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.RepoEntityBuilder;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.VertexType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class VlanTranslationTopologyBuilder {

    @Autowired
    private RepoEntityBuilder testBuilder;

    public void buildVlanTransTopo1()
    {
        log.info("Building Vlan Test Topology 1");

        Topology topo = buildThreeSwitchTopology();


        Map<String, TopoVertex> nameMap = buildNameVertexMap(topo.getVertices());

        Map<TopoVertex, TopoVertex> portDeviceMap = buildThreeSwitchPortDeviceMap(nameMap);

        Map<TopoVertex, List<Integer>> floorMap = new HashMap<>();
        floorMap.put(nameMap.get("A:0"), Collections.singletonList(1));
        floorMap.put(nameMap.get("A:1"), Collections.singletonList(1));
        floorMap.put(nameMap.get("A:2"), Collections.singletonList(1));
        floorMap.put(nameMap.get("B:0"), Collections.singletonList(1));
        floorMap.put(nameMap.get("B:1"), Collections.singletonList(1));
        floorMap.put(nameMap.get("B:2"), Collections.singletonList(1));
        floorMap.put(nameMap.get("C:0"), Collections.singletonList(1));
        floorMap.put(nameMap.get("C:1"), Collections.singletonList(1));
        floorMap.put(nameMap.get("C:2"), Collections.singletonList(1));

        Map<TopoVertex, List<Integer>> ceilingMap = new HashMap<>();
        ceilingMap.put(nameMap.get("A:0"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("A:1"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("A:2"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("B:0"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("B:1"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("B:2"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("C:0"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("C:1"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("C:2"), Collections.singletonList(5));

        testBuilder.populateRepos(topo.getVertices(), topo.getEdges(), portDeviceMap, floorMap, ceilingMap);
    }

    public void buildVlanTransTopo2()
    {
        log.info("Building Vlan Test Topology 1");

        Topology topo = buildThreeSwitchTopology();


        Map<String, TopoVertex> nameMap = buildNameVertexMap(topo.getVertices());

        Map<TopoVertex, TopoVertex> portDeviceMap = buildThreeSwitchPortDeviceMap(nameMap);

        Map<TopoVertex, List<Integer>> floorMap = new HashMap<>();
        floorMap.put(nameMap.get("A:0"), Collections.singletonList(4));
        floorMap.put(nameMap.get("A:1"), Collections.singletonList(4));
        floorMap.put(nameMap.get("A:2"), Collections.singletonList(1));
        floorMap.put(nameMap.get("B:0"), Collections.singletonList(4));
        floorMap.put(nameMap.get("B:1"), Collections.singletonList(4));
        floorMap.put(nameMap.get("B:2"), Collections.singletonList(4));
        floorMap.put(nameMap.get("C:0"), Collections.singletonList(4));
        floorMap.put(nameMap.get("C:1"), Collections.singletonList(4));
        floorMap.put(nameMap.get("C:2"), Collections.singletonList(1));

        Map<TopoVertex, List<Integer>> ceilingMap = new HashMap<>();
        ceilingMap.put(nameMap.get("A:0"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("A:1"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("A:2"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("B:0"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("B:1"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("B:2"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("C:0"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("C:1"), Collections.singletonList(5));
        ceilingMap.put(nameMap.get("C:2"), Collections.singletonList(5));

        testBuilder.populateRepos(topo.getVertices(), topo.getEdges(), portDeviceMap, floorMap, ceilingMap);
    }

    public void buildVlanTransTopo3()
    {
        log.info("Building Vlan Test Topology 1");

        Topology topo = buildThreeSwitchTopology();


        Map<String, TopoVertex> nameMap = buildNameVertexMap(topo.getVertices());

        Map<TopoVertex, TopoVertex> portDeviceMap = buildThreeSwitchPortDeviceMap(nameMap);

        Map<TopoVertex, List<Integer>> floorMap = new HashMap<>();
        floorMap.put(nameMap.get("A:0"), Collections.singletonList(1));
        floorMap.put(nameMap.get("A:1"), Collections.singletonList(1));
        floorMap.put(nameMap.get("A:2"), Collections.singletonList(1));
        floorMap.put(nameMap.get("B:0"), Collections.singletonList(1));
        floorMap.put(nameMap.get("B:1"), Collections.singletonList(1));
        floorMap.put(nameMap.get("B:2"), Collections.singletonList(1));
        floorMap.put(nameMap.get("C:0"), Collections.singletonList(1));
        floorMap.put(nameMap.get("C:1"), Collections.singletonList(1));
        floorMap.put(nameMap.get("C:2"), Collections.singletonList(1));

        Map<TopoVertex, List<Integer>> ceilingMap = new HashMap<>();
        ceilingMap.put(nameMap.get("A:0"), Collections.singletonList(2));
        ceilingMap.put(nameMap.get("A:1"), Collections.singletonList(2));
        ceilingMap.put(nameMap.get("A:2"), Collections.singletonList(2));
        ceilingMap.put(nameMap.get("B:0"), Collections.singletonList(2));
        ceilingMap.put(nameMap.get("B:1"), Collections.singletonList(2));
        ceilingMap.put(nameMap.get("B:2"), Collections.singletonList(2));
        ceilingMap.put(nameMap.get("C:0"), Collections.singletonList(2));
        ceilingMap.put(nameMap.get("C:1"), Collections.singletonList(2));
        ceilingMap.put(nameMap.get("C:2"), Collections.singletonList(2));

        testBuilder.populateRepos(topo.getVertices(), topo.getEdges(), portDeviceMap, floorMap, ceilingMap);
    }


    private Map<TopoVertex,TopoVertex> buildThreeSwitchPortDeviceMap(Map<String, TopoVertex> nameMap) {

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        portDeviceMap.put(nameMap.get("A:0"), nameMap.get("A"));
        portDeviceMap.put(nameMap.get("A:1"), nameMap.get("A"));
        portDeviceMap.put(nameMap.get("A:2"), nameMap.get("A"));

        portDeviceMap.put(nameMap.get("B:0"), nameMap.get("B"));
        portDeviceMap.put(nameMap.get("B:1"), nameMap.get("B"));
        portDeviceMap.put(nameMap.get("B:2"), nameMap.get("B"));

        portDeviceMap.put(nameMap.get("C:0"), nameMap.get("C"));
        portDeviceMap.put(nameMap.get("C:1"), nameMap.get("C"));
        portDeviceMap.put(nameMap.get("C:2"), nameMap.get("C"));

        return portDeviceMap;
    }

    private Map<String,TopoVertex> buildNameVertexMap(Set<TopoVertex> vertices) {
        return vertices.stream().collect(Collectors.toMap(TopoVertex::getUrn, v -> v));
    }

    private Topology buildThreeSwitchTopology(){
        TopoVertex nodeA = new TopoVertex("A", VertexType.SWITCH);
        TopoVertex portA0 = new TopoVertex("A:0", VertexType.PORT);
        TopoVertex portA1 = new TopoVertex("A:1", VertexType.PORT);
        TopoVertex portA2 = new TopoVertex("A:2", VertexType.PORT);

        TopoVertex nodeB = new TopoVertex("B", VertexType.SWITCH);
        TopoVertex portB0 = new TopoVertex("B:0", VertexType.PORT);
        TopoVertex portB1 = new TopoVertex("B:1", VertexType.PORT);
        TopoVertex portB2 = new TopoVertex("B:2", VertexType.PORT);

        TopoVertex nodeC = new TopoVertex("C", VertexType.SWITCH);
        TopoVertex portC0 = new TopoVertex("C:0", VertexType.PORT);
        TopoVertex portC1 = new TopoVertex("C:1", VertexType.PORT);
        TopoVertex portC2 = new TopoVertex("C:2", VertexType.PORT);

        //Internal Links
        TopoEdge edgeInt_0_A = new TopoEdge(portA0, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_1_A = new TopoEdge(portA1, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_2_A = new TopoEdge(portA2, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_0 = new TopoEdge(nodeA, portA0, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_1 = new TopoEdge(nodeA, portA1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_2 = new TopoEdge(nodeA, portA2, 0L, Layer.INTERNAL);

        TopoEdge edgeInt_0_B = new TopoEdge(portB0, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_1_B = new TopoEdge(portB1, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_2_B = new TopoEdge(portB2, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_0 = new TopoEdge(nodeB, portB0, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_1 = new TopoEdge(nodeB, portB1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_2 = new TopoEdge(nodeB, portB2, 0L, Layer.INTERNAL);

        TopoEdge edgeInt_0_C = new TopoEdge(portC0, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_1_C = new TopoEdge(portC1, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_2_C = new TopoEdge(portC2, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_0 = new TopoEdge(nodeC, portC0, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_1 = new TopoEdge(nodeC, portC1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_2 = new TopoEdge(nodeC, portC2, 0L, Layer.INTERNAL);

        // External Links
        TopoEdge edgeEx_A1_B0 = new TopoEdge(portA1, portB0, 1L, Layer.ETHERNET);
        TopoEdge edgeEx_B0_A1 = new TopoEdge(portB0, portA1, 1L, Layer.ETHERNET);
        TopoEdge edgeEx_C0_B1 = new TopoEdge(portC0, portB1, 1L, Layer.ETHERNET);
        TopoEdge edgeEx_B1_C0 = new TopoEdge(portB1, portC0, 1L, Layer.ETHERNET);

        Set<TopoVertex> topoNodes = new HashSet<>();
        topoNodes.add(nodeA);
        topoNodes.add(portA0);
        topoNodes.add(portA1);
        topoNodes.add(portA2);
        topoNodes.add(nodeB);
        topoNodes.add(portB0);
        topoNodes.add(portB1);
        topoNodes.add(portB2);
        topoNodes.add(nodeC);
        topoNodes.add(portC0);
        topoNodes.add(portC1);
        topoNodes.add(portC2);

        Set<TopoEdge> topoLinks = new HashSet<>();
        topoLinks.add(edgeInt_0_A);
        topoLinks.add(edgeInt_1_A);
        topoLinks.add(edgeInt_2_A);
        topoLinks.add(edgeInt_A_0);
        topoLinks.add(edgeInt_A_1);
        topoLinks.add(edgeInt_A_2);

        topoLinks.add(edgeInt_0_B);
        topoLinks.add(edgeInt_1_B);
        topoLinks.add(edgeInt_2_B);
        topoLinks.add(edgeInt_B_0);
        topoLinks.add(edgeInt_B_1);
        topoLinks.add(edgeInt_B_2);

        topoLinks.add(edgeInt_0_C);
        topoLinks.add(edgeInt_1_C);
        topoLinks.add(edgeInt_2_C);
        topoLinks.add(edgeInt_C_0);
        topoLinks.add(edgeInt_C_1);
        topoLinks.add(edgeInt_C_2);

        topoLinks.add(edgeEx_A1_B0);
        topoLinks.add(edgeEx_B0_A1);
        topoLinks.add(edgeEx_C0_B1);
        topoLinks.add(edgeEx_B1_C0);

        Topology topo = new Topology();
        topo.setEdges(topoLinks);
        topo.setVertices(topoNodes);
        return topo;
    }
}
