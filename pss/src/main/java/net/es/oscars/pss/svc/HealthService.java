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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HealthService {
    private ControlPlaneHealth health;

    public HealthService() {
        log.info("initialized health service");
        this.health = new ControlPlaneHealth();
        this.health.setDeviceStatus(new HashMap<>());
    }

    public ControlPlaneHealth getHealth() {
        return this.health;
    }

    public List<DeviceEntry> devicesToCheck(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(filename);
        return Arrays.asList(mapper.readValue(jsonFile, DeviceEntry[].class));

    }


    public Map<DeviceEntry, String> queueControlPlaneCheck(CommandQueuer queuer, String filename) throws IOException {
        Map<DeviceEntry, String> result = new HashMap<>();
        List<DeviceEntry> entries = this.devicesToCheck(filename);
        entries.forEach(e -> {
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
            result.put(e, commandId);
            log.info("added a new command " + commandId);

        });
        return result;
    }
}
