package net.es.oscars.bwavail.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.bwavail.svc.BandwidthAvailabilityService;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Provides bandwidth availability information for given paths or parts of the network.
 */
@Slf4j
@Controller
public class BandwidthAvailabilityController {

    @Autowired
    private BandwidthAvailabilityService bwAvailService;

    @RequestMapping(value = "/bwavail/path", method = RequestMethod.POST)
    @ResponseBody
    public BandwidthAvailabilityResponse getPathAvailability(@RequestBody BandwidthAvailabilityRequest request) {
        log.info("Retrieving Bandwidth Availability for Path(s).");
        log.info("Request Details: " + request.toString());

        BandwidthAvailabilityResponse response = bwAvailService.getBandwidthAvailabilityMap(request);
        //log.info("Response Details: " + response.toString());
        return response;
    }

    @RequestMapping(value = "/bwavail/ports", method = RequestMethod.POST)
    @ResponseBody
    public PortBandwidthAvailabilityResponse getAllPortAvailability(@RequestBody PortBandwidthAvailabilityRequest bwRequest)
    {
        log.info("Retrieving Bandwidth Availability for all Network Ports");
        log.info("Request Details: " + bwRequest.toString());

        PortBandwidthAvailabilityResponse response = bwAvailService.getBandwidthAvailabilityOnAllPorts(bwRequest);
        //log.info("Response Details: " + response.toString());
        return response;
    }
}
