package net.es.oscars.ui.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.viz.Position;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.ui.pop.UIPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Slf4j
@Controller
public class UIController {
    @Autowired
    private UIPopulator populator;

    @RequestMapping(value = "/ui/positions", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Position> ui_positions() {
        log.info("getting ui default positions");
        return populator.getPositions();
    }

}