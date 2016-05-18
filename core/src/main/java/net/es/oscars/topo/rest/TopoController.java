package net.es.oscars.topo.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.topo.ent.*;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Controller
public class TopoController {
    @Autowired
    private TopoService svc;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn(ex.getMessage(), ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn(ex.getMessage(), ex);
    }



    @RequestMapping(value = "/topo/vlanEdges", method = RequestMethod.GET)
    @ResponseBody
    public List<String> vlanEdges() {
        log.info("getting vlan edges");
        return svc.edges(Layer.ETHERNET);
    }

    @RequestMapping(value = "/topo/devices", method = RequestMethod.GET)
    @ResponseBody
    public List<String> devices() {
        log.info("getting devices");
        return svc.devices();
    }


}