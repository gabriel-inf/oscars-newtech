package net.es.oscars.pss.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.helpers.ResourceChooser;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.prop.PssConfig;
import net.es.oscars.resv.dao.ReservedPssResourceRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@Transactional
@Slf4j
public class PssResourceService {
    private ResourceChooser chooser;

    private TopoService topoService;

    private ReservedPssResourceRepository pssResRepo;

    private PssConfig pssConfig;

    @Autowired
    public PssResourceService(ResourceChooser chooser,
                              TopoService topoService,
                              ReservedPssResourceRepository pssResRepo,
                              PssConfig pssConfig) {
        this.chooser = chooser;
        this.topoService = topoService;
        this.pssResRepo = pssResRepo;
        this.pssConfig = pssConfig;
    }


    public void reserve(ConnectionE conn) throws PSSException {
        log.info("starting PSS resource reservation");
        ReservedVlanFlowE rvf = conn.getReserved().getVlanFlow();
        Instant beginning = conn.getReservedSchedule().get(0).toInstant();
        Instant ending = conn.getReservedSchedule().get(1).toInstant();

        // isolated junctions
        for (ReservedVlanJunctionE rvj : rvf.getJunctions()) {
            this.reserveIsolatedJunction(rvj, beginning, ending);
        }

        for (ReservedMplsPipeE rmp : rvf.getMplsPipes()) {
            this.reserveMplsPipe(rmp, beginning, ending);
        }

        rvf.getEthPipes().forEach(rep -> {
            this.reserveEthPipe(rep, beginning, ending);
        });
        try {
            log.debug("allocated PSS resources, connection is now:");
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(conn);
            log.debug (pretty);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    private void reserveIsolatedJunction(ReservedVlanJunctionE rvj, Instant beginning, Instant ending) throws PSSException {
        log.info("reserving PSS resources for an isolated junction, device: " + rvj.getDeviceUrn());
        UrnE device = topoService.device(rvj.getDeviceUrn());
        Set<ReservedPssResourceE> junctionResources = new HashSet<>();
        switch (device.getDeviceModel()) {
            case JUNIPER_EX:
                // no further identifiers to reserve
                break;
            case JUNIPER_MX:
                // no further identifiers to reserve
                break;
            case ALCATEL_SR7750:
                // we do need a service ID
                Integer svcId = this.chooseSvcId(rvj.getDeviceUrn(), beginning, ending);
                junctionResources.add(ReservedPssResourceE.makeSvcIdResource(rvj.getDeviceUrn(), svcId, beginning, ending));
                log.info("reserving Alcatel fixtures");
                rvj.getFixtures().forEach(f -> {
                    Integer inQosId = this.chooseQosId(rvj.getDeviceUrn(), ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);
                    Integer egQosId = this.chooseQosId(rvj.getDeviceUrn(), ResourceType.ALU_EGRESS_POLICY_ID, beginning, ending);
                    ReservedPssResourceE inQosIdRes = ReservedPssResourceE.makeQosIdResource(rvj.getDeviceUrn(), inQosId, ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);
                    ReservedPssResourceE egQosIdRes = ReservedPssResourceE.makeQosIdResource(rvj.getDeviceUrn(), egQosId, ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);
                    f.getReservedPssResources().add(inQosIdRes);
                    f.getReservedPssResources().add(egQosIdRes);
                });
                break;
        }
        rvj.getReservedPssResources().addAll(junctionResources);
    }

    private void reserveEthPipe(ReservedEthPipeE rep, Instant beginning, Instant ending) {

    }

    private void reserveMplsPipe(ReservedMplsPipeE rmp, Instant beginning, Instant ending) throws PSSException {
        // we will need a vcId for the pipe, so reserve one

        Integer vcId = chooseVcId(beginning, ending);
        log.info("decided to use vcId " + vcId + " for MPLS pipe");
        ReservedVlanJunctionE aj = rmp.getAJunction();
        ReservedVlanJunctionE zj = rmp.getZJunction();
        Set<ReservedVlanJunctionE> rvjs = new HashSet<>();
        rvjs.add(aj);
        rvjs.add(zj);
        for (ReservedVlanJunctionE rvj : rvjs) {
            // always reserve that vcId
            rvj.getReservedPssResources().add(ReservedPssResourceE.makeVcIdResource(vcId, beginning, ending));
            log.info("reserved a vcid " + vcId + " in junction: " + rvj.getDeviceUrn());

            UrnE device = topoService.device(rvj.getDeviceUrn());
            switch (device.getDeviceModel()) {
                case JUNIPER_EX:
                    throw new PSSException("device model " + device.getDeviceModel() + " does not support MPLS pipes!");
                case JUNIPER_MX:
                    // no further identifiers needed
                    break;
                case ALCATEL_SR7750:
                    Optional<Integer> maybeSvcId = this.aluSvcId(rvj.getDeviceUrn(), beginning, ending);
                    if (!maybeSvcId.isPresent()) {
                        log.info("need to make a service id for " + rvj.getDeviceUrn());
                        Integer svcId = this.chooseSvcId(rvj.getDeviceUrn(), beginning, ending);
                        rvj.getReservedPssResources().add(ReservedPssResourceE.makeSvcIdResource(rvj.getDeviceUrn(), svcId, beginning, ending));
                    } // we need to reserve an SDP per junction per pipe

                    Integer sdpId = this.chooseSdpId(beginning, ending);
                    rvj.getReservedPssResources().add(ReservedPssResourceE.makeSdpIdResource(rvj.getDeviceUrn(), sdpId, beginning, ending));
                    log.info("reserved sdpId " + sdpId + " in junction for " + rvj.getDeviceUrn());
                    log.info("reserving Alcatel fixtures (qos Ids etc) in junction for " + rvj.getDeviceUrn());
                    rvj.getFixtures().forEach(f -> {
                        Integer inQosId = this.chooseQosId(rvj.getDeviceUrn(), ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);
                        Integer egQosId = this.chooseQosId(rvj.getDeviceUrn(), ResourceType.ALU_EGRESS_POLICY_ID, beginning, ending);
                        log.info("qosIds : " + inQosId + " " + egQosId);
                        ReservedPssResourceE inQosIdRes = ReservedPssResourceE.makeQosIdResource(rvj.getDeviceUrn(), inQosId, ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);
                        ReservedPssResourceE egQosIdRes = ReservedPssResourceE.makeQosIdResource(rvj.getDeviceUrn(), egQosId, ResourceType.ALU_EGRESS_POLICY_ID, beginning, ending);
                        f.getReservedPssResources().add(inQosIdRes);
                        f.getReservedPssResources().add(egQosIdRes);
                    });
                    break;
            }
        }
    }

    private Optional<Integer> aluSvcId(String deviceUrn, Instant beginning, Instant ending) {
        Optional<List<ReservedPssResourceE>> maybeResvResources = pssResRepo.findOverlappingInterval(beginning, ending);
        if (maybeResvResources.isPresent()) {
            for (ReservedPssResourceE res : maybeResvResources.get()) {
                if (res.getUrn().equals(deviceUrn) && res.getResourceType().equals(ResourceType.ALU_SVC_ID)) {
                    return Optional.of(res.getResource());
                }
            }
        }
        return Optional.empty();

    }

    private Integer chooseQosId(String deviceUrn, ResourceType rt, Instant beginning, Instant ending) {
        return 6000;
    }

    private Integer chooseSdpId(Instant beginning, Instant ending) {
        return 6000;
    }


    private Integer chooseVcId(Instant beginning, Instant ending) throws PSSException {
        String vcIdRangeExpr = pssConfig.getVcidRange();
        if (!IntRangeParsing.isValidIntRangeInput(vcIdRangeExpr)) {
            throw new PSSException("invalid vc id range");
        }
        List<IntRange> ranges = IntRangeParsing.retrieveIntRanges(vcIdRangeExpr);
        if (ranges.size() != 1) {
            throw new PSSException("only one range supported for VC IDs");
        }
        IntRange range = ranges.get(0);
        log.info("choosing a VC ID in this range: " + vcIdRangeExpr);

        Set<Integer> reserved = new HashSet<>();

        Set<ReservedPssResourceE> reservedVcIds = this.findOverlappingReservedIds(beginning, ending, ResourceType.VC_ID);
        // gotta keep VC ids unique, so can never double-book one
        for (ReservedPssResourceE resVcId : reservedVcIds) {
            reserved.add(resVcId.getResource());
        }
        return choose(range, reserved);
    }


    private Integer chooseSvcId(String deviceUrn, Instant beginning, Instant ending) throws PSSException {
        String aluSvcRangeExpr = pssConfig.getAluSvcidRange();
        if (!IntRangeParsing.isValidIntRangeInput(aluSvcRangeExpr)) {
            throw new PSSException("invalid alu svc id range: "+aluSvcRangeExpr);
        }
        List<IntRange> ranges = IntRangeParsing.retrieveIntRanges(aluSvcRangeExpr);
        if (ranges.size() != 1) {
            throw new PSSException("only one range supported for VC IDs");
        }
        IntRange range = ranges.get(0);
        log.info("choosing a VC ID in this range: " + aluSvcRangeExpr);

        Set<Integer> reserved = new HashSet<>();

        Set<ReservedPssResourceE> reservedSvcIds = this.findOverlappingReservedIds(beginning, ending, ResourceType.ALU_SVC_ID);
        // SVC ids are unique to each router
        for (ReservedPssResourceE resSvcId : reservedSvcIds) {
            if (resSvcId.getUrn().equals(deviceUrn))
                reserved.add(resSvcId.getResource());
        }
        return choose(range, reserved);
    }

    private Integer choose(IntRange range, Set<Integer> reserved) throws PSSException {
        Integer origin = range.getFloor();
        Integer bound = range.getCeiling() + 1;

        try {
            Optional<Integer> maybeId = chooser.chooseInRange(origin, bound, reserved, ResourceChooser.Method.RANDOM);
            if (maybeId.isPresent()) {
                return maybeId.get();
            } else {
                throw new PSSException("Could not select an svc id");
            }
        } catch (ResourceChooser.ResourceChoiceException ex) {
            log.error(ex.getMessage(), ex);
            throw new PSSException(ex.getMessage());
        }
    }

    // returns all overlapping resources of defined type
    private Set<ReservedPssResourceE> findOverlappingReservedIds(Instant beginning, Instant ending, ResourceType rt) {
        Set<ReservedPssResourceE> reservedResources = new HashSet<>();

        Optional<List<ReservedPssResourceE>> maybeResvResources = pssResRepo.findOverlappingInterval(beginning, ending);

        maybeResvResources.ifPresent(rprEs ->
                rprEs.stream().filter(r -> r.getResourceType().equals(rt)).forEach(reservedResources::add)
        );

        return reservedResources;
    }


    public void release(ConnectionE conn) {

    }
}
