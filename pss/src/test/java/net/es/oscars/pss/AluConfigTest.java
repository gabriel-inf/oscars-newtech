package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.pss.st.ControlPlaneStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.spec.RouterTestSpec;
import net.es.oscars.pss.svc.CommandQueuer;
import net.es.oscars.pss.svc.CommandRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(locations = "file:config/test/application.properties")
public class AluConfigTest {

    @Autowired
    private CommandRunner runner;
    @Autowired
    private ParamsLoader loader;

    @Before
    public void before() throws InterruptedException {
        System.out.println("==============================================================================");
        System.out.println("Ready to run setup / teardown tests! These WILL attempt to configure routers.");
        System.out.println("Make sure you have configured test.properties correctly. ");
        System.out.println("Starting in 3 seconds. Ctrl-C to abort.");
        System.out.println("==============================================================================");
        Thread.sleep(3000);
    }

    @Test
    @Category(Integrations.class)
    public void singleAluTest() throws IOException {
        log.info("starting single ALU test");


        RouterTestSpec setupRts = loader.loadSpec("./config/test/testbed/setup-alu-single.json");


        Command setupCmd = Command.builder()
                .device(setupRts.getDevice())
                .model(setupRts.getModel())
                .type(CommandType.SETUP)
                .alu(setupRts.getAluParams())
                .mx(setupRts.getMxParams())
                .ex(setupRts.getExParams())
                .build();
        CommandStatus setupStatus = CommandStatus.builder()
                .configStatus(ConfigStatus.SUBMITTING)
                .lifecycleStatus(LifecycleStatus.PROCESSING)
                .device(setupRts.getDevice())
                .type(CommandType.SETUP)
                .lastUpdated(new Date())
                .commands("")
                .output("")

                .build();

        runner.run(setupStatus, setupCmd);

        RouterTestSpec tdRts = loader.loadSpec("./config/test/testbed/teardown-alu-single.json");


        Command tdCmd = Command.builder()
                .device(tdRts.getDevice())
                .model(tdRts.getModel())
                .type(CommandType.TEARDOWN)
                .alu(tdRts.getAluParams())
                .mx(tdRts.getMxParams())
                .ex(tdRts.getExParams())
                .build();
        CommandStatus tdStatus = CommandStatus.builder()
                .type(CommandType.TEARDOWN)
                .configStatus(ConfigStatus.SUBMITTING)
                .device(tdRts.getDevice())
                .commands("")
                .output("")
                .lastUpdated(new Date())
                .lifecycleStatus(LifecycleStatus.PROCESSING)
                .build();

        runner.run(tdStatus, tdCmd);


    }

}
