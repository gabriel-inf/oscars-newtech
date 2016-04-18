package net.es.oscars.spec;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.authnz.ent.EUser;
import net.es.oscars.dto.auth.Permissions;
import net.es.oscars.dto.auth.User;
import net.es.oscars.dto.spec.Specification;
import net.es.oscars.spec.dao.SpecificationRepository;
import net.es.oscars.spec.ent.SpecificationE;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@Controller
public class SpecController {

    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private SpecificationRepository specRepo;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @RequestMapping(value = "/spec/add", method = RequestMethod.POST)
    @ResponseBody
    public Specification add(@RequestBody Specification dtoSpec) {
        return convertToDto(specRepo.save(convertToEnt(dtoSpec)));
    }

    @RequestMapping(value = "/spec/get/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Specification byUsername(@PathVariable("id") Long id) {
        return convertToDto(specRepo.findById(id).orElseThrow(NoSuchElementException::new));
    }


    private SpecificationE convertToEnt(Specification dtoSpec) {
        SpecificationE specE = modelMapper.map(dtoSpec, SpecificationE.class);
        return specE;
    }

    private Specification convertToDto(SpecificationE specE) {
        Specification dtoSpec = modelMapper.map(specE, Specification.class);
        return dtoSpec;
    }
}