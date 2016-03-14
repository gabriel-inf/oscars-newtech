package net.es.oscars;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.resv.ResourceType;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.ds.DatastoreConfig;
import net.es.oscars.ds.topo.pop.TopoImporter;
import net.es.oscars.dto.rsrc.ReservableQty;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import org.apache.commons.collections15.Transformer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.security.access.method.P;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DatastoreConfig.class, loader = SpringApplicationContextLoader.class)
@WebIntegrationTest
public class Topo_IT {


    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private TopoImporter importer;

    @Before
    public void prepare() throws IOException {
        importer.importFromFile(true, "config/topo-basic/devices.json", "config/topo-basic/adjcies.json");
    }

    @Test
    public void testHello() throws Exception {

        String restPath = "https://localhost:8000/topo/layer/";
        String constrainingRestPath = "https://localhost:8000/constraining/";

        Topology ethTopo = restTemplate.getForObject(restPath + Layer.ETHERNET, Topology.class);

        Topology mplsTopo = restTemplate.getForObject(restPath + Layer.MPLS, Topology.class);

        log.info(ethTopo.toString());

        log.info(mplsTopo.toString());

        HashSet<String> allUrns = new HashSet<>();

        ethTopo.getVertices().stream().forEach(v -> allUrns.add(v.getUrn()));
        mplsTopo.getVertices().stream().forEach(v -> allUrns.add(v.getUrn()));


        List<TopoResource> topoResources = Arrays.asList(restTemplate.postForObject(constrainingRestPath, allUrns, TopoResource[].class));

        allUrns.stream().forEach( h -> {
            Set<TopoResource> applying = this.applyingTo(h, topoResources);
            applying.stream()
                    .forEach(tr -> {
                        log.info("vertex "+h+ " : "+ tr.toString());
                    });
        });

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

    }

    private Boolean bandwidthFits(Integer bandwidth, String urn, Collection<TopoResource> allResources) {
        Set<TopoResource> applyingTo = this.applyingTo(urn, allResources);
        if (applyingTo.isEmpty()) {
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
            return fits;


        }

    }

    private Set<TopoResource> applyingTo(String urn, Collection<TopoResource> allResources) {
        return  allResources.stream().filter(t -> t.getTopoVertexUrns().contains(urn)).collect(Collectors.toSet());
    }

}
