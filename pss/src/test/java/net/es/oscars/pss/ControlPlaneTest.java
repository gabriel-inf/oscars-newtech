package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.st.ControlPlaneStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.svc.CommandQueuer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(locations = "file:config/test/application.properties")
public class ControlPlaneTest {

    @Autowired
    private CommandQueuer queuer;


    @Before
    public void before() throws InterruptedException {
        System.out.println("==============================================================================");
        System.out.println("Ready to run integration tests! These WILL attempt to configure routers.");
        System.out.println("Make sure you have configured test.properties correctly. ");
        System.out.println("Starting in 3 seconds. Ctrl-C to abort.");
        System.out.println("==============================================================================");
        Thread.sleep(3000);
    }

    @Test
    @Category(Integrations.class)
    public void basicTest() throws NoSuchElementException, ControlPlaneException, InterruptedException {
        Map<String, DeviceModel> devices = new HashMap<>();
        devices.put("nersc-asw1", DeviceModel.JUNIPER_EX);
        devices.put("nersc-tb1", DeviceModel.ALCATEL_SR7750);

        Map<String, ControlPlaneStatus> statusMap = new HashMap<>();
        Set<String> commandIds = new HashSet<>();

        devices.entrySet().forEach(e -> {
            Command cmd = Command.builder()
                    .device(e.getKey())
                    .model(e.getValue())
                    .type(CommandType.CONTROL_PLANE_STATUS)
                    .build();

            String commandId = queuer.newCommand(cmd);
            commandIds.add(commandId);

        });
        Set<String> waitingFor = new HashSet<>();
        waitingFor.addAll(devices.keySet());

        int totalMs = 0;
        while (waitingFor.size() > 0 && totalMs < 60000) {
            Thread.sleep(2000);
            totalMs += 2000;
            waitingFor.clear();
            for (String commandId : commandIds) {
                log.debug("checking status for routerConfig " + commandId);
                CommandStatus status = queuer.getStatus(commandId).orElseThrow(NoSuchElementException::new);
                if (status.getLifecycleStatus().equals(LifecycleStatus.COMPLETED)) {
                    ControlPlaneStatus st = status.getControlPlaneStatus();
                    log.debug("control plane status for " + status.getDevice() + " : " + st);
                    statusMap.put(status.getDevice(), st);
                } else {
                    log.debug(status.getDevice() + " still waiting for completion");
                    waitingFor.add(status.getDevice());
                }
            }
        }
        if (waitingFor.size() > 0) {
            log.error("timed out waiting for some devices");
            throw new ControlPlaneException("timed out");

        }


        log.debug("done collecting statuses");

        for (String device : statusMap.keySet()) {
            ControlPlaneStatus st = statusMap.get(device);
            if (!st.equals(ControlPlaneStatus.VERIFIED)) {
                throw new ControlPlaneException("Could not verify " + device);
            }

        }

    }

}
