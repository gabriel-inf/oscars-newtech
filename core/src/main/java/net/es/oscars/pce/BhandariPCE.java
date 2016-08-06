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
        log.info(modifiedTopo.getEdges().toString());
        List<TopoEdge> modShortestPath = bellmanFordService.shortestPath(modifiedTopo, source, dest);

        return makePair(shortestPath, modShortestPath, modifiedTopo, reversedToOriginalMap, source, dest);

    }


    private List<List<TopoEdge>> makePair(List<TopoEdge> shortestPath, List<TopoEdge> modShortestPath,
                                          Topology topo, Map<TopoEdge, TopoEdge> reversedToOriginalMap,
                                          TopoVertex source, TopoVertex dest){

        // Store the shortest path pair
        List<List<TopoEdge>> shortestPathPair = new ArrayList<>();

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
        topo.setEdges(combinedEdges);

        List<TopoEdge> firstSp = bellmanFordService.shortestPath(topo, source, dest);
        if(firstSp.isEmpty()){
            log.info("No first disjoint shortest path from " + source.getUrn() + " to " + dest.getUrn() + " found");
            // Return the original shortest path
            shortestPathPair.add(shortestPath);
            return shortestPathPair;
        }
        combinedEdges.removeAll(firstSp);
        topo.setEdges(combinedEdges);

        List<TopoEdge> secondSp = bellmanFordService.shortestPath(topo, source, dest);
        if(secondSp.isEmpty()){
            log.info("First disjoint shortest path found: " +
                    firstSp.stream().map(e -> "(" + e.getA().getUrn() + ", " + e.getZ().getUrn() + ")").collect(Collectors.toList()));
            log.info("but no other disjoint shortest path from " + source.getUrn() + " to " + dest.getUrn() + " found");
            // Return the original shortest path
            shortestPathPair.add(shortestPath);
            return shortestPathPair;
        }

        shortestPathPair.add(firstSp);
        shortestPathPair.add(secondSp);


        return shortestPathPair;
    }

    public List<List<TopoEdge>> computeKDisjointPaths(Topology topo, TopoVertex source, TopoVertex dest, Integer k){
        if(k == 0){
            return new ArrayList<>();
        }


        List<List<TopoEdge>> paths = new ArrayList<>();

        // Find the first shortest path
        List<TopoEdge> shortestPath = bellmanFordService.shortestPath(topo, source, dest);
        if(shortestPath.isEmpty()){
            log.info("No shortest path from " + source.getUrn() + " to " + dest.getUrn() + " found");
            return new ArrayList<>();
        }
        paths.add(shortestPath);
        if(k == 1){
            return paths;
        }

        List<List<TopoEdge>> tempPaths = new ArrayList<>(paths);
        Map<TopoEdge, TopoEdge> reversedToOriginalMap = new HashMap<>();

        // Modify the topology
        Topology modifiedTopo = new Topology();
        modifiedTopo.setLayer(topo.getLayer());
        modifiedTopo.setVertices(topo.getVertices());
        modifiedTopo.setEdges(new HashSet<>(topo.getEdges()));
        for(Integer pIndex = 1; pIndex < k; pIndex++){

            // Get the previous shortest path
            List<TopoEdge> prevPath = tempPaths.get(pIndex-1);

            // Reverse and give negative weight to edges in shortest path
            for(TopoEdge pathEdge : prevPath){
                Long reversedMetric = -1 * pathEdge.getMetric();
                TopoEdge reversedEdge = new TopoEdge(pathEdge.getZ(), pathEdge.getA(), reversedMetric, pathEdge.getLayer());
                reversedToOriginalMap.put(reversedEdge, pathEdge);
                modifiedTopo.getEdges().remove(pathEdge);
                modifiedTopo.getEdges().add(reversedEdge);
            }

           // Find the new shortest path
            List<TopoEdge> modShortestPath = bellmanFordService.shortestPath(modifiedTopo, source, dest);
            tempPaths.add(modShortestPath);
        }
        return combine(shortestPath, tempPaths, reversedToOriginalMap, modifiedTopo, source, dest, k);
    }

    private List<List<TopoEdge>> combine(List<TopoEdge> shortestPath, List<List<TopoEdge>> tempPaths,
                                         Map<TopoEdge, TopoEdge> reversedToOriginalMap, Topology topo,
                                         TopoVertex source, TopoVertex dest, Integer k) {

        List<List<TopoEdge>> paths = new ArrayList<>();

        // Remove all inverse edges taken in new shortest path (along with mapped edge in original shortest path)
        Set<TopoEdge> combinedEdges = new HashSet<>();
        for (List<TopoEdge> tempPath : tempPaths) {
            for (TopoEdge modSpEdge : tempPath) {
                if (reversedToOriginalMap.containsKey(modSpEdge)) {
                    TopoEdge origSpEdge = reversedToOriginalMap.get(modSpEdge);
                    shortestPath.remove(origSpEdge);
                } else {
                    combinedEdges.add(modSpEdge);
                }
            }
        }
        combinedEdges.addAll(shortestPath);

        // Find the shortest paths given these combined edges
        log.info(combinedEdges.toString());
        topo.setEdges(combinedEdges);

        for(Integer pIndex = 0; pIndex < k; pIndex++){
            List<TopoEdge> sp = bellmanFordService.shortestPath(topo, source, dest);
            if(sp.isEmpty()){
                Integer pathNum = pIndex+1;
                log.info("Couldn't find disjoint path " + pathNum + " from " + source.getUrn() + " to " + dest.getUrn());
                log.info("Returning all found paths");
                return paths;
            }
            paths.add(sp);
            combinedEdges.removeAll(sp);
            topo.setEdges(combinedEdges);
        }

        return paths;
    }

}

