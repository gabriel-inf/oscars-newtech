package net.es.oscars.ds.spec;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.spec.dao.SpecificationRepository;
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
    @Autowired
    private ModelMapper modelMapper;

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


}