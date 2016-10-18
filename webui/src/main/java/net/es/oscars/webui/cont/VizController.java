package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.viz.VizGraph;
import net.es.oscars.webui.viz.VizExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.NoSuchElementException;

@Slf4j
@Controller
public class VizController {

    @Autowired
    private VizExporter vizExporter;
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/viz/topology/{classifier}", method = RequestMethod.GET)
    @ResponseBody
    public VizGraph viz_topology(@PathVariable String classifier) {
        if (classifier.equals("multilayer")) {
            return vizExporter.multilayerGraph();

        } else {
            throw new NoSuchElementException("bad classifier " + classifier);
        }
    }


    @RequestMapping(value = "/viz/connection/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public VizGraph viz_connection(@PathVariable String connectionId) {
        String restPath = "https://localhost:8000/resv/get/" + connectionId;

        Connection conn = restTemplate.getForObject(restPath, Connection.class);


        return vizExporter.connection(conn);
    }
}