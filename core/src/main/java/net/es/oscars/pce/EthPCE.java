package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.spec.ent.*;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.enums.DeviceModel;
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


    public VlanFlowE makeReserved(VlanFlowE req_f) throws PSSException, PCEException{

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
            this.handlePipes(req_p, res_f);
        }
        return res_f;
    }

    private void handlePipes(VlanPipeE req_p, VlanFlowE res_f) throws PSSException, PCEException {
        Set<Layer> layers = new HashSet<>();
        layers.add(Layer.ETHERNET);
        layers.add(Layer.MPLS);


        String aDeviceUrn = req_p.getAJunction().getDeviceUrn();
        String zDeviceUrn = req_p.getZJunction().getDeviceUrn();

        // TODO: we are CURRENTLY only doing symmetrical paths
        List<TopoEdge> symmetricalERO = bwPCE
                .bwConstrainedShortestPath(aDeviceUrn, zDeviceUrn, req_p.getAzMbps(), layers);

        // TODO: constrain this further by VLANs


        if (symmetricalERO.isEmpty()) {
            throw new PCEException("Empty path from BW PCE");
        }

        Map<String, DeviceModel> deviceModels = topoService.deviceModels();

        // now, decompose the path
        List<Map<Layer, List<TopoEdge>>>  segments = assistant.decompose(symmetricalERO, deviceModels);

        // for each segment:
        // if it is an Ethernet segment, make junctions, one per device
        // if it is an MPLS segment, make a pipe
        // all the while, make sure to merge in the current first and last junctions as needed

        for (int i = 0; i < segments.size(); i++) {
            Map<Layer, List<TopoEdge>> segment = segments.get(i);
            Optional<VlanJunctionE> mergeA = Optional.empty();
            Optional<VlanJunctionE> mergeZ = Optional.empty();
            if (i == 0) {
                mergeA = Optional.of(req_p.getAJunction());
            }
            if (i == segments.size() - 1) {
                mergeZ = Optional.of(req_p.getZJunction());
            }

            List<TopoEdge> edges;

            if (segment.size() != 1) {
               throw new PCEException("invalid segmentation");
            }
            if (segment.containsKey(Layer.ETHERNET)) {
                if (segment.get(Layer.ETHERNET).size() != 3) {
                    throw new PCEException("invalid segmentation");
                }

                edges = segment.get(Layer.ETHERNET);

                // an ethernet segment: a list of junctions, one per device

                // TODO: do something with these junctions!
                List<VlanJunctionE> vjs = assistant.makeEthernetJunctions(edges,
                        req_p.getAzMbps(), req_p.getZaMbps(),
                        mergeA, mergeZ, deviceModels);


            } else if (segment.containsKey(Layer.MPLS)) {
                edges = segment.get(Layer.MPLS);
                // TODO: do something with this pipe!
                VlanPipeE pipe = assistant.makeVplsPipe(edges, req_p.getAzMbps(), req_p.getZaMbps(),
                        mergeA, mergeZ, deviceModels);

            } else {
                throw new PCEException("invalid segmentation");
            }
        }

        // TODO: decide VLANs and reserve them
        // TODO: collect needed resources from assistant


    }


    private VlanJunctionE reserveSimpleJunction(VlanJunctionE req_j) throws PSSException {
        String deviceUrn = req_j.getDeviceUrn();
        EDevice device = topoService.device(deviceUrn);

        VlanJunctionE rsv_j = VlanJunctionE.builder()
                .deviceUrn(deviceUrn)
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .junctionType(assistant.decideJunctionType(device.getModel()))
                .build();

        // TODO: go from needed to reserving them
        Map<String, ResourceType> neededResources = assistant.neededJunctionResources(rsv_j);


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
                .fixtureType(assistant.decideFixtureType(device.getModel()))
                .build();
    }

}
