package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.*;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.beans.ConfigResult;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.beans.ControlPlaneResult;
import net.es.oscars.pss.rancid.RancidArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class CommandRunner {
    private RouterConfigBuilder builder;
    private RancidRunner rancidRunner;

    @Autowired
    public CommandRunner(RancidRunner rancidRunner, RouterConfigBuilder builder) {
        this.rancidRunner = rancidRunner;
        this.builder = builder;
    }

    public void run(CommandStatus status, Command command) {
        try {

            switch (command.getType()) {
                case CONFIG_STATUS:
                    break;
                case OPERATIONAL_STATUS:
                    break;
                case CONTROL_PLANE_STATUS:
                    ControlPlaneResult res = cplStatus(command.getDevice(), command.getModel());
                    status.setControlPlaneStatus(res.getStatus());
                    break;
                case SETUP:
                    status.setConfigStatus(ConfigStatus.SUBMITTING);
                    ConfigResult confRes = setup(command, status);
                    status.setConfigStatus(confRes.getStatus());
                    break;
                case TEARDOWN:
                    break;

            }
        } catch (ControlPlaneException ex) {
            log.error("CPE", ex);
            status.setControlPlaneStatus(ControlPlaneStatus.UNKNOWN);
        }
    }

    private ConfigResult setup(Command command, CommandStatus status) {

        ConfigResult result = ConfigResult.builder().build();

        try {
            RancidArguments args = builder.setup(command);
            rancidRunner.runRancid(args);
            result.setStatus(ConfigStatus.VERIFIED);

        } catch (IOException | InterruptedException | TimeoutException | ControlPlaneException | ConfigException ex) {
            log.error("Rancid error", ex);
            result.setStatus(ConfigStatus.FAILED);

        }
        return result;

    }


    private ControlPlaneResult cplStatus(String device, DeviceModel model) throws ControlPlaneException {

        ControlPlaneResult result = ControlPlaneResult.builder().build();

        try {
            RancidArguments args = builder.controlPlaneCheck(device, model);
            rancidRunner.runRancid(args);
            result.setStatus(ControlPlaneStatus.VERIFIED);

        } catch (IOException | InterruptedException | TimeoutException | ControlPlaneException | ConfigException ex) {
            log.error("Rancid error", ex);
            result.setStatus(ControlPlaneStatus.FAILED);
        }
        return result;

    }


}
