package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.spec.ent.*;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class EthPCE {

    @Autowired
    private PCEAssistant assistant;

    @Autowired
    private TopoService topoService;

    @Autowired
    private BandwidthPCE bwPCE;


    public VlanFlowE makeReserved(VlanFlowE req_f) throws PSSException {

        Set<Layer> layers = new HashSet<>();
        layers.add(Layer.ETHERNET);
        layers.add(Layer.MPLS);

//        Graph<TopoVertex, TopoEdge> g = bwPCE.bwConstrainedGraph(layers, startInstant, endInstant);

        // make a flow entry
        VlanFlowE res_f = VlanFlowE.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        // plain junctions (not in pipes)
        for (VlanJunctionE bpJunction : req_f.getJunctions()) {
            VlanJunctionE schJunction = this.reserveSimpleJunction(bpJunction);
            res_f.getJunctions().add(schJunction);
        }


        // handle pipes
        for (VlanPipeE req_p : req_f.getPipes()) {
            Set<VlanPipeE> res_ps = this.makePipes(req_p);
            res_f.getPipes().addAll(res_ps);
        }
        return res_f;
    }


    private Set<VlanPipeE> makePipes(VlanPipeE req_p) throws PSSException {
        EDevice aDevice = topoService.device(req_p.getAJunction().getDeviceUrn());
        EDevice zDevice = topoService.device(req_p.getZJunction().getDeviceUrn());

        EthPipeType pipeType = assistant.decidePipeType(aDevice, zDevice);

        // TODO: actually different for pipes than simple
        VlanJunctionE aj = reserveSimpleJunction(req_p.getAJunction());
        VlanJunctionE zj = reserveSimpleJunction(req_p.getZJunction());






        // TODO: finish this
        // A-Z ERO
        // List<TopoEdge> azEro = bwPCE.bwConstrainedShortestPath(aj.getDeviceUrn(), zj.getDeviceUrn(), req_p.getAzMbps(), layers);


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

        Set<VlanPipeE> res_ps = new HashSet<>();
        res_ps.add(res_p);
        return res_ps;
    }


    private VlanJunctionE reserveSimpleJunction(VlanJunctionE req_j) throws PSSException {
        String deviceUrn = req_j.getDeviceUrn();
        EDevice device = topoService.device(deviceUrn);

        VlanJunctionE rsv_j = VlanJunctionE.builder()
                .deviceUrn(deviceUrn)
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .junctionType(assistant.decideJunctionType(device))
                .build();

        assistant.reserveJunctionResources(rsv_j);


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

}
