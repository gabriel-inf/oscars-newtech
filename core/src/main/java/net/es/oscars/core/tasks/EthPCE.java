package net.es.oscars.core.tasks;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.topo.Layer;

import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import org.apache.commons.collections15.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class EthPCE {
    @Autowired
    private RestTemplate restTemplate;


    @Scheduled(fixedDelay = 10000)
    public void findPath() {
        String topoName = "net";

        String restPath = "https://localhost:8000/topo/"+topoName+"/layer/";

        Topology ethTopo = restTemplate.getForObject(restPath+Layer.ETHERNET, Topology.class);

        Topology mplsTopo = restTemplate.getForObject(restPath+Layer.MPLS, Topology.class);

        log.info(ethTopo.toString());

        log.info(mplsTopo.toString());


        Graph<TopoVertex, TopoEdge> g = new UndirectedSparseMultigraph<>();

        Transformer<TopoEdge, Double> wtTransformer = edge -> edge.getMetric().doubleValue();

        mplsTopo.getVertices().stream().forEach(g::addVertex);
        mplsTopo.getEdges().stream().forEach(e -> {
            TopoVertex a = new TopoVertex(e.getA());
            TopoVertex z = new TopoVertex(e.getZ());
            TopoEdge az = new TopoEdge(a, z);
            az.setLayer(Layer.MPLS);
            az.setMetric(e.getMetrics().get(Layer.MPLS));
            g.addEdge(az, a, z, EdgeType.UNDIRECTED);
        });

        ethTopo.getVertices().stream().forEach(g::addVertex);
        ethTopo.getEdges().stream().forEach(e -> {
            TopoVertex a = new TopoVertex(e.getA());
            TopoVertex z = new TopoVertex(e.getZ());
            TopoEdge az = new TopoEdge(a, z);
            az.setLayer(Layer.ETHERNET);
            az.setMetric(e.getMetrics().get(Layer.ETHERNET));
            g.addEdge(az, a, z, EdgeType.UNDIRECTED);
        });


        DijkstraShortestPath<TopoVertex,TopoEdge> alg = new DijkstraShortestPath<>(g, wtTransformer);

        TopoVertex src = new TopoVertex("nersc-tb1:3/1/1");
        TopoVertex dst = new TopoVertex("star-tb1:1/1/1");
        List<TopoEdge> path = alg.getPath(src, dst);
//        path.stream().forEach(e -> { log.info(e.getA().getUrn() + " -- "+e.getLayer()+" -- "+ e.getZ().getUrn()); } );

        src = new TopoVertex("nersc-asw1:xe-1/1/0");
        dst = new TopoVertex("star-tb1:1/1/1");
        path = alg.getPath(src, dst);
        path.stream().forEach(e -> { log.info(e.getA().getUrn() + " -- "+e.getLayer()+" -- "+ e.getZ().getUrn()); } );

    }

}
