package net.es.oscars.pss.svc;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.params.alu.*;
import net.es.oscars.pss.beans.AluTemplatePaths;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
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

    public String teardown(AluParams params) throws ConfigException {
        this.verifyAluQosParams(params.getQoses(), params.getAluVpls().getSaps());
        AluTemplatePaths atp = AluTemplatePaths.builder()
                .lsp("alu/alu-mpls_lsp-teardown.ftl")
                .qos("alu/alu-qos-teardown.ftl")
                .sdp("alu/alu-sdp-teardown.ftl")
                .path("alu/alu-mpls_path-teardown.ftl")
                .vpls("alu/alu-vpls_service-teardown.ftl")
                .loopback("alu/alu-vpls_loopback-teardown.ftl")
                .build();
        return fill(atp, params, true);
    }

    public String setup(AluParams params) throws ConfigException {

        this.verifyAluQosParams(params.getQoses(), params.getAluVpls().getSaps());
        AluTemplatePaths atp = AluTemplatePaths.builder()
                .lsp("alu/alu-mpls_lsp-setup.ftl")
                .qos("alu/alu-qos-setup.ftl")
                .sdp("alu/alu-sdp-setup.ftl")
                .path("alu/alu-mpls_path-setup.ftl")
                .vpls("alu/alu-vpls_service-setup.ftl")
                .loopback("alu/alu-vpls_loopback-setup.ftl")
                .build();
        return fill(atp, params, false);
    }

    private String fill(AluTemplatePaths atp, AluParams params, boolean reverse) throws ConfigException {

        String top = "alu/alu-top.ftl";

        Map<String, Object> root = new HashMap<>();

        List<String> fragments = new ArrayList<>();

        try {

            if (params.getQoses() == null || params.getQoses().isEmpty()) {
                log.info("No QOS config (weird!)");
            } else {
                root.put("qosList", params.getQoses());
                root.put("apply", params.getApplyQos());
                String qosConfig = stringifier.stringify(root, atp.getQos());
                if (reverse) {
                    fragments.add(0, qosConfig);
                } else {
                    fragments.add(qosConfig);
                }
            }

            if (params.getPaths() == null || params.getPaths().isEmpty()) {
                log.info("No paths, skipping..");

            } else {
                root = new HashMap<>();
                root.put("paths", params.getPaths());
                String pathConfig = stringifier.stringify(root, atp.getPath());
                if (reverse) {
                    fragments.add(0, pathConfig);
                } else {
                    fragments.add(pathConfig);
                }
            }

            if (params.getLsps() == null || params.getLsps().isEmpty()) {
                log.info("No LSPs, skipping..");
            } else {
                root = new HashMap<>();
                root.put("lsps", params.getLsps());
                String lspConfig = stringifier.stringify(root, atp.getLsp());
                if (reverse) {
                    fragments.add(0, lspConfig);
                } else {
                    fragments.add(lspConfig);
                }
            }

            if (params.getSdps() == null || params.getSdps().isEmpty()) {
                log.info("No SDPs, skipping..");
            } else {
                root = new HashMap<>();
                root.put("sdps", params.getSdps());
                String sdpConfig = stringifier.stringify(root, atp.getSdp());
                if (reverse) {
                    fragments.add(0, sdpConfig);
                } else {
                    fragments.add(sdpConfig);
                }
            }

            if (params.getLoopbackInterface() == null) {
                log.info("No loopback, skipping..");
            } else {
                root = new HashMap<>();
                root.put("loopback_ifce_name", params.getLoopbackInterface());
                root.put("loopback_address", params.getLoopbackAddress());
                String loopbackCfg = stringifier.stringify(root, atp.getLoopback());
                if (reverse) {
                    fragments.add(0, loopbackCfg);
                } else {
                    fragments.add(loopbackCfg);
                }
            }


            root = new HashMap<>();
            root.put("vpls", params.getAluVpls());
            String vplsServiceConfig = stringifier.stringify(root, atp.getVpls());
            if (reverse) {
                fragments.add(0, vplsServiceConfig);
            } else {
                fragments.add(vplsServiceConfig);
            }
            return assembler.assemble(fragments, top);
        } catch (IOException | TemplateException ex) {
            log.error("templating error", ex);
            throw new ConfigException("template system error");
        }
    }


    private void verifyAluQosParams(List<AluQos> qoses, List<AluSap> saps) throws ConfigException {
        Set<Integer> sapInQosIds = new HashSet<>();
        Set<Integer> sapEgQosIds = new HashSet<>();
        saps.forEach(sap -> {
            if (sap.getIngressQosId() != null) {
                sapInQosIds.add(sap.getIngressQosId());
            }
            if (sap.getEgressQosId() != null) {
                sapEgQosIds.add(sap.getEgressQosId());
            }
        });
        Set<Integer> inQosIds = new HashSet<>();
        Set<Integer> egQosIds = new HashSet<>();
        for (AluQos qos : qoses) {
            if (qos.getPolicyId() == null) {
                throw new ConfigException("qos policy id missing");
            }
            if (qos.getType() == null) {
                throw new ConfigException("qos type missing");
            }
            if (qos.getType().equals(AluQosType.SAP_INGRESS)) {
                if (inQosIds.contains(qos.getPolicyId())) {
                    throw new ConfigException("duplicate ingress qos policy id");
                }
                inQosIds.add(qos.getPolicyId());
            } else {
                if (egQosIds.contains(qos.getPolicyId())) {
                    throw new ConfigException("duplicate egress qos policy id");
                }
                egQosIds.add(qos.getPolicyId());
            }
        }
        boolean ok = true;
        String error = "";
        if (!sapInQosIds.equals(inQosIds)) {
            ok = false;
            error = "Ingress qos id mismatch";
        }
        if (!sapEgQosIds.equals(egQosIds)) {
            ok = false;
            error += " Egress qos id mismatch";
        }
        if (!ok) {
            throw new ConfigException(error);
        }


    }


}
