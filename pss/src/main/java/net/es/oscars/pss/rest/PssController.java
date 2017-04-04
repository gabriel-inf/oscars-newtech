package net.es.oscars.pss.rest;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.GenerateResponse;
import net.es.oscars.dto.pss.cp.ControlPlaneHealth;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.svc.HealthService;
import net.es.oscars.pss.svc.RouterConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
public class PssController {

    private HealthService healthService;
    private RouterConfigBuilder routerConfigBuilder;

    @Autowired
    public PssController(HealthService healthService, RouterConfigBuilder routerConfigBuilder) {
        this.healthService = healthService;
        this.routerConfigBuilder = routerConfigBuilder;
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }


    @RequestMapping(value = "/command", method = RequestMethod.POST)
    public CommandResponse command(@RequestBody Command cmd) {
        log.info("received a command, connId: " + cmd.getConnectionId() + " device: " + cmd.getDevice());

        return CommandResponse.builder().build();

    }

    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public GenerateResponse generate(@RequestBody Command cmd) throws ConfigException {
        log.info("generating router configs");
        String generated = routerConfigBuilder.generate(cmd);
        return GenerateResponse.builder()
                .connectionId(cmd.getConnectionId())
                .device(cmd.getDevice())
                .commandType(cmd.getType())
                .generated(generated)
                .build();
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public CommandStatus commandStatus(@PathVariable("commandId") String commandId) {
        return CommandStatus.builder().build();
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ControlPlaneHealth health() {
        return healthService.getHealth();
    }




}
