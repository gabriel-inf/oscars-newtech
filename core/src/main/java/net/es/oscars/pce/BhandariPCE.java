package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class BhandariPCE {

    @Autowired
    private BellmanFordPCE bellmanFordPCE;

    public List<List<TopoEdge>> computePathPair(Topology topo, TopoVertex source, TopoVertex dest) {

        List<TopoVertex> sources = new ArrayList<>();
        List<TopoVertex> destinations = new ArrayList<>();

        for(Integer pIndex = 0; pIndex < 2; pIndex++){
            sources.add(source);
            destinations.add(dest);
        }

        return computePaths(topo, sources, destinations);
    }

    public List<List<TopoEdge>> computeKDisjointPaths(Topology topo, TopoVertex source, TopoVertex dest, Integer k){
        if(k == 0){
            return new ArrayList<>();
        }

        List<TopoVertex> sources = new ArrayList<>();
        List<TopoVertex> destinations = new ArrayList<>();

        for(Integer pIndex = 0; pIndex < k; pIndex++){
            sources.add(source);
            destinations.add(dest);
        }

        return computePaths(topo, sources, destinations);
    }

    public List<List<TopoEdge>> computeDisjointPathsBetweenPairs(Topology topo, List<List<TopoVertex>> pairs){
        List<TopoVertex> sources = new ArrayList<>();
        List<TopoVertex> destinations = new ArrayList<>();
        for(List<TopoVertex> pair : pairs){
            sources.add(pair.get(0));
            destinations.add(pair.get(1));
        }

        return computePaths(topo, sources, destinations);
    }

    public List<List<TopoEdge>> computeMultipleDisjointPathsBetweenPairs(Topology topo, Map<List<TopoVertex>, Integer> pairMap){
        List<TopoVertex> sources = new ArrayList<>();
        List<TopoVertex> destinations = new ArrayList<>();
        for(List<TopoVertex> pair : pairMap.keySet()){
            for(Integer pIndex = 0; pIndex < pairMap.get(pair); pIndex++){
                sources.add(pair.get(0));
                destinations.add(pair.get(1));
            }
        }

        return computePaths(topo, sources, destinations);
    }

    private List<List<TopoEdge>> computePaths(Topology topo, List<TopoVertex> sources, List<TopoVertex> destinations){

        // Find the first shortest path
        List<TopoEdge> shortestPath = bellmanFordPCE.shortestPath(topo, sources.get(0), destinations.get(0));
        if(shortestPath.isEmpty()){
            log.info("No shortest path from " + sources.get(0).getUrn() + " to " + destinations.get(0).getUrn() + " found");
            return new ArrayList<>();
        }

        List<List<TopoEdge>> paths = new ArrayList<>();
        paths.add(shortestPath);

        if(sources.size() == 1 && destinations.size() == 1){
            return paths;
        }

        List<List<TopoEdge>> tempPaths = new ArrayList<>(paths);
        Map<TopoEdge, TopoEdge> reversedToOriginalMap = new HashMap<>();

        // Modify the topology
        Topology modifiedTopo = new Topology();
        modifiedTopo.setLayer(topo.getLayer());
        modifiedTopo.setVertices(topo.getVertices());
        modifiedTopo.setEdges(new HashSet<>(topo.getEdges()));
        for(Integer pIndex = 1; pIndex < sources.size(); pIndex++){

            // Retrieve the source and destinaton for this path
            TopoVertex s = sources.get(pIndex);
            TopoVertex d = destinations.get(pIndex);

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
            List<TopoEdge> modShortestPath = bellmanFordPCE.shortestPath(modifiedTopo, s, d);
            tempPaths.add(modShortestPath);
        }
        return combine(shortestPath, tempPaths, reversedToOriginalMap, modifiedTopo, sources, destinations);

    }

    private List<List<TopoEdge>> combine(List<TopoEdge> shortestPath, List<List<TopoEdge>> tempPaths,
                                         Map<TopoEdge, TopoEdge> reversedToOriginalMap, Topology topo,
                                         List<TopoVertex> sources, List<TopoVertex> destinations) {

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


        for(Integer pIndex = 0; pIndex < sources.size(); pIndex++){
            TopoVertex source = sources.get(pIndex);
            TopoVertex dest = destinations.get(pIndex);
            List<TopoEdge> sp = bellmanFordPCE.shortestPath(topo, source, dest);
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

