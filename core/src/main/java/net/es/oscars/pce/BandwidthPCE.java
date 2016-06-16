package net.es.oscars.pce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.enums.VertexType;
import net.es.oscars.topo.svc.TopoService;
import org.apache.commons.collections15.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BandwidthPCE {

    @Autowired
    private TopoService topoService;


    public List<TopoEdge> bwConstrainedShortestPath(String aUrn, String zUrn, Integer bandwidth, Set<Layer> layers) {
        log.info("finding bandwidth constrained path between " + aUrn + " -- " + zUrn + " for " + bandwidth + " mbps");

        List<ReservableBandwidthE> bandwidths = topoService.reservableBandwidths();
        // TODO: subtract already reserved bandwidths

        Graph<TopoVertex, TopoEdge> g = new DirectedSparseMultigraph<>();

        Transformer<TopoEdge, Double> wtTransformer = edge -> edge.getMetric().doubleValue();

        layers.stream().forEach(l -> {
            this.addToGraph(topoService.layer(l), l, g, bandwidth, bandwidths);
        });

        String pretty = null;
        try {
            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(g);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info(pretty);


        DijkstraShortestPath<TopoVertex, TopoEdge> alg = new DijkstraShortestPath<>(g, wtTransformer);

        TopoVertex src = new TopoVertex(aUrn, VertexType.PORT);
        TopoVertex dst = new TopoVertex(zUrn, VertexType.PORT);
        List<TopoEdge> path = alg.getPath(src, dst);

        log.info("calculated path ");
        if (path.isEmpty()) {
            log.error("no path found");
        }

        path.stream().forEach(h -> {
            log.info(h.getA().getUrn() + " -- " + h.getLayer() + " -- " + h.getZ().getUrn());
        });
        return path;
    }

    private void addToGraph(Topology topo, Layer layer, Graph<TopoVertex, TopoEdge> g, Integer bandwidth, List<ReservableBandwidthE> bandwidths) {

        topo.getVertices().stream().forEach(v -> {
            log.info("adding vertex to " + layer + " topo " + v.getUrn());
            g.addVertex(v);
        });

        topo.getEdges().stream().forEach(e -> {
            boolean bwFitsOnA = this.bandwidthFits(bandwidth, e.getA().getUrn(), bandwidths);
            boolean bwFitsOnZ = this.bandwidthFits(bandwidth, e.getZ().getUrn(), bandwidths);

            if (bwFitsOnA && bwFitsOnZ) {

                TopoVertex a = new TopoVertex(e.getA().getUrn(), e.getA().getVertexType());
                TopoVertex z = new TopoVertex(e.getZ().getUrn(), e.getZ().getVertexType());
                TopoEdge az = TopoEdge.builder().a(a).z(z).build();

                if (e.getLayer().equals(Layer.INTERNAL)) {
                    az.setLayer(Layer.INTERNAL);
                    az.setMetric(e.getMetric());;
                    log.info("adding edge " + e.getA() + " -- INTERNAL -- " + e.getZ());
                } else {
                    az.setLayer(layer);
                    az.setMetric(e.getMetric());
                    log.info("adding edge " + e.getA() + " -- " + layer + " -- " + e.getZ());
                }

                g.addEdge(az, a, z, EdgeType.DIRECTED);
            } else {
                log.info("not enough BW on edge " + e.getA() + " -- " + layer + " -- " + e.getZ());

            }
        });

    }

    private Boolean bandwidthFits(Integer bandwidth, String urn, List<ReservableBandwidthE> bandwidths) {

        log.debug("checking if " + urn + " has enough bandwidth " + bandwidth);
        List<ReservableBandwidthE> matching = bandwidths.stream().filter(bw -> bw.getUrn().getUrn().equals(urn)).collect(Collectors.toList());

        assert matching.size() <= 1;
        if (matching.isEmpty()) {
            log.info("bandwidth does not apply to " + urn);
            return true;
        } else {
            log.info("bandwidth fits on urn: " + urn);
            return matching.get(0).getBandwidth() >= bandwidth;

        }

    }
}
