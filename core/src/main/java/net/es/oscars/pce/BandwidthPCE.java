package net.es.oscars.pce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.rsrc.ReservableQty;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.spec.ent.VlanFixtureE;
import net.es.oscars.spec.ent.VlanFlowE;
import net.es.oscars.spec.ent.VlanJunctionE;
import net.es.oscars.spec.ent.VlanPipeE;
import net.es.oscars.topo.ent.EDevice;
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
        List<TopoResource> constraining = topoService.constraining();

        Graph<TopoVertex, TopoEdge> g = new UndirectedSparseMultigraph<>();

        Transformer<TopoEdge, Double> wtTransformer = edge -> edge.getMetric().doubleValue();

        layers.stream().forEach(l -> {
            this.addToGraph(topoService.layer(l), l, g, bandwidth, constraining);
        });

        String pretty = null;
        try {
            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(g);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info(pretty);


        DijkstraShortestPath<TopoVertex, TopoEdge> alg = new DijkstraShortestPath<>(g, wtTransformer);

        TopoVertex src = new TopoVertex(aUrn);
        TopoVertex dst = new TopoVertex(zUrn);
        List<TopoEdge> path = alg.getPath(src, dst);

        log.info("finished with path!");

        path.stream().forEach(h -> {
            log.info(h.getA().getUrn() + " -- " + h.getLayer() + " -- " + h.getZ().getUrn());
        });
        return path;
    }

    private void addToGraph(Topology topo, Layer layer, Graph<TopoVertex, TopoEdge> g, Integer bandwidth, List<TopoResource> constraining) {

        topo.getVertices().stream().forEach(v -> {
            log.info("adding vertex to " + layer + " topo " + v.getUrn());
            g.addVertex(v);
        });

        topo.getEdges().stream().forEach(e -> {
            boolean bwFitsOnA = this.bandwidthFits(bandwidth, e.getA(), constraining);
            boolean bwFitsOnZ = this.bandwidthFits(bandwidth, e.getZ(), constraining);

            if (bwFitsOnA && bwFitsOnZ) {

                TopoVertex a = new TopoVertex(e.getA());
                TopoVertex z = new TopoVertex(e.getZ());
                TopoEdge az = new TopoEdge(a, z);

                if (e.getMetrics().containsKey(Layer.INTERNAL)) {
                    az.setLayer(Layer.INTERNAL);
                    az.setMetric(e.getMetrics().get(Layer.INTERNAL));
                    log.info("adding edge " + e.getA() + " -- INTERNAL -- " + e.getZ());
                } else {
                    az.setLayer(layer);
                    az.setMetric(e.getMetrics().get(layer));
                    log.info("adding edge " + e.getA() + " -- " + layer + " -- " + e.getZ());
                }

                g.addEdge(az, a, z, EdgeType.UNDIRECTED);
            } else {
                log.info("not enough BW on edge " + e.getA() + " -- " + layer + " -- " + e.getZ());

            }
        });

    }

    private Set<TopoResource> applyingTo(String urn, Collection<TopoResource> allResources) {
        return allResources.stream().filter(t -> t.getTopoVertexUrns().contains(urn)).collect(Collectors.toSet());
    }

    private Boolean bandwidthFits(Integer bandwidth, String urn, Collection<TopoResource> allResources) {
        log.debug("checking if " + urn + " has enough bandwidth " + bandwidth);
        Set<TopoResource> applyingTo = this.applyingTo(urn, allResources);
        if (applyingTo.isEmpty()) {
            log.info("bandwidth does not apply");
            return true;
        } else {
            boolean fits = true;
            for (TopoResource tr : applyingTo) {
                List<ReservableQty> bwQties = tr.getReservableQties().stream()
                        .filter(t -> t.getType().equals(ResourceType.BANDWIDTH)).collect(Collectors.toList());

                boolean fitsOnThis = bwQties.isEmpty() || bwQties.stream().filter(q -> q.getRange().contains(bandwidth)).findAny().isPresent();

                if (!fitsOnThis) {
                    fits = false;
                }
            }
            log.info("bandwidth fits? " + fits);
            return fits;


        }

    }
}
