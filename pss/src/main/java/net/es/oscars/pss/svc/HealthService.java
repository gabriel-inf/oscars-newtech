package net.es.oscars.pss.svc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.cp.ControlPlaneHealth;
import net.es.oscars.pss.beans.DeviceEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class HealthService {

    private ControlPlaneHealth health = new ControlPlaneHealth();

    public ControlPlaneHealth getHealth() {
        return this.health;
    }

    public void queueControlPlaneCheck(CommandQueuer queuer, String filename) {

        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(filename);
        try {
            List<DeviceEntry> devicesToCheck = Arrays.asList(mapper.readValue(jsonFile, DeviceEntry[].class));

            devicesToCheck.forEach(e -> {
                Command cmd = Command.builder()
                        .device(e.getDevice())
                        .model(e.getModel())
                        .type(CommandType.CONTROL_PLANE_STATUS)
                        .connectionId(null)
                        .refresh(false)
                        .ex(null)
                        .mx(null)
                        .alu(null)
                        .build();

                String commandId = queuer.newCommand(cmd);
                log.info("added a new command " + commandId);

            });
        } catch (IOException ex) {
            log.error("IO error, ex");
        }

    }
}
