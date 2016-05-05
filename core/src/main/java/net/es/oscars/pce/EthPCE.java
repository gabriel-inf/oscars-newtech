package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.dao.ReservedPssResourceRepository;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.spec.ent.*;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class EthPCE {

    @Autowired
    private PCEAssistant pceAssistant;

    @Autowired
    private TopoService topoService;

    @Autowired
    private BandwidthPCE bwPCE;

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private ReservedVlanRepository vlanRepo;

    @Autowired
    private ReservedPssResourceRepository pssResourceRepo;


    public VlanFlowE makeReserved(VlanFlowE req_f, ScheduleSpecificationE schedSpec) throws PSSException, PCEException {
        log.info("making reserved for flow: "+req_f.toString());

        Set<Layer> layers = new HashSet<>();
        layers.add(Layer.ETHERNET);
        layers.add(Layer.MPLS);

        // make a flow entry
        VlanFlowE res_f = VlanFlowE.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        // part 1: plain junctions (not in pipes): same device
        for (VlanJunctionE bpJunction : req_f.getJunctions()) {
            log.info("making a simple junction: "+bpJunction);
            VlanJunctionE schJunction = this.reserveSimpleJunction(bpJunction, schedSpec);
            res_f.getJunctions().add(schJunction);
        }

        // part 2: pipes - i.e. not in same device
        for (VlanPipeE req_p : req_f.getPipes()) {
            log.info("handling a pipe: " + req_p.toString());
            this.handlePipes(req_p, res_f);
        }
        return res_f;
    }

    /**
     *
     * @param req_p the requested pipe
     * @param res_f the flow to-be-reserved
     * @throws PSSException
     * @throws PCEException
     */
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
        List<Map<Layer, List<TopoEdge>>> segments = pceAssistant.decompose(symmetricalERO, deviceModels);

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
                List<VlanJunctionE> vjs = pceAssistant.makeEthernetJunctions(edges,
                        req_p.getAzMbps(), req_p.getZaMbps(),
                        mergeA, mergeZ, deviceModels);


            } else if (segment.containsKey(Layer.MPLS)) {
                edges = segment.get(Layer.MPLS);
                // TODO: do something with this pipe!
                VlanPipeE pipe = pceAssistant.makeVplsPipe(edges, req_p.getAzMbps(), req_p.getZaMbps(),
                        mergeA, mergeZ, deviceModels);

            } else {
                throw new PCEException("invalid segmentation");
            }
        }

        // TODO: decide VLANs and reserve them
        // TODO: collect needed resources from pceAssistant



    }


    private VlanJunctionE reserveSimpleJunction(VlanJunctionE req_j, ScheduleSpecificationE scheduleSpec) throws PCEException, PSSException {
        String deviceUrn = req_j.getDeviceUrn();
        EDevice device = topoService.device(deviceUrn);

        VlanJunctionE rsv_j = VlanJunctionE.builder()
                .deviceUrn(deviceUrn)
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .junctionType(pceAssistant.decideJunctionType(device.getModel()))
                .build();


        for (VlanFixtureE req_f : req_j.getFixtures()) {
            VlanFixtureE res_f = this.makeFixture(req_f, device);
            rsv_j.getFixtures().add(res_f);

            saveFixtureResources(res_f, scheduleSpec);
        }



        return rsv_j;
    }




    public void saveFixtureResources(VlanFixtureE fixture, ScheduleSpecificationE scheduleSpec) {

        // TODO: time windows
        Instant beginning = scheduleSpec.getNotBefore().toInstant();
        Instant ending = scheduleSpec.getNotAfter().toInstant();

        // bandwidth
        ReservedBandwidthE reserved_bw = ReservedBandwidthE.builder()
                .beginning(beginning)
                .ending(ending)
                .urn(fixture.getPortUrn())
                .bandwidth(fixture.getInMbps())
                .build();


        // build and save it
        bwRepo.save(reserved_bw);

        // TODO: VLAN

    }



    private VlanFixtureE makeFixture(VlanFixtureE bpFixture, EDevice device) throws PSSException {

        return VlanFixtureE.builder()
                .egMbps(bpFixture.getEgMbps())
                .inMbps(bpFixture.getInMbps())
                .portUrn(bpFixture.getPortUrn())
                .vlanExpression(bpFixture.getVlanExpression())
                .fixtureType(pceAssistant.decideFixtureType(device.getModel()))
                .build();
    }

}
