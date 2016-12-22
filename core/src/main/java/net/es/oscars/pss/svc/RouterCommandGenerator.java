package net.es.oscars.pss.svc;

import freemarker.template.TemplateException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.Interval;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.rsrc.ReservablePssResource;
import net.es.oscars.dto.spec.ReservedPssResource;
import net.es.oscars.dto.spec.ReservedVlan;
import net.es.oscars.dto.spec.ReservedVlanFixture;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.cmd.*;
import net.es.oscars.pss.dao.RouterCommandsRepository;
import net.es.oscars.pss.dao.TemplateRepository;
import net.es.oscars.pss.ent.RouterCommandsE;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
import net.es.oscars.resv.dao.ConnectionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
public class RouterCommandGenerator {

    @Autowired
    private RouterCommandsRepository rcRepo;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private TopoService topoService;

    @Autowired
    private Stringifier stringifier;

    @Autowired
    private Assembler assembler;

    @Autowired
    private TemplateRepository tpr;


    public void generateConfig(ConnectionE conn) throws PSSException {
        log.info("generating config");
        conn.getStates().setProv(ProvState.GENERATING);
        connRepo.save(conn);
        ReservedVlanFlowE rvf = conn.getReserved().getVlanFlow();

        Set<ReservedVlanJunctionE> rvj_set = new HashSet<>();
        rvj_set.addAll(rvf.getJunctions());
        Map<String, List<String>> isolatedJunctionCommands = isolatedJunctionCommands(rvj_set);

        Map<String, List<String>> ethCommands = new HashMap<>();
        Map<String, List<String>> mplsCommands = new HashMap<>();
        for (ReservedEthPipeE rep : rvf.getEthPipes()) {
            log.info("generating config for eth pipe " + rep.getId());
            Map<String, List<String>> thisPipeCommands = this.ethPipeCommands(rep);
            thisPipeCommands.forEach((urn, commands) -> {
                if (!ethCommands.keySet().contains(urn)) {
                    ethCommands.put(urn, new ArrayList<>());
                }
                ethCommands.get(urn).addAll(commands);

            });

        }

        for (ReservedMplsPipeE rep : rvf.getMplsPipes()) {
            log.info("generating config for mpls pipe " + rep.getId());
            Map<String, List<String>> thisPipeCommands = this.mplsPipeCommands(rep);
            thisPipeCommands.forEach((urn, commands) -> {
                if (!mplsCommands.keySet().contains(urn)) {
                    mplsCommands.put(urn, new ArrayList<>());
                }
                mplsCommands.get(urn).addAll(commands);

            });
        }
        Map<String, List<String>> commandsByDevice = new HashMap<>();

        isolatedJunctionCommands.forEach((urn, commands) -> {
            log.info("adding isolated junction commands for " + urn);
            commandsByDevice.put(urn, commands);

        });

        ethCommands.forEach((urn, commands) -> {
            log.info("adding ethernet commands for " + urn);
            if (!commandsByDevice.keySet().contains(urn)) {
                commandsByDevice.put(urn, new ArrayList<>());
            }
            commandsByDevice.get(urn).addAll(commands);
        });

        mplsCommands.forEach((urn, commands) -> {
            log.info("adding mpls commands for " + urn);
            if (!commandsByDevice.keySet().contains(urn)) {
                commandsByDevice.put(urn, new ArrayList<>());
            }
            commandsByDevice.get(urn).addAll(commands);
        });

        for (String deviceUrn : commandsByDevice.keySet()) {
            List<String> commands = commandsByDevice.get(deviceUrn);
            RouterCommandsE rc_e = RouterCommandsE.builder()
                    .connectionId(conn.getConnectionId())
                    .deviceUrn(deviceUrn)
                    .contents(String.join("\n", commands))
                    .build();

            rcRepo.save(rc_e);
            log.info("saved router commands: " + rc_e.toString());
        }


        conn.getStates().setProv(ProvState.DISMANTLED_AUTO);
        connRepo.save(conn);
        log.info(" done generating config");
    }

