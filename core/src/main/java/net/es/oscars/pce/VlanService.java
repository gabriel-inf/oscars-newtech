package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.enums.VertexType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class VlanService {

    @Autowired
    private ReservedVlanRepository resvVlanRepo;

    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * BUILD RESERVED/AVAILABLE/REQUESTED VLAN COLLECTIONS/MAPS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Given a set of reserved junctions and reserved ethernet pipes, retrieve all reserved VLAN objects
     * within the specified schedule period.
     * @param reservedJunctions - Set of reserved junctions
     * @param reservedEthPipes - Set of reserved ethernet pipes
     * @param sched - Requested schedule
     * @return
     */
    public List<ReservedVlanE> createReservedVlanList(Set<ReservedVlanJunctionE> reservedJunctions,
                                                      Set<ReservedEthPipeE> reservedEthPipes,
                                                      ScheduleSpecificationE sched){

        // Retrieve all VLAN IDs reserved so far from junctions & pipes
        List<ReservedVlanE> rsvVlans = getReservedVlansFromJunctions(reservedJunctions);
        rsvVlans.addAll(getReservedVlansFromEthernetPipes(reservedEthPipes));
        rsvVlans.addAll(getReservedVlansFromRepo(sched.getNotBefore(), sched.getNotAfter()));

        return rsvVlans;
    }

    /**
     * Build a mapping of UrnE objects to ReservedVlanE objects.
     * @param rsvVlanList - A list of all reserved VLAN IDs
     * @return A map of UrnE to ReservedVlanE objects
     */
    public Map<UrnE, List<ReservedVlanE>> buildReservedVlanMap(List<ReservedVlanE> rsvVlanList) {

        Map<UrnE, List<ReservedVlanE>> map = new HashMap<>();
        for(ReservedVlanE resv : rsvVlanList){
            UrnE resvUrn = resv.getUrn();
            if(!map.containsKey(resvUrn)){
                map.put(resvUrn, new ArrayList<>());
            }
            map.get(resvUrn).add(resv);
        }
        return map;
    }

    private Map<UrnE,Set<Integer>> buildRequestedVlanIdMap(RequestedVlanPipeE reqPipe,
                                                           Map<UrnE, Set<Integer>> availableVlanMap) {
        Map<UrnE, Set<Integer>> requestedVlanIdMap = new HashMap<>();

        Set<RequestedVlanFixtureE> fixtures = new HashSet<>(reqPipe.getAJunction().getFixtures());
        fixtures.addAll(reqPipe.getZJunction().getFixtures());

        for(RequestedVlanFixtureE fix : fixtures){
            Set<Integer> requestedVlans = getIntegersFromRanges(getIntRangesFromString(fix.getVlanExpression()));
            if(requestedVlans.isEmpty()){
                requestedVlanIdMap.put(fix.getPortUrn(), availableVlanMap.get(fix.getPortUrn()));
            }
            else {
                requestedVlanIdMap.put(fix.getPortUrn(), requestedVlans);
            }
        }
        return requestedVlanIdMap;
    }

    public Map<UrnE, Set<Integer>> buildAvailableVlanIdMap(Map<String, UrnE> urnMap, List<ReservedVlanE> reservedVlans) {
        // Build empty map of available VLAN IDs per URN
        Map<UrnE, Set<Integer>> availableVlanIdMap = new HashMap<>();

        // Get map of all reserved VLAN IDs per URN
        Map<UrnE, Set<Integer>> reservedVlanIdMap = buildReservedVlanIdMap(urnMap, reservedVlans);

        String stringified = this.stringifyVlanMap(reservedVlanIdMap);

        log.info("Reserved VLAN ID Map: " + stringified);

        // Get map of all reservable VLAN IDs per URN
        Map<UrnE, Set<Integer>> reservableVlanIdMap = buildReservableVlanIdMap(urnMap);
        stringified = this.stringifyVlanMap(reservableVlanIdMap);

        log.info("Reservable VLAN ID Map: " + stringified);

        urnMap.values().stream().filter(urn -> urn.getUrnType().equals(UrnType.IFCE)).filter(urn -> urn.getReservableVlans() != null).forEach(urn -> {
            Set<Integer> availableIds = reservableVlanIdMap.get(urn);
            availableIds.removeAll(reservedVlanIdMap.get(urn));
            availableVlanIdMap.put(urn, availableIds);
        });

        stringified = this.stringifyVlanMap(availableVlanIdMap);

        log.info("Available VLAN ID Map: " + stringified);

        return availableVlanIdMap;
    }

    private String stringifyVlanMap(Map<UrnE, Set<Integer>> input) {
        Map<UrnE, String> output = new HashMap<>();
        input.keySet().forEach(urn -> {
            List<Integer> availVlans = new ArrayList<>();
            availVlans.addAll(input.get(urn));
            Collections.sort(availVlans);
            Set<IntRange> ranges = new HashSet<>();

            IntRange range = IntRange.builder().floor(0).ceiling(0).build();
            for (Integer idx = 0; idx < availVlans.size(); idx++) {
                Integer vlan = availVlans.get(idx);
                if (range.getCeiling() == 0) {
                    range.setFloor(vlan);
                    range.setCeiling(vlan);
                } else if (idx == availVlans.size() - 1) {
                    range.setCeiling(vlan);
                    ranges.add(range);
                } else {
                    if (range.getCeiling() +1 == vlan) {
                        range.setCeiling(vlan);
                    } else {
                        ranges.add(range);
                        range = IntRange.builder().floor(vlan).ceiling(vlan).build();
                    }
                }
            }

            String row = ranges.toString();


            output.put(urn, row);
        });


        return output.toString();

    }


    private Map<UrnE,Set<Integer>> buildReservableVlanIdMap(Map<String, UrnE> urnMap) {
        Map<UrnE, Set<Integer>> reservableVlanIdMap = new HashMap<>();

        urnMap.values().stream().filter(urn -> urn.getUrnType().equals(UrnType.IFCE)).filter(urn -> urn.getReservableVlans() != null).forEach(urn -> {
            List<IntRange> ranges = urn.getReservableVlans().getVlanRanges().stream().map(IntRangeE::toDtoIntRange).collect(Collectors.toList());
            reservableVlanIdMap.put(urn, getIntegersFromRanges(ranges));
        });
        return reservableVlanIdMap;
    }

    private Map<UrnE, Set<Integer>> buildReservedVlanIdMap(Map<String, UrnE> urnMap, List<ReservedVlanE> reservedVlans){
        Map<UrnE, Set<Integer>> reservedVlanIdMap = new HashMap<>();
        urnMap.values().stream().filter(urn -> urn.getUrnType().equals(UrnType.IFCE)).forEach(urn -> reservedVlanIdMap.put(urn, new HashSet<>()));

        for(ReservedVlanE rsvVlan : reservedVlans){
            Integer vlanId = rsvVlan.getVlan();
            UrnE urn = rsvVlan.getUrn();
            reservedVlanIdMap.get(urn).add(vlanId);
        }
        return reservedVlanIdMap;
    }

    private Map<UrnE, Set<Integer>> buildValidVlanIdMap(Map<UrnE, Set<Integer>> requestedVlanMap,
                                                        Map<UrnE, Set<Integer>> availableVlanMap) {
        Map<UrnE, Set<Integer>> validVlanIdMap = new HashMap<>();
        for(UrnE urn : requestedVlanMap.keySet()){
            Set<Integer> requestedVlans = requestedVlanMap.get(urn);
            Set<Integer> availableVlans = availableVlanMap.get(urn);

            availableVlans.retainAll(requestedVlans);
            validVlanIdMap.put(urn, availableVlans);
        }
        return validVlanIdMap;
    }



    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * GET VLAN COLLECTIONS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Get a list of all VLAN IDs reserved between a start and end date/time.
     * @param start - The start of the time range
     * @param end - The end of the time range
     * @return A list of reserved VLAN IDs
     */
    public List<ReservedVlanE> getReservedVlansFromRepo(Date start, Date end){
        //Get all Reserved VLan between start and end
        Optional<List<ReservedVlanE>> optResvVlan = resvVlanRepo.findOverlappingInterval(start.toInstant(), end.toInstant());
        return optResvVlan.isPresent() ? optResvVlan.get() : new ArrayList<>();
    }

    /**
     * Retrieve all reserved VLAN IDs from a set of reserved junctions
     * @param junctions - Set of reserved junctions.
     * @return A list of all VLAN IDs reserved at those junctions.
     */
    public List<ReservedVlanE> getReservedVlansFromJunctions(Set<ReservedVlanJunctionE> junctions){
        return junctions
                .stream()
                .map(ReservedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(ReservedVlanFixtureE::getReservedVlan)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all Reserved VLAN IDs from a set of reserved pipes.
     * @param reservedPipes - Set of reserved pipes
     * @return A list of all reserved VLAN IDs within the set of reserved pipes.
     */
    public List<ReservedVlanE> getReservedVlansFromEthernetPipes(Set<ReservedEthPipeE> reservedPipes) {
        List<ReservedVlanE> reservedVlans = new ArrayList<>();
        Set<ReservedVlanJunctionE> junctions = new HashSet<>();
        for(ReservedEthPipeE pipe : reservedPipes){
            junctions.add(pipe.getAJunction());
            junctions.add(pipe.getZJunction());
            reservedVlans.addAll(pipe.getReservedVlans());
        }
        reservedVlans.addAll(getReservedVlansFromJunctions(junctions));
        return reservedVlans;
    }

    /**
     * Get the VLAN IDs available at this fixture.
     * @param reqFix - The requested VLAN fixture (used to retrieve the reservable set of VLANs).
     * @param rsvVlans - The reserved VLAN IDs.
     * @return The set of available VLAN IDs at this fixture (may be empty)
     */
    public Set<Integer> getAvailableVlanIdsFromFixture(RequestedVlanFixtureE reqFix, List<ReservedVlanE> rsvVlans){

        // Build map from URNs to Reserved VLAN lists
        Map<UrnE, List<ReservedVlanE>> rsvVlanMap = buildReservedVlanMap(rsvVlans);

        // Get the set of reserved VLAN IDs at this fixture
        List<ReservedVlanE> rsvVlansAtFixture = rsvVlanMap.containsKey(reqFix.getPortUrn()) ?
                rsvVlanMap.get(reqFix.getPortUrn()) : new ArrayList<>();
        Set<Integer> reservedVlanIds = rsvVlansAtFixture.stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());

        // Find all reservable VLAN IDs at this fixture
        Set<Integer> reservableVlanIds = getIntegersFromRanges(
                reqFix.getPortUrn()
                        .getReservableVlans()
                        .getVlanRanges()
                        .stream()
                        .map(IntRangeE::toDtoIntRange)
                        .collect(Collectors.toList()));

        // Return all reservable VLAN Ids which are not reserved
        return reservableVlanIds
                .stream()
                .filter(id -> !reservedVlanIds.contains(id))
                .collect(Collectors.toSet());
    }


    /**
     * Get all VLAN IDs available at a URN based on the possible reservable ranges and the currently reserved IDs.
     * @param urnMap - Mapping of URN string to UrnE objects
     * @param urn - The currently considered URN string
     * @param ranges - List of IntRanges, representing all ranges of VLAN IDs supported at this URN
     * @param resvVlanMap - A mapping representing the Reserved VLANs at a URN
     * @return Set of VLAN IDs that are both supported and not reserved at a URN
     */
    public Set<Integer> getAvailableVlanIds(Map<String, UrnE> urnMap, String urn, List<IntRange> ranges,
                                            Map<UrnE, List<ReservedVlanE>> resvVlanMap) {
        // Get the supported reservable VLAN IDs
        Set<Integer> reservableVlanIds = getIntegersFromRanges(ranges);

        UrnE aUrn = urnMap.get(urn);
        // If this URN is in the reserved VLAN map
        if(resvVlanMap.containsKey(aUrn)) {
            // Get the reserved VLAN IDs
            List<Integer> resvVlanIds = resvVlanMap
                    .get(aUrn)
                    .stream()
                    .map(ReservedVlanE::getVlan)
                    .collect(Collectors.toList());

            // Return the supported IDs that are not reserved
            return reservableVlanIds
                    .stream()
                    .filter(id -> !resvVlanIds.contains(id))
                    .collect(Collectors.toSet());
        }
        else{
            return reservableVlanIds;
        }
    }

    /**
     * Using the list of URNs and the URN string, find the matching UrnE object and retrieve all of its
     * reservable IntRanges.
     * @param urnMap - Map of URN name to UrnE object.
     * @param matchingUrn - String representation of the desired URN.
     * @return - All IntRanges supported at the URN.
     */
    public List<IntRange> getVlanRangesFromUrnString(Map<String, UrnE> urnMap, String matchingUrn){
        if(urnMap.get(matchingUrn) == null || urnMap.get(matchingUrn).getReservableVlans() == null)
            return new ArrayList<>();
        return urnMap.get(matchingUrn)
                .getReservableVlans()
                .getVlanRanges()
                .stream()
                .map(IntRangeE::toDtoIntRange)
                .collect(Collectors.toList());
    }

    /**
     * Get the requested set of VLAN tags from a junction by streaming through the fixtures in the junction.
     * @param junction - The requested VLAN junction.
     * @return The set of VLAN tags (Integers) requested for fixtures at that junction.
     */
    public List<IntRange> getVlansFromJunction(RequestedVlanJunctionE junction){
        // Stream through the junction's fixtures, map the requested VLAN expression to a set of Integers
        return junction.getFixtures().stream()
                .map(RequestedVlanFixtureE::getVlanExpression)
                .map(this::getIntRangesFromString)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Select VLANs for Junctions/Pipes/Paths, Evaluate paths for sufficient VLANs
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Select a VLAN ID for a junction. All fixtures on the junction must use their requested VLAN tag.
     * @param req_j - The requested junction.
     * @return A valid VLAN iD for each fixture on this junction.
     */
    public Map<RequestedVlanFixtureE, Integer> selectVLANsForJunction(RequestedVlanJunctionE req_j, List<ReservedVlanE> rsvVlans){

        // All requested fixtures
        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();

        Map<RequestedVlanFixtureE, Integer> vlanIDPerFixture = new HashMap<>();

        // For each requested fixture
        for(RequestedVlanFixtureE reqFix : reqFixtures){
            // Get the available VLAN IDs
            Set<Integer> availableVlans = getAvailableVlanIdsFromFixture(reqFix, rsvVlans);

            // Get the requested VLAN expression
            String vlanExpression = reqFix.getVlanExpression();
            if(vlanExpression == null){
                vlanExpression = "any";
            }
            // Convert that expression into a set of requested IDs
            Set<Integer> reqVlanIds = getIntegersFromRanges(getIntRangesFromString(vlanExpression));
            // Find the overlap between available VLAN IDs and requested VLAN IDs
            Set<Integer> validVlans = addToSetOverlap(availableVlans, reqVlanIds);

            // If there are no valid IDs, return -1 (indicating an error)
            if(validVlans.isEmpty()){
                log.error("Requested VLAN IDs " + reqVlanIds + " not available at " + reqFix.getPortUrn().toString());
                vlanIDPerFixture.put(reqFix, -1);
            }
            else{
                List<Integer> valid = validVlans.stream().sorted().collect(Collectors.toList());
                vlanIDPerFixture.put(reqFix, valid.get(0));
            }
        }

        return vlanIDPerFixture;
    }

    /**
     * Return a mapping of URN entity to VLAN Integer ID. This map will contain the URNs of all fixtures, junctions,
     * and ports in-between the junction.
     * @param reqPipe - The requested pipe.
     * @param urnMap - A mapping of URN string to URN entity
     * @param reservedVlans - List of all reserved VLANs
     * @param azERO - The AZ edges
     * @param zaERO - The ZA edges
     * @return Map from URN entity to VLAN ID chosen for that entity.
     */
    public Map<UrnE, Integer> selectVlansForPipe(RequestedVlanPipeE reqPipe, Map<String, UrnE> urnMap, List<ReservedVlanE> reservedVlans,
                                                 List<TopoEdge> azERO, List<TopoEdge> zaERO){

        // Confirm that there is at least one VLAN ID that can support every segment (given what has been reserved so far)
        // Get the available VLANs at all URNs
        Map<UrnE, Set<Integer>> availableVlanMap = buildAvailableVlanIdMap(urnMap, reservedVlans);
        // Get the requested VLANs per Fixture URN
        Map<UrnE, Set<Integer>> requestedVlanMap = buildRequestedVlanIdMap(reqPipe, availableVlanMap);
        // Get the "valid" VLANs per Fixture URN
        Map<UrnE, Set<Integer>> validVlanMap = buildValidVlanIdMap(requestedVlanMap, availableVlanMap);
        // Create a map of URNs to chosen VLAN IDs
        return selectVlansForPath(azERO, zaERO, urnMap, availableVlanMap, validVlanMap);

    }


    /**
     * Given an AZ and ZA path, determine which VLAN IDs will work for those paths. Also needs a URN String -> Entity map,
     * a URN Entity -> Set of available VLANs map, and a URN Entity -> Set of valid VLANs map (available and requested per
     * fixture).
     * @param azERO - The AZ path (edges)
     * @param zaERO - The ZA path (edges)
     * @param urnMap - A map of URN String to Entity
     * @param availableVlanMap - A map of URN entity to available VLANs
     * @param validVlanMap - A map of fixture URN entity to requested & available VLANs
     * @return A map containing the VLAN chosen for each URN (-1 if none possible)
     */
    private Map<UrnE,Integer> selectVlansForPath(List<TopoEdge> azERO, List<TopoEdge> zaERO, Map<String, UrnE> urnMap,
                                                 Map<UrnE, Set<Integer>> availableVlanMap,
                                                 Map<UrnE, Set<Integer>> validVlanMap) {
        Map<UrnE, Integer> chosenVlanMap = new HashMap<>();

        // Retrive port URNs for pipe from the AZ and ZA EROs
        Set<UrnE> pipeUrns = getUrnsFromListOfEdges(azERO, urnMap);
        pipeUrns.addAll(getUrnsFromListOfEdges(zaERO, urnMap));
        pipeUrns = pipeUrns.stream().filter(u -> u.getUrnType().equals(UrnType.IFCE)).collect(Collectors.toSet());

        // Get the VLANs available across the AZ/ZA path
        Set<Integer> availableVlansAcrossPath = findAvailableVlansBidirectional(azERO, zaERO, availableVlanMap, urnMap);
        // Get the valid VLANs across the fixtures
        Set<Integer> availableVlansAcrossFixtures = getVlanOverlapAcrossMap(validVlanMap);

        // If there is any overlap between these two sets, use this ID for everything
        Set<Integer> availableEverywhere = new HashSet<>(availableVlansAcrossFixtures);
        availableEverywhere.retainAll(availableVlansAcrossPath);
        if(!availableEverywhere.isEmpty()){
            List<Integer> options = availableEverywhere.stream().sorted().collect(Collectors.toList());
            Integer chosenVlan = options.get(0);
            chosenVlanMap = pipeUrns.stream().collect(Collectors.toMap(u -> u, u -> chosenVlan));
        }
        // Otherwise, iterate through the valid vlans per fixture
        // For each valid VLAN, see if it is available across the path
        // If so, use that for the path
        // Either way, choose an arbitrary VLAN for the fixture
        else {
            // Remove all fixtures from the set of pipe URNs
            pipeUrns = pipeUrns.stream().filter(urn -> !validVlanMap.containsKey(urn)).collect(Collectors.toSet());
            boolean pipeVlansAssigned = false;

            // For each fixture, find if there are any overlapping VLANs between the path and the fixture
            for (UrnE fixUrn : validVlanMap.keySet()) {
                Set<Integer> overlappingVlans = new HashSet<>(validVlanMap.get(fixUrn));
                overlappingVlans.retainAll(availableVlansAcrossPath);
                // If there is at least one VLAN ID in common between this fixture and the other ports in the path
                if (!overlappingVlans.isEmpty() && !pipeVlansAssigned) {
                    pipeVlansAssigned = true;
                    // Choose VLAN ID
                    List<Integer> options = overlappingVlans.stream().sorted().collect(Collectors.toList());
                    Integer chosenVlan = options.get(0);
                    // Assign this VLAN to every URN in the pipe
                    for(UrnE pipeUrn : pipeUrns){
                        chosenVlanMap.put(pipeUrn, chosenVlan);
                    }
                }
                if(!validVlanMap.get(fixUrn).isEmpty()) {
                    List<Integer> options = validVlanMap.get(fixUrn).stream().sorted().collect(Collectors.toList());
                    Integer chosenVlan = options.get(0);
                    chosenVlanMap.put(fixUrn, chosenVlan);
                }
                else{
                    chosenVlanMap.put(fixUrn, -1);
                }
            }

            // If no VLAN tags available at fixtures could be used, assign any VLAN available across the path
            // To every port in the path (excluding fixtures)
            if(!pipeVlansAssigned){
                // If there's at least one VLAN ID available across the path
                if(!availableVlansAcrossPath.isEmpty()){
                    // Choose VLAN ID
                    List<Integer> options = availableVlansAcrossPath.stream().sorted().collect(Collectors.toList());
                    Integer chosenVlan = options.get(0);
                    // Assign this VLAN to every URN in the pipe
                    for(UrnE pipeUrn : pipeUrns){
                        chosenVlanMap.put(pipeUrn, chosenVlan);
                    }
                }
                else{
                    for(UrnE pipeUrn : pipeUrns){
                        chosenVlanMap.put(pipeUrn, -1);
                    }
                }
            }
        }

        return chosenVlanMap;
    }


    /**
     * Check both AZ and ZA paths, determine which VLAN IDs are available across both.
     * @param azERO - The AZ path
     * @param zaERO - The ZA path
     * @param availableVlanMap - Map of available VLAN IDs
     * @param urnMap - Map of URN String -> Urn Entity
     * @return Set of VLAN IDs available across both path edges
     */
    private Set<Integer> findAvailableVlansBidirectional(List<TopoEdge> azERO, List<TopoEdge> zaERO,
                                                         Map<UrnE, Set<Integer>> availableVlanMap, Map<String, UrnE> urnMap) {
        // Ignore the first and last edges of each ERO
        // These edges connect to fixtures
        Set<Integer> availableVlans = null;

        // Get a list of all non-fixture to device edges

        List<TopoEdge> combinedEdges = azERO.subList(1, azERO.size()-1)
                .stream()
                .filter(e -> !e.getLayer().equals(Layer.MPLS))
                .collect(Collectors.toList());
        combinedEdges.addAll(
                zaERO.subList(1, zaERO.size()-1)
                        .stream()
                        .filter(e -> !e.getLayer().equals(Layer.MPLS))
                        .collect(Collectors.toList()));

        // Loop through all edges in the AZ / ZA combined path
        // Ignore MPLS edges
        for(TopoEdge edge : combinedEdges){
            if(edge.getA().getVertexType().equals(VertexType.PORT)){
                Set<Integer> vlans = getVertexVlansFromMap(edge.getA(), availableVlanMap, urnMap);
                if(availableVlans == null){
                    availableVlans = new HashSet<>(vlans);
                }
                else{
                    availableVlans.retainAll(vlans);
                }
            }
            if(edge.getZ().getVertexType().equals(VertexType.PORT)){
                Set<Integer> vlans = getVertexVlansFromMap(edge.getZ(), availableVlanMap, urnMap);
                if(availableVlans == null){
                    availableVlans = new HashSet<>(vlans);
                }
                else{
                    availableVlans.retainAll(vlans);
                }
            }
        }
        return availableVlans != null ? availableVlans : new HashSet<>();
    }


    /**
     * Return a pruned set of edges where the nodes on either end of the edge support at least one of the specified VLANs.
     * A map of URNs are used to retrive the reservable VLAN sets from nodes (where applicable). Builds a mapping from
     * each available VLAN id to the set of edges that support that ID. Using this mapping, the largest set of edges
     * that supports a requested VLAN id (or any VLAN id if none are specified) is returned.
     * @param availableEdges - The set of currently available edges, which will be pruned further using VLAN tags.
     * @param urnMap - Map of URN name to UrnE object.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param availVlanMap - Map of UrnE objects to a List<> of available VLAN tags.
     * @return The input edges, pruned using the input set of VLAN tags.
     */
    public Set<TopoEdge> findMaxValidEdgeSet(Set<TopoEdge> availableEdges, Map<String, UrnE> urnMap,
                                             List<IntRange> vlans, Map<UrnE, Set<Integer>> availVlanMap) {


        // Find a set of matching edges for each available VLAN id
        Map<Integer, Set<TopoEdge>> edgesPerId = findEdgeSetPerVlanID(availableEdges, urnMap, availVlanMap);
        // Get a set of the requested VLAN ids
        Set<Integer> idsInRanges = getIntegersFromRanges(vlans);
        // Find the largest set of TopoEdges that meet the request
        Set<TopoEdge> bestSet = new HashSet<>();
        for(Integer id : edgesPerId.keySet()){
            // Ignore the set of edges where both terminating nodes do not have reservable VLAN fields
            // Add them to the best set of edges after this loop
            if(id == -1){
                continue;
            }
            // If the currently considered ID matches the request (or there are no VLANs requested)
            // and the set of edges supporting this ID are larger than the current best
            // choose this set of edges
            if((idsInRanges.contains(id) || idsInRanges.isEmpty()) && edgesPerId.get(id).size() > bestSet.size()){
                bestSet = edgesPerId.get(id);
            }
        }
        // Add all edges where neither terminating node has reservable VLAN attributes
        bestSet.addAll(edgesPerId.get(-1));
        return bestSet;
    }


    /**
     * Traverse the set of edges, and create a map of VLAN ID to the edges where that ID is available
     * @param edges - The set of edges.
     * @param urnMap - Map of URN name to UrnE object.
     * @return A (possibly empty) set of VLAN tags that are available across every edge.
     */
    public Map<Integer, Set<TopoEdge>> findEdgeSetPerVlanID(Set<TopoEdge> edges, Map<String, UrnE> urnMap,
                                                            Map<UrnE, Set<Integer>> availVlanMap){
        // Overlap is used to track all VLAN tags that are available across every edge.
        Map<Integer, Set<TopoEdge>> edgesPerId = new HashMap<>();
        edgesPerId.put(-1, new HashSet<>());
        for(TopoEdge edge : edges){
            // Overlap is used to track all VLAN tags that are available across both endpoints of an edge
            Set<Integer> overlap = new HashSet<>();
            // Get all possible VLAN ranges reservable at the a and z ends of the edge
            List<IntRange> aRanges = getVlanRangesFromUrnString(urnMap, edge.getA().getUrn());
            List<IntRange> zRanges = getVlanRangesFromUrnString(urnMap, edge.getZ().getUrn());

            // If neither edge has reservable VLAN fields, add the edge to the "-1" VLAN tag list.
            // These edges do not need to be pruned, and will be added at the end to the best set of edges
            if(aRanges.isEmpty() && zRanges.isEmpty() || edge.getLayer().equals(Layer.MPLS)){
                edgesPerId.get(-1).add(edge);
            }
            // Otherwise, find the intersection between the VLAN ranges (if any), and add the edge to the list
            // matching each overlapping VLAN ID.
            else{
                // Find what VLAN ids are actually available at A and Z
                if(!aRanges.isEmpty()){
                    addToSetOverlap(overlap, availVlanMap.get(urnMap.get(edge.getA().getUrn())));
                }
                if(!zRanges.isEmpty()){
                    addToSetOverlap(overlap, availVlanMap.get(urnMap.get(edge.getZ().getUrn())));
                }

                // For overlapping IDs, put that edge into the map
                for(Integer id: overlap){
                    if(!edgesPerId.containsKey(id)){
                        edgesPerId.put(id, new HashSet<>());
                    }
                    edgesPerId.get(id).add(edge);
                }
            }

        }
        return edgesPerId;
    }

    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * HELPER METHODS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */


    /**
     * Given a list of strings, convert all valid strings into IntRanges.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return A list of IntRanges, each representing a range of VLAN ID values parsed from a string.
     */
    public List<IntRange> getIntRangesFromString(String vlans){
        if(IntRangeParsing.isValidIntRangeInput(vlans)){
            try {
                return IntRangeParsing.retrieveIntRanges(vlans);
            }catch(Exception e){
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Return all of the VLAN ids contained within the list of VLAN ranges.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The set of VLAN ids making up the input ranges.
     */
    public Set<Integer> getIntegersFromRanges(List<IntRange> vlans){
        return vlans
                .stream()
                .map(this::getSetOfNumbersInRange)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieve a set of all Integers that fall within the specified IntRange.
     * @param aRange - The specified IntRange
     * @return The set of Integers contained within the range.
     */
    public Set<Integer> getSetOfNumbersInRange(IntRange aRange) {
        Set<Integer> numbers = new HashSet<>();
        for(Integer num = aRange.getFloor(); num <= aRange.getCeiling(); num++){
            numbers.add(num);
        }
        return numbers;
    }

    /**
     * Iterate through the set of overlapping VLAN tags, keep the elements in that set which are also
     * contained in every IntRange passed in.
     * @param overlap - The set of overlapping VLAN tags.
     * @param other - Another set of VLAN tags, to be overlapped with the current overlapping set.
     * @return The (possibly reduced) set of overlapping VLAN tags.
     */
    public Set<Integer> addToSetOverlap(Set<Integer> overlap, Set<Integer> other){
        // If there are no ranges available, just return the current overlap set
        if(other.isEmpty()){
            return overlap;
        }
        // If the overlap does not already have elements, add all of the tags retrieved from this range
        if(overlap.isEmpty()){
            overlap.addAll(other);
        }else{
            // Otherwise, find the intersection between the current overlap and the tags retrieved from this range
            overlap.retainAll(other);
        }
        return overlap;
    }

    /**
     * Given a map of Urn Entity -> Sets of Integers (VLANs), find the overlap across the keyset
     * @param vlanMap - Map of URNs to VLANs
     * @return The intersection across the keyset of the map
     */
    private Set<Integer> getVlanOverlapAcrossMap(Map<UrnE, Set<Integer>> vlanMap) {
        Set<Integer> overlappingVlans = null;
        for(UrnE urn: vlanMap.keySet()){
            Set<Integer> vlans = vlanMap.get(urn);
            if(overlappingVlans == null){
                overlappingVlans = new HashSet<>(vlans);
            }
            else{
                overlappingVlans.retainAll(vlans);
            }
        }
        return overlappingVlans;
    }

    /**
     * Given a particular Vertex, a VLAN map, and a URN map, return the set of Integers at the vertex's URN.
     * @param v - The vertex
     * @param vlanMap - The VLAN map
     * @param urnMap - The URN map
     * @return A set of VLAN IDs from the vertex's corresponding entry in the VLAN map
     */
    private Set<Integer> getVertexVlansFromMap(TopoVertex v, Map<UrnE, Set<Integer>> vlanMap,
                                               Map<String, UrnE> urnMap){
        String aUrnString = v.getUrn();
        UrnE urn = urnMap.getOrDefault(aUrnString, null);
        return vlanMap.getOrDefault(urn, new HashSet<>());
    }

    /**
     * Given a list of edges and a URN map, get a list of all URNs in that list.
     * @param edges - The list of edges
     * @param urnMap - A map of URN strings to URN entities
     * @return Set of all URNs across the list of edges
     */
    private Set<UrnE> getUrnsFromListOfEdges(List<TopoEdge> edges, Map<String, UrnE> urnMap){
        Set<UrnE> urns = new HashSet<>();
        for(TopoEdge edge : edges){
            String aUrnString = edge.getA().getUrn();
            UrnE aUrn = urnMap.getOrDefault(aUrnString, null);

            String zUrnString = edge.getZ().getUrn();
            UrnE zUrn = urnMap.getOrDefault(zUrnString, null);

            if(aUrn != null){
                urns.add(aUrn);
            }
            if(zUrn != null){
                urns.add(zUrn);
            }
        }
        return urns;
    }

}
