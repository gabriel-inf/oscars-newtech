package net.es.oscars.pss.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cp.ControlPlaneHealth;
import net.es.oscars.pss.svc.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
public class PssController {

    private HealthService healthService;

    @Autowired
    public PssController(HealthService healthService) {
        this.healthService = healthService;
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }


    @RequestMapping(value = "/command", method = RequestMethod.POST)
    public CommandResponse command(@RequestBody Command cmd) {
        log.info("received a command, connId: " + cmd.getConnectionId() + " device: " + cmd.getDevice());

        CommandResponse response = CommandResponse.builder().build();

        return response;

    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public CommandStatus commandStatus(@PathVariable("commandId") String commandId) {
        CommandStatus result = CommandStatus.builder().build();
        return result;
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ControlPlaneHealth health() {
        return healthService.getHealth();
    }




}
