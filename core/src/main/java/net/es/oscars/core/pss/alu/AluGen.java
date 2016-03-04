package net.es.oscars.core.pss.alu;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.core.pss.ftl.AluGenerationParams;
import net.es.oscars.core.pss.ftl.util.Mender;
import net.es.oscars.core.pss.ftl.util.Stringifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AluGen {
    @Autowired
    private Stringifier stringifier;

    @Autowired
    private Mender mender;

    @Autowired
    private AluParamsBuilder builder;

    @PostConstruct
    public String genSetup() throws IOException, TemplateException {

        // TODO: externalize those / integrate with template param objects
        String templateDir = "./config/templates";
        String qosTpl = "alu-qos-setup.ftl";
        String lspTpl = "alu-mpls_lsp-setup.ftl";

        String pathTpl= "alu-mpls_path-setup.ftl";
        String sdpTpl = "alu-sdp-setup.ftl";
        String vplsLoopbackTpl = "alu-vpls_loopback-setup.ftl";
        String vplsServiceTpl = "alu-vpls_service-setup.ftl";


        List<String> fragments = new ArrayList<>();
        AluGenerationParams params = builder.sampleParams();


        Map<String, Object> root = new HashMap<>();
        root.put("qosList", params.getQoses());
        root.put("protect", params.getAluVpls().getProtectVcId().isPresent());
        root.put("apply", params.getApplyQos());
        String qosConfig = stringifier.stringify(root, templateDir, qosTpl);
        fragments.add(qosConfig);


        root = new HashMap<>();
        root.put("lsps", params.getLsps());
        String lspConfig = stringifier.stringify(root, templateDir, lspTpl);
        fragments.add(lspConfig);

        root = new HashMap<>();
        root.put("paths", params.getPaths());
        String pathConfig = stringifier.stringify(root, templateDir, pathTpl);
        fragments.add(pathConfig);

        root = new HashMap<>();
        root.put("sdps", params.getSdps());
        String sdpConfig = stringifier.stringify(root, templateDir, sdpTpl);
        fragments.add(sdpConfig);


        root = new HashMap<>();
        root.put("loopback_ifce_name", params.getLoopbackInterface());
        root.put("loopback_address", params.getLoopbackAddress());
        String loopbackConfig = stringifier.stringify(root, templateDir, vplsLoopbackTpl);
        fragments.add(loopbackConfig);

        root = new HashMap<>();
        root.put("vpls", params.getAluVpls());
        String vplsServiceConfig = stringifier.stringify(root, templateDir, vplsServiceTpl);
        fragments.add(vplsServiceConfig);




        String menderTemplate = "alu-top.ftl";
        String mended = mender.mend(fragments, templateDir, menderTemplate);

        log.info(mended);
        return mended;

    }


}
