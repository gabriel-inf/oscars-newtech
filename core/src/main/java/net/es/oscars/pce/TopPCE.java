package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.enums.DeviceType;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.pce.exc.InvalidUrnException;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.BidirectionalPathE;
import net.es.oscars.topo.ent.EdgeE;
import net.es.oscars.topo.ent.ReservableVlanE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TopPCE {

    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private Layer3PCE layer3PCE;

    @Autowired
    private TranslationPCE transPCE;

    @Autowired
    private PalindromicalPCE palindromicalPCE;

    @Autowired
    private NonPalindromicalPCE nonPalindromicPCE;

    @Autowired
    private EroPCE eroPCE;

    @Autowired
    private SurvivabilityPCE survivabilityPCE;

    @Autowired
    private VlanService vlanService;

    @Autowired
    private BandwidthService bwService;

    @Autowired
    private UrnRepository urnRepo;


    /**
     * Given a requested Blueprint (made up of a VLAN or Layer3 Flow) and a Schedule Specification, attempt
     * to reserve available resources to meet the demand. If it is not possible, return an empty Optional<ReservedBlueprintE>
     *
     * @param requested - Requested blueprint
     * @param schedSpec - Requested schedule
     * @return ReservedBlueprint containing the reserved resources, or an empty Optional if the reservation is not possible.
     * @throws PCEException
     * @throws PSSException
     */
    public Optional<ReservedBlueprintE> makeReserved(RequestedBlueprintE requested, ScheduleSpecificationE schedSpec,
                                                     List<Date> reservedSched) throws PCEException, PSSException {

        // Verify that the input is valid
        verifyRequested(requested);

        // Initialize an empty Optional<>
        Optional<ReservedBlueprintE> reserved = Optional.empty();

        log.info("Handling Request");
        // Retrieve the VLAN flow
        RequestedVlanFlowE req_f = requested.getVlanFlow();

        // Get flow parameters
        String connId = req_f.getContainerConnectionId();
        Integer minPipes = req_f.getMinPipes();
        Integer maxPipes = req_f.getMaxPipes();


        // Generate start and end combos from the lists
        List<Date> startDates = schedSpec.getStartDates();
        List<Date> endDates = schedSpec.getEndDates();

        List<List<Date>> ranges = new ArrayList<>();
        for(Date startDate : startDates){
            for(Date endDate : endDates){
                ranges.add(Arrays.asList(startDate, endDate));
            }
        }

        // Sort ranges by duration - smallest first
        ranges.sort(Comparator.comparing(r -> getDuration(r.get(0), r.get(1))));

        List<Long> durations = ranges.stream().map(r -> getDuration(r.get(0), r.get(1))).collect(Collectors.toList());

        // Go through each range, attempt to reserve resources for each range
        // If you hit a range that does not work, later ranges will not work either
        // Use the largest range that works (if any)
        List<Boolean> validRanges = ranges.stream().map(r -> false).collect(Collectors.toList());

        // Store results per range
        List<Set<ReservedMplsPipeE>> mplsPipesPerRange = validRanges.stream().map(v -> new HashSet<ReservedMplsPipeE>())
                .collect(Collectors.toList());
        List<Set<ReservedEthPipeE>> ethPipesPerRange = validRanges.stream().map(v -> new HashSet<ReservedEthPipeE>())
                .collect(Collectors.toList());
        List<Set<ReservedVlanJunctionE>> junctionsPerRange = validRanges.stream().map(v -> new HashSet<ReservedVlanJunctionE>())
                .collect(Collectors.toList());
        List<Set<BidirectionalPathE>> allPathsPerRange = validRanges.stream().map(v -> new HashSet<BidirectionalPathE>())
                .collect(Collectors.toList());

        Integer chosenRangeIndex = -1;

        for(Integer rangeIndex = 0; rangeIndex < ranges.size(); rangeIndex++) {

            // Skip ranges where it is less than the minimum duration
            if(schedSpec.getMinimumDuration() != null && durations.get(rangeIndex) < schedSpec.getMinimumDuration()){
                continue;
            }
            // Otherwise, test if the request can be satisfied in this duration
            Boolean isValid = handleRequestForRange(req_f.getJunctions(), req_f.getPipes(), minPipes, maxPipes, connId, ranges,
                    mplsPipesPerRange, ethPipesPerRange, junctionsPerRange, allPathsPerRange, rangeIndex);

            validRanges.set(rangeIndex, isValid);

            // If valid, mark this as the next chosen index
            // If we reached the minimum duration threshold, break
            if(isValid){
                chosenRangeIndex = rangeIndex;
                if(schedSpec.getMinimumDuration() == null || durations.get(rangeIndex) >= schedSpec.getMinimumDuration()){
                    break;
                }
            }
        }

        // If no valid ranges
        if(chosenRangeIndex == -1){
            return reserved;
        }
        log.info("PCE success.");

        // If you make it to this point, then you must have at least one range where the reservation can be made
        // and meets the minimum duration requirement

        // Make the reserved flow
        ReservedVlanFlowE res_f = ReservedVlanFlowE.builder()
                .junctions(junctionsPerRange.get(chosenRangeIndex))
                .ethPipes(ethPipesPerRange.get(chosenRangeIndex))
                .mplsPipes(mplsPipesPerRange.get(chosenRangeIndex))
                .allPaths(allPathsPerRange.get(chosenRangeIndex))
                .containerConnectionId(requested.getContainerConnectionId())
                .build();

        // Build the reserved Blueprint
        reserved = Optional.of(ReservedBlueprintE.builder().vlanFlow(res_f).containerConnectionId(res_f.getContainerConnectionId()).build());

        // Store the chosen dates
        reservedSched.add(ranges.get(chosenRangeIndex).get(0));
        reservedSched.add(ranges.get(chosenRangeIndex).get(1));
        return reserved;
    }

    private List<RequestedVlanPipeE> sortPipes(Set<RequestedVlanPipeE> pipes, Integer minPipes,
                                               Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> repoVlans) {

        // Sort any pipes with priority less than MAX_INT
        Set<RequestedVlanPipeE> priorityPipes = pipes.stream()
                .filter(p -> p.getPriority() < Integer.MAX_VALUE)
                .collect(Collectors.toSet());
        Set<RequestedVlanPipeE> otherPipes = pipes.stream()
                .filter(p -> p.getPriority() == Integer.MAX_VALUE)
                .collect(Collectors.toSet());

        List<RequestedVlanPipeE> sortedPipes = new ArrayList<>();
        if(priorityPipes.size() > 0){
            Comparator<RequestedVlanPipeE> byPriority = Comparator.comparing(RequestedVlanPipeE::getPriority);
            sortedPipes = priorityPipes.stream().sorted(byPriority).collect(Collectors.toList());
        }

        // Sort the remaining pipes by hop count, bandwidth, URNs

        // Only calculate hop count if you're doing Manycast
        // As indicated by only needing a minimum number of pipes less than the pipe set size
        Map<RequestedVlanPipeE, Integer> hopCountMap = minPipes < pipes.size() ?
                buildPathHopCountMap(otherPipes, bwAvailMap, repoVlans) : new HashMap<>();

        Comparator<RequestedVlanPipeE> byPathHopCount = Comparator.comparing(hopCountMap::get);
        // Sort by largest bandwidth first
        Comparator<RequestedVlanPipeE> byAzMbps = Comparator.comparing(RequestedVlanPipeE::getAzMbps).reversed();
        // Sort by largest bandwidth first
        Comparator<RequestedVlanPipeE> byZaMbps = Comparator.comparing(RequestedVlanPipeE::getZaMbps).reversed();

        Comparator<RequestedVlanPipeE> byAJunction = (p1, p2) ->
                p1.getAJunction().getDeviceUrn().compareToIgnoreCase(p2.getAJunction().getDeviceUrn());
        Comparator<RequestedVlanPipeE> byZJunction = (p1, p2) ->
                p1.getZJunction().getDeviceUrn().compareToIgnoreCase(p2.getZJunction().getDeviceUrn());

        // Only compare by hop count if doing manycast
        if(minPipes < pipes.size()){
            sortedPipes.addAll(otherPipes.stream().sorted(byPathHopCount.thenComparing(byAzMbps).thenComparing(byZaMbps)
                    .thenComparing(byAJunction).thenComparing(byZJunction)).collect(Collectors.toList()));
        }
        else{
            sortedPipes.addAll(otherPipes.stream().sorted(byAzMbps.thenComparing(byZaMbps)
                    .thenComparing(byAJunction).thenComparing(byZJunction)).collect(Collectors.toList()));
        }
        return sortedPipes;
    }

    private Map<RequestedVlanPipeE, Integer> buildPathHopCountMap(Set<RequestedVlanPipeE> pipes,
                                                               Map<String, Map<String, Integer>> bwAvailMap,
                                                               List<ReservedVlanE> rsvVlans) {

        Map<RequestedVlanPipeE, Integer> pathLengthMap = new HashMap<>();
        for(RequestedVlanPipeE pipe : pipes){
            // Sum up the hop count for all paths
            Map<String, List<TopoEdge>> paths = findShortestConstrainedPath(pipe, bwAvailMap, rsvVlans);
            Integer total = paths!= null ? paths.values().stream().mapToInt(List::size).sum() : Integer.MAX_VALUE;
            pathLengthMap.put(pipe, total);
        }
        return pathLengthMap;
    }

    private List<RequestedVlanJunctionE> sortJunctions(Set<RequestedVlanJunctionE> junctions) {
        return junctions.stream().sorted((a, b) -> a.getDeviceUrn().compareToIgnoreCase(b.getDeviceUrn())).collect(Collectors.toList());
    }

    private Boolean handleRequestForRange(Set<RequestedVlanJunctionE> junctions,
                                          Set<RequestedVlanPipeE> pipes, Integer minPipes, Integer maxPipes,
                                          String connId,
                                          List<List<Date>> ranges,
                                          List<Set<ReservedMplsPipeE>> mplsPipesPerRange,
                                          List<Set<ReservedEthPipeE>> ethPipesPerRange,
                                          List<Set<ReservedVlanJunctionE>> junctionsPerRange,
                                          List<Set<BidirectionalPathE>> allPathsPerRange,  Integer rangeIndex) throws PCEException, PSSException {
        // Get the start and end date
        Date start = ranges.get(rangeIndex).get(0);
        Date end = ranges.get(rangeIndex).get(1);

        // Create temporary storage for reserved pipes and junctions
        Set<ReservedMplsPipeE> reservedMplsPipes = new HashSet<>();
        Set<ReservedEthPipeE> reservedEthPipes = new HashSet<>();
        Set<ReservedVlanJunctionE> reservedEthJunctions = new HashSet<>();
        Set<BidirectionalPathE> allPaths = new HashSet<>();


        // Get map of parent device vertex -> set of port vertices
        Map<String, Set<String>> deviceToPortMap = topoService.buildDeviceToPortMap().getMap();
        Map<String, String> portToDeviceMap = topoService.buildPortToDeviceMap(deviceToPortMap);

        // Initialize the bandwidth availability map
        List<ReservedBandwidthE> reservedBandwidths = bwService.getReservedBandwidthFromRepo(start, end);
        Map<String, Map<String, Integer>> bwAvailMap = bwService.buildBandwidthAvailabilityMapFromUrnRepo(reservedBandwidths);

        List<ReservedVlanE> repoVlans = vlanService.getReservedVlansFromRepo(start, end);

        // Store the min/max number of pipes needed
        // Sort the junctions and pipes - provide a consistent ordering
        List<RequestedVlanJunctionE> reqJunctions = junctions.size()>1 ? sortJunctions(junctions) : new ArrayList<>(junctions);
        List<RequestedVlanPipeE> reqPipes = pipes.size()>1? sortPipes(pipes, minPipes, bwAvailMap, repoVlans) : new ArrayList<>(pipes);


        // Attempt to reserve simple junctions
        log.info("Handling Simple Junctions");
        Set<ReservedVlanJunctionE> simpleJunctions = handleRequestedJunctions(reqJunctions, start, end, deviceToPortMap,
                portToDeviceMap, connId, bwAvailMap, repoVlans);
        log.info("All simple junctions handled");
        // If not all junctions were able to be reserved, no later range can work
        // Break the loop, then check for any valid ranges
        if (simpleJunctions.size() != reqJunctions.size()) {
            return false;
        }

        // Clone the bandwidth availability map: multiple attempts at reserving pipes won't affect the original
        Map<String, Map<String, Integer>> clonedAvailMap = new HashMap<>(bwAvailMap);

        // Keep track of the number of successfully reserved pipes (numReserved)
        // Attempt to reserve all requested pipes
        log.info("Starting to handle pipes");
        Integer numReserved = handleRequestedPipes(reqPipes, start, end, simpleJunctions, reservedMplsPipes, reservedEthPipes,
                deviceToPortMap, portToDeviceMap, allPaths, connId, clonedAvailMap, repoVlans, maxPipes);

        // If pipes were not able to be reserved in the original order, try reversing the order pipes are attempted
        if (numReserved < minPipes && (reqPipes.size() > 1)) {
            log.info("Insufficient number of pipes reserved, trying reverse order.");
            log.info("Num reserved: " + numReserved + ", Num Required: " + minPipes);
            Collections.reverse(reqPipes);
            reservedEthPipes = new HashSet<>();
            reservedMplsPipes = new HashSet<>();
            reservedEthJunctions = new HashSet<>();
            allPaths = new HashSet<>();
            clonedAvailMap = new HashMap<>(bwAvailMap);
            numReserved = handleRequestedPipes(reqPipes, start, end, simpleJunctions, reservedMplsPipes, reservedEthPipes,
                    deviceToPortMap, portToDeviceMap, allPaths, connId, clonedAvailMap, repoVlans, maxPipes);
        }

        // If the pipes still cannot be reserved, no later range can work
        // Break the loop, then check for any valid ranges
        if (numReserved < minPipes) {
            log.info("Insufficient number of pipes reserved, request failed.");
            log.info("Num reserved: " + numReserved + ", Num Required: " + minPipes);
            return false;
        }
        // All pipes were successfully found, store the reserved resources
        Set<ReservedVlanJunctionE> reservedJunctions = new HashSet<>(simpleJunctions);
        reservedJunctions.addAll(reservedEthJunctions);

        // Add paths for each simple junction
        addJunctionPaths(allPaths, reservedJunctions);

        // Store the pipes, junctions, and paths
        mplsPipesPerRange.set(rangeIndex, reservedMplsPipes);
        ethPipesPerRange.set(rangeIndex, reservedEthPipes);
        junctionsPerRange.set(rangeIndex, reservedJunctions);
        allPathsPerRange.set(rangeIndex, allPaths);

        return true;
    }

    public Long getDuration(Date start, Date end){
        return ChronoUnit.MINUTES.between(start.toInstant(), end.toInstant());
    }


    private Set<ReservedVlanJunctionE> handleRequestedJunctions(List<RequestedVlanJunctionE> reqJunctions, Date start, Date end,
                                                                Map<String, Set<String>> deviceToPortMap,
                                                                Map<String, String> portToDeviceMap, String connId,
                                                                Map<String, Map<String, Integer>> bwAvailMap,
                                                                List<ReservedVlanE> repoVlans) throws PCEException, PSSException {

        Set<ReservedVlanJunctionE> simpleJunctions = new HashSet<>();
        List<ReservedBandwidthE> newestBandwidths = new ArrayList<>();
        for(RequestedVlanJunctionE reqJunction: reqJunctions) {

            // Update list of reserved VLAN IDs
            List<ReservedVlanE> rsvVlans = vlanService.createReservedVlanList(simpleJunctions, new HashSet<>());
            rsvVlans.addAll(repoVlans);

            ReservedVlanJunctionE junction = null;
            junction = transPCE.reserveSimpleJunction(reqJunction, bwAvailMap, rsvVlans, deviceToPortMap, portToDeviceMap, start, end, connId);
            if (junction != null) {
                simpleJunctions.add(junction);
            }

            // Update list of reserved bandwidths
            if(junction != null){
                Set<ReservedVlanJunctionE> junctionSet = new HashSet<>();
                junctionSet.add(junction);
                newestBandwidths = bwService.getReservedBandwidthsFromJunctions(junctionSet);
            }
            // Update the availability map
            bwService.amendBandwidthAvailabilityMap(bwAvailMap, newestBandwidths);
        }
        return simpleJunctions;
    }
    /**
     * Given a list of requested pipes and a schedule, attempt to reserve junctions & pipes to implement the request.
     * Sets of currently reserved junctions and pipes are passed in to track how many resources have already been reserved.
     *
     * @param pipes             - The requested pipes
     * @param start             - The requested start date
     * @param end               - The requested end date
     * @param simpleJunctions   - The currently reserved independent junctions
     * @param reservedMplsPipes - The currently reserved MPLS pipes
     * @param reservedEthPipes  - The currently reserved Ethernet pipes
     * @param deviceToPortMap   - Map of matching ports for each device
     * @param portToDeviceMap   - Map of matching device for each port
     * @param connectionId      - The unique ID of the connection containing the requested pipes
     * @param bwAvailMap        - Mapping of Ingress and Egress bandwidth available at each URN
     * @param maxPipes          - The maximum number of pipes that need to be reserved
     * @return The number of requested pipes which were able to be reserved
     */
    private Integer handleRequestedPipes(List<RequestedVlanPipeE> pipes, Date start, Date end,
                                         Set<ReservedVlanJunctionE> simpleJunctions, Set<ReservedMplsPipeE> reservedMplsPipes,
                                         Set<ReservedEthPipeE> reservedEthPipes, Map<String, Set<String>> deviceToPortMap,
                                         Map<String, String> portToDeviceMap, Set<BidirectionalPathE> allPaths,
                                         String connectionId, Map<String, Map<String, Integer>> bwAvailMap,
                                         List<ReservedVlanE> repoVlans, Integer maxPipes) {
        // The number of requested pipes successfully reserved
        Integer numReserved = 0;


        // Loop through all requested pipes
        for (RequestedVlanPipeE pipe : pipes) {

            // Stop when you've reserved enough pipes
            if(Objects.equals(numReserved, maxPipes)){
                break;
            }

            // Clone the initial map, each pipe will use an updated version of the previous pipe's BW map
            Map<String, Map<String, Integer>> pipeBwAvailMap = new HashMap<>(bwAvailMap);

            // Update list of reserved VLAN IDs
            List<ReservedVlanE> rsvVlans = vlanService.createReservedVlanList(simpleJunctions, reservedEthPipes);
            rsvVlans.addAll(repoVlans);

            // Find the shortest path(s) for the pipe, build a map for the AZ and ZA path(s)
            Map<String, List<TopoEdge>> eroMapForPipe = findShortestConstrainedPath(pipe, pipeBwAvailMap, rsvVlans);

            // If the paths are valid, attempt to reserve the resources
            if (verifyEros(eroMapForPipe)) {
                // Set of reserved pipes for this requested pipe
                Set<ReservedEthPipeE> newEthPipes = new HashSet<>();
                Set<ReservedMplsPipeE> newMplsPipes = new HashSet<>();

                boolean successful = true;
                // Go through all AZ/ZA pairs. May just be one.
                for (Integer i = 1; i < eroMapForPipe.size() / 2 + 1; i++) {
                    // Get the AZ and ZA paths
                    List<TopoEdge> azERO = eroMapForPipe.size() == 2 ? eroMapForPipe.get("az") : eroMapForPipe.get("az" + i);
                    List<TopoEdge> zaERO = eroMapForPipe.size() == 2 ? eroMapForPipe.get("za") : eroMapForPipe.get("za" + i);

                    // Store the paths
                    allPaths.add(BidirectionalPathE.builder()
                            .azPath(includeFixtures(convertTopoEdgePathToEdges(azERO), pipe, true))
                            .zaPath(includeFixtures(convertTopoEdgePathToEdges(zaERO), pipe, false))
                            .build());

                    // Store the response from translation PCE
                    TranslationPCEResponse transPceResponse;

                    // Try to get the reserved resources
                    try {
                        transPceResponse = transPCE.reserveRequestedPipe(pipe, azERO, zaERO, pipeBwAvailMap, rsvVlans,
                                deviceToPortMap, portToDeviceMap, start, end, connectionId);
                        // Check if
                        if(transPceResponse != null){
                            newEthPipes.addAll(transPceResponse.getEthPipes());
                            newMplsPipes.addAll(transPceResponse.getMplsPipes());

                            // Update the pipe's bandwidth availability map
                            List<ReservedBandwidthE> newBandwidths = bwService.getReservedBandwidthsFromEthPipes(newEthPipes);
                            newBandwidths.addAll(bwService.getReservedBandwidthsFromMplsPipes(newMplsPipes));
                            bwService.amendBandwidthAvailabilityMap(pipeBwAvailMap, newBandwidths);
                        }
                        else{
                            successful = false;
                            break;
                        }
                    }
                    // If it failed, decrement the number reserved
                    catch (Exception e) {
                        log.info(e.getMessage());
                        // commented out as I don't like stack traces in normal operation
                        // e.printStackTrace();
                        successful = false;
                        break;
                    }

                }

                // If you successfully reserved all paths for the requested pipe
                if(successful){
                    // Update the bandwidth availability map for subsequent pipes
                    bwAvailMap = new HashMap<>(pipeBwAvailMap);

                    // Store the new reserved pipes
                    reservedEthPipes.addAll(newEthPipes);
                    reservedMplsPipes.addAll(newMplsPipes);

                    // Update the number of reserved pipes
                    numReserved++;
                }
            }


        }
        return numReserved;
    }

    /**
     * Given a requested pipe and schedule, find the shortest path that meets the demand given what has been requested
     * so far.
     *
     * @param pipe          - The requested pipe.
     * @param bwAvailMap    - A map of available "Ingress" and "Egress" bandwidth at each URN.
     * @param rsvVlans      - A list of all reserved VLANs (so far)
     * @return A map containing the AZ and ZA shortest paths
     */
    private Map<String, List<TopoEdge>> findShortestConstrainedPath(RequestedVlanPipeE pipe,
                                                                    Map<String, Map<String, Integer>> bwAvailMap,
                                                                    List<ReservedVlanE> rsvVlans) {
        //log.info("Computing Shortest Constrained Path");
        Map<String, List<TopoEdge>> eroMap = null;

        try {
            if (!pipe.getEroSurvivability().equals(SurvivabilityType.SURVIVABILITY_NONE) && pipe.getNumPaths() > 1) {
                //log.info("Entering Survivability PCE");
                eroMap = survivabilityPCE.computeSurvivableERO(pipe, bwAvailMap, rsvVlans);
                //log.info("Exiting Survivability PCE");
            } else if (!pipe.getAzERO().isEmpty() && !pipe.getZaERO().isEmpty()) {
                //log.info("Attempting to reserve specified Explicit Route Object");
                eroMap = eroPCE.computeSpecifiedERO(pipe, bwAvailMap, rsvVlans);
            } else if (pipe.getEroPalindromic().equals(PalindromicType.PALINDROME)) {
                //log.info("Entering Palindromical PCE");
                eroMap = palindromicalPCE.computePalindromicERO(pipe, bwAvailMap, rsvVlans);       // A->Z ERO is palindrome of Z->A ERO
                //log.info("Exiting Palindromical PCE");
            } else {
                //log.info("Entering NON-Palindromical PCE");
                eroMap = nonPalindromicPCE.computeNonPalindromicERO(pipe, bwAvailMap, rsvVlans);       // A->Z ERO is NOT palindrome of Z->A ERO
                //log.info("Exiting NON-Palindromical PCE");
            }
        } catch (PCEException e) {
            log.error("Failed to find shortest constrained path. " + e.getMessage());
        }

        return eroMap;
    }


    /**
     * Verify that there both the AZ and ZA paths were found given a map of shortest paths.
     *
     * @param eroMap The map containing the AZ and ZA shortest paths (keys: "az" and "za")
     * @return True if both paths found, False otherwise.
     */
    private boolean verifyEros(Map<String, List<TopoEdge>> eroMap) {
        if (eroMap != null) {
            if (eroMap.size() % 2 == 0 && eroMap.size() >= 2) {
                return eroMap.values().stream().allMatch(l -> l.size() > 0);
            }
        }

        return false;
    }

    /**
     * Confirm that the requested blueprint is valid.
     *
     * @param requested The requested blueprint.
     * @throws PCEException
     */
    public void verifyRequested(RequestedBlueprintE requested) throws PCEException {
        log.info("starting verification");
        if (requested == null) {
            throw new PCEException("Null blueprint!");
        }
        if (requested.getVlanFlow() == null) {
            throw new PCEException("No VLAN flows");
        }

        RequestedVlanFlowE flow = requested.getVlanFlow();

        log.info("verifying junctions & pipes");
        if (flow.getJunctions().isEmpty() && flow.getPipes().isEmpty()) {
            throw new PCEException("Junctions and pipes both empty.");
        }

        Set<RequestedVlanJunctionE> allJunctions = new HashSet<>();
        allJunctions.addAll(flow.getJunctions());
        flow.getPipes().forEach(t -> {
            allJunctions.add(t.getAJunction());
            allJunctions.add(t.getZJunction());
        });

        for (RequestedVlanJunctionE junction : allJunctions) {
            // throws exception if device not found in topology
            List<String> badUrns = new ArrayList<>();
            try {
                topoService.device(junction.getDeviceUrn());
            } catch (NoSuchElementException ex) {
                badUrns.add(junction.getDeviceUrn());
            }
            for (RequestedVlanFixtureE fixture : junction.getFixtures()) {
                try {
                    topoService.getUrn(fixture.getPortUrn());
                } catch (NoSuchElementException ex) {
                    badUrns.add(fixture.getPortUrn());
                }
            }
            if (!badUrns.isEmpty()) {
                throw new InvalidUrnException("Some requested urns not found.", badUrns);
            }
            validVlanRequest(junction);
        }

        log.info("all junctions & pipes are ok");
    }

    private void validVlanRequest(RequestedVlanJunctionE junction) throws PCEException{
        // Confirm that VLANs can be reserved
        // Either: Junction is a router, and the fixtures must have reservable VLANs
        // Or:     Junction is a switch, and there must be reservable VLANs on the switch
        String juncUrnString = junction.getDeviceUrn();
        Optional<UrnE> urnOpt = urnRepo.findByUrn(juncUrnString);
        if(urnOpt.isPresent()){
            UrnE urn = urnOpt.get();
            if(urn.getDeviceType().equals(DeviceType.SWITCH)){
                ReservableVlanE resvVlan = urn.getReservableVlans();
                if(resvVlan == null){
                    throw new PCEException("Unable to reserve VLANs on " + juncUrnString);
                }
            }
            // Evaluate the Router's fixtures
            else{
                validateFixtures(junction);
            }
        }
        else{
            throw new PCEException("Junction URN " + juncUrnString + " does not exist in repo.");
        }
    }

    private void validateFixtures(RequestedVlanJunctionE junction) throws PCEException{
        Set<RequestedVlanFixtureE> fixtures = junction.getFixtures();
        for(RequestedVlanFixtureE fix : fixtures){
            Optional<UrnE> fixUrnOpt = urnRepo.findByUrn(fix.getPortUrn());
            if(fixUrnOpt.isPresent()){
                UrnE fixtureUrn = fixUrnOpt.get();
                ReservableVlanE resvVlan = fixtureUrn.getReservableVlans();
                if(resvVlan == null){
                    throw new PCEException("Unable to reserve VLANs on fixture " + fixtureUrn.getUrn());
                }
            }
            else{
                throw new PCEException("Fixture URN " + fix.getPortUrn() + " does not exist in repo.");
            }
        }
    }


    /**
     * Construct a list of EdgeEs (simplified edges) from a list of TopoEdges.
     * Note: This list does not contain every (fixture, device) edge used (as of 11/9/2016)
     * @param topoEdges - List of TopoEdges
     * @return A list of EdgeEs based on the TopoEdges
     */
    private List<EdgeE> convertTopoEdgePathToEdges(List<TopoEdge> topoEdges) {
        List<EdgeE> edges = new ArrayList<>();
        for(TopoEdge topoEdge : topoEdges){
            TopoVertex a = topoEdge.getA();
            TopoVertex z = topoEdge.getZ();
            String aType = a.getVertexType().equals(VertexType.PORT) ? "PORT" : "DEVICE";
            String zType = z.getVertexType().equals(VertexType.PORT) ? "PORT" : "DEVICE";
            edges.add(EdgeE.builder().origin(a.getUrn()).originType(aType).target(z.getUrn()).targetType(zType).build());
        }
        return edges;
    }


    /**
     * Given a list of EdgeEs, add all (Fixture, Device) edges that aren't already in the list.
     * @param edges - The current list of EdgeEs
     * @param reqPipe - THe requested pipe (contains requested fixtures)
     * @return An update list of EdgeEs
     */
    private List<EdgeE> includeFixtures(List<EdgeE> edges, RequestedVlanPipeE reqPipe, Boolean isAzPath){
        RequestedVlanJunctionE aJunction = reqPipe.getAJunction();
        RequestedVlanJunctionE zJunction = reqPipe.getZJunction();

        // Add the fixtures at the A junction: to the beginning if AZ path, to the end if ZA path
        for(RequestedVlanFixtureE reqFix : aJunction.getFixtures()){
            if(isAzPath){
                EdgeE fixEdge = EdgeE.builder()
                        .origin(reqFix.getPortUrn())
                        .originType("PORT")
                        .target(aJunction.getDeviceUrn())
                        .targetType("DEVICE")
                        .build();
                if(!edges.contains(fixEdge)){
                    edges.add(0, fixEdge);
                }
            }
            if(!isAzPath){
                EdgeE fixEdge = EdgeE.builder()
                        .origin(aJunction.getDeviceUrn())
                        .originType("DEVICE")
                        .target(reqFix.getPortUrn())
                        .targetType("PORT")
                        .build();
                if(!edges.contains(fixEdge)){
                    edges.add(fixEdge);
                }
            }
        }

        // Add the fixtures at the Z junction: to the end if AZ path, to the beginning if ZA path
        for(RequestedVlanFixtureE reqFix : zJunction.getFixtures()){
            if(isAzPath){
                EdgeE fixEdge = EdgeE.builder()
                        .origin(zJunction.getDeviceUrn())
                        .originType("DEVICE")
                        .target(reqFix.getPortUrn())
                        .targetType("PORT")
                        .build();
                if(!edges.contains(fixEdge)){
                    edges.add(fixEdge);
                }
            }
            if(!isAzPath){
                EdgeE fixEdge = EdgeE.builder()
                        .origin(reqFix.getPortUrn())
                        .originType("PORT")
                        .target(zJunction.getDeviceUrn())
                        .targetType("DEVICE")
                        .build();
                if(!edges.contains(fixEdge)){
                    edges.add(0, fixEdge);
                }
            }
        }
        return edges;
    }

    private void addJunctionPaths(Set<BidirectionalPathE> allPaths, Set<ReservedVlanJunctionE> reservedJunctions) {
        for (ReservedVlanJunctionE junction : reservedJunctions) {

            Set<String> fixs = junction.getFixtures().stream().map(ReservedVlanFixtureE::getIfceUrn).collect(Collectors.toSet());
            Set<TopoEdge> edges = topoService.getMultilayerTopology().getEdges()
                    .stream()
                    .filter(e -> junction.getDeviceUrn().equals(e.getA().getUrn()) || junction.getDeviceUrn().equals(e.getZ().getUrn()))
                    .filter(e -> fixs.contains(e.getA().getUrn()) || fixs.contains(e.getZ().getUrn()))
                    .collect(Collectors.toSet());
            List<TopoEdge> fixToJunctionEdges = edges
                    .stream()
                    .filter(e -> e.getZ().getUrn().equals(junction.getDeviceUrn()))
                    .sorted((e1, e2) -> e1.getA().getUrn().compareToIgnoreCase(e2.getA().getUrn()))
                    .collect(Collectors.toList());
            List<TopoEdge> junctionToFixEdges = edges
                    .stream()
                    .filter(e -> e.getA().getUrn().equals(junction.getDeviceUrn()))
                    .sorted((e1, e2) -> e1.getZ().getUrn().compareToIgnoreCase(e2.getZ().getUrn()))
                    .collect(Collectors.toList());
            // (Fix, Junction), (Junction, Fix), (Junction, Fix), ...., (Junction, Fix)
            List<TopoEdge> azPath = junctionToFixEdges.size() > 1 ? junctionToFixEdges.subList(1, junctionToFixEdges.size()) : new ArrayList<>();
            if(fixToJunctionEdges.size() > 0) {
                azPath.add(0, fixToJunctionEdges.get(0));
            }
            else{
                Optional<TopoVertex> junctionVertex = topoService.getMultilayerTopology().getVertexByUrn(junction.getDeviceUrn());
                junctionVertex.ifPresent(topoVertex -> azPath.add(TopoEdge.builder().a(topoVertex).z(topoVertex).layer(Layer.INTERNAL).metric(0L).build()));
            }
            // Reverse above list
            List<TopoEdge> zaPath = new ArrayList<>(azPath);
            Collections.reverse(zaPath);
            allPaths.add(BidirectionalPathE.builder()
                    .azPath(convertTopoEdgePathToEdges(azPath))
                    .zaPath(convertTopoEdgePathToEdges(zaPath))
                    .build());
        }
    }
}