package net.es.oscars.oscarsapi;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Slf4j
@Controller
public class BandwidthController {

    @Autowired
    private RestTemplate restTemplate;

    private final String oscarsUrl = "https://localhost:8000";

    public BandwidthAvailabilityResponse getBandwidthPath(@ModelAttribute BandwidthAvailabilityRequest request) {
        log.info("Submitting bandwidth availability query for path(s).");

        String restPath = oscarsUrl + "/bwavail/path";
        BandwidthAvailabilityResponse response;
        try {
            response = restTemplate.postForObject(restPath, request, BandwidthAvailabilityResponse.class);
        } catch (Exception e){
            log.info("Communication with OSCARS failed");
            log.info(e.getMessage());
            log.info("Returning an empty map");
            response = handlePathQueryException();
        }
        return response;
    }

    public PortBandwidthAvailabilityResponse getBandwidthPorts(@ModelAttribute PortBandwidthAvailabilityRequest request) {
        log.info("Submitting bandwidth availability query for all ports.");

        String restPath = oscarsUrl + "/bwavail/ports";
        PortBandwidthAvailabilityResponse response;
        try {
            response = restTemplate.postForObject(restPath, request, PortBandwidthAvailabilityResponse.class);
        } catch (Exception e){
            log.info("Communication with OSCARS failed");
            log.info(e.getMessage());
            log.info("Returning an empty map");
            response = handlePortQueryException();
        }
        return response;
    }

    public BandwidthAvailabilityResponse handlePathQueryException(){
        return BandwidthAvailabilityResponse.builder()
                .pathNameMap(new HashMap<>())
                .pathPairMap(new HashMap<>())
                .bwAvailabilityMap(new HashMap<>())
                .minAvailableBwMap(new HashMap<>())
                .build();
    }

    public PortBandwidthAvailabilityResponse handlePortQueryException(){
        return PortBandwidthAvailabilityResponse.builder()
                .bwAvailabilityMap(new HashMap<>())
                .build();
    }
}
