package net.es.oscars.pss;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.cmd.AluGenerationParams;
import net.es.oscars.pss.dao.TemplateRepository;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(PssUnitTestConfiguration.class)
public class AluGenTest {
    @Autowired
    private Stringifier stringifier;

    @Autowired
    private Assembler assembler;

    @Autowired
    private AluParamsBuilder builder;

    @Autowired
    private TemplateRepository tpr;

    @Test
    public void genSetup() throws IOException, TemplateException {
        tpr.findAll();

        // TODO: externalize those / integrate with template param objects
        String qosTpl = "alu-qos-setup";
        String lspTpl = "alu-mpls_lsp-setup";

        String pathTpl= "alu-mpls_path-setup";
        String sdpTpl = "alu-sdp-setup";
        String vplsLoopbackTpl = "alu-vpls_loopback-setup";
        String vplsServiceTpl = "alu-vpls_service-setup";
        String menderTemplate = "alu-top";


        List<String> fragments = new ArrayList<>();
        AluGenerationParams params = builder.sampleParams();


        Map<String, Object> root = new HashMap<>();
        root.put("qosList", params.getQoses());
        root.put("protect", params.getAluVpls().getProtectVcId());
        root.put("apply", params.getApplyQos());
        String qosConfig = stringifier.stringify(root, qosTpl);
        fragments.add(qosConfig);


        root = new HashMap<>();
        root.put("lsps", params.getLsps());
        String lspConfig = stringifier.stringify(root, lspTpl);
        fragments.add(lspConfig);

        root = new HashMap<>();
        root.put("paths", params.getPaths());
        String pathConfig = stringifier.stringify(root,pathTpl);
        fragments.add(pathConfig);

        root = new HashMap<>();
        root.put("sdps", params.getSdps());
        String sdpConfig = stringifier.stringify(root, sdpTpl);
        fragments.add(sdpConfig);


        root = new HashMap<>();
        root.put("loopback_ifce_name", params.getLoopbackInterface());
        root.put("loopback_address", params.getLoopbackAddress());
        String loopbackConfig = stringifier.stringify(root, vplsLoopbackTpl);
        fragments.add(loopbackConfig);

        root = new HashMap<>();
        root.put("vpls", params.getAluVpls());
        String vplsServiceConfig = stringifier.stringify(root, vplsServiceTpl);
        fragments.add(vplsServiceConfig);




        String assembled = assembler.assemble(fragments, menderTemplate);

        log.info(assembled);

    }
}
