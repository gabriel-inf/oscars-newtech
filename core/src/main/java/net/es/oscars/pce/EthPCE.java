package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.spec.VlanFixture;
import net.es.oscars.dto.spec.VlanJunction;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedResourceRepository;
import net.es.oscars.resv.ent.ReservedResourceE;
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
    private ReservedResourceRepository resourceRepo;


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



        this.decideAndSaveReserved(rsv_j, scheduleSpec);



        return rsv_j;
    }

    public void decideAndSaveReserved(VlanJunctionE rsv_j,
                                      ScheduleSpecificationE scheduleSpec) throws PSSException, PCEException {


        Map<ResourceType, List<String>> neededResources = pceAssistant.neededJunctionResources(rsv_j);
        log.info("needed junction resources: " +neededResources.toString());

        // TODO: time windows?
        Instant beginning = scheduleSpec.getNotBefore().toInstant();
        Instant ending = scheduleSpec.getNotAfter().toInstant();


        // collect reservable resources
        List<TopoResource> reservable = topoService.reservable();

        // collect reserved resources
        List<ReservedResourceE> reserved = new ArrayList<>();

        Optional<List<ReservedResourceE>> reservedOpt = resourceRepo.findOverlappingInterval(beginning, ending);
        if (reservedOpt.isPresent()) {
            reserved = reservedOpt.get();
        }


        for (ResourceType rt : neededResources.keySet()) {
            List<String> urns = neededResources.get(rt);
            Integer resource;
            // constraining resources are not decided here
            if (rt.equals(ResourceType.BANDWIDTH)) {
                // skip this

            } else {
                // decide what the newly reserved resource will be
                resource = decideRangeResource(reserved, reservable, urns, rt);
                ReservedResourceE re = ReservedResourceE.builder()
                        .beginning(beginning)
                        .ending(ending)
                        .resourceType(rt)
                        .urns(urns)
                        .resource(resource)
                        .build();

                // build and save it
                resourceRepo.save(re);
            }


        }

    }

    public Integer decideRangeResource(List<ReservedResourceE> reserved,
                                       List<TopoResource> reservable,
                                       List<String> urns, ResourceType rt) throws PCEException {

        log.info("trying to decide "+rt+" for urns: "+urns.toString());
        /*

        the logic is:
        our topology defines a resource as a resourcetype + a set of intRanges + a set of URNs

        our reserved resource is a resourcetype + a specific integer + a set of URNs

        we need to match the set of URNs + the type

        we will have exactly ONE resource
        and 0 .. N reserved


        */


        // for each of our urns

        // find what is reserved & what is reservable for that specific urn


        TopoResource tr = TopoAssistant.resourcefAllUrnsPlusType(urns, rt, reservable).orElseThrow(PCEException::new);
        Set<ReservedResourceE> rrs = TopoAssistant.reservedOfAllUrnsPlusType(urns, rt, reserved);

        for (ReservedResourceE rr : rrs) {
            tr = TopoAssistant.subtractReserved(tr, rr, rt);
        }


        Set<IntRange> availRanges = tr.getReservableRanges().get(rt);
        log.info("available: "+ availRanges.toString());
        if (availRanges.isEmpty()) {
            throw new PCEException("no resource available!");
        }
        Integer resource = availRanges.iterator().next().getFloor();
        log.info("decided "+ resource);

        return resource;

    }




    public void saveFixtureResources(VlanFixtureE fixture, ScheduleSpecificationE scheduleSpec) {

        // TODO: time windows
        Instant beginning = scheduleSpec.getNotBefore().toInstant();
        Instant ending = scheduleSpec.getNotAfter().toInstant();
        List<String> urns = new ArrayList<>();
        urns.add(fixture.getPortUrn());

        // bandwidth

        // TODO: asymmetrical bandwidth reservations
        Integer resource  = fixture.getInMbps();
        ResourceType rt = ResourceType.BANDWIDTH;

        ReservedResourceE re = ReservedResourceE.builder()
                .beginning(beginning)
                .ending(ending)
                .resourceType(rt)
                .urns(urns)
                .resource(resource)
                .build();

        // build and save it
        resourceRepo.save(re);

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
