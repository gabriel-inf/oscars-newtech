package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.spec.VlanFixture;
import net.es.oscars.dto.spec.VlanJunction;
import net.es.oscars.dto.spec.VlanPipe;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.spec.ent.*;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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


    public VlanFlowE makeReserved(VlanFlowE req_f) throws PSSException {

        // make a flow entry
        VlanFlowE res_f = VlanFlowE.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();


        // plain junctions (not in pipes): decide type
        for (VlanJunctionE bpJunction : req_f.getJunctions()) {
            VlanJunctionE schJunction = this.makeJunction(bpJunction);
            res_f.getJunctions().add(schJunction);
        }


        for (VlanPipeE req_p : req_f.getPipes()) {
            VlanPipeE res_p = this.makePipe(req_p);
            res_f.getPipes().add(res_p);

        }
        return res_f;
    }

    private VlanPipeE makePipe(VlanPipeE req_p) throws PSSException {
        VlanJunctionE aj = makeJunction(req_p.getAJunction());
        VlanJunctionE zj = makeJunction(req_p.getZJunction());
        EDevice aDevice = topoService.device(req_p.getAJunction().getDeviceUrn());
        EDevice zDevice = topoService.device(req_p.getZJunction().getDeviceUrn());

        EthPipeType pipeType = assistant.decidePipeType(aDevice, zDevice);

        // TODO: EROs, resource IDs, decompose hybrid pipes
        VlanPipeE res_p = VlanPipeE.builder()
                .azMbps(req_p.getAzMbps())
                .zaMbps(req_p.getZaMbps())
                .aJunction(aj)
                .zJunction(zj)
                .pipeType(pipeType)
                .azERO(new ArrayList<>())
                .zaERO(new ArrayList<>())
                .resourceIds(new HashSet<>())
                .build();
        return res_p;
    }




    private VlanJunctionE makeJunction(VlanJunctionE req_j) throws PSSException {
        String deviceUrn = req_j.getDeviceUrn();
        EDevice device = topoService.device(deviceUrn);

        VlanJunctionE rsv_j = VlanJunctionE.builder()
                .deviceUrn(deviceUrn)
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .junctionType(assistant.decideJunctionType(device))
                .build();

        for (VlanFixtureE req_f : req_j.getFixtures()) {
            VlanFixtureE res_f = this.makeFixture(req_f, device);
            rsv_j.getFixtures().add(res_f);
        }

        return rsv_j;
    }

    private VlanFixtureE makeFixture(VlanFixtureE bpFixture, EDevice device) throws PSSException {
        return VlanFixtureE.builder()
                .egMbps(bpFixture.getEgMbps())
                .inMbps(bpFixture.getInMbps())
                .portUrn(bpFixture.getPortUrn())
                .vlanExpression(bpFixture.getVlanExpression())
                .fixtureType(assistant.decideFixtureType(device))
                .build();
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
