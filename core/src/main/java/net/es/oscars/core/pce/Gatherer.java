package net.es.oscars.core.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.resv.ResourceType;
import net.es.oscars.dto.resv.Interval;
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


    private Set<TopoResource> applyingTo(String urn, Collection<TopoResource> allResources) {
        return allResources.stream().filter(t -> t.getTopoVertexUrns().contains(urn)).collect(Collectors.toSet());
    }

    private Boolean bandwidthFits(Integer bandwidth, String urn, Collection<TopoResource> allResources) {
        Set<TopoResource> applyingTo = this.applyingTo(urn, allResources);
        if (applyingTo.isEmpty()) {
            return true;
        } else {
            boolean fits = true;
            for (TopoResource tr : applyingTo) {
                List<ReservableQty> bwQties = tr.getReservableQties().stream()
                        .filter(t -> t.getType().equals(ResourceType.BANDWIDTH)).collect(Collectors.toList());

                boolean fitsOnThis = bwQties.isEmpty() || bwQties.stream().filter(q -> q.getRange().contains(bandwidth)).findAny().isPresent();

                if (!fitsOnThis) {
                    fits = false;
                }
            }
            return fits;


        }

    }
}
