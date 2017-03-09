package net.es.oscars.pss.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
public class PssController {


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }


    @RequestMapping(value = "/pss/command", method = RequestMethod.POST)
    public CommandResponse command(@RequestBody Command cmd) {
        log.info("received a command, connId: " + cmd.getConnectionId() + " device: " + cmd.getDevice());

        CommandResponse response = CommandResponse.builder().build();

        return response;

    }

    @RequestMapping(value = "/pss/status", method = RequestMethod.GET)
    public CommandStatus commandStatus(@PathVariable("commandId") String commandId) {
        CommandStatus result = CommandStatus.builder().build();
        return result;
    }



}
