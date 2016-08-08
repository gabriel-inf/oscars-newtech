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
    private BellmanFordPCE bellmanFordPCE;


    public List<List<TopoEdge>> computeDisjointPaths(Topology topo, TopoVertex source, TopoVertex dest, Integer k){
        if(k == 0){
            return new ArrayList<>();
        }

        return computePaths(topo, source, dest, k);
    }


    private List<List<TopoEdge>> computePaths(Topology topo, TopoVertex source, TopoVertex dest, Integer k){

        // Find the first shortest path
        List<TopoEdge> shortestPath = bellmanFordPCE.shortestPath(topo, source, dest);
        if(shortestPath.isEmpty()){
            log.info("No shortest path from " + source.getUrn() + " to " + dest.getUrn() + " found");
            return new ArrayList<>();
        }
        logPath(shortestPath, "First Shortest Path");

        List<List<TopoEdge>> paths = new ArrayList<>();
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
            List<TopoEdge> modShortestPath = bellmanFordPCE.shortestPath(modifiedTopo, source, dest);
            logPath(modShortestPath, "SP for (" + source.getUrn() + "," + dest.getUrn() + ")");
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
        logPath(new ArrayList<>(combinedEdges), "Combined Edges");
        topo.setEdges(combinedEdges);

        for(Integer pIndex = 0; pIndex < k; pIndex++){
            List<TopoEdge> sp = bellmanFordPCE.shortestPath(topo, source, dest);
            if(sp.isEmpty()){
                log.info("Couldn't find disjoint path from " + source.getUrn() + " to " + dest.getUrn());
                log.info("Returning all found paths");
                return paths;
            }
            paths.add(sp);
            combinedEdges.removeAll(sp);
            topo.setEdges(combinedEdges);
        }

        return paths;
    }

    private void logPath(List<TopoEdge> path, String title){
        log.info(title + ": " + path.stream().map(e -> "(" + e.getA().getUrn() + ", " + e.getZ().getUrn() + ")").collect(Collectors.toList()).toString());
    }

    /* Not currently functional

    public List<List<TopoEdge>> computeDisjointPaths(Topology topo, Set<TopoVertex> sources, Set<TopoVertex> dests){
        return computePaths(topo, new ArrayList<>(sources), new ArrayList<>(dests));
    }
    public List<List<TopoEdge>> computeDisjointPaths(Topology topo, List<List<TopoVertex>> pairs){
        List<TopoVertex> sources = new ArrayList<>();
        List<TopoVertex> destinations = new ArrayList<>();
        for(List<TopoVertex> pair : pairs){
            sources.add(pair.get(0));
            destinations.add(pair.get(1));
        }

        return computePaths(topo, sources, destinations);
    }

    public List<List<TopoEdge>> computeDisjointPaths(Topology topo, Map<List<TopoVertex>, Integer> sourceDestPairMap){
        List<TopoVertex> sources = new ArrayList<>();
        List<TopoVertex> destinations = new ArrayList<>();
        for(List<TopoVertex> pair : sourceDestPairMap.keySet()){
            for(Integer pIndex = 0; pIndex < sourceDestPairMap.get(pair); pIndex++){
                sources.add(pair.get(0));
                destinations.add(pair.get(1));
            }
        }

        return computePaths(topo, sources, destinations);
    }
    */
}

