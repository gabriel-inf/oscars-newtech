package net.es.oscars.pss.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.spec.ReservedBlueprint;
import net.es.oscars.dto.spec.ReservedVlanFlow;
import net.es.oscars.dto.spec.ReservedVlanJunction;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
import net.es.oscars.resv.dao.ConnectionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
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

    public void generateConfig(ConnectionE conn) throws PSSException {
        rcg.generateConfig(conn);
    }

    public void reserve(ConnectionE conn) {
        log.info("starting PSS resource reservation");
        ReservedVlanFlowE rvf = conn.getReserved().getVlanFlow();
        Set<ReservedVlanJunctionE> rvj_set = new HashSet<>();
        rvj_set.addAll(rvf.getJunctions());
        rvf.getMplsPipes().forEach(mp -> {
            rvj_set.add(mp.getAJunction());
            rvj_set.add(mp.getZJunction());
        });
        rvf.getEthPipes().forEach(mp -> {
            rvj_set.add(mp.getAJunction());
            rvj_set.add(mp.getZJunction());
        });
        reservePssJunction(rvj_set,
                conn.getReservedSchedule().get(0).toInstant(),
                conn.getReservedSchedule().get(1).toInstant());
        connRepo.save(conn);
        log.info("saved PSS resources, connection is now:");
        try {
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(conn);
            log.info(pretty);     // commented for output readability

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void reservePssJunction(Set<ReservedVlanJunctionE> rvjs,
                                    Instant beginning, Instant ending) {
        if (rvjs == null) {
            return;
        }
        for (ReservedVlanJunctionE rvj : rvjs) {
            UrnE device = topoService.device(rvj.getDeviceUrn());
            Set<ReservedPssResourceE> junctionResources = new HashSet<>();
            switch (device.getDeviceModel()) {
                case ALCATEL_SR7750:
                    junctionResources = reserveJuniperVplsBase(device, beginning, ending);
                    rvj.getFixtures().forEach(f -> {
                        UrnE fixtureUrn = topoService.getUrn(f.getIfceUrn());
                        f.getReservedPssResources().addAll(reserveAlcatelFixture(fixtureUrn, beginning, ending));
                    });

                    break;
                case JUNIPER_EX:
                    // no further identifiers to reserve
                    break;
                case JUNIPER_MX:
                    junctionResources = reserveAlcatelVplsBase(device, beginning, ending);
                    break;
            }
            rvj.getReservedPssResources().addAll(junctionResources);

        }


    }

    // TODO: decide correct identifier, don't hardcode

    private Set<ReservedPssResourceE> reserveJuniperVplsBase(UrnE device, Instant beginning, Instant ending) {
        Set<ReservedPssResourceE> resources = new HashSet<>();
        ReservedPssResourceE vplsId = ReservedPssResourceE.builder()
                .resource(6000)
                .urn(device.getUrn())
                .resourceType(ResourceType.VC_ID)
                .beginning(beginning)
                .ending(ending)
                .build();
        resources.add(vplsId);
        return resources;
    }

    private Set<ReservedPssResourceE> reserveAlcatelFixture(UrnE port, Instant beginning, Instant ending) {
        Set<ReservedPssResourceE> resources = new HashSet<>();
        ReservedPssResourceE inQosId = ReservedPssResourceE.builder()
                .resource(6000)
                .urn(port.getUrn())
                .resourceType(ResourceType.ALU_INGRESS_POLICY_ID)
                .beginning(beginning)
                .ending(ending)
                .build();
        ReservedPssResourceE egQosId = ReservedPssResourceE.builder()
                .resource(6000)
                .urn(port.getUrn())
                .resourceType(ResourceType.ALU_EGRESS_POLICY_ID)
                .beginning(beginning)
                .ending(ending)
                .build();
        resources.add(inQosId);
        resources.add(egQosId);
        return resources;
    }

    private Set<ReservedPssResourceE> reserveAlcatelVplsBase(UrnE device, Instant beginning, Instant ending) {
        Set<ReservedPssResourceE> resources = new HashSet<>();
        ReservedPssResourceE vplsId = ReservedPssResourceE.builder()
                .resource(6000)
                .urn(device.getUrn())
                .resourceType(ResourceType.VC_ID)
                .beginning(beginning)
                .ending(ending)
                .build();
        ReservedPssResourceE sdpId = ReservedPssResourceE.builder()
                .urn(device.getUrn())
                .resource(6000)
                .resourceType(ResourceType.ALU_SDP_ID)
                .beginning(beginning)
                .ending(ending)
                .build();
        resources.add(vplsId);
        resources.add(sdpId);
        return resources;
    }


    public void release(ConnectionE conn) {

    }
}
