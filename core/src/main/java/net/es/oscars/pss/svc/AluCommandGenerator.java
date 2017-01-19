package net.es.oscars.pss.svc;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.cmd.*;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.pss.ent.UrnAddressE;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class AluCommandGenerator {

    @Autowired
    private Stringifier stringifier;

    @Autowired
    private Assembler assembler;

    @Autowired
    private UrnAddressRepository addrRepo;

    @Autowired
    private MiscHelper help;

    private Optional<Integer> aluJunctionSdpId(ReservedVlanJunctionE rvj) {
        return help.pssResourceOfType(rvj.getReservedPssResources(), ResourceType.ALU_SDP_ID);
    }


    private Optional<Integer> aluFixtureInQos(ReservedVlanFixtureE rvf) {
        return help.pssResourceOfType(rvf.getReservedPssResources(), ResourceType.ALU_INGRESS_POLICY_ID);

    }

    private Optional<Integer> aluFixtureEgQos(ReservedVlanFixtureE rvf) {
        return help.pssResourceOfType(rvf.getReservedPssResources(), ResourceType.ALU_EGRESS_POLICY_ID);
    }

    public String isolatedJunction(ReservedVlanJunctionE rvj, ConnectionE conn) throws PSSException {
        AluGenerationParams params = AluGenerationParams.builder()
                .applyQos(true)
                .lsps(new ArrayList<>())
                .qoses(new ArrayList<>())
                .paths(new ArrayList<>())
                .sdps(new ArrayList<>())
                .build();


        Optional<Integer> maybeVcId = help.junctionVcId(rvj);
        if (maybeVcId.isPresent()) {

            ArrayList<AluSap> saps = new ArrayList<>();
            for (ReservedVlanFixtureE rvf : rvj.getFixtures()) {
                AluFixtureParams fixtureParams = alcatelFixture(rvf, conn);
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

            String out = "";

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

    public AluFixtureParams alcatelFixture(ReservedVlanFixtureE rvf, ConnectionE conn) throws PSSException {

        Optional<Integer> maybeInQosId = aluFixtureInQos(rvf);
        Optional<Integer> maybeEgQosId = aluFixtureEgQos(rvf);
        if (maybeEgQosId.isPresent() && maybeInQosId.isPresent()) {

            List<AluSap> saps = new ArrayList<>();
            Integer inQosId = maybeInQosId.get();
            Integer egQosId = maybeEgQosId.get();
            String inDescription = "oscars-qos-in-"+conn.getConnectionId();
            String egDescription = "oscars-qos-eg-"+conn.getConnectionId();

            AluQos inQos = AluQos.builder().
                    type(AluQosType.SAP_INGRESS)
                    .description(inDescription)
                    .mbps(rvf.getReservedBandwidth().getInBandwidth())
                    .policing(Policing.STRICT)
                    .policyId(inQosId)
                    .policyName(inDescription)
                    .build();

            AluQos egQos = AluQos.builder().
                    type(AluQosType.SAP_EGRESS)
                    .description(egDescription)
                    .mbps(rvf.getReservedBandwidth().getEgBandwidth())
                    .policing(Policing.STRICT)
                    .policyId(egQosId)
                    .policyName(egDescription)
                    .build();



            for (ReservedVlanE vlan : rvf.getReservedVlans()) {
                String description = "oscars-sap-"+conn.getConnectionId();

                AluSap sap = AluSap.builder()
                        .description(description)
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

    public String ethPipe(ReservedVlanJunctionE rvj, ReservedEthPipeE rep, List<String> ero) throws PSSException {
        return "";

    }
    public String mplsPipe(ConnectionE conn, ReservedVlanJunctionE from, ReservedVlanJunctionE to,
                           ReservedMplsPipeE rmp, List<String> ero) throws PSSException {
        AluGenerationParams params = AluGenerationParams.builder()
                .applyQos(true)
                .lsps(new ArrayList<>())
                .qoses(new ArrayList<>())
                .paths(new ArrayList<>())
                .sdps(new ArrayList<>())
                .build();


        Optional<Integer> maybeVcId = help.junctionVcId(from);
        Optional<Integer> maybeSdpId = help.junctionSdpId(from);
        if (maybeVcId.isPresent() && maybeSdpId.isPresent()) {

            ArrayList<AluSap> saps = new ArrayList<>();
            for (ReservedVlanFixtureE rvf : from.getFixtures()) {
                AluFixtureParams fixtureParams = alcatelFixture(rvf, conn);
                saps.addAll(fixtureParams.getSaps());
                params.getQoses().add(fixtureParams.getInQos());
                params.getQoses().add(fixtureParams.getEgQos());
            }

            String description = "OSCARS "+conn.getConnectionId();
            String servicename = "oscars-svc-"+conn.getConnectionId();


            // TODO: multiple paths and whatnot? how?
            MplsPath path = help.mplsPathBuilder(conn, ero);
            params.getPaths().add(path);

            String toAddr;

            Optional<UrnAddressE> maybeAddr = addrRepo.findByUrn(to.getDeviceUrn());
            if (maybeAddr.isPresent()) {
                toAddr = maybeAddr.get().getIpv4Address();
                AluSdp sdp = AluSdp.builder()
                        .sdpId(maybeSdpId.get())
                        .lspName("oscars-lsp-"+conn.getConnectionId())
                        .farEnd(toAddr)
                        .description(description)
                        .build();
                params.getSdps().add(sdp);

                Lsp lsp = Lsp.builder()
                        .holdPriority(5)
                        .setupPriority(5)
                        .metric(65000)
                        .name("oscars-lsp-"+conn.getConnectionId())
                        .pathName(path.getName())
                        .to(toAddr)
                        .build();
                params.getLsps().add(lsp);

            } else {
                throw new PSSException("could not locate loopback address for "+to.getDeviceUrn());
            }


            AluVpls vpls = AluVpls.builder()
                    .description(description)
                    .vcId(maybeVcId.get())
                    .saps(saps)
                    .serviceName(servicename)
                    .build();

            String sdpTpl = "alu-sdp-setup";
            String pathTpl = "alu-mpls_path-setup";
            String lspTpl = "alu-mpls_lsp-setup";
            String qosTpl = "alu-qos-setup";
            String vplsServiceTpl = "alu-vpls_service-setup";
            String menderTemplate = "alu-top";
            params.setAluVpls(vpls);

            String out = "";

            Map<String, Object> root = new HashMap<>();

            List<String> fragments = new ArrayList<>();
            try {

                root.put("qosList", params.getQoses());
                root.put("apply", params.getApplyQos());
                String qosConfig = stringifier.stringify(root, qosTpl);
                fragments.add(qosConfig);


                root = new HashMap<>();
                root.put("paths", params.getPaths());
                String pathConfig = stringifier.stringify(root, pathTpl);
                fragments.add(pathConfig);

                root = new HashMap<>();
                root.put("lsps", params.getLsps());
                String lspConfig = stringifier.stringify(root, lspTpl);
                fragments.add(lspConfig);


                root = new HashMap<>();
                root.put("sdps", params.getSdps());
                String sdpConfig = stringifier.stringify(root, sdpTpl);
                fragments.add(sdpConfig);

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
            String msg = "";
            if (!maybeVcId.isPresent()) {
                msg += "no vc id! ";
            }
            if (!maybeSdpId.isPresent()) {
                msg += "no sdp id! ";

            }
            throw new PSSException(msg);
        }


    }



}