    private Map<String, List<String>> isolatedJunctionCommands(Collection<ReservedVlanJunctionE> rvj_set)
            throws PSSException {
        Map<String, List<String>> result = new HashMap<>();
        for (ReservedVlanJunctionE rvj : rvj_set) {
            String deviceUrn = rvj.getDeviceUrn();
            log.info("generating junction commands for " + deviceUrn);
            if (!result.containsKey(deviceUrn)) {
                result.put(deviceUrn, new ArrayList<>());
            }
            UrnE device = topoService.device(rvj.getDeviceUrn());
            switch (device.getDeviceModel()) {
                case ALCATEL_SR7750:
                    result.get(deviceUrn).add(alcatelIsolatedJunction(rvj));
                    break;
                case JUNIPER_EX:
                    result.get(deviceUrn).add(juniperExJunction(rvj));
                    break;
                case JUNIPER_MX:
                    result.get(deviceUrn).add(juniperMxJunction(rvj));

                    break;
            }
        }
        return result;

    }


    private Map<String, List<String>> ethPipeCommands(ReservedEthPipeE rep) throws PSSException {
        Set<ReservedVlanJunctionE> rvjs = new HashSet<>();
        rvjs.add(rep.getAJunction());
        rvjs.add(rep.getZJunction());
        // TODO: make this correct
        return isolatedJunctionCommands(rvjs);
    }

    private Map<String, List<String>> mplsPipeCommands(ReservedMplsPipeE rep) throws PSSException {
        Set<ReservedVlanJunctionE> rvjs = new HashSet<>();
        rvjs.add(rep.getAJunction());
        rvjs.add(rep.getZJunction());
        // TODO: make this correct
        return isolatedJunctionCommands(rvjs);
    }

    private String alcatelIsolatedJunction(ReservedVlanJunctionE rvj) throws PSSException {
        AluGenerationParams params = AluGenerationParams.builder()
                .applyQos(true)
                .lsps(new ArrayList<>())
                .qoses(new ArrayList<>())
                .paths(new ArrayList<>())
                .sdps(new ArrayList<>())
                .build();


        Optional<Integer> maybeVcId = junctionVcId(rvj);
        if (maybeVcId.isPresent()) {

            ArrayList<AluSap> saps = new ArrayList<>();
            for (ReservedVlanFixtureE rvf : rvj.getFixtures()) {
                AluFixtureParams fixtureParams = alcatelFixture(rvf);
                saps.addAll(fixtureParams.getSaps());
                params.getQoses().add(fixtureParams.getInQos());
                params.getQoses().add(fixtureParams.getEgQos());
            }


            AluVpls vpls = AluVpls.builder()
                    .description("description")
                    .vcId(maybeVcId.get())
                    .saps(saps)
                    .serviceName("service name")
                    .build();

            String qosTpl = "alu-qos-setup";
            String vplsServiceTpl = "alu-vpls_service-setup";
            String menderTemplate = "alu-top";
            params.setAluVpls(vpls);


            // TODO: no samples
            String out = "Alcatel router config for junction ; " + rvj.getDeviceUrn() + " \n";

            List<String> fragments = new ArrayList<>();
            try {

                Map<String, Object> root = new HashMap<>();
                root.put("qosList", params.getQoses());
                root.put("apply", params.getApplyQos());
                String qosConfig = stringifier.stringify(root, qosTpl);
                fragments.add(qosConfig);


                root = new HashMap<>();
                root.put("vpls", params.getAluVpls());
                String vplsServiceConfig = stringifier.stringify(root, vplsServiceTpl);
                fragments.add(vplsServiceConfig);

                String assembled = assembler.assemble(fragments, menderTemplate);

                return out + assembled;
            } catch (IOException ex) {
                log.error("IOException!", ex);
                throw  new PSSException("error with IO");

            } catch (TemplateException ex) {
                log.error("TemplateException!", ex);
                throw  new PSSException("error with template");

            }


        } else {
            throw new PSSException("no vc id!");
        }

    }

