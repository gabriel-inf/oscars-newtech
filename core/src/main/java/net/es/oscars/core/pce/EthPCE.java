package net.es.oscars.core.pce;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import org.apache.commons.collections15.Transformer;

import java.util.List;

/**
 * Created by haniotak on 3/17/16.
 */
public class EthPCE {
    public void keepit() {
/*
        Integer bandwidth = 1000;

        Graph<TopoVertex, TopoEdge> g = new UndirectedSparseMultigraph<>();

        Transformer<TopoEdge, Double> wtTransformer = edge -> edge.getMetric().doubleValue();

        mplsTopo.getVertices().stream().forEach(g::addVertex);
        mplsTopo.getEdges().stream().forEach(e -> {
            boolean bwFitsOnA = this.bandwidthFits(bandwidth, e.getA(), topoResources);
            boolean bwFitsOnZ = this.bandwidthFits(bandwidth, e.getZ(), topoResources);

            if (bwFitsOnA && bwFitsOnZ) {
                log.info("adding edge "+e.getA()+ " -- MPLS -- "+ e.getZ());

                TopoVertex a = new TopoVertex(e.getA());
                TopoVertex z = new TopoVertex(e.getZ());
                TopoEdge az = new TopoEdge(a, z);
                az.setLayer(Layer.MPLS);
                az.setMetric(e.getMetrics().get(Layer.MPLS));
                g.addEdge(az, a, z, EdgeType.UNDIRECTED);
            } else {
                log.info("not enough BW on edge "+e.getA()+ " -- MPLS -- "+ e.getZ());

            }
        });

        ethTopo.getVertices().stream().forEach(g::addVertex);
        ethTopo.getEdges().stream().forEach(e -> {
            log.info("adding edge "+e.getA()+ " -- ETH -- "+ e.getZ());
            TopoVertex a = new TopoVertex(e.getA());
            TopoVertex z = new TopoVertex(e.getZ());
            TopoEdge az = new TopoEdge(a, z);
            az.setLayer(Layer.ETHERNET);
            az.setMetric(e.getMetrics().get(Layer.ETHERNET));
            g.addEdge(az, a, z, EdgeType.UNDIRECTED);
        });


        DijkstraShortestPath<TopoVertex, TopoEdge> alg = new DijkstraShortestPath<>(g, wtTransformer);


        TopoVertex src = new TopoVertex("nersc-asw1:xe-1/1/0");
        TopoVertex dst = new TopoVertex("star-tb1:3/1/1");
        List<TopoEdge> path = alg.getPath(src, dst);

        log.info("finished with path!");

        path.stream().forEach(h -> {
            log.info(h.getA().getUrn() + " -- " + h.getLayer() + " -- " + h.getZ().getUrn());
        });
*/
    }
}
