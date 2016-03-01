package net.es.oscars.ds.topo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.ds.topo.dao.DevGroupRepository;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@Controller
public class TopoController {

    @Autowired
    private DevGroupRepository devGrpRepo;

    @Autowired
    private ModelMapper modelMapper;


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



    @RequestMapping(value = "/topo/layer/{layer}", method = RequestMethod.GET)
    @ResponseBody
    public Topology groupDevs(@PathVariable("layer") String layer) {

        log.info("topology for layer " + layer);
        Layer l_enum = Layer.get(layer);

        Topology topo = new Topology();
        topo.setLayer(l_enum);
        TopoVertex a = new TopoVertex("alpha");
        TopoVertex b = new TopoVertex("beta");
        TopoEdge ab = new TopoEdge(a, b);
        topo.getVertices().add(a);
        topo.getVertices().add(b);
        topo.getEdges().add(ab);

        return topo;
    }


}