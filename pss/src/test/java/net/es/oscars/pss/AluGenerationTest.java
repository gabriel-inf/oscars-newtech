package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.svc.AluCommandGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(locations = "file:config/test.properties")
public class AluGenerationTest {

    @Autowired
    private AluParamsBuilder builder;

    @Autowired
    private AluCommandGenerator commandGen;


    @Test
    @Category(UnitTests.class)
    public void generateAluConfig() throws ConfigException {
        AluParams params = builder.sampleParams();

        String config = commandGen.setup(params);

        log.info("config generated: \n"+config);

    }

}
