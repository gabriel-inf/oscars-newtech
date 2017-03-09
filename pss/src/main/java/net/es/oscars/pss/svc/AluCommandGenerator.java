package net.es.oscars.pss.svc;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.params.alu.*;
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


    public String setup(AluParams params) throws ConfigException {
        AluVpls vpls = params.getAluVpls();


        String sdpTpl = "alu/alu-sdp-setup.ftl";
        String pathTpl = "alu/alu-mpls_path-setup.ftl";
        String lspTpl = "alu/alu-mpls_lsp-setup.ftl";
        String qosTpl = "alu/alu-qos-setup.ftl";
        String vplsServiceTpl = "alu/alu-vpls_service-setup.ftl";
        String menderTemplate = "alu/alu-top.ftl";
        params.setAluVpls(vpls);


        Map<String, Object> root = new HashMap<>();

        List<String> fragments = new ArrayList<>();

        try {
            this.verifyAluQosParams(params.getQoses(), params.getAluVpls().getSaps());

            if (params.getQoses() == null || params.getQoses().isEmpty()) {
                log.info("No QOS config (weird!)");
            } else {
                root.put("qosList", params.getQoses());
                root.put("apply", params.getApplyQos());
                String qosConfig = stringifier.stringify(root, qosTpl);
                fragments.add(qosConfig);

            }

            if (params.getPaths() == null || params.getPaths().isEmpty()) {
                log.info("No paths, skipping..");

            } else {
                root = new HashMap<>();
                root.put("paths", params.getPaths());
                String pathConfig = stringifier.stringify(root, pathTpl);
                fragments.add(pathConfig);

            }

            if (params.getLsps() == null || params.getLsps().isEmpty()) {
                log.info("No LSPs, skipping..");
            } else {
                root = new HashMap<>();
                root.put("lsps", params.getLsps());
                String lspConfig = stringifier.stringify(root, lspTpl);
                fragments.add(lspConfig);
            }

            if (params.getSdps() == null || params.getSdps().isEmpty()) {
                log.info("No SDPs, skipping..");
            } else {
                root = new HashMap<>();
                root.put("sdps", params.getSdps());
                String sdpConfig = stringifier.stringify(root, sdpTpl);
                fragments.add(sdpConfig);
            }


            root = new HashMap<>();
            root.put("vpls", params.getAluVpls());
            String vplsServiceConfig = stringifier.stringify(root, vplsServiceTpl);
            fragments.add(vplsServiceConfig);

            return assembler.assemble(fragments, menderTemplate);
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
            error = "defined ingress qos ids do not match SAP qos ids";
        }
        if (!sapEgQosIds.equals(egQosIds)) {
            ok = false;
            error += " defined egress qos ids do not match SAP qos ids";
        }
        if (!ok) {
            throw new ConfigException(error);
        }



    }



}
