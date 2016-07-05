package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.dao.ReservedPssResourceRepository;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TranslationPCE {
    @Autowired
    private PCEAssistant pceAssistant;

    @Autowired
    private TopoService topoService;

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private ReservedVlanRepository vlanRepo;

    @Autowired
    private ReservedPssResourceRepository pssResourceRepo;

    @Autowired
    private UrnRepository urnRepository;

    @Autowired
    private PruningService pruningService;

    /**
     * Creates a ReservedVlanJunctionE given a request for ingress/egress traffic within a device.
     * @param req_j - The requested junction
     * @param sched - The requested schedule
     * @param simpleJunctions - A set of all singular requested junctions so far
     * @return The Reserved Junction
     * @throws PCEException
     * @throws PSSException
     */
    public ReservedVlanJunctionE reserveSimpleJunction(RequestedVlanJunctionE req_j, ScheduleSpecificationE sched,
                                                       Set<ReservedVlanJunctionE> simpleJunctions)
            throws PCEException, PSSException {

        // Retrieve the URN of the requested junction, if it is in the repository
        String deviceUrn = req_j.getDeviceUrn().getUrn();
        Optional<UrnE> optUrn = urnRepository.findByUrn(deviceUrn);

        UrnE urn;
        if(optUrn.isPresent()){
            urn = optUrn.get();
        }
        else{
            log.error("URN " + deviceUrn + " not found in URN Repository");
            return null;
        }

        // Create a reserved junction with an empty set of fixtures / PSS resources
        ReservedVlanJunctionE rsv_j = pceAssistant.createReservedJunction(urn, new HashSet<>(), new HashSet<>(),
                pceAssistant.decideJunctionType(urn.getDeviceModel()));

        log.info("IN TransPCE 1");
        // Select a VLAN ID for this junction
        Integer vlanId = selectVLANForJunction(req_j, sched, simpleJunctions);
        if(vlanId == -1){
            return null;
        }

        log.info("IN TransPCE 2");
        // Confirm that there is sufficient available bandwidth
        boolean sufficientBandwidth = confirmSufficientBandwidth(req_j, sched, simpleJunctions);
        if(!sufficientBandwidth){
            return null;
        }

        // For each requested fixture, create a reserved bandwdith and reserved VLAN object
        // and store them in a Reserved Fixture
        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();
        for(RequestedVlanFixtureE reqFix : reqFixtures){
            ReservedBandwidthE rsvBw = pceAssistant.createReservedBandwidth(reqFix.getPortUrn(), reqFix.getInMbps(),
                    reqFix.getEgMbps(), sched);

            ReservedVlanE rsvVlan = pceAssistant.createReservedVlan(reqFix.getPortUrn(), vlanId, sched);

            ReservedVlanFixtureE rsvFix = pceAssistant.createReservedFixture(reqFix.getPortUrn(), new HashSet<>(),
                    rsvVlan, rsvBw, pceAssistant.decideFixtureType(reqFix.getPortUrn().getDeviceModel()));

            // Add the fixtures to the Reserved Junction
            rsv_j.getFixtures().add(rsvFix);
        }

        return rsv_j;
    }

    /**
     * Create a set of reserved pipes/junctions from a requested pipe. A requested pipe can produce:
     * One requested junction for each ethernet device along the path
     * One pipe for each MPLS segment along the path
     * This function will add to the reservedPipes and reservedEthJunctions sets passed in as input
     * @param reqPipe - THe requested pipe, containing details on the requested endpoints/bandwidth/VLANs
     * @param sched - The requested schedule (i.e. start/end date)
     * @param azERO - The physical path taken by the pipe in the A->Z direction
     * @param zaERO - The physical path taken by the pipe in the Z->A direction
     * @param simpleJunctions - The set of all individual junctions reserved so far (populated before reserving any pipes)
     * @param reservedPipes - The set of all reserved pipes so far
     * @param reservedEthJunctions - The set of all reserved ethernet junctions so far
     * @throws PCEException
     * @throws PSSException
     */
    public void reserveRequestedPipe(RequestedVlanPipeE reqPipe, ScheduleSpecificationE sched, List<TopoEdge> azERO,
                                     List<TopoEdge> zaERO, Set<ReservedVlanJunctionE> simpleJunctions,
                                     Set<ReservedEthPipeE> reservedPipes, Set<ReservedVlanJunctionE> reservedEthJunctions)
    throws PCEException, PSSException{

        // Retrieve a map of URN strings to device models
        Map<String, DeviceModel> deviceModels = topoService.deviceModels();


        // now, decompose the path
        List<Map<Layer, List<TopoEdge>>> azSegments = PCEAssistant.decompose(azERO, deviceModels);
        List<Map<Layer, List<TopoEdge>>> zaSegments = PCEAssistant.decompose(zaERO, deviceModels);
        assert(azSegments.size() == zaSegments.size());

        // Build a urn map
        Map<String , UrnE> urnMap = new HashMap<>();
        urnRepository.findAll().stream().forEach(u -> {
            urnMap.put(u.getUrn(), u);
        });


        // Combine the lists of reserved junctions
        Set<ReservedVlanJunctionE> reservedJunctions = new HashSet<>(simpleJunctions);
        reservedJunctions.addAll(reservedEthJunctions);

        // Retrieve all bandwidth reserved so far from pipes & junctions
        List<ReservedBandwidthE> rsvBandwidths = retrieveReservedBandwidths(reservedJunctions);
        rsvBandwidths.addAll(retrieveReservedBandwidthsFromPipes(reservedPipes));
        rsvBandwidths.addAll(pruningService.getReservedBandwidth(sched.getNotBefore(), sched.getNotAfter()));

        // Retrieve all VLAN ids reserved so far from pipes & junctions
        List<ReservedVlanE> rsvVlans = retrieveReservedVlans(reservedJunctions);
        rsvVlans.addAll(retrieveReservedVlansFromPipes(reservedPipes));
        rsvVlans.addAll(pruningService.getReservedVlans(sched.getNotBefore(), sched.getNotAfter()));

        // Confirm that there is sufficient bandwidth to meet the request (given what has been reserved so far)
        boolean sufficientBw = checkForSufficientBw(urnMap, reqPipe, sched, azSegments, zaSegments, rsvBandwidths);
        if(!sufficientBw){
            throw new PCEException("Insufficient Bandwidth to meet requested pipe" +
                    reqPipe.toString() + " given previous pipes in flow");
        }


        // Confirm that there is at least one VLAN ID that can support every segment (given what has been reserved so far)
        Set<Integer> validVlanIds = selectVlanIds(urnMap, reqPipe, sched, azSegments, zaSegments, rsvVlans);
        if(validVlanIds.isEmpty()){
            throw new PCEException("Insufficient VLANs to meet requested pipe " +
                    reqPipe.toString() + " viven previous pipes in flow");
        }

        // for each segment:
        // if it is an Ethernet segment, make junctions, one per device
        // if it is an MPLS segment, make a pipe
        // all the while, make sure to merge in the current first and last junctions as needed

        for (int i = 0; i < azSegments.size(); i++) {
            // Get az segment and za segment
            Map<Layer, List<TopoEdge>> azSegment = azSegments.get(i);
            Map<Layer, List<TopoEdge>> zaSegment = zaSegments.get(i);

            // Get Chosen VLAN ID for segment
            Integer vlanId = validVlanIds.iterator().next();

            // Create mergeA and MergeZ for AZ segment
            Optional<ReservedVlanJunctionE> mergeA = Optional.empty();
            Optional<ReservedVlanJunctionE> mergeZ = Optional.empty();
            if (i == 0) {
                UrnE urn = reqPipe.getAJunction().getDeviceUrn();

                mergeA = Optional.of(pceAssistant.createReservedJunction(urn, new HashSet<>(), new HashSet<>()
                        , pceAssistant.decideJunctionType(deviceModels.get(urn.getUrn()))));
            }
            if (i == azSegments.size() - 1) {
                UrnE urn = reqPipe.getZJunction().getDeviceUrn();
                mergeZ = Optional.of(pceAssistant.createReservedJunction(urn, new HashSet<>(), new HashSet<>()
                        , pceAssistant.decideJunctionType(deviceModels.get(urn.getUrn()))));
            }


            List<TopoEdge> azEdges;
            List<TopoEdge> zaEdges;

            if (azSegment.size() != 1) {
                throw new PCEException("invalid segmentation");
            }
            if (azSegment.containsKey(Layer.ETHERNET)) {
                if (azSegment.get(Layer.ETHERNET).size() != 3) {
                    throw new PCEException("invalid segmentation");
                }

                azEdges = azSegment.get(Layer.ETHERNET);

                // an ethernet segment: a list of junctions, one per device

                List<ReservedVlanJunctionE> azVjs = pceAssistant.makeEthernetJunctions(azEdges,
                        reqPipe.getAzMbps(), reqPipe.getZaMbps(), vlanId,
                        mergeA, mergeZ,
                        sched,
                        urnMap,
                        deviceModels);

                // Add these to the list of ongoing reserved junctions
                reservedEthJunctions.addAll(azVjs);

            } else if (azSegment.containsKey(Layer.MPLS)) {
                azEdges = azSegment.get(Layer.MPLS);
                zaEdges = zaSegment.get(Layer.MPLS);

                ReservedEthPipeE pipe = pceAssistant.makeVplsPipe(azEdges, zaEdges, reqPipe.getAzMbps(),
                        reqPipe.getZaMbps(), vlanId, mergeA, mergeZ, urnMap, deviceModels, sched);

                // Add this pipe to the list of ongoing reserved pipes
                reservedPipes.add(pipe);

            } else {
                throw new PCEException("invalid segmentation");
            }
        }
    }

    /**
     * Examine all segments, confirm that the requested bandwidth can be supported given the bandwidth reservations
     * passed in.
     * @param urnMap - A map of URN string to URN objects
     * @param reqPipe - The requested pipe
     * @param sched - The requested schedule
     * @param azSegments - The path in the A->Z direction, split up into ETHERNET and MPLS segments
     * @param zaSegments - The path in the Z->A direction, split up into ETHERNET and MPLS segments
     * @param rsvBandwidths - A list of bandwidth reservations, which affect bandwidth availability
     * @return True, if there is sufficient bandwidth across all segments. False, otherwise.
     */
    private boolean checkForSufficientBw(Map<String, UrnE> urnMap, RequestedVlanPipeE reqPipe,
                                         ScheduleSpecificationE sched, List<Map<Layer, List<TopoEdge>>> azSegments,
                                         List<Map<Layer, List<TopoEdge>>> zaSegments, List<ReservedBandwidthE> rsvBandwidths) {

        // Build a map, allowing us to retrieve a list of ReservedBandwidth given the associated URN
        Map<UrnE, List<ReservedBandwidthE>> resvBwMap = pruningService.buildReservedBandwidthMap(rsvBandwidths);

        // Get the requested az and za bandwidth
        Integer azMbps = reqPipe.getAzMbps();
        Integer zaMbps = reqPipe.getZaMbps();

        // For each AZ segment, fail the test if there is insufficient bandwidth
        for(Map<Layer, List<TopoEdge>> segment : azSegments){
            if(!sufficientBandwidthForSegment(segment, urnMap, resvBwMap, azMbps, zaMbps)){
                return false;
            }
        }

        // For each ZA segment, fail the test if there is insufficient bandwidth
        for(Map<Layer, List<TopoEdge>> segment : zaSegments){
            if(!sufficientBandwidthForSegment(segment, urnMap, resvBwMap, azMbps, zaMbps)){
                return false;
            }
        }
        return true;
    }

    /**
     * Given a particular segment, iterate through the edges and confirm that there is sufficient bandwidth
     * available
     * @param segment - A segment map, mapping a layer type to a series of edges
     * @param urnMap - A mapping of URN strings to URN objects
     * @param resvBwMap - A mapping of URN objects to lists of reserved bandwidth for that URN
     * @param azMbps - The bandwidth in the AZ direction
     * @param zaMbps - The bandwidth the ZA direction
     * @return True, if the segment can support the requested bandwidth. False, otherwise.
     */
    private boolean sufficientBandwidthForSegment(Map<Layer, List<TopoEdge>> segment, Map<String, UrnE> urnMap,
                                                 Map<UrnE, List<ReservedBandwidthE>> resvBwMap, Integer azMbps,
                                                 Integer zaMbps){
        // For each list of edges in the segment
        for(List<TopoEdge> edges : segment.values()){
            // For each edge in that list
            for(TopoEdge edge : edges){

                // Retrieve the URNs
                String urnStringA = edge.getA().getUrn();
                String urnStringZ = edge.getZ().getUrn();
                if(!urnMap.containsKey(urnStringA) || !urnMap.containsKey(urnStringZ)){
                    return false;
                }
                UrnE urnA = urnMap.get(urnStringA);
                UrnE urnZ = urnMap.get(urnStringZ);

                // If URN A has reservable bandwidth, confirm that there is enough available
                if(urnA.getReservableBandwidth() != null){
                    if(!sufficientBandwidthAtUrn(urnA, urnA.getReservableBandwidth(), resvBwMap, azMbps, zaMbps)){
                        return false;
                    }
                }

                // If URN Z has reservable bandwidth, confirm that there is enough available
                if(urnZ.getReservableBandwidth() != null){
                    if(!sufficientBandwidthAtUrn(urnZ, urnZ.getReservableBandwidth(), resvBwMap, azMbps, zaMbps)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Given a specific URN, determine if there is enough bandwidth available to support the requested bandwidth
     * @param urn - The URN
     * @param reservableBw - List of Reservable Bandwidth at that URN
     * @param resvBwMap - Map of URNs to Reserved Bandwidth
     * @param inMbps - Requested ingress MBPS
     * @param egMbps - Requested egress MBPS
     * @return True, if there is enough available bandwidth at the URN. False, otherwise
     */
    private boolean sufficientBandwidthAtUrn(UrnE urn, ReservableBandwidthE reservableBw,
                                              Map<UrnE, List<ReservedBandwidthE>> resvBwMap, Integer inMbps, Integer egMbps){
        Map<String, Integer> bwAvail = pruningService.getBwAvailabilityForUrn(urn, reservableBw, resvBwMap);
        if(bwAvail.get("Ingress") < inMbps || bwAvail.get("Egress") < egMbps){
            log.error("Insufficient Bandwidth at " + urn.toString() + ". Requested: " +
                    inMbps + " In and " + egMbps + " Out. Available: " + bwAvail.get("Ingress") +
                    " In and " + bwAvail.get("Egress") + " Out.");
            return false;
        }
        return true;
    }

    /**
     * Retrieve a list of VLAN IDs that can be used given the AZ and ZA segments, the requested VLAN ranges, and
     * the VLAN IDs reserved so far.
     * @param urnMap - Mapping of URN string to URN object
     * @param reqPipe - The requested pipe
     * @param sched - The requested schedule
     * @param azSegments - The AZ path, divided into list of edges per ETHERNET/MPLS segment
     * @param zaSegments - The ZA path, dividied into list of edges per ETHERNET/MPLS segment
     * @param rsvVlans - The reserved VLAN IDs
     * @return A set of all viable VLAN IDs to meet the demand (set may be empty)
     */
    private Set<Integer> selectVlanIds(Map<String, UrnE> urnMap,
                                        RequestedVlanPipeE reqPipe, ScheduleSpecificationE sched,
                                        List<Map<Layer, List<TopoEdge>>> azSegments,
                                        List<Map<Layer, List<TopoEdge>>> zaSegments, List<ReservedVlanE> rsvVlans) {


        Set<Integer> overlappingVlanIds = new HashSet<>();
        // Map of URN to associated list of reserved VLANs
        Map<UrnE, List<ReservedVlanE>> rsvVlanMap = pruningService.buildReservedVlanMap(rsvVlans);

        // The requested VLAN Expression
        String vlanExpression = reqPipe.getAJunction().getFixtures().iterator().next().getVlanExpression();
        // Convert that expression into a set of IDs
        Set<Integer> requestedVlanIds = pruningService
                .getIntegersFromRanges(pruningService.getIntRangesFromString(vlanExpression));

        // Find all valid IDs for the AZ segments
        List<Set<Integer>> azValidIdsPerSegment = new ArrayList<>();
        for(Map<Layer, List<TopoEdge>> segment: azSegments){
            if(segment.containsKey(Layer.ETHERNET)){
                // Find all valid IDs for the ETHERNET segment
                Set<Integer> validIdsForSegment = getValidIdsForSegment(segment, requestedVlanIds, urnMap, rsvVlanMap);
                // If that segment has no valid IDs return an empty set
                if(validIdsForSegment.isEmpty()){
                    return overlappingVlanIds;
                }
                azValidIdsPerSegment.add(getValidIdsForSegment(segment, requestedVlanIds, urnMap, rsvVlanMap));
            }
        }

        // Find all valid IDs for the ZA segments
        List<Set<Integer>> zaValidIdsPerSegment = new ArrayList<>();
        for(Map<Layer, List<TopoEdge>> segment: zaSegments){
            if(segment.containsKey(Layer.ETHERNET)){
                // Find all valid IDs for the ETHERNET segment
                Set<Integer> validIdsForSegment = getValidIdsForSegment(segment, requestedVlanIds, urnMap, rsvVlanMap);
                // If that segment has no valid IDs return an empty set
                if(validIdsForSegment.isEmpty()){
                    return overlappingVlanIds;
                }
                zaValidIdsPerSegment.add(getValidIdsForSegment(segment, requestedVlanIds, urnMap, rsvVlanMap));
            }
        }

        //TODO: VLAN Translation
        //For now: Just use same VLAN ID on all segments
        // Find the intersection between the AZ and ZA valid VLAN IDs
        for(Set<Integer> validIds : azValidIdsPerSegment){
            overlappingVlanIds = pruningService.addToOverlap(overlappingVlanIds, validIds);
        }
        for(Set<Integer> validIds : zaValidIdsPerSegment){
            overlappingVlanIds = pruningService.addToOverlap(overlappingVlanIds, validIds);
        }
        return overlappingVlanIds;
    }

    /**
     * Find all valid VLAN iDs for a given (ETHERNET) segment.
     * @param segment - The given segment (only checks for valid IDs if segment map key is ETHERNET)
     * @param requestedVlanIds - The requested VLAN IDs
     * @param urnMap - Map of URN string to URN objects
     * @param rsvVlanMap - Map of URN objects to list of reserved VLAN IDs at that URN
     * @return The set of vlaid VLAN IDs for this segments
     */
    private Set<Integer> getValidIdsForSegment(Map<Layer, List<TopoEdge>> segment, Set<Integer> requestedVlanIds,
                                               Map<String, UrnE> urnMap, Map<UrnE, List<ReservedVlanE>> rsvVlanMap){
        // Set for holding valid VLAN IDs
        Set<Integer> validIds = new HashSet<>();
        // Only check if segment is on the ETHERNET layer
        if(segment.containsKey(Layer.ETHERNET)) {
            // Get the list of edges
            List<TopoEdge> segmentEdges = segment.get(Layer.ETHERNET);

            // Return a map of VLAN ID to sets of edges that support that ID
            Map<Integer, Set<TopoEdge>> edgesPerVlanId = pruningService
                    .findEdgesPerVlanId(new HashSet<>(segmentEdges), urnMap, rsvVlanMap);

            // For each VLAN ID
            for (Integer id : edgesPerVlanId.keySet()) {
                // Get the edges
                Set<TopoEdge> edgesForId = edgesPerVlanId.get(id);
                // Confirm that all edges in the segment support this id
                // If they do, add this ID as a valid ID
                if (edgesForId.size() == segmentEdges.size()) {
                    if (requestedVlanIds.contains(id) || requestedVlanIds.isEmpty()) {
                        validIds.add(id);
                    }
                }
            }
        }
        return validIds;
    }


    /**
     * Confirm that a requested VLAN junction supports the requested bandwidth. Checks each fixture of the junction.
     * @param req_j - The requested junction.
     * @param sched - The requested schedule.
     * @param rsvJunctions - Set of already reserved junctions
     * @return True, if there is enough bandwidth at every fixture. False, otherwise.
     */
    public boolean confirmSufficientBandwidth(RequestedVlanJunctionE req_j, ScheduleSpecificationE sched,
                                              Set<ReservedVlanJunctionE> rsvJunctions){

        // All requested fixtures on this junction
        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();

        // Get reserved bandwidth from repository
        List<ReservedBandwidthE> reservedBandwidthList = pruningService.getReservedBandwidth(sched.getNotBefore(), sched.getNotAfter());
        // Combine list with bandwidth retrieved from the passed in junctions
        reservedBandwidthList.addAll(retrieveReservedBandwidths(rsvJunctions));

        // Build a bandwidth map for the reserved bandwidth
        Map<UrnE, List<ReservedBandwidthE>> resvBwMap = pruningService.buildReservedBandwidthMap(reservedBandwidthList);


        // For each requested fixture,
        for(RequestedVlanFixtureE reqFix: reqFixtures){
            // Get the maximum reservable bandwidth for that fixture
            ReservableBandwidthE reservableBw = reqFix.getPortUrn().getReservableBandwidth();
            // Confirum that there is enough available bandwidth at that URN
            if(!sufficientBandwidthAtUrn(reqFix.getPortUrn(), reservableBw,
                    resvBwMap, reqFix.getInMbps(), reqFix.getEgMbps())){
                return false;
            }
        }
        return true;
    }

    /**
     * Select a VLAN ID for a junction. All fixtures on the junction must use the same VLAN tag.
     * @param req_j - The requested junction.
     * @param sched - The requested schedule.
     * @param rsvJunctions - The set of reserved junctions.
     * @return A valid VLAN iD for this junction.
     */
    public Integer selectVLANForJunction(RequestedVlanJunctionE req_j, ScheduleSpecificationE sched,
                                         Set<ReservedVlanJunctionE> rsvJunctions){
        // Get Reserved VLANs from Repository
        List<ReservedVlanE> rsvVlans = pruningService.getReservedVlans(sched.getNotBefore(), sched.getNotAfter());

        // Add already reserved VLANs from passed in junctions
        rsvVlans.addAll(retrieveReservedVlans(rsvJunctions));

        // All requested fixtures
        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();

        // Holds the intersection of VLAN IDs across all requested fixtures at this junction
        Set<Integer> overlap = null;

        // For each requested fixture
        for(RequestedVlanFixtureE reqFix : reqFixtures){
            // Get the available VLAN IDs
            log.info("IN TransPCE: selectVLAN 1");
            Set<Integer> availableVlans = getAvailableVlanIds(reqFix, rsvVlans);
            log.info("IN TransPCE: selectVLAN 2");

            // Get the requested VLAN expression
            String vlanExpression = reqFix.getVlanExpression();
            if(vlanExpression == null){
                vlanExpression = "any";
            }
            // Convert that expression into a set of requested IDs
            Set<Integer> reqVlanIds = pruningService.getIntegersFromRanges(pruningService.getIntRangesFromString(vlanExpression));
            // Find the overlap between available VLAN iDs and requested VLAN IDs
            Set<Integer> validVlans = pruningService.addToOverlap(availableVlans, reqVlanIds);
            // If this is the first iteration, set the overlap to be equal to the valid VLAN IDs
            if(overlap == null){
                overlap = validVlans;
            }
            // Otherwise, find the intersection between the current overlap set and the VLAN IDs valid at this fixture
            else {
                overlap = pruningService.addToOverlap(overlap, validVlans);
            }
            // If there is no intersection / valid IDs, return -1 (indicating an error)
            if(overlap.isEmpty()){
                log.error("Requested VLAN IDs " + reqVlanIds + " not available at " + reqFix.getPortUrn().toString());
                return -1;
            }
        }

        // The chosen VLAN ID
        if(overlap == null || overlap.isEmpty())
            return -1;
        return overlap.iterator().next();
    }

    /**
     * Get the VLAN IDs available at this fixture.
     * @param reqFix - The requested VLAN fixture (used to retrieve the reservable set of VLANs).
     * @param rsvVlans - The reserved VLAN IDs.
     * @return The set of available VLAN IDs at this fixture (may be empty)
     */
    public Set<Integer> getAvailableVlanIds(RequestedVlanFixtureE reqFix, List<ReservedVlanE> rsvVlans){

        // Build map from URNs to Reserved VLAN lists
        Map<UrnE, List<ReservedVlanE>> rsvVlanMap = pruningService.buildReservedVlanMap(rsvVlans);

        // Get the set of reserved VLAN IDs at this fixture
        List<ReservedVlanE> rsvVlansAtFixture = rsvVlanMap.containsKey(reqFix.getPortUrn()) ?
                rsvVlanMap.get(reqFix.getPortUrn()) : new ArrayList<>();
        log.info("IN TransPCE: getVlanIDs 3");
        log.info("FIX URN: " + reqFix.getPortUrn());
        Set<Integer> reservedVlanIds = rsvVlansAtFixture.stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());
        log.info("IN TransPCE: getVlanIDs 4");

        // Find all reservable VLAN IDs at this fixture
        Set<Integer> reservableVlanIds = pruningService.getIntegersFromRanges(
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
     * Retrieve all reserved bandwidths from a set of reserved junctions.
     * @param junctions - Set of reserved junctions.
     * @return A list of all bandwidth reserved at those junctions.
     */
    public List<ReservedBandwidthE> retrieveReservedBandwidths(Set<ReservedVlanJunctionE> junctions){
        return junctions
                .stream()
                .map(ReservedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(ReservedVlanFixtureE::getReservedBandwidth)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all reserved VLAN IDs from a set of reserved junctions
     * @param junctions - Set of reserved junctions.
     * @return A list of all VLAN IDs reserved at those junctions.
     */
    public List<ReservedVlanE> retrieveReservedVlans(Set<ReservedVlanJunctionE> junctions){
        return junctions
                .stream()
                .map(ReservedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(ReservedVlanFixtureE::getReservedVlan)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all Reserved Bandwidth from a set of reserved pipes.
     * @param reservedPipes - Set of reserved pipes
     * @return A list of all reserved bandwidth within the set of reserved pipes.
     */
    public List<ReservedBandwidthE> retrieveReservedBandwidthsFromPipes(Set<ReservedEthPipeE> reservedPipes) {
        return reservedPipes
                .stream()
                .map(ReservedEthPipeE::getReservedBandwidths)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all Reserved VLAN IDs from a set of reserved pipes (retrieved from the junctions).
     * @param reservedPipes - Set of reserved pipes
     * @return A list of all reserved VLAN IDs within the set of reserved pipes.
     */
    public List<ReservedVlanE> retrieveReservedVlansFromPipes(Set<ReservedEthPipeE> reservedPipes) {
        Set<ReservedVlanJunctionE> junctions = new HashSet<>();
        for(ReservedEthPipeE pipe : reservedPipes){
            junctions.add(pipe.getAJunction());
            junctions.add(pipe.getZJunction());
        }
        return retrieveReservedVlans(junctions);
    }
}
