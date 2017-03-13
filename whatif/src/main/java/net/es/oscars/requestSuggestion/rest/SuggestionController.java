package net.es.oscars.requestSuggestion.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.CircuitSpecification;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ReservationDetails;
import net.es.oscars.requestSuggestion.dto.VolumeRequestSpecification;
import net.es.oscars.requestSuggestion.svc.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
public class SuggestionController {

    @Autowired
    SuggestionService suggestionService;

    @RequestMapping(value = "/whatif/suggestion/volume", method = RequestMethod.POST)
    @ResponseBody
    public List<Connection> submitSpec(@RequestBody VolumeRequestSpecification spec){
        System.out.println("Processing volume request: " + spec.toString());
        return suggestionService.generateSuggestions(spec);
    }
}
