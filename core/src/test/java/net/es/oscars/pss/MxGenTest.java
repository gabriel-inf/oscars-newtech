package net.es.oscars.pss;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.pss.cmd.AluGenerationParams;
import net.es.oscars.pss.cmd.MxGenerationParams;
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
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
public class MxGenTest {
    @Autowired
    private Stringifier stringifier;

    @Autowired
    private Assembler assembler;

    @Autowired
    private MxParamsBuilder builder;

    @Autowired
    private TemplateRepository tpr;

    @Test
    public void genSetup() throws IOException, TemplateException {
        tpr.findAll();

        String vplsServiceTpl = "mx-vpls_service-setup";
        String menderTemplate = "mx-top";


        List<String> fragments = new ArrayList<>();
        MxGenerationParams params = builder.sampleParams();


        Map<String, Object> root = new HashMap<>();

        root.put("vpls", params.getMxVpls());
        String vplsServiceConfig = stringifier.stringify(root, vplsServiceTpl);
        fragments.add(vplsServiceConfig);




        String assembled = assembler.assemble(fragments, menderTemplate);

        log.info(assembled);

    }
}
