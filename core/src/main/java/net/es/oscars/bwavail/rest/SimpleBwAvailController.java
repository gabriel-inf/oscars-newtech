package net.es.oscars.bwavail.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.*;
import net.es.oscars.bwavail.svc.BandwidthAvailabilityGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Provides a simplified endpoint for submitting bandwidth availability requests.
 */
@Slf4j
@Controller
public class SimpleBwAvailController {

    @Autowired
    BandwidthAvailabilityController bwAvailController;

    @Autowired
    BandwidthAvailabilityGenerationService bwAvailGenService;

    @RequestMapping(value = "/simple_bwavail/path", method = RequestMethod.POST)
    @ResponseBody
    public BandwidthAvailabilityResponse getPathAvailability(@RequestBody SimpleBandwidthAvailabilityRequest request) {
        BandwidthAvailabilityRequest bwAvailRequest = bwAvailGenService.generateBandwidthAvailabilityRequest(request);
        return bwAvailController.getPathAvailability(bwAvailRequest);
    }

    @RequestMapping(value = "/simple_bwavail/ports", method = RequestMethod.POST)
    @ResponseBody
    public PortBandwidthAvailabilityResponse getAllPortAvailability(@RequestBody SimplePortBandwidthAvailabilityRequest request) {
        PortBandwidthAvailabilityRequest bwAvailRequest = bwAvailGenService.generatePortBandwidthAvailabilityRequest(request);
        return bwAvailController.getAllPortAvailability(bwAvailRequest);
    }
}
