package net.es.oscars.whatif.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.whatif.dto.WhatifResponse;
import net.es.oscars.whatif.dto.WhatifSpecification;
import net.es.oscars.whatif.svc.SuggestionService;
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

    SuggestionService suggestionService;

    @Autowired
    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @RequestMapping(value = "/whatif/suggestion/volume", method = RequestMethod.POST)
    @ResponseBody
    public WhatifResponse submitSpec(@RequestBody WhatifSpecification spec){
        System.out.println("Processing volume request: " + spec.toString());
        return WhatifResponse.builder().connections(suggestionService.generateSuggestions(spec)).build();
    }
}
