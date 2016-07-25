package net.es.oscars.bwavail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.pce.BandwidthService;
import net.es.oscars.pce.DijkstraPCE;
import net.es.oscars.pce.PruningService;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BandwidthAvailabilityService {

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    @Autowired
    private TopoService topoService;

    @Autowired
    private BandwidthService bwService;


    public BandwidthAvailabilityResponse
    getBandwidthAvailabilityMap(BandwidthAvailabilityRequest request) {

        //Map<UrnE, Map<Instant, Integer>> evtMap = new HashMap<>();

        Integer rsvBw = 0;                      // Currently reserved bandwidth.
        Topology topo;                          // Multilayer topology.
        TopoVertex src, dst;                    // Source and destination.
        List<ReservedBandwidthE> rsvList;       // List of all reservations.
        List<ReservedBandwidthE> pathRsvList;   // List of reservations which effect the path.

         List<TopoVertex> path;                 // Path from source to destination.

                List<String> urnPath = new ArrayList<>();                       // Path as a list of URNs.
                List<Instant> allEventTimes = new ArrayList<>();                // List of all event times.
                Map<UrnE, List<ReservedBandwidthE>> rsvMap = new HashMap<>();   // Map of reservations.
                Map<UrnE, List<BwEvent>> eventMap = new HashMap<>();            // Map of bandwidth changes.
                Map<UrnE, List<BwEvent>> bwMap = new HashMap<>();               // Bandwidth map.
                Map<UrnE, Integer> curBw = new HashMap<>();                     // Current bandwidth at URN.

        // Prune topology for minimum bandwidth and any vlans IDs.
        topo =
                pruningService.pruneWithBwVlans(topoService.getMultilayerTopology(),
                        request.getMinBandwidth(),
                        "ALL", request.getStartDate(), request.getEndDate());

        // Ensure source and destination are present in topology.
        Optional<TopoVertex> optSrc = topo.getVertexByUrn(request.getSource());
        Optional<TopoVertex> optDst =
                topo.getVertexByUrn(request.getDestination());
        if (optSrc.isPresent() && optDst.isPresent()) {
            src = optSrc.get();
            dst = optDst.get();

            // Compute path, convert to list of URNs.
            path =
                    dijkstraPCE.translatePathEdgesToVertices(dijkstraPCE.computeShortestPathEdges(
                            topo, src, dst));
            for (TopoVertex tv : path)
                urnPath.add(tv.getUrn());

            // Get a list of all reservations during the map interval.
            Optional<List<ReservedBandwidthE>> optRsvList =
                    bwRepo.findOverlappingInterval(
                            request.getStartDate().toInstant(),
                            request.getEndDate().toInstant());
            if (optRsvList.isPresent() && !path.isEmpty()) {
                rsvList = optRsvList.get();

                // Filter reservations to those which effect the path.
                pathRsvList = rsvList.stream().filter(rsv ->
                        urnPath.contains(rsv.getUrn().toString()))
                        .collect(Collectors.toList());

                // Build a map of URNs to reservations.
                rsvMap.putAll(bwService.buildReservedBandwidthMap(pathRsvList));
            }
        }

        for (UrnE urnE : rsvMap.keySet()) {
            eventMap.put(urnE, new ArrayList<>());
            bwMap.put(urnE, new ArrayList<>());

            // Abstract events from reservations.
            for (ReservedBandwidthE rsvBwE : rsvMap.get(urnE)) {
                eventMap.get(urnE).add(new BwEvent(rsvBwE.getBeginning(), (-1) *
                        rsvBwE.getInBandwidth()));
                eventMap.get(urnE).add(new BwEvent(rsvBwE.getEnding(),
                        rsvBwE.getInBandwidth()));

                // Update list of all event times.
                if (!allEventTimes.contains(rsvBwE.getBeginning()))
                    allEventTimes.add(rsvBwE.getBeginning());
                if(!allEventTimes.contains(rsvBwE.getEnding()))
                    allEventTimes.add(rsvBwE.getEnding());
            }

            // Sort events for current URN.
            eventMap.get(urnE).sort((t1, t2) ->
                    t1.getTime().compareTo(t2.getTime()));

            // Calculate initial reserved bandwidth on URN.
            for (BwEvent bwE : eventMap.get(urnE))
            {
                if (bwE.getTime().isAfter(request.getStartDate().toInstant()))
                    break;

                rsvBw += bwE.getBw();
                eventMap.get(urnE).remove(bwE);
            }

            // Add initial bandwidth for URN.
            bwMap.get(urnE).add(new BwEvent(request.getStartDate().toInstant(),
                    urnE.getReservableBandwidth().getBandwidth() - rsvBw));

            // Add subsequent bandwidth events for URN.
            for (BwEvent bwE : eventMap.get(urnE))
            {
                // Next event is after interval end time.
                if (bwE.getTime().isAfter(request.getEndDate().toInstant()))
                {
                    // Add final bandwidth event.
                    bwMap.get(urnE).add(new
                            BwEvent(request.getEndDate().toInstant(),
                            urnE.getReservableBandwidth().getBandwidth() -
                                    rsvBw));

                    break;
                }

                // Calculate bandwidth at URN and add bandwidth event.
                rsvBw += bwE.getBw();
                bwMap.get(urnE).add(new BwEvent(bwE.getTime(),
                        urnE.getReservableBandwidth().getBandwidth() - rsvBw));
            }
        }

        /*
        for (Instant i : allEventTimes)
        {
            for (UrnE urn : bwMap.keySet())
            {
                if (evtMap.get(urn).keySet().contains(i))
                {

                }
            }
        }
        */

        return BandwidthAvailabilityResponse.builder()
                .build();
    }

    @Data
    @AllArgsConstructor
    private class BwEvent{
        private Instant time;
        private Integer bw;

        // private Integer inBw;
        // private Integer egBw;
    }
}