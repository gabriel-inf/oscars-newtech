package net.es.oscars.pss.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.pss.svc.CommandQueuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class CommandPopulator {

    private CommandQueuer queuer;
    @Value("${check.filename}")
    String devicesFile;

    @Autowired
    public void CommandPopulator(CommandQueuer queuer) {
        this.queuer = queuer;
    }

    @PostConstruct
    public void makeCommand() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(devicesFile);

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
            log.info("added a new command "+commandId);

        });
    }

}
