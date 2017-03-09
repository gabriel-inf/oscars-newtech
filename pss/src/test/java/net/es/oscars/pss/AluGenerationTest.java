package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.spec.RouterTestSpec;
import net.es.oscars.pss.svc.AluCommandGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(locations = "file:config/test/application.properties")
public class AluGenerationTest {

    @Autowired
    private ParamsLoader loader;

    @Autowired
    private AluCommandGenerator commandGen;


    @Test
    @Category(UnitTests.class)
    public void makeAluConfig() throws ConfigException, IOException {

        List<RouterTestSpec> setupSpecs = loader.loadSpecs(CommandType.SETUP);

        for (RouterTestSpec spec : setupSpecs) {
            if (spec.getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                if (!spec.getShouldFail()) {
                    String config = commandGen.setup(spec.getAluParams());
                    log.info("config generated: \n" + config);
                }
            }
        }
    }

    @Test(expected = ConfigException.class)
    @Category(UnitTests.class)
    public void failToMakeAluConfig() throws ConfigException, IOException {

        List<RouterTestSpec> setupSpecs = loader.loadSpecs(CommandType.SETUP);

        for (RouterTestSpec spec : setupSpecs) {
            if (spec.getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                if (spec.getShouldFail()) {
                    commandGen.setup(spec.getAluParams());
                }
            }
        }

    }
}
