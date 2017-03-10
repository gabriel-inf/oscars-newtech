package net.es.oscars.pss.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.dao.RouterCommandsRepository;
import net.es.oscars.pss.ent.RouterCommandsE;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Controller
public class PssController {

    @Autowired
    private RouterCommandsRepository rcRepo;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }


    @RequestMapping(value = "/pss/commands/{connectionId}/{deviceUrn}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> commands(@PathVariable("connectionId") String connectionId,
                                        @PathVariable("deviceUrn") String deviceUrn) {
        log.info("retrieving commands for " + connectionId + " " + deviceUrn);
        Map<String, String> result = new HashMap<>();

        Optional<RouterCommandsE> maybeRce = rcRepo.findByConnectionIdAndDeviceUrn(connectionId, deviceUrn);
        if (maybeRce.isPresent()) {
            result.put("commands", maybeRce.get().getContents());

        } else {
            result.put("commands", "");

        }
        return result;

    }


}
