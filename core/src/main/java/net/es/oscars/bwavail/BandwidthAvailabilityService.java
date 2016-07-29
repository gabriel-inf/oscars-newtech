package net.es.oscars.bwavail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.bwavail.enums.BandwidthAvailabilityRequest;
import net.es.oscars.bwavail.enums.BandwidthAvailabilityResponse;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.helpers.ReservedEntityDecomposer;
import net.es.oscars.pce.*;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.RequestedBlueprintE;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.resv.ent.ReservedBlueprintE;
import net.es.oscars.resv.ent.ScheduleSpecificationE;
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
    private TopPCE topPCE;

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private RequestedEntityBuilder entityBuilder;

    @Autowired
    private ReservedEntityDecomposer entityDecomposer;

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


    public BandwidthAvailabilityResponse getBandwidthAvailabilityMap(BandwidthAvailabilityRequest request) {

        Set<UrnE> urns = new HashSet<>();
        List<ReservedBandwidthE> rsvList = new ArrayList<>();
        Map<String, Map<Instant, Integer>> bwMaps = new HashMap<>();
        Map<UrnE, List<ReservedBandwidthE>> rsvMap;


        /* * * * This is necessary... * * * */
        
        ScheduleSpecificationE reqSchSpec = entityBuilder.buildSchedule(request.getStartDate(), request.getEndDate());
        RequestedBlueprintE reqBlueprint = entityBuilder.buildRequest(request.getSrcPort(), request.getSrcDevice(),
                request.getDstPort(), request.getDstDevice(), request.getMinAzBandwidth(), request.getMinZabandwidth(),
                request.getPathType(), request.getSurvivabilityType(), "any");

        try
        {
            Optional<ReservedBlueprintE> optRsvBlueprint = topPCE.makeReserved(reqBlueprint, reqSchSpec);
            if (optRsvBlueprint.isPresent())
                urns.addAll(entityDecomposer.decomposeReservedBlueprint(optRsvBlueprint.get()));
        }
        catch (PCEException pceEx) { log.info(pceEx.getMessage()); }
        catch (PSSException pssEx) { log.info(pssEx.getMessage()); }

        Optional<List<ReservedBandwidthE>> optRsvList = bwRepo.findOverlappingInterval(
                request.getStartDate().toInstant(), request.getEndDate().toInstant());
        if (optRsvList.isPresent())
            rsvList.addAll(optRsvList.get());

        rsvList = rsvList.stream().filter(rsv -> urns.contains(rsv.getUrn())).collect(Collectors.toList());

        rsvMap = bwService.buildReservedBandwidthMap(rsvList);
        
        /* ^ ^ ^ This is necessary ^ ^ ^ */


        bwMaps = BuildMaps(rsvMap, request.getStartDate(), request.getEndDate());


        return BandwidthAvailabilityResponse.builder()
                .build();
    }

    private Map<String, Map<Instant, Integer>> BuildMaps(Map<UrnE, List<ReservedBandwidthE>> inMap, Date start, Date end)
    {
        Map<String, Map<Instant, Integer>> bwMaps = new HashMap<>();    // A->Z and Z->A bandwidth maps
        Map<UrnE, Integer> curAzBw = new HashMap<>();                   // Available A->Z bandwidth at each URN
        Map<UrnE, Integer> curZaBw = new HashMap<>();                   // Available Z->A bandwidth at each URN

        bwMaps.put("AZ", new HashMap<>());
        bwMaps.put("ZA", new HashMap<>());

        List<BwEvent> bwEvents = AbstractEvents(inMap);

        // Initialize available bandwidth to max supported at each URN.
        // !!! Ingress & egress do not map directly to A->Z & Z->A.
        for (UrnE urn : inMap.keySet())
        {
            curAzBw.put(urn, urn.getReservableBandwidth().getIngressBw());
            curZaBw.put(urn, urn.getReservableBandwidth().getEgressBw());
        }

        for (BwEvent bwEvent : bwEvents)
        {
            // Event is before start of interval. Only update available bandwidths.
            if (!bwEvent.getTime().isAfter(start.toInstant()))
            {
                curAzBw.replace(bwEvent.getUrn(), curAzBw.get(bwEvent.getUrn()) - bwEvent.getInBw());
                curZaBw.replace(bwEvent.getUrn(), curZaBw.get(bwEvent.getUrn()) - bwEvent.getEgBw());
            }

            // Event is in interval of interest.
            else if (bwEvent.getTime().isAfter(start.toInstant()) && !bwEvent.getTime().isAfter(end.toInstant()))
            {
                if (bwMaps.get("AZ").isEmpty())
                {
                    // Add starting point to the bandwidth maps if not done already.
                    bwMaps.get("AZ").put(start.toInstant(), FindMin(curAzBw));
                    bwMaps.get("ZA").put(start.toInstant(), FindMin(curZaBw));
                }

                // Update bandwidth at URN, and add new points to the bandwidth maps.
                curAzBw.replace(bwEvent.getUrn(), curAzBw.get(bwEvent.getUrn()) - bwEvent.getInBw());
                curZaBw.replace(bwEvent.getUrn(), curZaBw.get(bwEvent.getUrn()) - bwEvent.getEgBw());
                bwMaps.get("AZ").put(start.toInstant(), FindMin(curAzBw));
                bwMaps.get("AZ").put(start.toInstant(), FindMin(curZaBw));
            }

            // Event is after interval.
            else
            {
                // Add ending points to bandwidth maps and break from loop.
                bwMaps.get("AZ").put(end.toInstant(), FindMin(curAzBw));
                bwMaps.get("AZ").put(end.toInstant(), FindMin(curZaBw));
                break;
            }
        }

        return bwMaps;
    }

    private Integer FindMin(Map<UrnE, Integer> inMap)
    {
        assert (!inMap.isEmpty());

        Integer curMin = Integer.MAX_VALUE;

        for (UrnE urn : inMap.keySet())
        {
            if (inMap.get(urn) < curMin)
                curMin = inMap.get(urn);
        }

        return curMin;
    }

    private List<BwEvent> AbstractEvents(Map<UrnE, List<ReservedBandwidthE>> inMap)
    {
        List<BwEvent> outList = new ArrayList<>();

        for (UrnE urn : inMap.keySet())
        {
            for (ReservedBandwidthE rsv : inMap.get(urn))
            {
                outList.add(new BwEvent(rsv.getUrn(), rsv.getBeginning(), -1 * rsv.getInBandwidth(), -1 * rsv.getEgBandwidth()));
                outList.add(new BwEvent(rsv.getUrn(), rsv.getEnding(), rsv.getInBandwidth(), rsv.getEgBandwidth()));
            }
        }

        outList.sort((t1, t2) -> t1.getTime().compareTo(t2.getTime()));

        return outList;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class BwEvent
    {
        private UrnE urn;
        private Instant time;
        private Integer inBw;
        private Integer egBw;
    }
}
