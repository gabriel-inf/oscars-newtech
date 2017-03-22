package net.es.oscars.pss.svc;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.rancid.RancidArguments;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class RancidRunner {
    private RancidProps props;

    @Autowired
    public RancidRunner(RancidProps props) {
        this.props = props;
    }

    public void runRancid(RancidArguments arguments)
            throws ControlPlaneException, IOException, InterruptedException, TimeoutException {

        File temp = File.createTempFile("oscars-routerConfig-", ".tmp");

        log.info("routerConfig: " + arguments.getRouterConfig());

        FileUtils.writeStringToFile(temp, arguments.getRouterConfig());
        String tmpPath = temp.getAbsolutePath();
        log.info("created temp file" + tmpPath);
        String host = props.getHost();
        String cloginrc = props.getCloginrc();

        if (host.equals("localhost")) {
            String[] rancidCliArgs = {
                    arguments.getExecutable(),
                    "-x", tmpPath,
                    "-f", cloginrc,
                    arguments.getRouter()
            };

            // run local rancid
            new ProcessExecutor().command(rancidCliArgs)
                    .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(getClass().getName() + ".rancid"))
                            .asInfo()).execute();

        } else {

            String remotePath = "/tmp/" + temp.getName();
            String scpTo = host + ":" + remotePath;

            // scp the file to remote host: /tmp/
            try {
                log.debug("SCPing: " + tmpPath + " -> " + scpTo);
                new ProcessExecutor().command("scp", tmpPath, scpTo)
                        .exitValues(0)
                        .execute();

                // run remote rancid..
                new ProcessExecutor().command("ssh",
                        host, arguments.getExecutable(),
                        "-x", remotePath,
                        "-f", cloginrc,
                        arguments.getRouter())
                        .exitValue(0)
                        .redirectOutput(Slf4jStream.of(LoggerFactory.getLogger(getClass().getName() + ".rancid"))
                                .asInfo()).execute();



                log.debug("deleting: " + scpTo);

                String remoteDelete = "rm " + remotePath;
                new ProcessExecutor().command("ssh", host, remoteDelete)
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
