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
import net.es.oscars.resv.ent.*;
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

    private final static String INGRESS = "INGRESS";
    private final static String EGRESS = "EGRESS";
    private final static String AZ = "AZ";
    private final static String ZA = "ZA";

    public BandwidthAvailabilityResponse getBandwidthAvailabilityMap(BandwidthAvailabilityRequest request) {

        Set<UrnE> urns = new HashSet<>();
        List<ReservedBandwidthE> rsvList = new ArrayList<>();
        Map<String, Map<Instant, Integer>> bwMaps;
        Map<UrnE, List<ReservedBandwidthE>> rsvMap;
        ReservedBlueprintE rsvBlueprint = null;
        
        ScheduleSpecificationE reqSchSpec = entityBuilder.buildSchedule(request.getStartDate(), request.getEndDate());
        RequestedBlueprintE reqBlueprint = entityBuilder.buildRequest(request.getSrcPort(), request.getSrcDevice(),
                request.getDstPort(), request.getDstDevice(), request.getMinAzBandwidth(), request.getMinZaBandwidth(),
                request.getPalindromicType(), request.getSurvivabilityType(), "any");

        try
        {
            Optional<ReservedBlueprintE> optRsvBlueprint = topPCE.makeReserved(reqBlueprint, reqSchSpec);
            if (optRsvBlueprint.isPresent())
            {
                rsvBlueprint = optRsvBlueprint.get();
                urns.addAll(entityDecomposer.decomposeReservedBlueprint(rsvBlueprint)
                        .stream()
                        .filter(urn -> urn.getReservableBandwidth() != null)
                        .collect(Collectors.toList()));
            }
        }
        catch (PCEException | PSSException exception) { log.info(exception.getMessage()); }

        Optional<List<ReservedBandwidthE>> optRsvList = bwRepo.findOverlappingInterval(
                request.getStartDate().toInstant(), request.getEndDate().toInstant());
        if (optRsvList.isPresent())
            rsvList.addAll(optRsvList.get());

        rsvList = rsvList.stream().filter(rsv -> urns.contains(rsv.getUrn())).collect(Collectors.toList());

        rsvMap = bwService.buildReservedBandwidthMap(rsvList);
        bwMaps = buildMaps(rsvMap, request.getStartDate().toInstant(), request.getEndDate().toInstant(), rsvBlueprint);

        Map<String, Integer> minsAndMaxes = getMinimumsAndMaximums(bwMaps);

        return BandwidthAvailabilityResponse.builder()
                .requestID(request.getRequestID())
                .srcDevice(request.getSrcDevice())
                .srcPort(request.getSrcPort())
                .dstDevice(request.getDstDevice())
                .dstPort(request.getDstPort())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .minRequestedAzBandwidth(request.getMinAzBandwidth())
                .minRequestedZaBandwidth(request.getMinZaBandwidth())
                .minAvailableAzBandwidth(minsAndMaxes.get("minAZ"))
                .minAvailableZaBandwidth(minsAndMaxes.get("minZA"))
                .maxAvailableAzBandwidth(minsAndMaxes.get("maxAZ"))
                .maxAvailableZaBandwidth(minsAndMaxes.get("maxZA"))
                .azBwAvailMap(bwMaps.get(AZ))
                .zaBwAvailMap(bwMaps.get(ZA))
                .build();
    }


    /**
     * Builds maps of the available bandwidth.
     * @param rsvMap Map of URNs to Reserved Bandwidths.
     * @param start Start time for bandwidth maps.
     * @param end End time for bandwidth maps.
     * @param blueprint Reserved blueprint.
     * @return Maps of available bandwidth.
     */
    private Map<String, Map<Instant, Integer>> buildMaps(Map<UrnE, List<ReservedBandwidthE>> rsvMap,
                                                         Instant start, Instant end, ReservedBlueprintE blueprint)
    {
        List<String> INGRESS_EGRESS = new ArrayList<>();                // Simplifies conditionals.
        INGRESS_EGRESS.add(INGRESS);
        INGRESS_EGRESS.add(EGRESS);

        Map<String, Map<UrnE, Integer>> curUrnBw = new HashMap<>();     // Available ingress and egress at urn.
        curUrnBw.put(INGRESS, new HashMap<>());
        curUrnBw.put(EGRESS, new HashMap<>());

        Map<String, Map<Instant, Integer>> bwMaps = new HashMap<>();    // A->Z and Z->A bandwidth Maps.
        bwMaps.put(AZ, new HashMap<>());
        bwMaps.put(ZA, new HashMap<>());

        // Map of path direction -> urns -> Ingress and/or Egress.
        Map<String, Map<UrnE, List<String>>> urnTables = buildUrnTables(blueprint);

        Set<UrnE> urns = urnTables.values().stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toSet());

        // Sorted list of bandwidth events.
        List<BwEvent> bwEvents = abstractEvents(rsvMap);
        
        for (UrnE urn : urns)
        {
            // Initialize each urns available bandwidth to max supported.
            curUrnBw.get(INGRESS).put(urn, urn.getReservableBandwidth().getIngressBw());
            curUrnBw.get(EGRESS).put(urn, urn.getReservableBandwidth().getEgressBw());
        }

        if(bwEvents.isEmpty()){
            for (String path : bwMaps.keySet()) {
                Integer minBw = Integer.MAX_VALUE;
                for(UrnE urn: urns) {
                    Integer urnBw = 0;
                    if (urnTables.get(path).get(urn).containsAll(INGRESS_EGRESS)) {
                        urnBw = Math.min(curUrnBw.get(INGRESS).get(urn), curUrnBw.get(EGRESS).get(urn));
                    } else if (urnTables.get(path).get(urn).contains(INGRESS)) {
                        urnBw = curUrnBw.get(INGRESS).get(urn);
                    } else if (urnTables.get(path).get(urn).contains(EGRESS)) {
                        urnBw = curUrnBw.get(EGRESS).get(urn);
                    }
                    minBw = urnBw < minBw ? urnBw : minBw;
                }
                bwMaps.get(path).put(start, minBw);
            }
        }

        // Loop through bandwidth events.
        for (BwEvent bwEvent : bwEvents)
        {
            // Event is before start of interval.
            if (!bwEvent.getTime().isAfter(start))
            {
                // Only update available bandwidths.
                curUrnBw.get(INGRESS).replace(bwEvent.getUrn(), curUrnBw.get(INGRESS).get(bwEvent.getUrn()) + bwEvent.getInBw());
                curUrnBw.get(EGRESS).replace(bwEvent.getUrn(), curUrnBw.get(EGRESS).get(bwEvent.getUrn()) + bwEvent.getEgBw());
            }

            // Event is in interval of interest.
            else if (bwEvent.getTime().isAfter(start) && !bwEvent.getTime().isAfter(end))
            {
                // Add start point for each direction if not done already.
                for (String path : bwMaps.keySet()) {
                    if (bwMaps.get(path).isEmpty()) {
                        if (urnTables.get(path).get(bwEvent.getUrn()).containsAll(INGRESS_EGRESS)) {
                            bwMaps.get(path).put(start, findMin(findMin(curUrnBw.get(INGRESS)), findMin(curUrnBw.get(EGRESS))));
                        } else if (urnTables.get(path).get(bwEvent.getUrn()).contains(INGRESS)) {
                            bwMaps.get(path).put(start, findMin(curUrnBw.get(INGRESS)));
                        } else if (urnTables.get(path).get(bwEvent.getUrn()).contains(EGRESS)) {
                            bwMaps.get(path).put(start, findMin(curUrnBw.get(EGRESS)));
                        }
                    }
                }

                // Update available bandwidths.
                curUrnBw.get(INGRESS).replace(bwEvent.getUrn(), curUrnBw.get(INGRESS).get(bwEvent.getUrn()) + bwEvent.getInBw());
                curUrnBw.get(EGRESS).replace(bwEvent.getUrn(), curUrnBw.get(EGRESS).get(bwEvent.getUrn()) + bwEvent.getEgBw());

                // For each direction, replace or add point for current event.
                for (String path : bwMaps.keySet()) {
                    if (bwMaps.get(path).containsKey(bwEvent.getTime())) {
                        if (urnTables.get(path).get(bwEvent.getUrn()).containsAll(INGRESS_EGRESS)) {
                            bwMaps.get(path).replace(start, findMin(findMin(curUrnBw.get(INGRESS)), findMin(curUrnBw.get(EGRESS))));
                        } else if (urnTables.get(path).get(bwEvent.getUrn()).contains(INGRESS)) {
                            bwMaps.get(path).replace(start, findMin(curUrnBw.get(INGRESS)));
                        } else if (urnTables.get(path).get(bwEvent.getUrn()).contains(EGRESS)) {
                            bwMaps.get(path).replace(start, findMin(curUrnBw.get(EGRESS)));
                        }
                    }
                    else{
                        if (urnTables.get(path).get(bwEvent.getUrn()).containsAll(INGRESS_EGRESS)) {
                            bwMaps.get(path).put(start, findMin(findMin(curUrnBw.get(INGRESS)), findMin(curUrnBw.get(EGRESS))));
                        } else if (urnTables.get(path).get(bwEvent.getUrn()).contains(INGRESS)) {
                            bwMaps.get(path).put(start, findMin(curUrnBw.get(INGRESS)));
                        } else if (urnTables.get(path).get(bwEvent.getUrn()).contains(EGRESS)) {
                            bwMaps.get(path).put(start, findMin(curUrnBw.get(EGRESS)));
                        }
                    }
                }
            }

            // Event is after interval of interest.
            else
            {
                // Add ending points and break.
                for (String path : bwMaps.keySet()) {
                    if (urnTables.get(path).get(bwEvent.getUrn()).containsAll(INGRESS_EGRESS)) {
                        bwMaps.get(path).put(start, findMin(findMin(curUrnBw.get(INGRESS)), findMin(curUrnBw.get(EGRESS))));
                    } else if (urnTables.get(path).get(bwEvent.getUrn()).contains(INGRESS)) {
                        bwMaps.get(path).put(start, findMin(curUrnBw.get(INGRESS)));
                    } else if (urnTables.get(path).get(bwEvent.getUrn()).contains(EGRESS)) {
                        bwMaps.get(path).put(start, findMin(curUrnBw.get(EGRESS)));
                    }
                }
                break;
            }
        }
        return bwMaps;
    }

    /**
     * Returns the minimum of two Integers.
     * @param i1 Integer to be compared.
     * @param i2 Integer to be compared.
     * @return Minimum of i1 & i2.
     */
    private Integer findMin(Integer i1, Integer i2)
    {
        if (i1 < i2)
            return i1;
        return i2;
    }

    /**
     * Returns the minimum
     * @param inMap Map of URNs to Integers.
     * @return Minimum integer in inMap.
     */
    private Integer findMin(Map<UrnE, Integer> inMap)
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

    /**
     * Creates a map of which URNs are used for each path.
     * @param blueprint Reserved Blueprint.
     * @return Map of URNs used for each path.
     */
    private Map<String, Map<UrnE , List<String>>> buildUrnTables(ReservedBlueprintE blueprint)
    {
        boolean isIngress;
        Map<String, Map<UrnE, List<String>>> urnTables = new HashMap<>();
        urnTables.put(AZ, new HashMap<>());
        urnTables.put(ZA, new HashMap<>());

        // Handle MPLS pipes first.
        for (ReservedMplsPipeE pipe : blueprint.getVlanFlow().getMplsPipes())
        {
            // Handle fixtures
            for(ReservedVlanFixtureE fix : pipe.getAJunction().getFixtures()){
                UrnE urn = fix.getIfceUrn();
                urnTables.get(AZ).putIfAbsent(urn, new ArrayList<>());
                urnTables.get(AZ).get(urn).add(INGRESS);
                urnTables.get(ZA).putIfAbsent(urn, new ArrayList<>());
                urnTables.get(ZA).get(urn).add(EGRESS);
            }
            for(ReservedVlanFixtureE fix : pipe.getZJunction().getFixtures()){
                UrnE urn = fix.getIfceUrn();
                urnTables.get(AZ).putIfAbsent(urn, new ArrayList<>());
                urnTables.get(AZ).get(urn).add(EGRESS);
                urnTables.get(ZA).putIfAbsent(urn, new ArrayList<>());
                urnTables.get(ZA).get(urn).add(INGRESS);
            }
            // A->Z starts with egress.
            isIngress = false;

            for (UrnE urn : entityDecomposer.decomposeMplsPipeIntoAzEROList(pipe).stream().filter(u -> u.getReservableBandwidth() != null).collect(Collectors.toList()))
            {
                urnTables.get(AZ).putIfAbsent(urn, new ArrayList<>());
                
                if (isIngress)
                    urnTables.get(AZ).get(urn).add(INGRESS);
                else
                    urnTables.get(AZ).get(urn).add(EGRESS);

                // Alternate ingress/egress.
                isIngress = Boolean.logicalXor(isIngress, true);
            }

            // Z-A starts with egress.
            isIngress = false;
            
            for (UrnE urn : entityDecomposer.decomposeMplsPipeIntoZaEROList(pipe).stream().filter(u -> u.getReservableBandwidth() != null).collect(Collectors.toList()))
            {
                urnTables.get(ZA).putIfAbsent(urn, new ArrayList<>());

                if (isIngress)
                    urnTables.get(ZA).get(urn).add(INGRESS);
                else
                    urnTables.get(ZA).get(urn).add(EGRESS);

                // Alternate ingress/egress.
                isIngress = Boolean.logicalXor(isIngress, true);
            }
        }

        // Handle ethernet pipes.
        for (ReservedEthPipeE pipe : blueprint.getVlanFlow().getEthPipes())
        {
            // Handle fixtures
            for(ReservedVlanFixtureE fix : pipe.getAJunction().getFixtures()){
                UrnE urn = fix.getIfceUrn();
                urnTables.get(AZ).putIfAbsent(urn, new ArrayList<>());
                urnTables.get(AZ).get(urn).add(INGRESS);
                urnTables.get(ZA).putIfAbsent(urn, new ArrayList<>());
                urnTables.get(ZA).get(urn).add(EGRESS);
            }
            for(ReservedVlanFixtureE fix : pipe.getZJunction().getFixtures()){
                UrnE urn = fix.getIfceUrn();
                urnTables.get(AZ).putIfAbsent(urn, new ArrayList<>());
                urnTables.get(AZ).get(urn).add(EGRESS);
                urnTables.get(ZA).putIfAbsent(urn, new ArrayList<>());
                urnTables.get(ZA).get(urn).add(INGRESS);
            }

            // A->Z starts with egress.
            isIngress = false;

            for (UrnE urn : entityDecomposer.decomposeEthPipeIntoAzEROList(pipe))
            {
                urnTables.get(AZ).putIfAbsent(urn, new ArrayList<>());

                if (isIngress)
                    urnTables.get(AZ).get(urn).add(INGRESS);
                else
                    urnTables.get(AZ).get(urn).add(EGRESS);

                // Alternate ingress/egress.
                isIngress = Boolean.logicalXor(isIngress, true);
            }

            // Z->A starts with ingress.
            isIngress = true;

            for (UrnE urn : entityDecomposer.decomposeEthPipeIntoZaEROList(pipe))
            {
                urnTables.get(ZA).putIfAbsent(urn, new ArrayList<>());

                if (isIngress)
                    urnTables.get(ZA).get(urn).add(INGRESS);
                else
                    urnTables.get(ZA).get(urn).add(EGRESS);

                // Alternate ingress/egress.
                isIngress = Boolean.logicalXor(isIngress, true);
            }
        }
        
        return urnTables;
    }

    /**
     * Abstracts bandwidth events from a map of URNs to Reserved Bandwidths.
     * @param rsvMap Map of URNs to Reserved Bandwidths.
     * @return List of bandwidth events sorted chronologically.
     */
    private List<BwEvent> abstractEvents(Map<UrnE, List<ReservedBandwidthE>> rsvMap)
    {
        List<BwEvent> eventList = new ArrayList<>();

        for (UrnE urn : rsvMap.keySet())
        {
            for (ReservedBandwidthE rsv : rsvMap.get(urn))
            {
                eventList.add(new BwEvent(rsv.getUrn(), rsv.getBeginning(), (-1) * rsv.getInBandwidth(), (-1) * rsv.getEgBandwidth()));
                eventList.add(new BwEvent(rsv.getUrn(), rsv.getEnding(), rsv.getInBandwidth(), rsv.getEgBandwidth()));
            }
        }

        eventList.sort((t1, t2) -> t1.getTime().compareTo(t2.getTime()));

        return eventList;
    }

    /**
     * Pull out the maximum and minimum bandwidth values from the AZ and ZA directions of the input
     * bandwidth maps.
     * @param bwMaps - A mapping from "AZ" or "ZA" to a Map of time Instants and the bandwdith available at those times
     * @return A map from "minAZ", "maxAZ", "minZA", and "MaxAZ" to corresponding integer bandwidth values.
     */
    private Map<String,Integer> getMinimumsAndMaximums(Map<String, Map<Instant, Integer>> bwMaps) {
        Map<Instant, Integer> azEvents = bwMaps.get(AZ);
        Map<Instant, Integer> zaEvents = bwMaps.get(ZA);

        Integer minAZ = Integer.MAX_VALUE;
        Integer minZA = Integer.MAX_VALUE;
        Integer maxAZ = 0;
        Integer maxZA = 0;

        for(Integer azBw : azEvents.values()){
            if(azBw < minAZ){
                minAZ = azBw;
            }
            if(azBw > maxAZ){
                maxAZ = azBw;
            }
        }
        for(Integer zaBw : zaEvents.values()){
            if(zaBw < minZA){
                minZA = zaBw;
            }
            if(zaBw > maxZA){
                maxZA = zaBw;
            }
        }

        Map<String, Integer> minMaxMap = new HashMap<>();
        minMaxMap.put("minAZ", minAZ);
        minMaxMap.put("minZA", minZA);
        minMaxMap.put("maxAZ", maxAZ);
        minMaxMap.put("maxZA", maxZA);

        return minMaxMap;
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