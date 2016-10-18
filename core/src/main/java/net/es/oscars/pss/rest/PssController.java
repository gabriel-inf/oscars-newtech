package net.es.oscars.pss.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.dao.RouterCommandsRepository;
import net.es.oscars.pss.dao.TemplateRepository;
import net.es.oscars.pss.ent.RouterCommandsE;
import net.es.oscars.pss.ent.TemplateE;
import net.es.oscars.dto.pss.RouterConfigTemplate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class PssController {
    @Autowired
    private TemplateRepository repository;

    private ModelMapper modelMapper = new ModelMapper();
    @Autowired
    private RouterCommandsRepository rcRepo;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @RequestMapping(value = "/pss/templates/", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getAll() {
        List<TemplateE> templateEs = repository.findAll();
        return templateEs.stream().map(TemplateE::getName).collect(Collectors.toList());
    }

    @RequestMapping(value = "/pss/templates/{name}", method = RequestMethod.GET)
    @ResponseBody
    public RouterConfigTemplate byName(@PathVariable("name") String name) {
        return convertToDto(repository.findByName(name).orElseThrow(NoSuchElementException::new));
    }

    private RouterConfigTemplate convertToDto(TemplateE templateE) {
        return modelMapper.map(templateE, RouterConfigTemplate.class);
    }

    @RequestMapping(value = "/pss/commands/{connectionId}/{deviceUrn}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> commands(@PathVariable("connectionId") String connectionId,
                                        @PathVariable("deviceUrn") String deviceUrn) {
        log.info("retrieving commands for "+connectionId+ " "+deviceUrn);
        RouterCommandsE rce = rcRepo
                .findByConnectionIdAndDeviceUrn(connectionId, deviceUrn)
                .orElseThrow(NoSuchElementException::new);
        Map<String, String>  result = new HashMap<>();
        result.put("commands", rce.getContents());
        return result;

    }


}
