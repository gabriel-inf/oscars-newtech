package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
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
public class BhandariPCE {

    @Autowired
    private BellmanFordPCE bellmanFordPCE;


    public List<List<TopoEdge>> computeDisjointPaths(Topology topo, TopoVertex source, TopoVertex dest, Integer k){
        if(k == 0){
            return new ArrayList<>();
        }

        return computePaths(topo, source, dest, k);
    }

    /** A specialized version of the BhandariPCE controller for supporting solutions requested by the SurvivableServiceLayerTopology.
     * The source and destination are ETHERNET-capable ports adjacent either to MPLS-capable ports OR adjacent to MPLS-capable devices.
     * Bhandari's algorithm would fail if the source and destination were to be port nodes because each can only be connected to one network link.
     * Therefore, this method identifies the nearest MPLS-capable devices to the incoming ETHERNET-capable ports, and passes them to the Bhandari algorithm code.
     * @param topo Topology consisting ONLY of MPLS-layer ports/devices, and some adjacencies to the ETHERNET=layer source/dest ports
     * @param source Ethernet-capable source port
     * @param dest Ethernet-capable destination port
     * @param k Number of disjoint paths requested between source and destination
     * @param edgesToIgnore A set of adjacencies connecting the MPLS-layer topology to the Ethernet-capable ports
     * @return
     */
    public List<List<TopoEdge>> computeDisjointPaths(Topology topo, TopoVertex source, TopoVertex dest, Integer k, Set<TopoEdge> edgesToIgnore)
    {
        if(k == 0)
            return new ArrayList<>();

        Set<TopoVertex> allTopoVertices = topo.getVertices();
        Set<TopoEdge> allTopoEdges = topo.getEdges();
        Set<TopoEdge> edgesRemoved = new HashSet<>();
        List<TopoEdge> initialEdges = new ArrayList<>();
        List<TopoEdge> terminalEdges = new ArrayList<>();
        TopoVertex newSrc = source;
        TopoVertex newDst = dest;

        // Determine which edges connect source to topology and mark them for removal. Set newSrc equal to node adjacent to source.
        for(TopoEdge ignoredEdge : edgesToIgnore)
        {
            if(ignoredEdge == null)
                continue;

            if(ignoredEdge.getA().equals(source) || ignoredEdge.getZ().equals(source))
            {
                edgesRemoved.add(ignoredEdge);
                if(ignoredEdge.getA().equals(source))
                {
                    newSrc = ignoredEdge.getZ();
                    initialEdges.add(ignoredEdge);
                }
            }
        }

        // Determine which edges connect dest to topology and mark them for removal. Set newDst equal to node adjacent to dest.
        for(TopoEdge ignoredEdge : edgesToIgnore)
        {
            if(ignoredEdge == null)
                continue;

            if(ignoredEdge.getA().equals(dest) || ignoredEdge.getZ().equals(dest))
            {
                edgesRemoved.add(ignoredEdge);
                if(ignoredEdge.getZ().equals(dest))
                {
                    newDst = ignoredEdge.getA();
                    terminalEdges.add(ignoredEdge);
                }
            }
        }

        assert(allTopoVertices.contains(newSrc));
        assert(allTopoVertices.contains(newDst));


        // If the newSrc is a port, find adjacent device and mark relevant adjacencies for removal.
        if(newSrc.getVertexType().equals(VertexType.PORT))
        {
            Set<TopoEdge> portToDeviceEdges = new HashSet<>();

            for(TopoEdge oneEdge : allTopoEdges)
            {
                if(oneEdge.getLayer().equals(Layer.INTERNAL) && (oneEdge.getA().equals(newSrc) || oneEdge.getZ().equals(newSrc)))
                    portToDeviceEdges.add(oneEdge);
            }

            for(TopoEdge oneRelevantEdge : portToDeviceEdges)
            {
                if(oneRelevantEdge.getA().equals(newSrc))
                {
                    newSrc = oneRelevantEdge.getZ();
                    initialEdges.add(oneRelevantEdge);
                    break;

                }
            }

            edgesRemoved.addAll(portToDeviceEdges);
        }

        // If the newDst is a port, find adjacent device and mark relevant adjacencies for removal.
        if(newDst.getVertexType().equals(VertexType.PORT))
        {
            Set<TopoEdge> portToDeviceEdges = new HashSet<>();

            for(TopoEdge oneEdge : allTopoEdges)
            {
                if(oneEdge.getLayer().equals(Layer.INTERNAL) && (oneEdge.getA().equals(newDst) || oneEdge.getZ().equals(newDst)))
                    portToDeviceEdges.add(oneEdge);
            }

            for(TopoEdge oneRelevantEdge : portToDeviceEdges)
            {
                if(oneRelevantEdge.getZ().equals(newDst))
                {
                    newDst = oneRelevantEdge.getA();
                    terminalEdges.add(oneRelevantEdge);
                    break;
                }
            }

            edgesRemoved.addAll(portToDeviceEdges);
        }

        assert(allTopoVertices.contains(newSrc));
        assert(allTopoVertices.contains(newDst));

        // Remove affected edges from topology so they aren't used in solution
        allTopoEdges.removeAll(edgesRemoved);

        // Bhandari's algorithm
        List<List<TopoEdge>> pathSet = computePaths(topo, newSrc, newDst, k);

        // Put the removed edges (which are not survivable) back into the solution path set
        for(List<TopoEdge> onePath : pathSet)
        {
            for(int edge = initialEdges.size()-1; edge >= 0; edge--)
                onePath.add(0, initialEdges.get(edge));

            for(int edge = terminalEdges.size()-1; edge >= 0; edge--)
                onePath.add(terminalEdges.get(edge));

        }

        // Reset topology to original format
        allTopoEdges.addAll(edgesRemoved);

        return pathSet;
    }

