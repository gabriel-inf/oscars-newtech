package net.es.oscars.bwavail.svc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityResponse;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.helpers.ReservedEntityDecomposer;
import net.es.oscars.pce.*;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.BidirectionalPathE;
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
    private TopoService topoService;

    @Autowired
    private BandwidthService bwService;

    private final static String INGRESS = "INGRESS";
    private final static String EGRESS = "EGRESS";
    private final static String AZ = "AZ";
    private final static String ZA = "ZA";
    private final static String AZMIN = "AZMIN";
    private final static String AZMAX = "AZMAX";
    private final static String ZAMIN = "ZAMIN";
    private final static String ZAMAX = "ZAMAX";

    /**
     * Given a bandwidth availability request, return a response that contains a mapping of the minimum
     * available bandwidth between the requested source & destination at different points between the requested
     * start and end times.
     *
     * @param request - The bandwidth availability request
     * @return The matching Bandwidth availability response
     */
    public BandwidthAvailabilityResponse getBandwidthAvailabilityMap(BandwidthAvailabilityRequest request) {

        log.info("Processing Bandwidth Availability Request");
        //Set default values for numPaths and isDisjoint if they are null
        if(request.getNumPaths() == null){
            request.setNumPaths(1);
        }
        if(request.getDisjointPaths() == null){
            request.setDisjointPaths(true);
        }

        // Confirm that the request involves nodes in the topology
        Topology topo = topoService.getMultilayerTopology();
        Boolean pairValid = validatePair(request, topo);
        Boolean pathValid = validateEros(request, topo);
        if (!pathValid && !pairValid) {
            log.info("Topology: " + topo);
            log.info("Invalid Bandwidth Availability Request. One of two problems may have occurred:");
            log.info("(1) Input paths are invalid: either null or elements in path are not in topology.");
            log.info("(2) Input source/dest pair is invalid: Either Devices/Ports are null or not in topology");
            return buildResponse(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        }

        // Create the requested schedule
        ScheduleSpecificationE reqSchSpec = entityBuilder.buildSchedule(request.getStartDate(), request.getEndDate());

        // Maps for keeping track of statistics - will go into the response payload
        Map<String, Integer> minAvailableBwMap = new HashMap<>();
        Map<String, Map<Instant, Integer>> bwAvailabilityMap = new HashMap<>();
        Map<String, String> pathPairMap = new HashMap<>();
        Map<String, List<String>> pathNameMap = new HashMap<>();

        // For each ERO, create a blueprint and get the response
        List<RequestedBlueprintE> requestedBlueprints = generateRequestedBlueprints(request, pairValid, pathValid, topo);

        List<Date> chosenDates = new ArrayList<>();

        Integer pathNum = 1;
        for(RequestedBlueprintE reqBlueprint : requestedBlueprints){
            try {
                Optional<ReservedBlueprintE> optRsvBlueprint = topPCE.makeReserved(reqBlueprint, reqSchSpec, chosenDates);
                ReservedBlueprintE rsvBlueprint;
                // If a path could be found, store the URNs used
                if (optRsvBlueprint.isPresent()) {
                    rsvBlueprint = optRsvBlueprint.get();
                    log.debug(rsvBlueprint.toString());
                    pathNum = processReservedBlueprint(rsvBlueprint, request, minAvailableBwMap, bwAvailabilityMap,
                            pathPairMap, pathNameMap, pathNum);
                }
                // Otherwise, add empty/zero entries for a failed request
                else {
                    pathNum = processFailedBlueprint(reqBlueprint, request, minAvailableBwMap, bwAvailabilityMap,
                            pathPairMap, pathNameMap, pathNum);
                }
            } catch (PCEException | PSSException exception) {
                log.info(exception.getMessage());
                // Add empty/zero entries for a failed request
                pathNum = processFailedBlueprint(reqBlueprint, request, minAvailableBwMap, bwAvailabilityMap,
                        pathPairMap, pathNameMap, pathNum);
            }

        }

        return buildResponse(minAvailableBwMap, bwAvailabilityMap, pathPairMap, pathNameMap);
    }

    private Integer processReservedBlueprint(ReservedBlueprintE rsvBlueprint, BandwidthAvailabilityRequest request,
                                          Map<String, Integer> minAvailableBwMap,
                                          Map<String, Map<Instant, Integer>> bwAvailabilityMap,
                                          Map<String, String> pathPairMap,
                                          Map<String, List<String>> pathNameMap, Integer pathNum) {

        Set<BidirectionalPathE> paths = rsvBlueprint.getVlanFlow().getAllPaths();
        for(BidirectionalPathE path : paths){
            List<String> azPath = entityDecomposer.decomposeEdgeList(path.getAzPath());
            List<String> zaPath = entityDecomposer.decomposeEdgeList(path.getZaPath());

            //Name the paths and increment the count
            String azPathName = "Az" + pathNum;
            String zaPathName = "Za" + pathNum;
            pathNum++;

            // Retrieve the reserved bandwidths that overlap with the given start and end time
            Optional<List<ReservedBandwidthE>> optRsvList = bwRepo.findOverlappingInterval(
                    request.getStartDate().toInstant(), request.getEndDate().toInstant());

            List<ReservedBandwidthE> rsvList = new ArrayList<>();

            if (optRsvList.isPresent())
                rsvList.addAll(optRsvList.get());
            // Filter out the reservations that do not involve this request's URNs
            Set<String> allElements = new HashSet<>(azPath);
            allElements.addAll(zaPath);
            rsvList = rsvList.stream().filter(rsv -> allElements.contains(rsv.getUrn())).collect(Collectors.toList());

            // Build a map from those reservations
            Map<String, List<ReservedBandwidthE>> rsvMap = bwService.buildReservedBandwidthMap(rsvList);

            // Create the AZ and ZA bandwidth maps given the reservations
            Map<String, Map<Instant, Integer>> bwMaps = buildMaps(rsvMap, request.getStartDate().toInstant(), request.getEndDate().toInstant(), azPath, zaPath);

            // Find the Min and Max for both AZ and ZA directions
            Map<String, Integer> minsAndMaxes = getMinimumsAndMaximums(bwMaps);

            // Store the min values for the AZ and ZA path
            minAvailableBwMap.put(azPathName, minsAndMaxes.get(AZMIN));
            minAvailableBwMap.put(zaPathName, minsAndMaxes.get(ZAMIN));

            // Store the <Instant, Bandwidth> time maps for each path
            bwAvailabilityMap.put(azPathName, bwMaps.get(AZ));
            bwAvailabilityMap.put(zaPathName, bwMaps.get(ZA));

            // Store the path pair map
            pathPairMap.put(azPathName, zaPathName);
            pathPairMap.put(zaPathName, azPathName);

            // Store the pathname to path map
            pathNameMap.put(azPathName, azPath);
            pathNameMap.put(zaPathName, zaPath);
        }

        return pathNum;
    }

    private Integer processFailedBlueprint(RequestedBlueprintE reqBlueprint, BandwidthAvailabilityRequest request,
                                        Map<String, Integer> minAvailableBwMap,
                                        Map<String, Map<Instant, Integer>> bwAvailabilityMap,
                                        Map<String, String> pathPairMap,
                                        Map<String, List<String>> pathNameMap, Integer pathNum) {

        Map<Instant, Integer> bwMap = new HashMap<>();
        bwMap.put(request.getStartDate().toInstant(), 0);
        bwMap.put(request.getEndDate().toInstant(), 0);
        RequestedVlanPipeE reqPipe = reqBlueprint.getVlanFlow().getPipes().iterator().next();
        List<String> azEro = reqPipe.getAzERO().size() > 0 ? reqPipe.getAzERO():
                Arrays.asList(reqPipe.getAJunction().getDeviceUrn(), reqPipe.getZJunction().getDeviceUrn());
        List<String> zaEro = reqPipe.getAzERO().size() > 0 ? reqPipe.getAzERO():
                Arrays.asList(reqPipe.getZJunction().getDeviceUrn(), reqPipe.getAJunction().getDeviceUrn());

        String azPathName = "Az"+pathNum;
        String zaPathName = "Za"+pathNum;
        pathNum++;

        pathNameMap.put(azPathName, azEro);
        pathNameMap.put(zaPathName, zaEro);

        pathPairMap.put(azPathName, zaPathName);
        pathPairMap.put(zaPathName, azPathName);

        minAvailableBwMap.put(azPathName, 0);
        minAvailableBwMap.put(zaPathName, 0);

        bwAvailabilityMap.put(azPathName, bwMap);
        bwAvailabilityMap.put(zaPathName, bwMap);

        return pathNum;
    }

    /**
     * Given a BW avail request, generate a list of requested blueprints
     */
    private List<RequestedBlueprintE> generateRequestedBlueprints(BandwidthAvailabilityRequest request,
                                                                  Boolean pairValid, Boolean pathValid,
                                                                  Topology topo){
        List<RequestedBlueprintE> blueprints = new ArrayList<>();

        if(pathValid) {
            for (Integer index = 0; index < request.getAzEros().size(); index++) {
                List<String> azERO = request.getAzEros().get(index);
                List<String> zaERO = request.getZaEros().get(index);

                // Build a junction instead if ERO is just: port -> device -> port
                if(isJunction(azERO, topo) && isJunction(zaERO, topo)){
                    log.info("Is a junction");
                    List<String> fixtures = new ArrayList<>(azERO);
                    fixtures.remove(1);
                    blueprints.add(entityBuilder.buildRequest(azERO.get(1), fixtures, request.getMinAzBandwidth(),
                            request.getMinZaBandwidth(), "any", "bwReq"));
                    log.info(blueprints.toString());
                }
                else {
                    blueprints.add(entityBuilder.buildRequest(azERO, zaERO, request.getMinAzBandwidth(),
                            request.getMinZaBandwidth(), "bwReq"));
                }
            }
        }
        if(pairValid) {
            //If source == dest, build a junction request instead
            if(request.getSrcDevice().equals(request.getDstDevice())){
                List<String> fixtures = new ArrayList<>(request.getSrcPorts());
                fixtures.addAll(request.getDstPorts());

                blueprints.add(entityBuilder.buildRequest(request.getSrcDevice(), fixtures, request.getMinAzBandwidth(),
                        request.getMinZaBandwidth(), "any", "bwReq"));
            }
            else{
                // Create one blueprint for the source, dest pair - PCE handles multiple paths
                // Determine survivability type for request
                //TODO: Support non-disjoint paths - Yen's Algorithm
                SurvivabilityType sType = SurvivabilityType.SURVIVABILITY_NONE;
                if (request.getDisjointPaths() == null || request.getDisjointPaths() || !request.getDisjointPaths()) {
                    sType = SurvivabilityType.SURVIVABILITY_TOTAL;
                }
                blueprints.add(entityBuilder.buildRequest(request.getSrcPorts(), request.getSrcDevice(),
                        request.getDstPorts(), request.getDstDevice(), request.getMinAzBandwidth(), request.getMinZaBandwidth(),
                        PalindromicType.PALINDROME, sType, "any", request.getNumPaths(), 1, 1, "bwReq"));
            }


        }
        return blueprints;
    }

    private boolean isJunction(List<String> path, Topology topo) {
        if(path.size() == 3){
            Optional<TopoVertex> fixOne = topo.getVertexByUrn(path.get(0));
            Optional<TopoVertex> device = topo.getVertexByUrn(path.get(1));
            Optional<TopoVertex> fixTwo = topo.getVertexByUrn(path.get(2));

            if(fixOne.isPresent() && device.isPresent() && fixTwo.isPresent()){
                return fixOne.get().getVertexType().equals(VertexType.PORT) &&
                        (device.get().getVertexType().equals(VertexType.ROUTER) || device.get().getVertexType().equals(VertexType.SWITCH)) &&
                        fixTwo.get().getVertexType().equals(VertexType.PORT);
            }
        }
        return false;
    }

    /**
     * Build a response given the passed in parameters.
     *
     * @param minAvailableBwMap - Maps a path name to the minimum available bandwidth value on that path.
     * @param bwAvailabilityMap   - Maps a path name to another map of time Instants --> min available bandwidth at that instant
     * @param pathPairMap   - Maps a path name to the matching reverse/forward path  name(i.e. AZ1 -> ZA1, and ZA1 -> AZ1)
     * @param pathNameMap - Maps a path name to a path
     * @return A bandwidth availability response
     */
    private BandwidthAvailabilityResponse buildResponse(Map<String, Integer> minAvailableBwMap,
                                                        Map<String, Map<Instant, Integer>> bwAvailabilityMap,
                                                        Map<String, String> pathPairMap,
                                                        Map<String, List<String>> pathNameMap) {
        return BandwidthAvailabilityResponse.builder()
                .minAvailableBwMap(minAvailableBwMap)
                .bwAvailabilityMap(bwAvailabilityMap)
                .pathPairMap(pathPairMap)
                .pathNameMap(pathNameMap)
                .build();
    }


    /**
     * Given a request, confirm if the requested path is in the topology.
     *
     * @param request - The bandwidth availability request
     * @return True if all requested nodes are in the system's topology, False otherwise.
     */
    private boolean validateEros(BandwidthAvailabilityRequest request, Topology topo) {
        Boolean erosSpecified = request.getAzEros().size() > 0 && request.getZaEros().size() > 0
                && request.getAzEros().size() == request.getZaEros().size();

        Boolean erosValid = false;
        if(erosSpecified){
            Boolean azValid = request.getAzEros()
                    .stream()
                    .map(path -> path.stream().allMatch(s -> topo.getVertexByUrn(s).isPresent()) && path.size() > 0)
                    .allMatch(status -> status);
            Boolean zaValid = request.getZaEros()
                    .stream()
                    .map(path -> path.stream().allMatch(s -> topo.getVertexByUrn(s).isPresent()) && path.size() > 0)
                    .allMatch(status -> status);
            erosValid =  azValid && zaValid;
        }
        return erosValid;
    }

    /**
     * Given a request, confirm if the requested source/destination ports/devices are in the topology.
     *
     * @param request - The bandwidth availability request
     * @return True if all requested nodes are in the system's topology, False otherwise.
     */
    private boolean validatePair(BandwidthAvailabilityRequest request, Topology topo) {
        Boolean pairSpecified = request.getSrcDevice() != null && request.getDstDevice() != null
                && request.getSrcPorts() != null && request.getDstPorts() != null && request.getDisjointPaths() != null
                && request.getNumPaths() != null;

        Boolean pairValid = false;
        if(pairSpecified){
            Boolean srcDevicePresent = topo.getVertexByUrn(request.getSrcDevice()).isPresent();
            Boolean srcPortsPresent = request.getSrcPorts().stream().allMatch(p -> topo.getVertexByUrn(p).isPresent());
            Boolean dstDevicePresent = topo.getVertexByUrn(request.getDstDevice()).isPresent();
            Boolean dstPortsPresent = request.getDstPorts().stream().allMatch(p -> topo.getVertexByUrn(p).isPresent());
            pairValid =  srcDevicePresent && srcPortsPresent && dstDevicePresent && dstPortsPresent;
        }
        return pairValid;
    }

    /**
     * Builds maps of the available bandwidth.
     *
     * @param rsvMap    Map of URNs to Reserved Bandwidths.
     * @param start     Start time for bandwidth maps.
     * @param end       End time for bandwidth maps.
     * @param azPath    All URN Strings in the A -> Z path
     * @param zaPath    All URN strings in the Z -> A path
     * @return Maps of available bandwidth.
     */
    private Map<String, Map<Instant, Integer>> buildMaps(Map<String, List<ReservedBandwidthE>> rsvMap,
                                                         Instant start,
                                                         Instant end,
                                                         List<String> azPath, List<String> zaPath) {

        // Initialize the container for currently available INGRESS and EGRESS at each URN
        Map<String, Map<String, Integer>> curUrnBw = new HashMap<>();     // Available ingress and egress at urn.
        curUrnBw.put(INGRESS, new HashMap<>());
        curUrnBw.put(EGRESS, new HashMap<>());

        // Initialize the container for the two bandwidth maps
        Map<String, Map<Instant, Integer>> bwMaps = new HashMap<>();    // A->Z and Z->A bandwidth Maps.
        bwMaps.put(AZ, new HashMap<>());
        bwMaps.put(ZA, new HashMap<>());

        // Map of path direction -> urns -> Ingress and/or Egress.
        Map<String, Map<String, List<String>>> urnTables = buildUrnTables(azPath, zaPath);

        // All URNs used in the URN tables
        Set<String> urns = urnTables.values().stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toSet());

        // Sorted list of bandwidth events.
        List<BwEvent> bwEvents = abstractEvents(rsvMap);

        // Initialize each URN's available bandwidth to max supported.
        for (String urn : urns) {
            UrnE urn_e = urnRepo.findByUrn(urn).orElseThrow(NoSuchElementException::new);
            curUrnBw.get(INGRESS).put(urn, urn_e.getReservableBandwidth().getIngressBw());
            curUrnBw.get(EGRESS).put(urn, urn_e.getReservableBandwidth().getEgressBw());
        }

        // Handle case with no bandwidth events.
        //log.info("BW Events: " + bwEvents);
        if (bwEvents.isEmpty()) {
            for (String path : bwMaps.keySet()) {
                Integer min = findMin(urns, curUrnBw, urnTables, path);
                // NOTE: puts a data point in for both the start and the end
                bwMaps.get(path).put(start, min);
                bwMaps.get(path).put(end, min);
            }
        }

        Instant previousTime = null;
        // Loop through bandwidth events.
        for (Integer eventIndex = 0; eventIndex < bwEvents.size(); eventIndex++) {
            BwEvent bwEvent = bwEvents.get(eventIndex);
            Instant currentTime = bwEvent.getTime();
            // Event is before start of interval.
            if (!currentTime.isAfter(start)) {
                // Update available bandwidths.
                updateAvailableBw(curUrnBw, bwEvent);
                // If this is the last event, or if the next event is after the start time, update the bandwidth map
                if (eventIndex == bwEvents.size() - 1 || bwEvents.get(eventIndex + 1).getTime().isAfter(start)) {
                    for (String path : bwMaps.keySet()) {
                        Integer min = findMin(urns, curUrnBw, urnTables, path);
                        bwMaps.get(path).put(start, min);
                        // If this is the last event, add a point for the end
                        if (eventIndex == bwEvents.size() - 1) {
                            bwMaps.get(path).put(end, min);
                        }
                    }
                }
            }

            // Event is in interval of interest.
            else if (currentTime.isAfter(start) && !currentTime.isAfter(end)) {
                // If this is the first event, then create a starting point
                if (eventIndex == 0) {
                    for (String path : bwMaps.keySet()) {
                        Integer min = findMin(urns, curUrnBw, urnTables, path);
                        bwMaps.get(path).put(start, min);
                    }
                }
                // Update available bandwidths.
                updateAvailableBw(curUrnBw, bwEvent);
                // If this is the last event, or if the next event is after the current time, update the bandwidth map
                if (eventIndex == bwEvents.size() - 1 || bwEvents.get(eventIndex + 1).getTime().isAfter(currentTime)) {
                    for (String path : bwMaps.keySet()) {
                        Integer min = findMin(urns, curUrnBw, urnTables, path);
                        bwMaps.get(path).put(currentTime, min);
                        // If this is the last event, add a point for the end
                        if (eventIndex == bwEvents.size() - 1 && currentTime.isBefore(end)) {
                            bwMaps.get(path).put(end, min);
                        }
                    }
                }
            }
            // Event is after interval of interest.
            else {
                if (previousTime == null) {
                    previousTime = start;
                }
                if (previousTime.isBefore(end)) {
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

        log.info("Bandwidth maps: " + bwMaps.toString());
        return bwMaps;
    }

    /**
     * Update the available bandwidth in the curUrnBw map at thr URN associated with the passed in BwEvent.
     *
     * @param curUrnBw - The map of currently available bandwidth at each URN
     * @param bwEvent  - The bandwidth event (with associated URN and change in Ingress/Egress bandwidth).
     */
    private void updateAvailableBw(Map<String, Map<String, Integer>> curUrnBw, BwEvent bwEvent) {
        curUrnBw.get(INGRESS).put(bwEvent.getUrn(), curUrnBw.get(INGRESS).get(bwEvent.getUrn()) + bwEvent.getInBw());
        curUrnBw.get(EGRESS).put(bwEvent.getUrn(), curUrnBw.get(EGRESS).get(bwEvent.getUrn()) + bwEvent.getEgBw());
    }

    /**
     * Find the minimum bandwidth available across the URNs in the curUrnBw map for a given direction (path).
     *
     * @param urns      - Set of URNs
     * @param curUrnBw  - Map of available Ingress/Egress bandwidth at different URNs
     * @param urnTables - Map determining whether a URN is used for ingress/egress, or both, for a direction
     * @param path      - The AZ or ZA direction
     * @return The minimum available bandwidth
     */
    private Integer findMin(Set<String> urns,
                            Map<String, Map<String, Integer>> curUrnBw,
                            Map<String, Map<String, List<String>>> urnTables,
                            String path) {
        Integer min = Integer.MAX_VALUE;
        for (String urn : urns) {
            Integer urnBw = findBw(urn, curUrnBw, urnTables.get(path).get(urn));
            min = urnBw < min ? urnBw : min;
        }
        return min;
    }


    /**
     * Returns the bandwidth available at URN.
     *
     * @param urn     URN of interest.
     * @param curBw   Map of ingress/egress -> urn -> available bandwidth.
     * @param usedBWs List specifying whether the path uses the URNs ingress, egress, or both.
     * @return The bandwidth available at URN.
     */
    private Integer findBw(String urn,
                           Map<String, Map<String, Integer>> curBw,
                           List<String> usedBWs) {
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
     *
     * @param azPath - All URN strings in the A->Z Path
     * @param zaPath - All URN strings in the Z->A path
     * @return Map of URNs used for each path.
     */
    private Map<String, Map<String, List<String>>> buildUrnTables(List<String> azPath, List<String> zaPath) {

        boolean isIngress;
        List<UrnE> azUrns = entityDecomposer.translateStringListToUrns(azPath);
        List<UrnE> zaUrns = entityDecomposer.translateStringListToUrns(zaPath);
        Map<String, Map<String, List<String>>> urnTables = new HashMap<>();

        urnTables.put(AZ, new HashMap<>());
        urnTables.put(ZA, new HashMap<>());

        Integer aDeviceIndex = -1;
        UrnE aDevice = null;
        Integer zDeviceIndex = -1;
        UrnE zDevice = null;



        for(Integer index = 0; index < azUrns.size(); index++){
            UrnE urn = azUrns.get(index);
            if(aDeviceIndex == -1){
                if(urn.getUrnType().equals(UrnType.IFCE)){
                    urnTables.get(AZ).putIfAbsent(urn.getUrn(), new ArrayList<>());
                    urnTables.get(AZ).get(urn.getUrn()).add(INGRESS);
                    urnTables.get(ZA).putIfAbsent(urn.getUrn(), new ArrayList<>());
                    urnTables.get(ZA).get(urn.getUrn()).add(EGRESS);
                }
                else{
                    aDeviceIndex = index;
                    aDevice = urn;
                }
            } else {
                break;
            }
        }

        for(Integer index = 0; index < zaUrns.size(); index++){
            UrnE urn = zaUrns.get(index);
            if(zDeviceIndex == -1){
                if(urn.getUrnType().equals(UrnType.IFCE)){
                    urnTables.get(AZ).putIfAbsent(urn.getUrn(), new ArrayList<>());
                    urnTables.get(AZ).get(urn.getUrn()).add(EGRESS);
                    urnTables.get(ZA).putIfAbsent(urn.getUrn(), new ArrayList<>());
                    urnTables.get(ZA).get(urn.getUrn()).add(INGRESS);
                }
                else{
                    zDeviceIndex = index;
                    zDevice = urn;
                }
            } else {
                break;
            }
        }

        if(azUrns.size() > 3 && zaUrns.size() > 3) {
            List<UrnE> intermediateAzUrns = azUrns.subList(aDeviceIndex + 1, azUrns.indexOf(zDevice)).stream()
                    .filter(u -> u.getReservableBandwidth() != null).collect(Collectors.toList());
            List<UrnE> intermediateZaUrns = zaUrns.subList(zDeviceIndex + 1, zaUrns.indexOf(aDevice)).stream()
                    .filter(u -> u.getReservableBandwidth() != null).collect(Collectors.toList());

            // Go through the intermediate URNs
            isIngress = false;

            for (UrnE urn : intermediateAzUrns) {
                urnTables.get(AZ).putIfAbsent(urn.getUrn(), new ArrayList<>());

                if (isIngress)
                    urnTables.get(AZ).get(urn.getUrn()).add(INGRESS);
                else
                    urnTables.get(AZ).get(urn.getUrn()).add(EGRESS);

                // Alternate ingress/egress.
                isIngress = Boolean.logicalXor(isIngress, true);
            }

            isIngress = false;

            for (UrnE urn_e : intermediateZaUrns) {
                urnTables.get(ZA).putIfAbsent(urn_e.getUrn(), new ArrayList<>());

                if (isIngress)
                    urnTables.get(ZA).get(urn_e.getUrn()).add(INGRESS);
                else
                    urnTables.get(ZA).get(urn_e.getUrn()).add(EGRESS);

                // Alternate ingress/egress.
                isIngress = Boolean.logicalXor(isIngress, true);
            }
        }

        return urnTables;
    }


    /**
     * Abstracts bandwidth events from a map of URNs to Reserved Bandwidths.
     *
     * @param rsvMap Map of URNs to Reserved Bandwidths.
     * @return List of bandwidth events sorted chronologically.
     */
    private List<BwEvent> abstractEvents(Map<String, List<ReservedBandwidthE>> rsvMap) {
        List<BwEvent> eventList = new ArrayList<>();

        for (String urn : rsvMap.keySet()) {
            for (ReservedBandwidthE rsv : rsvMap.get(urn)) {
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
     *
     * @param bwMaps - A mapping from "AZ" or "ZA" to a Map of time Instants and the bandwdith available at those times
     * @return A map from "minAZ", "maxAZ", "minZA", and "MaxAZ" to corresponding integer bandwidth values.
     */
    private Map<String, Integer> getMinimumsAndMaximums(Map<String, Map<Instant, Integer>> bwMaps) {
        Map<Instant, Integer> azEvents = bwMaps.get(AZ);
        Map<Instant, Integer> zaEvents = bwMaps.get(ZA);

        Integer minAZ = Integer.MAX_VALUE;
        Integer minZA = Integer.MAX_VALUE;
        Integer maxAZ = 0;
        Integer maxZA = 0;

        for (Integer azBw : azEvents.values()) {
            if (azBw < minAZ) {
                minAZ = azBw;
            }
            if (azBw > maxAZ) {
                maxAZ = azBw;
            }
        }
        for (Integer zaBw : zaEvents.values()) {
            if (zaBw < minZA) {
                minZA = zaBw;
            }
            if (zaBw > maxZA) {
                maxZA = zaBw;
            }
        }

        Map<String, Integer> minMaxMap = new HashMap<>();
        minMaxMap.put(AZMIN, minAZ);
        minMaxMap.put(ZAMIN, minZA);
        minMaxMap.put(AZMAX, maxAZ);
        minMaxMap.put(ZAMAX, maxZA);

        return minMaxMap;
    }

    public PortBandwidthAvailabilityResponse getBandwidthAvailabilityOnAllPorts(PortBandwidthAvailabilityRequest bwRequest)
    {
        Map<String, List<Integer>> urnAvailList = new HashMap<>();
        PortBandwidthAvailabilityResponse bwResponse = new PortBandwidthAvailabilityResponse();

        Optional<List<ReservedBandwidthE>> allBwOpt = bwRepo.findOverlappingInterval(bwRequest.getStartDate().toInstant(), bwRequest.getEndDate().toInstant());
        if(allBwOpt.isPresent()) {
            List<ReservedBandwidthE> allBW = allBwOpt.get();

            Map<String, Map<String, Integer>> bwAvailabilityMap = bwService.buildBandwidthAvailabilityMapFromUrnRepo(allBW);
            for(String urn : bwAvailabilityMap.keySet()){
                Map<String, Integer> ingressEgressMap = bwAvailabilityMap.get(urn);
                List<Integer> ingressEgress = Arrays.asList(ingressEgressMap.get("Ingress"), ingressEgressMap.get("Egress"));
                urnAvailList.put(urn, ingressEgress);
            }
        }

        bwResponse.setBwAvailabilityMap(urnAvailList);

        return bwResponse;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class BwEvent {
        private String urn;
        private Instant time;
        private Integer inBw;
        private Integer egBw;
    }
}