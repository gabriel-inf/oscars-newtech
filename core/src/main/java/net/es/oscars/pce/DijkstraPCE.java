package net.es.oscars.pce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.topo.svc.TopoService;
import org.apache.commons.collections15.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeremy on 6/15/16.
 */
@Slf4j
@Component
public class DijkstraPCE {
    @Autowired
    private TopoService topoService;


    /**
     * Computes Dijkstra's shortest path directed from srcVertex to dstVertex. Input topology is assumed to be pre-pruned based on bandwidth and vlan availability.
     *
     * @param topology  - pruned topology
     * @param srcVertex - source URN
     * @param dstVertex - destination URN
     * @return path as List of TopoEdge objects
     */
    public List<TopoEdge> computeShortestPathEdges(Topology topology, TopoVertex srcVertex, TopoVertex dstVertex) {

        Graph<TopoVertex, TopoEdge> graph = new DirectedSparseMultigraph<>();

         Transformer<TopoEdge, Double> wtTransformer = edge -> edge.getMetric().doubleValue();

        this.addToGraph(topology, graph);

        String pretty = null;
        try {
            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(graph);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // log.info(pretty);


        DijkstraShortestPath<TopoVertex, TopoEdge> alg = new DijkstraShortestPath<>(graph, wtTransformer);

        List<TopoEdge> path = alg.getPath(srcVertex, dstVertex);

        //log.info("calculated path: ");
        if (path.isEmpty()) {
            //log.error("no path found");
        }

        //path.stream().forEach(h -> log.info(h.getA().getUrn() + " -- " + h.getLayer() + " -- " + h.getZ().getUrn()));

        return path;
    }


    /**
     * Translates a List of TopoEdges representing a path into its corresponding TopoVertices
     *
     * @param pathEdges - List of TopoEdges representing path
     * @return path as list of TopoVertex objects
     */
    public List<TopoVertex> translatePathEdgesToVertices(List<TopoEdge> pathEdges) {
        List<TopoVertex> pathVertices = new ArrayList<>();

        pathVertices.add(pathEdges.get(0).getA());
        for (TopoEdge oneEdge : pathEdges) {
            pathVertices.add(oneEdge.getZ());
        }

        return pathVertices;
    }

    /**
     * Translates a List of TopoVertices representing a path into its corresponding String URNs
     *
     * @param pathVertices - List of TopoVertices representing path
     * @return path as list of URN Strings
     */
    public List<String> translatePathVerticesToStrings(List<TopoVertex> pathVertices) {
        List<String> pathStrings = new ArrayList<>();

        pathVertices.stream().forEach(l -> pathStrings.add(l.getUrn()));

        return pathStrings;
    }

    private void addToGraph(Topology topo, Graph<TopoVertex, TopoEdge> g) {
        topo.getVertices().stream()
                .forEach(v -> {
                            //log.info("adding vertex to Dijkstra graph: " + v.toString());
                            g.addVertex(v);
                        }
                );

        topo.getEdges().stream()
                .forEach(e -> {
                            TopoVertex nodeA = new TopoVertex(e.getA().getUrn(), e.getA().getVertexType(), e.getA().getPortLayer());
                            TopoVertex nodeZ = new TopoVertex(e.getZ().getUrn(), e.getZ().getVertexType(), e.getZ().getPortLayer());
                            TopoEdge az = TopoEdge.builder().a(nodeA).z(nodeZ).build();

                            az.setMetric(e.getMetric());
                            az.setLayer(e.getLayer());
                            //log.info("adding edge to Dijkstra graph: (" + e.getA().getUrn() + "," + e.getZ().getUrn() + ")");

                            g.addEdge(az, nodeA, nodeZ, EdgeType.DIRECTED);
                        }
                );
    }
}
