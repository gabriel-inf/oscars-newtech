package net.es.oscars.pss.svc;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.dto.pss.params.alu.AluVpls;
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


    public String vplsPipe(AluParams params) throws IOException, TemplateException {
        AluVpls vpls = params.getAluVpls();


        String sdpTpl = "alu-sdp-setup";
        String pathTpl = "alu-mpls_path-setup";
        String lspTpl = "alu-mpls_lsp-setup";
        String qosTpl = "alu-qos-setup";
        String vplsServiceTpl = "alu-vpls_service-setup";
        String menderTemplate = "alu-top";
        params.setAluVpls(vpls);


        Map<String, Object> root = new HashMap<>();

        List<String> fragments = new ArrayList<>();


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

        return assembler.assemble(fragments, menderTemplate);

    }


}
