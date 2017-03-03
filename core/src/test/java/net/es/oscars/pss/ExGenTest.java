package net.es.oscars.pss;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.pss.cmd.ExGenerationParams;
import net.es.oscars.pss.dao.TemplateRepository;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=CoreUnitTestConfiguration.class)
public class ExGenTest {
    @Autowired
    private Stringifier stringifier;

    @Autowired
    private Assembler assembler;

    @Autowired
    private ExParamsBuilder builder;

    @Autowired
    private TemplateRepository tpr;

    @Test
    public void genSetup() throws IOException, TemplateException {
        tpr.findAll();

        String vlanTemplate = "ex-vlan-setup";
        String menderTemplate = "ex-top";


        List<String> fragments = new ArrayList<>();
        ExGenerationParams params = builder.sampleParams();


        Map<String, Object> root = new HashMap<>();

        root.put("vlan", params.getExVlan());
        root.put("ifces", params.getIfces());
        String vplsServiceConfig = stringifier.stringify(root, vlanTemplate);
        fragments.add(vplsServiceConfig);




        String assembled = assembler.assemble(fragments, menderTemplate);

        log.info(assembled);

    }
}
