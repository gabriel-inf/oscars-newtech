package net.es.oscars.core.pss.alu;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.pss.AluGenerationParams;
import net.es.oscars.core.pss.util.Mender;
import net.es.oscars.core.pss.util.Stringifier;
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
        root.put("protect", params.getAluVpls().getProtectVcId().isPresent());
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




        String mended = mender.mend(fragments, menderTemplate);

        log.info(mended);
        return mended;

    }


}
