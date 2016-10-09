package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.viz.VizGraph;
import net.es.oscars.webui.viz.VizExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@Controller
public class VizController {

    @Autowired
    private VizExporter vizExporter;

    @RequestMapping(value = "/graphs/{classifier}", method = RequestMethod.GET)
    @ResponseBody
    public VizGraph viz_for(@PathVariable String classifier) {
        if (classifier.equals("multilayer")) {
            return vizExporter.multilayerGraph();

        } else {
            throw new NoSuchElementException("bad classifier " + classifier);
        }
    }
    @RequestMapping("/viz")
    public String viz(Model model) {
        return "viz";
    }

}