    private AluFixtureParams alcatelFixture(ReservedVlanFixtureE rvf) throws PSSException {

        Optional<Integer> maybeInQosId = aluFixtureInQos(rvf);
        Optional<Integer> maybeEgQosId = aluFixtureEgQos(rvf);
        if (maybeEgQosId.isPresent() && maybeInQosId.isPresent()) {

            List<AluSap> saps = new ArrayList<>();
            Integer inQosId = maybeInQosId.get();
            Integer egQosId = maybeEgQosId.get();

            AluQos inQos = AluQos.builder().
                    type(AluQosType.SAP_INGRESS)
                    .description("in_description")
                    .mbps(rvf.getReservedBandwidth().getInBandwidth())
                    .policing(Policing.STRICT)
                    .policyId(inQosId)
                    .policyName("in_name")
                    .build();

            AluQos egQos = AluQos.builder().
                    type(AluQosType.SAP_EGRESS)
                    .description("eg_description")
                    .mbps(rvf.getReservedBandwidth().getEgBandwidth())
                    .policing(Policing.STRICT)
                    .policyId(egQosId)
                    .policyName("eg_name")
                    .build();



            for (ReservedVlanE vlan : rvf.getReservedVlans()) {

                AluSap sap = AluSap.builder()
                        .description("desc")
                        .vlan(vlan.getVlan())
                        .port(rvf.getIfceUrn())
                        .egressQosId(egQosId)
                        .ingressQosId(inQosId)
                        .build();
                saps.add(sap);
            }
            AluFixtureParams params = AluFixtureParams.builder()
                    .inQos(inQos)
                    .egQos(egQos)
                    .saps(saps)
                    .build();
            return params;


        } else {
            throw new PSSException("no in / eg qos id!");
        }

    }


    private String juniperMxJunction(ReservedVlanJunctionE rvj) {
        // TODO: no samples
        String out = "Sample Juniper MX router config for junction ; " + rvj.getDeviceUrn() + " \n";
        for (ReservedVlanFixtureE f : rvj.getFixtures()) {
            out += f.getIfceUrn();
            out += "  bw : " + f.getReservedBandwidth().getInBandwidth() + " / " + f.getReservedBandwidth().getEgBandwidth() + "\n";
            out += "  vlan : " + vlanString(f);
        }
        return out;
    }

    private String juniperExJunction(ReservedVlanJunctionE rvj) {
        // TODO: no samples
        String out = "Sample Juniper EX switch config for junction ; " + rvj.getDeviceUrn() + " \n";
        for (ReservedVlanFixtureE f : rvj.getFixtures()) {
            out += f.getIfceUrn();
            out += "  bw : " + f.getReservedBandwidth().getInBandwidth() + " / " + f.getReservedBandwidth().getEgBandwidth() + "\n";
            out += "  vlan : " + vlanString(f);
        }
        return out;
    }

    private String vlanString(ReservedVlanFixtureE f) {
        List<String> vlans = f.getReservedVlans()
                .stream()
                .map(ReservedVlanE::getVlan)
                .map(Object::toString)
                .collect(Collectors.toList());
        return String.join(",", vlans);
    }

    private Optional<Integer> aluJunctionSdpId(ReservedVlanJunctionE rvj) {
        return pssResourceOfType(rvj.getReservedPssResources(), ResourceType.ALU_SDP_ID);
    }


    private Optional<Integer> aluFixtureInQos(ReservedVlanFixtureE rvf) {
        return pssResourceOfType(rvf.getReservedPssResources(), ResourceType.ALU_INGRESS_POLICY_ID);

    }

    private Optional<Integer> aluFixtureEgQos(ReservedVlanFixtureE rvf) {
        return pssResourceOfType(rvf.getReservedPssResources(), ResourceType.ALU_EGRESS_POLICY_ID);
    }

    private Optional<Integer> junctionVcId(ReservedVlanJunctionE rvj) {
        return pssResourceOfType(rvj.getReservedPssResources(), ResourceType.VC_ID);
    }


    private Optional<Integer> pssResourceOfType(Set<ReservedPssResourceE> resources, ResourceType rt) {
        Optional<Integer> resource = Optional.empty();

        for (ReservedPssResourceE rps : resources) {
            if (rps.getResourceType().equals(rt)) {
                resource = Optional.of(rps.getResource());
            }
        }
        return resource;


    }


}
