package net.es.oscars.pss.rtr;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.AbstractPssTest;
import net.es.oscars.pss.ctg.AluTests;
import net.es.oscars.pss.ctg.RouterTests;
import net.es.oscars.pss.help.PssTestConfig;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.help.ParamsLoader;
import net.es.oscars.pss.help.RouterTestSpec;
import net.es.oscars.pss.svc.CommandRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

@Slf4j
public class AluConfigTest extends AbstractPssTest {

    @Autowired
    private CommandRunner runner;
    @Autowired
    private ParamsLoader loader;
    @Autowired
    private RancidProps rancidProps;
    @Autowired
    private PssTestConfig pssTestConfig;

    @Before
    public void before() throws InterruptedException {
        System.out.println("==============================================================================");
        System.out.println("Ready to run setup / teardown tests! These WILL attempt to configure routers.");
        System.out.println("Make sure you have configured test.properties correctly. ");
        System.out.println("Starting in 3 seconds. Ctrl-C to abort.");
        System.out.println("==============================================================================");
        Thread.sleep(3000);
        rancidProps.setExecute(true);

    }

    @Test
    @Category({RouterTests.class, AluTests.class})
    public void singleAluTest() throws IOException {
        log.info("starting single ALU test");
        String prefix = pssTestConfig.getCaseDirectory();


        RouterTestSpec setupRts = loader.loadSpec(prefix + "/setup-alu-single.json");


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

        RouterTestSpec tdRts = loader.loadSpec(prefix + "/teardown-alu-single.json");


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

    @Test
    @Category({RouterTests.class, AluTests.class})
    public void twoAluTest() throws IOException, InterruptedException {
        String prefix = pssTestConfig.getCaseDirectory();


        log.info("starting two ALU test: setup");
        List<String> setups = new ArrayList<>();
        setups.add(prefix + "/setup-2_a-z.json");
        setups.add(prefix + "/setup-2_z-a.json");

        for (String setup : setups) {
            RouterTestSpec setupRts = loader.loadSpec(setup);
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


        }
        Thread.sleep(10000);

        List<String> teardowns = new ArrayList<>();
        teardowns.add(prefix + "/teardown-2_a-z.json");
        teardowns.add(prefix + "/teardown-2_z-a.json");

        for (String teardown : teardowns) {
            RouterTestSpec setupRts = loader.loadSpec(teardown);

            Command setupCmd = Command.builder()
                    .device(setupRts.getDevice())
                    .model(setupRts.getModel())
                    .type(CommandType.TEARDOWN)
                    .alu(setupRts.getAluParams())
                    .mx(setupRts.getMxParams())
                    .ex(setupRts.getExParams())
                    .build();
            CommandStatus setupStatus = CommandStatus.builder()
                    .configStatus(ConfigStatus.SUBMITTING)
                    .lifecycleStatus(LifecycleStatus.PROCESSING)
                    .device(setupRts.getDevice())
                    .type(CommandType.TEARDOWN)
                    .lastUpdated(new Date())
                    .commands("")
                    .output("")
                    .build();

            runner.run(setupStatus, setupCmd);

        }
    }

}
