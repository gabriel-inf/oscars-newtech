package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.PortLayer;
import net.es.oscars.dto.topo.enums.VertexType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BellmanFordPCE {

    public List<TopoEdge> shortestPath(Topology topo, TopoVertex source, TopoVertex dest){

        Map<TopoVertex, TopoEdge> edgeMap = bellmanFord(topo, source);
        if(edgeMap.isEmpty()){
            return new ArrayList<>();
        }

        List<TopoEdge> path = new ArrayList<>();
        TopoVertex currentNode = dest;
        while(!currentNode.equals(source)){
            if(!edgeMap.containsKey(currentNode)){
                path = new ArrayList<>();
                break;
            }
            TopoEdge edge = edgeMap.get(currentNode);
            path.add(edge);
            currentNode = edge.getA();
        }
        Collections.reverse(path);
        return path;
    }

    public Map<TopoVertex, List<TopoEdge>> allShortestPaths(Topology topo, TopoVertex source){

        Map<TopoVertex, TopoEdge> edgeMap = bellmanFord(topo, source);
        if(edgeMap.isEmpty()){
            return new HashMap<>();
        }

        Map<TopoVertex, List<TopoEdge>> allPaths = new HashMap<>();

        Set<TopoVertex> vertices = topo.getVertices();

        for(TopoVertex vertex : vertices){
            List<TopoEdge> path = new ArrayList<>();
            TopoVertex currentNode = vertex;
            while(!currentNode.equals(source)){
                if(!edgeMap.containsKey(currentNode)){
                    path = new ArrayList<>();
                    break;
                }
                TopoEdge edge = edgeMap.get(currentNode);
                path.add(edge);
                currentNode = edge.getA();
            }
            Collections.reverse(path);
            allPaths.put(vertex, path);
        }

        return allPaths;
    }

    private Map<TopoVertex, TopoEdge> bellmanFord(Topology topo, TopoVertex source){

        Map<TopoVertex, Long> distanceMap = new HashMap<>();
        Map<TopoVertex, TopoEdge> edgeMap = new HashMap<>();

        Set<TopoVertex> vertices = topo.getVertices();
        List<TopoEdge> edges = topo
                .getEdges()
                .stream()
                .sorted((a,z) -> a.getA().getUrn().compareToIgnoreCase(z.getA().getUrn()))
                .collect(Collectors.toList());

        for(TopoVertex vertex : vertices){
            distanceMap.put(vertex, 999999999L);
        }

        distanceMap.put(source, 0L);

        for(Integer i = 0; i < vertices.size()-1; i++){
            boolean noChanges = true;
            for(TopoEdge edge : edges){
                TopoVertex a = edge.getA();
                TopoVertex z = edge.getZ();

                if(!distanceMap.containsKey(a) || !distanceMap.containsKey(z))
                {
                    log.info("At least one vertex on an edge does not exist in topology");
                    log.info("Edge: " + edge);
                    return new HashMap<>();
                }

                Long weight = distanceMap.get(a) + edge.getMetric();
                if(weight < distanceMap.get(z)){
                    distanceMap.put(z, weight);
                    edgeMap.put(z, edge);
                    noChanges = false;
                }
            }
            if(noChanges){
                break;
            }
        }

        /*
        for(TopoEdge edge : edges){
            TopoVertex a = edge.getA();
            TopoVertex z = edge.getZ();
            if(distanceMap.get(a) + edge.getMetric() < distanceMap.get(z)){
                log.info("Graph has a negative cycle, impossible to find shortest paths");
                return new HashMap<>();
            }
        }*/

        return edgeMap;
    }

    private void populatePortLayers(Collection<TopoVertex> vertices, Map<TopoVertex,TopoVertex> portToDeviceMap, Map<TopoVertex, List<Integer>> bandwidthMap, Map<TopoVertex, List<Integer>> floorMap, Map<TopoVertex, List<Integer>> ceilingMap)
    {
        for(TopoVertex onePort : vertices)
        {
            if(!onePort.getVertexType().equals(VertexType.PORT))
                continue;

            if(!onePort.getPortLayer().equals(PortLayer.NONE))
                continue;

            TopoVertex correspondingDevice = portToDeviceMap.remove(onePort);
            List<Integer> floors = null;
            List<Integer> ceilings = null;
            List<Integer> portBWs = null;

            VertexType deviceType = correspondingDevice.getVertexType();

            if(bandwidthMap != null)
                portBWs = bandwidthMap.remove(onePort);
            if(floorMap != null)
                floors = floorMap.remove(onePort);
            if(ceilingMap != null)
                ceilings = ceilingMap.remove(onePort);

            // Set Port Layer //
            if(deviceType.equals(VertexType.SWITCH))
                onePort.setPortLayer(PortLayer.ETHERNET);
            else if(deviceType.equals(VertexType.ROUTER))
                onePort.setPortLayer(PortLayer.MPLS);

            portToDeviceMap.put(onePort, correspondingDevice);

            if(bandwidthMap != null)
                bandwidthMap.put(onePort, portBWs);
            if(floorMap != null)
                floorMap.put(onePort, floors);
            if(ceilingMap != null)
                ceilingMap.put(onePort, ceilings);
        }
    }
}
