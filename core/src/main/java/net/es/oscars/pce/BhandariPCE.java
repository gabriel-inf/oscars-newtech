package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BhandariPCE {

    @Autowired
    private BellmanFordService bellmanFordService;

    public List<List<TopoEdge>> computePathPair(Topology topo, TopoVertex source, TopoVertex dest){

        // Store the shortest path pair
        List<List<TopoEdge>> shortestPathPair = new ArrayList<>();

        // Find the first shortest path
        List<TopoEdge> shortestPath = bellmanFordService.shortestPath(topo, source, dest);
        if(shortestPath.isEmpty()){
            log.info("No shortest path from " + source.getUrn() + " to " + dest.getUrn() + " found");
            return new ArrayList<>();
        }

        // Modify the topology
        Topology modifiedTopo = new Topology();
        modifiedTopo.setLayer(topo.getLayer());
        modifiedTopo.setVertices(topo.getVertices());

        // Get the non-shortest path edges
        Set<TopoEdge> modifiedTopoEdges = topo.getEdges().stream().filter(e -> !shortestPath.contains(e)).collect(Collectors.toSet());

        // Reverse and give negative weight to edges in shortest path
        Map<TopoEdge, TopoEdge> reversedToOriginalMap = new HashMap<>();
        for(TopoEdge pathEdge : shortestPath){
            Long reversedMetric = -1 * pathEdge.getMetric();
            TopoEdge reversedEdge = new TopoEdge(pathEdge.getZ(), pathEdge.getA(), reversedMetric, pathEdge.getLayer());
            reversedToOriginalMap.put(reversedEdge, pathEdge);
        }

        // Add the non-shortest path edges, and the reversed edges, to the modified topology
        modifiedTopoEdges.addAll(reversedToOriginalMap.keySet());
        modifiedTopo.setEdges(modifiedTopoEdges);

        // Find the new shortest path
        List<TopoEdge> modShortestPath = bellmanFordService.shortestPath(modifiedTopo, source, dest);

        // Remove all inverse edges taken in new shortest path (along with mapped edge in original shortest path)
        Set<TopoEdge> combinedEdges = new HashSet<>();
        for(TopoEdge modSpEdge : modShortestPath){
            if(reversedToOriginalMap.containsKey(modSpEdge)){
                TopoEdge origSpEdge = reversedToOriginalMap.get(modSpEdge);
                shortestPath.remove(origSpEdge);
            }
            else{
                combinedEdges.add(modSpEdge);
            }
        }
        combinedEdges.addAll(shortestPath);

        // Find the two shortest paths given these combined edges
        modifiedTopo.setEdges(combinedEdges);

        List<TopoEdge> firstShortestPath = bellmanFordService.shortestPath(modifiedTopo, source, dest);
        if(firstShortestPath.isEmpty()){
            log.info("No first disjoint shortest path from " + source.getUrn() + " to " + dest.getUrn() + " found");
            return new ArrayList<>();
        }
        combinedEdges.removeAll(firstShortestPath);
        modifiedTopo.setEdges(combinedEdges);

        List<TopoEdge> secondShortestPath = bellmanFordService.shortestPath(modifiedTopo, source, dest);
        if(secondShortestPath.isEmpty()){
            log.info("First disjoint shortest path found: " +
                    firstShortestPath.stream().map(e -> "(" + e.getA().getUrn() + ", " + e.getZ().getUrn() + ")").collect(Collectors.toList()));
            log.info("but no other disjoint shortest path from " + source.getUrn() + " to " + dest.getUrn() + " found");
            return new ArrayList<>();
        }

        shortestPathPair.add(firstShortestPath);
        shortestPathPair.add(secondShortestPath);


        return shortestPathPair;
    }

}

