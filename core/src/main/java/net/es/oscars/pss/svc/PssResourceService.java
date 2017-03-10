package net.es.oscars.pss.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.prop.PssConfig;
import net.es.oscars.resv.dao.ConnectionRepository;
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
    @Autowired
    private TopoService topoService;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private RouterCommandGenerator rcg;

    @Autowired
    private ReservedPssResourceRepository pssResRepo;

    @Autowired
    private PssConfig pssConfig;

    public void generateConfig(ConnectionE conn) throws PSSException {
        rcg.generateConfig(conn);
    }

    public void reserve(ConnectionE conn) {
        log.info("starting PSS resource reservation");
        ReservedVlanFlowE rvf = conn.getReserved().getVlanFlow();
        Set<ReservedVlanJunctionE> rvj_set = new HashSet<>();
        Instant beginning = conn.getReservedSchedule().get(0).toInstant();
        Instant ending = conn.getReservedSchedule().get(1).toInstant();

        // isolated junctions
        rvf.getJunctions().forEach(rvj -> this.reserveIsolatedJunction(rvj, beginning, ending));

        rvf.getMplsPipes().forEach(rmp -> {
            this.reserveMplsPipe(rmp, beginning, ending);
        });

        rvf.getEthPipes().forEach(rep -> {
            this.reserveEthPipe(rep, beginning, ending);
        });


        connRepo.save(conn);
        log.info("saved PSS resources, connection is now:");
        try {
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(conn);
            log.info(pretty);     // commented for output readability

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void reserveIsolatedJunction(ReservedVlanJunctionE rvj, Instant beginning, Instant ending) {

        UrnE device = topoService.device(rvj.getDeviceUrn());

        Set<ReservedPssResourceE> junctionResources = new HashSet<>();

        Integer vcId;
        switch (device.getDeviceModel()) {
            case JUNIPER_EX:
                // no further identifiers to reserve
                break;
            case JUNIPER_MX:
                vcId = this.reserveVcId(beginning, ending);
                junctionResources.add(makeVcIdResource(vcId, beginning, ending));
                log.info("reserved vcid "+ vcId);

                break;
            case ALCATEL_SR7750:
                vcId = this.reserveVcId(beginning, ending);
                junctionResources.add(makeVcIdResource(vcId, beginning, ending));
                log.info("reserved vcid "+ vcId);

                Integer sdpId = this.reserveSdpId(beginning, ending);
                junctionResources.add(makeSdpIdResource(sdpId, beginning, ending));
                log.info("reserved sdpId "+ sdpId);

                log.info("reserving Alcatel fixtures");
                rvj.getFixtures().forEach(f -> {

                    Integer inQosId = this.reserveQosId(rvj.getDeviceUrn(), ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);
                    Integer egQosId = this.reserveQosId(rvj.getDeviceUrn(), ResourceType.ALU_EGRESS_POLICY_ID, beginning, ending);

                    ReservedPssResourceE inQosIdRes = this.makeQosIdResource(rvj.getDeviceUrn(), inQosId, ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);
                    ReservedPssResourceE egQosIdRes = this.makeQosIdResource(rvj.getDeviceUrn(), egQosId, ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);

                    f.getReservedPssResources().add(inQosIdRes);
                    f.getReservedPssResources().add(egQosIdRes);
                });
                break;
        }
        rvj.getReservedPssResources().addAll(junctionResources);
    }

    private void reserveEthPipe(ReservedEthPipeE rep, Instant beginning, Instant ending) {

    }

    private void reserveMplsPipe(ReservedMplsPipeE rmp, Instant beginning, Instant ending) {
        Integer vcId = reserveVcId(beginning, ending);
        ReservedVlanJunctionE aj = rmp.getAJunction();
        ReservedVlanJunctionE zj = rmp.getZJunction();


        Set<ReservedVlanJunctionE> rvjs = new HashSet<>();
        rvjs.add(aj);
        rvjs.add(zj);

        for (ReservedVlanJunctionE rvj : rvjs) {
            // VCID always there
            rvj.getReservedPssResources().add(makeVcIdResource(vcId, beginning, ending));
            log.info("reserved vcid " + vcId + " in junction for " + rvj.getDeviceUrn());

            UrnE device = topoService.device(rvj.getDeviceUrn());
            switch (device.getDeviceModel()) {
                case JUNIPER_EX:
                    // no further identifiers to reserve
                    break;
                case JUNIPER_MX:
                    // no further identifiers to reserve
                    break;
                case ALCATEL_SR7750:
                    Integer sdpId = this.reserveSdpId(beginning, ending);
                    rvj.getReservedPssResources().add(makeSdpIdResource(sdpId, beginning, ending));
                    log.info("reserved sdpId " + sdpId + " in junction for " + rvj.getDeviceUrn());

                    log.info("reserving Alcatel fixtures (qos Ids etc) in junction for " + rvj.getDeviceUrn());
                    rvj.getFixtures().forEach(f -> {

                        Integer inQosId = this.reserveQosId(rvj.getDeviceUrn(), ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);
                        Integer egQosId = this.reserveQosId(rvj.getDeviceUrn(), ResourceType.ALU_EGRESS_POLICY_ID, beginning, ending);
                        log.info("qosIds : " + inQosId + " " + egQosId);

                        ReservedPssResourceE inQosIdRes = this.makeQosIdResource(rvj.getDeviceUrn(), inQosId, ResourceType.ALU_INGRESS_POLICY_ID, beginning, ending);
                        ReservedPssResourceE egQosIdRes = this.makeQosIdResource(rvj.getDeviceUrn(), egQosId, ResourceType.ALU_EGRESS_POLICY_ID, beginning, ending);

                        f.getReservedPssResources().add(inQosIdRes);
                        f.getReservedPssResources().add(egQosIdRes);
                    });
                    break;

            }
        }

    }


    // do the choosing

    private Integer reserveQosId(String deviceUrn, ResourceType rt, Instant beginning, Instant ending) {
        return 6000;
    }

    private Integer reserveSdpId(Instant beginning, Instant ending) {
        return 6000;
    }


    private Integer reserveVcId(Instant beginning, Instant ending) {
        return 6000;
    }


    // returns all overlappign reosurces of defined type
    private Set<ReservedPssResourceE> findOverlappingReservedIds(Instant beginning, Instant ending, ResourceType rt) {
        Set<ReservedPssResourceE> reservedResources = new HashSet<>();

        Optional<List<ReservedPssResourceE>> maybeResvResources = pssResRepo.findOverlappingInterval(beginning, ending);

        maybeResvResources.ifPresent(rprEs ->
                rprEs.stream().filter(r -> r.getResourceType().equals(rt)).forEach(reservedResources::add)
        );

        return reservedResources;
    }

    private Optional<Integer> chooseNewId(Integer floor, Integer ceiling, Set<Integer> reserved) {
        for (Integer i = floor; i <= ceiling; i++) {
            if (!reserved.contains(i)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }


    private ReservedPssResourceE makeQosIdResource(String deviceUrn, Integer qosId, ResourceType rt,
                                                   Instant beginning, Instant ending) {
        ReservedPssResourceE qosIdRes = ReservedPssResourceE.builder()
                .resource(qosId)
                .urn(deviceUrn)
                .resourceType(rt)
                .beginning(beginning)
                .ending(ending)
                .build();
        return qosIdRes;
    }

    private ReservedPssResourceE makeVcIdResource(Integer vcId,
                                                  Instant beginning, Instant ending) {

        ReservedPssResourceE vplsId = ReservedPssResourceE.builder()
                .resource(vcId)
                .urn(ResourceType.GLOBAL)
                .resourceType(ResourceType.VC_ID)
                .beginning(beginning)
                .ending(ending)
                .build();
        return vplsId;
    }

    private ReservedPssResourceE makeSdpIdResource(Integer sdpId,
                                                   Instant beginning, Instant ending) {
        ReservedPssResourceE sdpIdRes = ReservedPssResourceE.builder()
                .urn(ResourceType.GLOBAL)
                .resource(sdpId)
                .resourceType(ResourceType.ALU_SDP_ID)
                .beginning(beginning)
                .ending(ending)
                .build();

        return sdpIdRes;
    }


    public void release(ConnectionE conn) {

    }
}
