package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.*;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.beans.ControlPlaneResult;
import net.es.oscars.pss.prop.PssConfig;
import net.es.oscars.pss.rancid.RancidArguments;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class CommandRunner {
    private PssConfig config;
    private ControlPlaneChecker controlPlaneChecker;

    @Autowired
    public CommandRunner(PssConfig config, ControlPlaneChecker controlPlaneChecker) {
        this.config = config;
        this.controlPlaneChecker = controlPlaneChecker;
    }
    public void run(CommandStatus status, Command command) {
        try {

            switch (command.getType()) {
                case CONFIG_STATUS:
                    break;
                case OPERATIONAL_STATUS:
                    break;
                case CONTROL_PLANE_STATUS:
                    this.cplStatus(status, command);
                    break;
                case SETUP:
                    break;
                case TEARDOWN:
                    break;

            }
        } catch (ControlPlaneException ex) {
            log.error("CPE", ex);
            status.setControlPlaneStatus(ControlPlaneStatus.UNKNOWN);
        }
    }

    private void cplStatus(CommandStatus status, Command command) throws ControlPlaneException {
        ControlPlaneResult res;
        switch (command.getModel()) {
            case ALCATEL_SR7750:
                res = cplStatus(command.getDevice(), command.getModel());
                status.setControlPlaneStatus(res.getStatus());
                break;
            case JUNIPER_EX:
                res = cplStatus(command.getDevice(), command.getModel());
                status.setControlPlaneStatus(res.getStatus());
                break;
            case JUNIPER_MX:
                res = cplStatus(command.getDevice(), command.getModel());
                status.setControlPlaneStatus(res.getStatus());
                break;
            default:
                throw new ControlPlaneException("unknown model");
        }
    }

    private ControlPlaneResult cplStatus(String device, DeviceModel model) throws ControlPlaneException {
        RancidArguments args = controlPlaneChecker.controlPlaneCheck(device, model);
        ControlPlaneResult result = ControlPlaneResult.builder().build();

        try {
            runRancid(args);
            result.setStatus(ControlPlaneStatus.VERIFIED);

        } catch (IOException ex) {
            log.error("IO error", ex);
            result.setStatus(ControlPlaneStatus.FAILED);

        } catch (InterruptedException ex) {
            log.error("Interrupted", ex);
            result.setStatus(ControlPlaneStatus.FAILED);

        } catch (TimeoutException ex) {
            log.error("Timeout", ex);
            result.setStatus(ControlPlaneStatus.FAILED);

        } catch (ControlPlaneException ex) {
            log.error("Rancid error", ex);
            result.setStatus(ControlPlaneStatus.FAILED);
        }
        return result;

    }

    private void runRancid(RancidArguments arguments)
            throws ControlPlaneException, IOException, InterruptedException, TimeoutException {

        File temp = File.createTempFile("oscars-command-", ".tmp");

        log.info("command: " + arguments.getCommand());

        FileUtils.writeStringToFile(temp, arguments.getCommand());
        String tmpPath = temp.getAbsolutePath();
        log.info("created temp file" + tmpPath);

        if (config.getRancidHost().equals("localhost")) {
            String[] rancidCliArgs = {
                    arguments.getExecutable(),
                    "-x", tmpPath,
                    "-f", config.getCloginrc(),
                    arguments.getRouter()
            };

            // run local rancid
            new ProcessExecutor().command(rancidCliArgs)
                    .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(getClass().getName() + ".rancid"))
                            .asInfo()).execute();

        } else {

            String remotePath = "/tmp/" + temp.getName();
            String scpTo = config.getRancidHost() + ":" + remotePath;

            // scp the file to remote host: /tmp/
            try {
                log.debug("SCPing: " + tmpPath + " -> " + scpTo);
                new ProcessExecutor().command("scp", tmpPath, scpTo)
                        .exitValues(0)
                        .execute();

                // run remote rancid..
                new ProcessExecutor().command("ssh",
                        config.getRancidHost(), arguments.getExecutable(),
                        "-x", remotePath,
                        "-f", config.getCloginrc(),
                        arguments.getRouter())
                        .exitValue(0)
                        .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(getClass().getName() + ".rancid"))
                                .asInfo()).execute();



                log.debug("deleting: " + scpTo);

                String remoteDelete = "rm " + remotePath;
                new ProcessExecutor().command("ssh", config.getRancidHost(), remoteDelete)
                        .exitValue(0).execute();


            } catch (InvalidExitValueException ex) {
                throw new ControlPlaneException("error running Rancid!");

            } finally {

                FileUtils.deleteQuietly(temp);
            }

        }
        // delete local file
        FileUtils.deleteQuietly(temp);

    }


}
