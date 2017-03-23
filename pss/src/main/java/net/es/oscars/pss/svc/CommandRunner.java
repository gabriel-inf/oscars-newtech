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
    private HealthService healthService;

    @Autowired
    public CommandRunner(RancidRunner rancidRunner, RouterConfigBuilder builder, HealthService healthService) {
        this.rancidRunner = rancidRunner;
        this.builder = builder;
        this.healthService = healthService;
    }

    public void run(CommandStatus status, Command command) {
        ConfigResult confRes;
        RancidArguments args;
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
                    args = builder.setup(command);
                    confRes = configure(args);
                    status.setConfigStatus(confRes.getStatus());
                    break;
                case TEARDOWN:
                    status.setConfigStatus(ConfigStatus.SUBMITTING);
                    args = builder.teardown(command);
                    confRes = configure(args);
                    status.setConfigStatus(confRes.getStatus());
                    break;

            }
        } catch (ControlPlaneException | ConfigException ex) {
            log.error("error", ex);
            status.setControlPlaneStatus(ControlPlaneStatus.UNKNOWN);
        }
    }

    private ConfigResult configure(RancidArguments args) {

        ConfigResult result = ConfigResult.builder().build();

        try {
            rancidRunner.runRancid(args);
            result.setStatus(ConfigStatus.VERIFIED);

        } catch (IOException | InterruptedException | TimeoutException | ControlPlaneException ex) {
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
            healthService.getHealth().getDeviceStatus().put(device, ControlPlaneStatus.VERIFIED);
            result.setStatus(ControlPlaneStatus.VERIFIED);

        } catch (IOException | InterruptedException | TimeoutException | ControlPlaneException | ConfigException ex) {
            log.error("Rancid error", ex);
            healthService.getHealth().getDeviceStatus().put(device, ControlPlaneStatus.FAILED);
            result.setStatus(ControlPlaneStatus.FAILED);
        }
        return result;

    }


}
