package net.es.oscars.simplebwavail.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.*;
import net.es.oscars.simpleresv.svc.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class BandwidthAvailabilityGenerationService {
    @Autowired
    private DateService dateService;

    public BandwidthAvailabilityRequest generateBandwidthAvailabilityRequest(SimpleBandwidthAvailabilityRequest request){
        Date start = dateService.parseDate(request.getStartDate());
        Date end = dateService.parseDate(request.getEndDate());
        return BandwidthAvailabilityRequest.builder()
                .srcDevice(request.getSrcDevice())
                .srcPorts(request.getSrcPorts())
                .dstDevice(request.getDstDevice())
                .dstPorts(request.getDstPorts())
                .azEros(request.getAzEros())
                .zaEros(request.getZaEros())
                .minAzBandwidth(request.getMinAzBandwidth())
                .minZaBandwidth(request.getMinZaBandwidth())
                .numPaths(request.getNumPaths())
                .disjointPaths(request.getDisjointPaths())
                .startDate(start)
                .endDate(end)
                .build();
    }

    public PortBandwidthAvailabilityRequest generatePortBandwidthAvailabilityRequest(SimplePortBandwidthAvailabilityRequest request){
        Date start = dateService.parseDate(request.getStartDate());
        Date end = dateService.parseDate(request.getEndDate());
        return PortBandwidthAvailabilityRequest.builder()
                .startDate(start)
                .endDate(end)
                .build();
    }
}
