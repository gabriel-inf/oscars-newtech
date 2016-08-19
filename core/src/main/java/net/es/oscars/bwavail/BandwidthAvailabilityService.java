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

        if(!validateRequest(request)){
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
                    .minAvailableAzBandwidth(-1)
                    .minAvailableZaBandwidth(-1)
                    .maxAvailableAzBandwidth(-1)
                    .maxAvailableZaBandwidth(-1)
                    .azBwAvailMap(new HashMap<>())
                    .zaBwAvailMap(new HashMap<>())
                    .build();
        }

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


        //rsvList = bwRepo.findAll();

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

    private boolean validateRequest(BandwidthAvailabilityRequest request){
        // Confirm that src/dest devices/ports are actually in the topology
        Topology topo = topoService.getMultilayerTopology();

        Optional<TopoVertex> srcDevice = topo.getVertexByUrn(request.getSrcDevice());
        Optional<TopoVertex> srcPort = topo.getVertexByUrn(request.getSrcPort());
        Optional<TopoVertex> dstDevice = topo.getVertexByUrn(request.getDstDevice());
        Optional<TopoVertex> dstPort = topo.getVertexByUrn(request.getDstPort());

        return srcDevice.isPresent() && srcPort.isPresent() && dstDevice.isPresent() && dstPort.isPresent();
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
        Integer curMin = Integer.MAX_VALUE;                                     // Store current min for a path.

        Map<String, Integer> prevMin = new HashMap<>();     // Track previous min for each path.
        prevMin.put(AZ, 0);
        prevMin.put(ZA, 0);

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

        // Handle case with no bandwidth events.
        log.info("BW Events: " + bwEvents);
        if(bwEvents.isEmpty()){
            for (String path : bwMaps.keySet()) {
                Integer min = findMin(urns, curUrnBw, urnTables, path);
                bwMaps.get(path).put(start, min);
                bwMaps.get(path).put(end, min);
            }
        }

        Instant previousTime = null;
        // Loop through bandwidth events.
        for (Integer eventIndex = 0; eventIndex < bwEvents.size(); eventIndex++)
        {
            BwEvent bwEvent = bwEvents.get(eventIndex);
            Instant currentTime = bwEvent.getTime();
            // Event is before start of interval.
            if (!currentTime.isAfter(start))
            {
                // Update available bandwidths.
                updateAvailableBw(curUrnBw, bwEvent);
                // If this is the last event, or if the next event is after the start time, update the bandwidth map
                if(eventIndex == bwEvents.size()-1 || bwEvents.get(eventIndex+1).getTime().isAfter(start)){
                    for (String path : bwMaps.keySet()) {
                        Integer min = findMin(urns, curUrnBw, urnTables, path);
                        bwMaps.get(path).put(start, min);
                        // If this is the last event, add a point for the end
                        if(eventIndex == bwEvents.size()-1){
                            bwMaps.get(path).put(end, min);
                        }
                    }
                }
            }

            // Event is in interval of interest.
            else if (currentTime.isAfter(start) && !currentTime.isAfter(end))
            {
                // If this is the first event, then create a starting point
                if(eventIndex == 0){
                    for (String path : bwMaps.keySet()) {
                        Integer min = findMin(urns, curUrnBw, urnTables, path);
                        bwMaps.get(path).put(start, min);
                    }
                }
                // Update available bandwidths.
                updateAvailableBw(curUrnBw, bwEvent);
                // If this is the last event, or if the next event is after the current time, update the bandwidth map
                if(eventIndex == bwEvents.size()-1 || bwEvents.get(eventIndex+1).getTime().isAfter(currentTime)){
                    for (String path : bwMaps.keySet()) {
                        Integer min = findMin(urns, curUrnBw, urnTables, path);
                        bwMaps.get(path).put(currentTime, min);
                        // If this is the last event, add a point for the end
                        if(eventIndex == bwEvents.size()-1 && currentTime.isBefore(end)){
                            bwMaps.get(path).put(end, min);
                        }
                    }
                }
            }
            // Event is after interval of interest.
            else
            {
                if(previousTime == null){
                    previousTime = start;
                }
                if(previousTime.isBefore(end)) {
                    // Add ending points and break.
                    for (String path : bwMaps.keySet()) {
                        Integer min = findMin(urns, curUrnBw, urnTables, path);
                        bwMaps.get(path).put(end, min);
                    }
                }
                break;
            }
            previousTime = currentTime;
        }
        return bwMaps;
    }

    private void updateAvailableBw(Map<String, Map<UrnE, Integer>> curUrnBw, BwEvent bwEvent) {
        curUrnBw.get(INGRESS).put(bwEvent.getUrn(), curUrnBw.get(INGRESS).get(bwEvent.getUrn()) + bwEvent.getInBw());
        curUrnBw.get(EGRESS).put(bwEvent.getUrn(), curUrnBw.get(EGRESS).get(bwEvent.getUrn()) + bwEvent.getEgBw());
    }

    private Integer findMin(Set<UrnE> urns, Map<String, Map<UrnE, Integer>> curUrnBw,
                            Map<String, Map<UrnE, List<String>>> urnTables, String path) {
        Integer min = Integer.MAX_VALUE;
        for (UrnE urn : urns) {
            Integer urnBw = findBw(urn, curUrnBw, urnTables.get(path).get(urn));
            min = urnBw < min ? urnBw : min;
        }
        return min;
    }


    /**
     * Returns the bandwidth available at URN.
     * @param urn URN of interest.
     * @param curBw Map of ingress/egress -> urn -> available bandwidth.
     * @param usedBWs List specifying whether the path uses the URNs ingress, egress, or both.
     * @return The bandwidth available at URN.
     */
    private Integer findBw(UrnE urn, Map<String, Map<UrnE, Integer>> curBw, List<String> usedBWs)
    {
        if (usedBWs.contains(INGRESS) && usedBWs.contains(EGRESS))
            return Math.min(curBw.get(INGRESS).get(urn), curBw.get(EGRESS).get(urn));
        else if (usedBWs.contains(INGRESS))
            return curBw.get(INGRESS).get(urn);
        else if (usedBWs.contains(EGRESS))
            return curBw.get(EGRESS).get(urn);

        return 0;
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

            // Z->A starts with egress.
            isIngress = false;

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