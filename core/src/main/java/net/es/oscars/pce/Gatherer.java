package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.Interval;
import net.es.oscars.dto.resv.ReservedResource;
import net.es.oscars.dto.resv.ReservedResponse;
import net.es.oscars.dto.rsrc.ReservableQty;
import net.es.oscars.dto.rsrc.TopoResource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Gatherer {

    public List<ReservedResource> gatherReserved(Interval interval, RestTemplate restTemplate) {


        log.info("getting reserved");
        // gather reserved resources
        String reservedRestPath = "https://localhost:8000/queryReserved";

        Instant beginning = interval.getBeginning();
        Instant ending = interval.getEnding();

        URI targetUrl = UriComponentsBuilder.fromUriString(reservedRestPath)
                .queryParam("beginning", beginning)
                .queryParam("ending", ending)
                .build()
                .toUri();

        log.info("target url:  \"" + targetUrl + "\"");

        ReservedResponse rr = restTemplate.getForObject(targetUrl, ReservedResponse.class);
        rr.getReservedResources().stream().forEach(t -> {
            log.info(t.toString());
        });

        return rr.getReservedResources();
    }

    public List<TopoResource> gatherConstraining(RestTemplate restTemplate) {

        // gather constraining resources
        String constrainingRestPath = "https://localhost:8000/constraining";
        List<TopoResource> topoResources = Arrays.asList(restTemplate.getForObject(constrainingRestPath, TopoResource[].class));

        topoResources.stream().forEach(t -> {
            log.info(t.toString());
        });

        return topoResources;

    }

}
