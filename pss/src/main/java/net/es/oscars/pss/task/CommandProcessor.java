package net.es.oscars.pss.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.svc.CommandQueuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CommandProcessor {
    private CommandQueuer queuer;

    @Autowired
    public CommandProcessor(CommandQueuer queuer) {
        this.queuer = queuer;
    }

    @Scheduled(fixedDelay = 1000)
    public void processsCommands() throws InterruptedException {
        queuer.ofLifecycleStatus(LifecycleStatus.WAITING).entrySet().forEach(e -> {
            String commandId = e.getKey();
            CommandStatus status = e.getValue();
            log.info("processing a command with id "+commandId);
            status.setLifecycleStatus(LifecycleStatus.PROCESSING);


        });

    }



}