    private List<List<TopoEdge>> computePaths(Topology topo, TopoVertex source, TopoVertex dest, Integer k){

        // Find the first shortest path
        List<TopoEdge> shortestPath = bellmanFordPCE.shortestPath(topo, source, dest);
        if(shortestPath.isEmpty()){
            log.info("No shortest path from " + source.getUrn() + " to " + dest.getUrn() + " found");
            return new ArrayList<>();
        }
        //logPath(shortestPath, "First Shortest Path");

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
                Set<TopoEdge> allBetweenPair = findAllBetweenPair(pathEdge.getA(), pathEdge.getZ(), modifiedTopo.getEdges());
                modifiedTopo.getEdges().removeAll(allBetweenPair);
                modifiedTopo.getEdges().add(reversedEdge);
            }

            // Find the new shortest path
            List<TopoEdge> modShortestPath = bellmanFordPCE.shortestPath(modifiedTopo, source, dest);
            //(modShortestPath, "SP on modified topology for (" + source.getUrn() + "," + dest.getUrn() + ")");
            tempPaths.add(modShortestPath);
        }
        return combine(shortestPath, tempPaths, reversedToOriginalMap, modifiedTopo, source, dest, k);

    }

    private Set<TopoEdge> findAllBetweenPair(TopoVertex src, TopoVertex dst, Set<TopoEdge> edges){
        return edges.stream()
                .filter(e -> e.getA().equals(src) && e.getZ().equals(dst) || e.getA().equals(dst) && e.getZ().equals(src))
                .collect(Collectors.toSet());
    }

    private List<List<TopoEdge>> combine(List<TopoEdge> shortestPath, List<List<TopoEdge>> tempPaths,
                                         Map<TopoEdge, TopoEdge> reversedToOriginalMap, Topology topo,
                                         TopoVertex source, TopoVertex dest, Integer k) {

        List<List<TopoEdge>> paths = new ArrayList<>();

        // Remove all inverse edges taken in new shortest path (along with mapped edge in original shortest path)
        Set<TopoEdge> combinedEdges = new HashSet<>();
        for (Integer index = 1; index < tempPaths.size(); index++) {
            List<TopoEdge> tempPath = tempPaths.get(index);
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
        //logPath(new ArrayList<>(combinedEdges), "Combined Edges");
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

