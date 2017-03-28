package net.es.oscars.bwavail.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.*;
import net.es.oscars.resv.svc.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class BandwidthAvailabilityGenerationService {
    @Autowired
    private DateService dateService;

    public BandwidthAvailabilityRequest generateBandwidthAvailabilityRequest(SimpleBandwidthAvailabilityRequest request){
        Date start = dateService.parseDate(request.getStartDate());
        Date end = dateService.parseDate(request.getEndDate());
        return generateBandwidthAvailabilityRequest(request.getSrcDevice(), request.getSrcPorts(), request.getDstDevice(),
                request.getDstPorts(), request.getAzEros(), request.getZaEros(), request.getMinAzBandwidth(),
                request.getMinZaBandwidth(), request.getNumPaths(), request.getDisjointPaths(), start, end);
    }

    public BandwidthAvailabilityRequest generateBandwidthAvailabilityRequest(String srcDevice, Set<String> srcPorts,
                                                                             String dstDevice, Set<String> dstPorts,
                                                                             List<List<String>> azEROs, List<List<String>> zaEros,
                                                                             Integer minAzBandwidth, Integer minZaBandwidth,
                                                                             Integer numPaths, Boolean disjointPaths,
                                                                             Date start, Date end){
        return BandwidthAvailabilityRequest.builder()
                .srcDevice(srcDevice)
                .srcPorts(srcPorts)
                .dstDevice(dstDevice)
                .dstPorts(dstPorts)
                .azEros(azEROs)
                .zaEros(zaEros)
                .minAzBandwidth(minAzBandwidth)
                .minZaBandwidth(minZaBandwidth)
                .numPaths(numPaths)
                .disjointPaths(disjointPaths)
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
