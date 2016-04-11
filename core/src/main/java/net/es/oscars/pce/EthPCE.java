package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.spec.ent.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class EthPCE {
    public ESchematic makeSchematic(EBlueprint blueprint) throws PCEException {
        verifyBlueprint(blueprint);
        ESchematic schematic = ESchematic.builder().flows(new HashSet<>()).build();
        for (EFlow flow : blueprint.getFlows()) {

            for (EVlanPipe pipe : flow.getPipes()) {


            }

        }
        return schematic;

    }


    public void verifyBlueprint(EBlueprint blueprint) throws PCEException {
        log.info("starting verification");
        if (blueprint == null) {
            throw new PCEException("Null blueprint");
        } else if (blueprint.getFlows() == null || blueprint.getFlows().isEmpty()) {
            throw new PCEException("No flows");
        } else if (blueprint.getFlows().size() != 1) {
            throw new PCEException("Exactly one flow supported right now");
        }

        EFlow flow = blueprint.getFlows().iterator().next();

        log.info("verifying junctions & pipes");
        if (flow.getJunctions().isEmpty() && flow.getPipes().isEmpty()) {
            throw new PCEException("Junctions or pipes both empty.");
        }

        Set<EVlanJunction> allJunctions = flow.getJunctions();
        flow.getPipes().stream().forEach(t -> {
            allJunctions.add(t.getAJunction());
            allJunctions.add(t.getZJunction());
        });

        Set<String> junctionsWithNoFixtures = allJunctions.stream().
                filter(t -> t.getFixtures().isEmpty()).
                map(EVlanJunction::getDeviceUrn).collect(Collectors.toSet());

        if (!junctionsWithNoFixtures.isEmpty()) {
            throw new PCEException("Junctions with no fixtures found: " + String.join(" ", junctionsWithNoFixtures));
        }

    }


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
