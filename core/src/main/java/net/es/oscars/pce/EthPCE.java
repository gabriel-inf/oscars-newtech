package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.spec.ent.*;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EthPCE {

    @Autowired
    private PCEAssistant assistant;

    @Autowired
    private TopoService topoService;

    public BlueprintE makeReserved(BlueprintE requested) throws PCEException, PSSException {

        verifyBlueprint(requested);

        BlueprintE reserved = BlueprintE.builder()
                .layer3Flows(new HashSet<>())
                .vlanFlows(new HashSet<>())
                .build();


        for (VlanFlowE flow : requested.getVlanFlows()) {
            // make a flow entry
            VlanFlowE reservedFlow = VlanFlowE.builder()
                    .junctions(new HashSet<>())
                    .pipes(new HashSet<>())
                    .build();

            reserved.getVlanFlows().add(reservedFlow);

            // plain junctions (not in pipes): decide type
            for (VlanJunctionE bpJunction : flow.getJunctions()) {
                VlanJunctionE schJunction = this.makeReservedJunction(bpJunction);
                reservedFlow.getJunctions().add(schJunction);
            }

            for (VlanPipeE pipe : flow.getPipes()) {
//

            }

        }
        return reserved;

    }

    private VlanJunctionE makeReservedJunction(VlanJunctionE bpJunction) throws PSSException {
        String deviceUrn = bpJunction.getDeviceUrn();
        EDevice device = topoService.device(deviceUrn);

        VlanJunctionE rsvJunction = VlanJunctionE.builder()
                .deviceUrn(deviceUrn)
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .junctionType(assistant.decideJunctionType(device))
                .build();

        bpJunction.getFixtures().stream().forEach(t -> {
            try {
                VlanFixtureE bpFixture = this.makeReservedFixture(t, device);
                rsvJunction.getFixtures().add(bpFixture);
            } catch (PSSException e) {
                // oh, java 8
                e.printStackTrace();
            }
        });

        return rsvJunction;
    }

    private VlanFixtureE makeReservedFixture(VlanFixtureE bpFixture, EDevice device) throws PSSException {
        return VlanFixtureE.builder()
                .egMbps(bpFixture.getEgMbps())
                .inMbps(bpFixture.getInMbps())
                .portUrn(bpFixture.getPortUrn())
                .vlanExpression(bpFixture.getVlanExpression())
                .fixtureType(assistant.decideFixtureType(device))
                .build();

    }



    public void verifyBlueprint(BlueprintE blueprint) throws PCEException {
        log.info("starting verification");
        if (blueprint == null) {
            throw new PCEException("Null blueprint!");
        } else if (blueprint.getVlanFlows() == null || blueprint.getVlanFlows().isEmpty()) {
            throw new PCEException("No VLAN flows");
        } else if (blueprint.getVlanFlows().size() != 1) {
            throw new PCEException("Exactly one flow supported right now");
        }

        VlanFlowE flow = blueprint.getVlanFlows().iterator().next();

        log.info("verifying junctions & pipes");
        if (flow.getJunctions().isEmpty() && flow.getPipes().isEmpty()) {
            throw new PCEException("Junctions or pipes both empty.");
        }

        Set<VlanJunctionE> allJunctions = flow.getJunctions();
        flow.getPipes().stream().forEach(t -> {
            allJunctions.add(t.getAJunction());
            allJunctions.add(t.getZJunction());
        });

        for (VlanJunctionE junction: allJunctions) {
            EDevice device = topoService.device(junction.getDeviceUrn());
        }

        Set<String> junctionsWithNoFixtures = allJunctions.stream().
                filter(t -> t.getFixtures().isEmpty()).
                map(VlanJunctionE::getDeviceUrn).collect(Collectors.toSet());

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
