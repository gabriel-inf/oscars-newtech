package net.es.oscars.simplebwavail.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.*;
import net.es.oscars.oscarsapi.BandwidthController;
import net.es.oscars.simplebwavail.svc.BandwidthAvailabilityGenerationService;
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
    BandwidthController bandwidthController;

    @Autowired
    BandwidthAvailabilityGenerationService bwAvailGenService;

    @RequestMapping(value = "/whatif/bwavail/path", method = RequestMethod.POST)
    @ResponseBody
    public BandwidthAvailabilityResponse getPathAvailability(@RequestBody SimpleBandwidthAvailabilityRequest request) {
        BandwidthAvailabilityRequest bwAvailRequest = bwAvailGenService.generateBandwidthAvailabilityRequest(request);
        return bandwidthController.getBandwidthPath(bwAvailRequest);
    }

    @RequestMapping(value = "/whatif/bwavail/ports", method = RequestMethod.POST)
    @ResponseBody
    public PortBandwidthAvailabilityResponse getAllPortAvailability(@RequestBody SimplePortBandwidthAvailabilityRequest request) {
        PortBandwidthAvailabilityRequest bwAvailRequest = bwAvailGenService.generatePortBandwidthAvailabilityRequest(request);
        return bandwidthController.getBandwidthPorts(bwAvailRequest);
    }
}
