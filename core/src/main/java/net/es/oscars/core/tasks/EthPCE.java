package net.es.oscars.core.tasks;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.UrnEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

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

        DirectedGraph<TopoVertex, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        ethTopo.getVertices().stream().forEach(g::addVertex);
        mplsTopo.getVertices().stream().forEach(g::addVertex);

        Set<UrnEdge> edges = ethTopo.getEdges();
        edges.addAll(mplsTopo.getEdges());

        for (UrnEdge e : edges) {
            TopoVertex a = new TopoVertex(e.getA());
            TopoVertex z = new TopoVertex(e.getZ());
            g.addEdge(z, a);
            g.addEdge(a, z);
        }

        log.info(g.toString());



    }

}
