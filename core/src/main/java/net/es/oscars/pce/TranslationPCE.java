package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
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
    private UrnRepository urnRepository;

    @Autowired
    private VlanService vlanService;

    @Autowired
    private BandwidthService bwService;


    /**
     * Creates a ReservedVlanJunctionE given a request for ingress/egress traffic within a device.
     *
     * @param req_j           - The requested junction
     * @param deviceToPortMap - Map from each device name to set of port names
     * @param portToDeviceMap - Map from each port name to parent device name
     * @param start                 - The requested start date
     * @param end                   - The requested end date
     * @return The Reserved Junction
     * @throws PCEException
     * @throws PSSException
     */
    public ReservedVlanJunctionE reserveSimpleJunction(RequestedVlanJunctionE req_j,
                                                       List<ReservedBandwidthE> reservedBandwidths,
                                                       List<ReservedVlanE> reservedVlans,
                                                       Map<String, Set<String>> deviceToPortMap,
                                                       Map<String, String> portToDeviceMap, Date start, Date end, String connectionId)
            throws PCEException, PSSException {

        // Retrieve the URN of the requested junction, if it is in the repository
        String deviceUrnString = req_j.getDeviceUrn();
        Map<String, UrnE> urnMap = urnRepository.findAll().stream().collect(Collectors.toMap(UrnE::getUrn, u -> u));
        UrnE deviceUrn = urnMap.get(deviceUrnString);

        // Create a reserved junction with an empty set of fixtures / PSS resources
        ReservedVlanJunctionE rsv_j = createReservedJunction(
                deviceUrnString,
                new HashSet<>(),
                new HashSet<>(),
                pceAssistant.decideJunctionType(deviceUrn.getDeviceModel()),
                new HashSet<>());

        if(req_j.getFixtures().size() == 0){
            return rsv_j;
        }

        // Select VLAN IDs for this junction
        Map<String, Set<Integer>> urnVlanMap = vlanService.selectVLANsForJunction(req_j, reservedVlans,
                deviceToPortMap, portToDeviceMap, urnMap);
        if (urnVlanMap.keySet().stream().anyMatch(urn -> urnVlanMap.get(urn).isEmpty())) {
            log.error("could not choose VLAN id");
            return null;
        }
        log.info("Chosen VLAN Map: " + urnVlanMap);

        // Confirm that there is sufficient available bandwidth
        boolean sufficientBandwidth = bwService.evaluateBandwidthJunction(req_j, reservedBandwidths);
        if (!sufficientBandwidth) {
            return null;
        }

        // Add the union of the reserved VLAN IDs to this junction
        Map<String, Set<ReservedVlanE>> urnToRsvVlanMap = new HashMap<>();
        for (String portUrn : urnVlanMap.keySet()) {
            Set<Integer> vlans = urnVlanMap.get(portUrn);
            urnToRsvVlanMap.put(portUrn, vlans.stream()
                    .map(vlan -> createReservedVlan(portUrn, vlan, start, end))
                    .collect(Collectors.toSet()));
        }

        // For each requested fixture, create a reserved bandwdith and reserved VLAN object
        // and store them in a Reserved Fixture
        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();
        for (RequestedVlanFixtureE reqFix : reqFixtures) {
            ReservedBandwidthE rsvBw = createReservedBandwidth(reqFix.getPortUrn(), reqFix.getInMbps(), reqFix.getEgMbps(), start, end, connectionId);

            UrnE j_deviceUrn = urnMap.get(req_j.getDeviceUrn());

            ReservedVlanFixtureE rsvFix = createReservedFixture(reqFix.getPortUrn(), new HashSet<>(),
                    urnToRsvVlanMap.get(reqFix.getPortUrn()), rsvBw, pceAssistant.decideFixtureType(j_deviceUrn.getDeviceModel()));

            // Add the fixtures to the Reserved Junction
            rsv_j.getFixtures().add(rsvFix);
        }

        rsv_j.getReservedVlans().addAll(urnToRsvVlanMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));

        return rsv_j;
    }

    /**
     * Create a set of reserved pipes/junctions from a requested pipe. A requested pipe can produce:
     * One pipe for each pair of Ethernet devices
     * One pipe for each MPLS segment along the path
     * This function will add to the reservedMplsPipes and reservedEthPipes sets passed in as input
     *
     * @param reqPipe            - THe requested pipe, containing details on the requested endpoints/bandwidth/VLANs
     * @param azERO              - The physical path taken by the pipe in the A->Z direction
     * @param zaERO              - The physical path taken by the pipe in the Z->A direction
     * @param reservedBandwidths - The list of all bandwidth reserved so far
     * @param reservedVlans      - The list of all VLAN IDs reserved so far
     * @param reservedMplsPipes  - The set of all reserved MPLS pipes so far
     * @param reservedEthPipes   - The set of all reserved Ethernet pipes so far
     * @param deviceToPortMap
     * @param portToDeviceMap    @throws PCEException
     * @throws PSSException
     */
    public void reserveRequestedPipe(RequestedVlanPipeE reqPipe,
                                     List<TopoEdge> azERO,
                                     List<TopoEdge> zaERO,
                                     List<ReservedBandwidthE> reservedBandwidths,
                                     List<ReservedVlanE> reservedVlans,
                                     Set<ReservedMplsPipeE> reservedMplsPipes,
                                     Set<ReservedEthPipeE> reservedEthPipes,
                                     Map<String, Set<String>> deviceToPortMap,
                                     Map<String, String> portToDeviceMap, Date start, Date end, String connectionId)
            throws PCEException, PSSException {

        // Build a urn map
        Map<String, UrnE> urnMap = new HashMap<>();
        urnRepository.findAll().forEach(u -> {
            urnMap.put(u.getUrn(), u);
        });

        Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap = evaluateRequestedBandwidth(reservedBandwidths, azERO, zaERO, reqPipe, urnMap);

        Map<String, Set<Integer>> chosenVlanMap
                = vlanService.selectVlansForPipe(reqPipe, urnMap, reservedVlans, azERO, zaERO, deviceToPortMap, portToDeviceMap);

        Boolean validVlanAssignment = evaluateAssignedVlanMap(chosenVlanMap, urnMap, portToDeviceMap);
        if(!validVlanAssignment){
            throw new PCEException(("VLAN could not not be found for all URNs"));
        }
        Map<TopoVertex, ReservedVlanJunctionE> junctionMap = new HashMap<>();

        List<Map<Layer, List<TopoVertex>>> azSegments = PCEAssistant.decompose(azERO);
        List<Map<Layer, List<TopoVertex>>> zaSegments = PCEAssistant.decompose(zaERO);

        reserveEntities(reqPipe, azSegments, zaSegments, urnMap, requestedBandwidthMap, chosenVlanMap, reservedEthPipes,
                reservedMplsPipes, junctionMap, portToDeviceMap, start, end, connectionId);
    }

    public void reserveRequestedPipeWithPairs(RequestedVlanPipeE reqPipe, List<List<TopoEdge>> azEROs,
                                              List<List<TopoEdge>> zaEROs, List<ReservedBandwidthE> reservedBandwidths,
                                              List<ReservedVlanE> reservedVlans, Set<ReservedMplsPipeE> reservedMplsPipes,
                                              Set<ReservedEthPipeE> reservedEthPipes, Map<String, Set<String>> deviceToPortMap,
                                              Map<String, String> portToDeviceMap, Date start, Date end, String connectionId) throws PSSException, PCEException {

        List<TopoEdge> combinedAzERO = combineEROs(azEROs);
        List<TopoEdge> combinedZaERO = combineEROs(zaEROs);

        // Build a urn map
        Map<String, UrnE> urnMap = new HashMap<>();
        urnRepository.findAll().forEach(u -> {
            urnMap.put(u.getUrn(), u);
        });

        Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap = evaluateRequestedBandwidth(reservedBandwidths,
                combinedAzERO, combinedZaERO, reqPipe, urnMap);


        Map<String, Set<Integer>> chosenVlanMap = vlanService.selectVlansForPipe(reqPipe, urnMap, reservedVlans,
                combinedAzERO, combinedZaERO, deviceToPortMap, portToDeviceMap);

        Boolean validVlanAssignment = evaluateAssignedVlanMap(chosenVlanMap, urnMap, portToDeviceMap);
        if(!validVlanAssignment){
            throw new PCEException(("VLAN could not not be found for all URNs"));
        }

        Map<TopoVertex, ReservedVlanJunctionE> junctionMap = new HashMap<>();

        Set<ReservedEthPipeE> tempEthPipes = new HashSet<>();
        Set<ReservedMplsPipeE> tempMplsPipes = new HashSet<>();
        for (Integer pairIndex = 0; pairIndex < azEROs.size(); pairIndex++) {
            List<TopoEdge> azERO = azEROs.get(pairIndex);
            List<TopoEdge> zaERO = zaEROs.get(pairIndex);

            List<Map<Layer, List<TopoVertex>>> azSegments = PCEAssistant.decompose(azERO);
            List<Map<Layer, List<TopoVertex>>> zaSegments = PCEAssistant.decompose(zaERO);
            if (azSegments.size() > 0 && zaSegments.size() > 0) {
                reserveEntities(reqPipe, azSegments, zaSegments, urnMap, requestedBandwidthMap,
                        chosenVlanMap, tempEthPipes, tempMplsPipes, junctionMap, portToDeviceMap, start, end, connectionId);
            }
        }

        Set<ReservedEthPipeE> filteredEthPipes = pceAssistant.filterEthPipeSet(tempEthPipes);
        Set<ReservedMplsPipeE> filteredMplsPipes = pceAssistant.filterMplsPipeSet(tempMplsPipes);

        reservedEthPipes.addAll(filteredEthPipes);
        reservedMplsPipes.addAll(filteredMplsPipes);
    }

    private Boolean evaluateAssignedVlanMap(Map<String, Set<Integer>> chosenVlanMap, Map<String, UrnE> urnMap, Map<String, String> portToDeviceMap) {
        Boolean valid = true;
        for (String urn : chosenVlanMap.keySet()) {
            UrnE topoUrn = urnMap.get(urn);
            Boolean parentHasVlans = evaluateIfParentHasVlans(topoUrn, portToDeviceMap, urnMap);
            if (chosenVlanMap.get(urn).isEmpty() && (topoUrn.getReservableVlans() != null || parentHasVlans)) {
                valid = false;
            }
        }
        return valid;
    }

    private Boolean evaluateIfParentHasVlans(UrnE topoUrn, Map<String, String> portToDeviceMap, Map<String, UrnE> urnMap){
        Boolean parentHasVlans = false;
        if(topoUrn.getUrnType().equals(UrnType.IFCE) && topoUrn.getReservableVlans() == null){
            String urnString = topoUrn.getUrn();
            String parentString = portToDeviceMap.get(urnString);
            UrnE parentUrn = urnMap.get(parentString);
            parentHasVlans = parentUrn.getReservableVlans() != null;
        }
        return parentHasVlans;
    }

    private Map<TopoVertex, Map<String, Integer>> evaluateRequestedBandwidth(List<ReservedBandwidthE> reservedBandwidths,
                                                                             List<TopoEdge> azERO, List<TopoEdge> zaERO,
                                                                             RequestedVlanPipeE reqPipe,
                                                                             Map<String, UrnE> urnMap) throws PCEException {
        // Get map of "Ingress" and "Egress" bandwidth availability
        Map<String, Map<String, Integer>> availBwMap = bwService.buildBandwidthAvailabilityMap(reservedBandwidths);

        // Returns a mapping from topovertices (ports) to an "Ingress"/"Egress" map of the total Ingress/Egress
        // Requested bandwidth at that port across both the azERO and the zaERO

        List<List<TopoEdge>> EROs = Arrays.asList(azERO, zaERO);
        List<Integer> bandwidths = Arrays.asList(reqPipe.getAzMbps(), reqPipe.getZaMbps());

        Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap = bwService.buildRequestedBandwidthMap(EROs, bandwidths, reqPipe);

        testBandwidthRequirements(reqPipe, azERO, zaERO, urnMap, reqPipe.getAzMbps(), reqPipe.getZaMbps(), availBwMap,
                requestedBandwidthMap, reservedBandwidths);

        return requestedBandwidthMap;
    }

    private void reserveEntities(RequestedVlanPipeE reqPipe,
                                 List<Map<Layer, List<TopoVertex>>> azSegments,
                                 List<Map<Layer, List<TopoVertex>>> zaSegments,
                                 Map<String, UrnE> urnMap,
                                 Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap,
                                 Map<String, Set<Integer>> chosenVlanMap,
                                 Set<ReservedEthPipeE> reservedEthPipes,
                                 Set<ReservedMplsPipeE> reservedMplsPipes,
                                 Map<TopoVertex, ReservedVlanJunctionE> junctionMap,
                                 Map<String, String> portToDeviceMap,
                                 Date start, Date end, String connectionId) throws PSSException {

        // Retrieve a map of URN strings to device models
        Map<String, DeviceModel> deviceModels = topoService.deviceModels();

        // Retrieve requested junctions
        Set<RequestedVlanJunctionE> reqPipeJunctions = new HashSet<>();
        reqPipeJunctions.add(reqPipe.getAJunction());
        reqPipeJunctions.add(reqPipe.getZJunction());

        // now, decompose the path
        assert (azSegments.size() == zaSegments.size());

        // Map of junction pairs to pipe EROs
        Map<List<TopoVertex>, Map<String, List<TopoVertex>>> junctionPairToPipeEROMap = new HashMap<>();

        // Mapping of Junction Pairs to Layer
        Map<List<TopoVertex>, Layer> allJunctionPairs = new HashMap<>();

        pceAssistant.constructJunctionPairToPipeEROMap(junctionPairToPipeEROMap, allJunctionPairs, azSegments, zaSegments);

        // Now, we have a map of Junction Pairs to the AZ and ZA pipe EROS between them.
        // (All in TopoVertex form)
        // Now we need to create the reserved objects
        // ETHERNET Pipe: needs bandwidth and VLANs
        // MPLS Pipe: needs bandwidth
        // All junctions: Add fixtures if requested

        for (List<TopoVertex> junctionPair : allJunctionPairs.keySet()) {
            Layer thisLayer = allJunctionPairs.get(junctionPair);

            Map<String, List<TopoVertex>> pipeEroMap = junctionPairToPipeEROMap.get(junctionPair);

            List<String> azPipeEro = pipeEroMap.get("AZ")
                    .stream()
                    .map(TopoVertex::getUrn)
                    .collect(Collectors.toList());
            List<String> zaPipeEro = pipeEroMap.get("ZA")
                    .stream()
                    .map(TopoVertex::getUrn)
                    .collect(Collectors.toList());


            TopoVertex aVertex = junctionPair.get(0);
            TopoVertex zVertex = junctionPair.get(1);

            ReservedVlanJunctionE aJunction;
            ReservedVlanJunctionE zJunction;

            // Create or retrieve A Junction
            if (!junctionMap.containsKey(aVertex)) {
                aJunction = createJunctionAndFixtures(aVertex, urnMap, deviceModels, reqPipeJunctions, chosenVlanMap, portToDeviceMap, start, end, connectionId);
                junctionMap.put(aVertex, aJunction);
            } else {
                aJunction = junctionMap.get(aVertex);
            }
            // Create Z Junction
            if (!junctionMap.containsKey(zVertex)) {
                zJunction = createJunctionAndFixtures(zVertex, urnMap, deviceModels, reqPipeJunctions, chosenVlanMap, portToDeviceMap, start, end, connectionId);
                junctionMap.put(zVertex, zJunction);
            } else {
                zJunction = junctionMap.get(zVertex);
            }

            DeviceModel aModel = deviceModels.get(aJunction.getDeviceUrn());
            DeviceModel zModel = deviceModels.get(zJunction.getDeviceUrn());

            Set<ReservedBandwidthE> pipeBandwidths = createReservedBandwidthForEROs(pipeEroMap.get("AZ"),
                    pipeEroMap.get("ZA"), requestedBandwidthMap, start, end, connectionId);

            if (thisLayer.equals(Layer.MPLS)) {
                ReservedMplsPipeE mplsPipe = ReservedMplsPipeE.builder()
                        .aJunction(aJunction)
                        .zJunction(zJunction)
                        .azERO(azPipeEro)
                        .zaERO(zaPipeEro)
                        .reservedBandwidths(pipeBandwidths)
                        .reservedPssResources(new HashSet<>())
                        .pipeType(pceAssistant.decideMplsPipeType(aModel, zModel))
                        .build();
                reservedMplsPipes.add(mplsPipe);
            }
            // ETHERNET
            else {
                Set<ReservedVlanE> pipeVlans = createReservedVlanForEROs(pipeEroMap.get("AZ"),
                        pipeEroMap.get("ZA"), urnMap, chosenVlanMap, start, end);

                ReservedEthPipeE ethPipe = ReservedEthPipeE.builder()
                        .aJunction(aJunction)
                        .zJunction(zJunction)
                        .azERO(azPipeEro)
                        .zaERO(zaPipeEro)
                        .reservedBandwidths(pipeBandwidths)
                        .reservedVlans(pipeVlans)
                        .reservedPssResources(new HashSet<>())
                        .pipeType(pceAssistant.decideEthPipeType(aModel, zModel))
                        .build();
                reservedEthPipes.add(ethPipe);
            }
        }
    }

    private List<TopoEdge> combineEROs(List<List<TopoEdge>> EROs) {
        List<TopoEdge> combinedERO = new ArrayList<>();
        TopoEdge firstEdge = null;
        TopoEdge lastEdge = null;
        for (List<TopoEdge> ero : EROs) {
            if (firstEdge == null) {
                firstEdge = ero.get(0);
                combinedERO.add(firstEdge);
            }
            combinedERO.addAll(ero.subList(1, ero.size() - 1).stream().distinct().collect(Collectors.toList()));
            if (lastEdge == null) {
                lastEdge = ero.get(ero.size() - 1);
            }
        }
        combinedERO.add(lastEdge);
        return combinedERO;
    }

    /**
     * Given two lists of EROS in the AZ and ZA direction, a map of URNs, a map of the requested bandwidth at each URN,
     * and the requested schedule, return a combined set of reserved bandwidth objects for the AZ and ZA paths
     *
     * @param az                    - The AZ vertices
     * @param za                    - The ZA vertices
     * @param requestedBandwidthMap - A mapping of Vertex objects to "Ingress"/"Egress" requested bandwidth
     * @param start                 - The requested start date
     * @param end                   - The requested end date
     * @return A set of all reserved bandwidth for every port (across both paths)
     */
    public Set<ReservedBandwidthE> createReservedBandwidthForEROs(List<TopoVertex> az, List<TopoVertex> za,
                                                                  Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap,
                                                                  Date start, Date end, String connectionId) {
        // Combine the AZ and ZA EROs
        Set<TopoVertex> combined = new HashSet<>(az);
        combined.addAll(za);

        // Store the reserved bandwidths
        Set<ReservedBandwidthE> reservedBandwidths = new HashSet<>();

        // For each vertex in the combined set, retrieve the requested Ingress/Egress bandwidth, and create a reserved
        // bandwidth object
        combined.stream().filter(requestedBandwidthMap::containsKey).forEach(vertex -> {
            Integer reqInMbps = requestedBandwidthMap.get(vertex).get("Ingress");
            Integer reqEgMbps = requestedBandwidthMap.get(vertex).get("Egress");

            ReservedBandwidthE rsvBw = createReservedBandwidth(vertex.getUrn(), reqInMbps, reqEgMbps, start, end, connectionId);
            reservedBandwidths.add(rsvBw);

        });

        // Return the set
        return reservedBandwidths;
    }

    /**
     * Given two lists of EROS in the AZ and ZA direction, a map of URNs, a chosen vlan ID,
     * and the requested schedule, return a combined set of reserved VLAN objects for the AZ and ZA paths
     *
     * @param az      - The AZ vertices
     * @param za      - The ZA vertices
     * @param urnMap  - A mapping of URN string to URN object
     * @param vlanMap - A mapping of URN object to assigned VLAN ID
     * @param start                 - The requested start date
     * @param end                   - The requested end date
     * @return A set of all reserved VLAN objects for every port (across both paths)
     */
    public Set<ReservedVlanE> createReservedVlanForEROs(List<TopoVertex> az, List<TopoVertex> za,
                                                        Map<String, UrnE> urnMap,
                                                        Map<String, Set<Integer>> vlanMap, Date start, Date end) {


        // Combine the AZ and ZA EROs

        Set<UrnE> combined = new HashSet<>();
        combined.addAll(az.stream().map(TopoVertex::getUrn).filter(urnMap::containsKey).map(urnMap::get).collect(Collectors.toSet()));

        combined.addAll(za.stream().map(TopoVertex::getUrn).filter(urnMap::containsKey).map(urnMap::get).collect(Collectors.toSet()));

        combined = combined.stream().filter(urn -> urn.getReservableVlans() != null).collect(Collectors.toSet());

        // Store the reserved vlans
        Set<ReservedVlanE> reservedVlans = new HashSet<>();

        // For each vertex in the combined set, retrieve the requested Ingress/Egress bandwidth, and create a reserved
        // bandwidth object
        combined.stream().filter(urn -> urn.getUrnType().equals(UrnType.IFCE)).forEach(vertex -> {
            UrnE urn = urnMap.get(vertex.getUrn());

            Set<ReservedVlanE> urnReservedVlans = vlanMap.get(urn.getUrn()).stream()
                    .map(id -> createReservedVlan(urn.getUrn(), id, start, end))
                    .collect(Collectors.toSet());

            reservedVlans.addAll(urnReservedVlans);
        });

        // Return the set
        return reservedVlans;
    }

    /**
     * Create a Junction and it's associated fixtures given the input parameters.
     *
     * @param device             - The device vertex associated with the junction
     * @param urnMap             - A mapping of URN strings to URN objects
     * @param deviceModels       - A mapping of URN strings to Device Models
     * @param requestedJunctions - A set of requested junctions - used to determine attributes of junction/fixtures
     * @param vlanMap            - Map of assigned VLANs for each fixture
     * @param start                 - The requested start date
     * @param end                   - The requested end date
     * @return A Reserved Vlan Junction with Reserved Fixtures (if contained in a matching requested junction)
     * @throws PSSException
     */
    public ReservedVlanJunctionE createJunctionAndFixtures(TopoVertex device, Map<String, UrnE> urnMap,
                                                           Map<String, DeviceModel> deviceModels,
                                                           Set<RequestedVlanJunctionE> requestedJunctions,
                                                           Map<String, Set<Integer>> vlanMap,
                                                           Map<String, String> portToDeviceMap, Date start, Date end, String connectionId)
            throws PSSException {
        // Get this junction's URN, Device Model, and Typing for its Fixtures
        UrnE aUrn = urnMap.get(device.getUrn());
        DeviceModel model = deviceModels.get(aUrn.getUrn());
        EthFixtureType fixType = pceAssistant.decideFixtureType(model);
        String urnString = aUrn.getUrn();

        // Create the set of Reserved Fixtures by filtering for requested junctions that match the URN
        Set<ReservedVlanFixtureE> reservedVlanFixtures = requestedJunctions
                .stream()
                .filter(reqJunction -> reqJunction.getDeviceUrn().equals(urnString))
                .map(RequestedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(reqFix -> createFixtureAndResources(reqFix.getPortUrn(), fixType,
                        reqFix.getInMbps(), reqFix.getEgMbps(), vlanMap.get(reqFix.getPortUrn()), start, end, connectionId))
                .collect(Collectors.toSet());

        Set<ReservedVlanE> reservedVlans = reservedVlanFixtures.stream()
                .map(ReservedVlanFixtureE::getReservedVlans)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<String> fixtureUrns = reservedVlanFixtures.stream().map(ReservedVlanFixtureE::getIfceUrn).collect(Collectors.toSet());
        for (String portUrn : vlanMap.keySet()) {
            if (!fixtureUrns.contains(portUrn) && portToDeviceMap.get(portUrn).equals(device.getUrn())) {
                reservedVlans.addAll(vlanMap.get(portUrn).stream()
                        .map(id -> createReservedVlan(portUrn, id, start, end))
                        .collect(Collectors.toSet()));
            }
        }

        // Return the Reserved Junction (with the set of reserved VLAN fixtures).
        return createReservedJunction(urnString, new HashSet<>(),
                reservedVlanFixtures, pceAssistant.decideJunctionType(model), reservedVlans);
    }

    /**
     * Create a Reserved Fixtures and it's associated resources (VLAN and Bandwidth) using the input.
     *
     * @param portUrn     - The URN of the desired fixture
     * @param fixtureType - The typing of the desired fixture
     * @param azMbps      - The requested ingress bandwidth
     * @param zaMbps      - The requested egress bandwidth
     * @param vlanIds     - The assigned VLAN IDs
     * @param start                 - The requested start date
     * @param end                   - The requested end date
     * @return The reserved fixture, containing all of its reserved resources
     */
    public ReservedVlanFixtureE createFixtureAndResources(String portUrn, EthFixtureType fixtureType, Integer azMbps,
                                                          Integer zaMbps, Set<Integer> vlanIds, Date start, Date end, String connectionId) {


        // Create reserved resources for Fixture
        ReservedBandwidthE rsvBw = createReservedBandwidth(portUrn, azMbps, zaMbps, start, end, connectionId);
        Set<ReservedVlanE> rsvVlans = vlanIds.stream().map(id -> createReservedVlan(portUrn, id, start, end)).collect(Collectors.toSet());
        // Create Fixture
        return createReservedFixture(portUrn, new HashSet<>(), rsvVlans, rsvBw, fixtureType);
    }

    /**
     * Create a reserved junction given the input
     *
     * @param urn          - The junction's URN
     * @param pssResources - The junction's PSS Resources
     * @param fixtures     - The junction's fixtures
     * @param junctionType - The junction's type
     * @return The Reserved VLAN Junction
     */
    public ReservedVlanJunctionE createReservedJunction(String urn, Set<ReservedPssResourceE> pssResources,
                                                        Set<ReservedVlanFixtureE> fixtures, EthJunctionType junctionType,
                                                        Set<ReservedVlanE> reservedVlans) {
        return ReservedVlanJunctionE.builder()
                .deviceUrn(urn)
                .reservedPssResources(pssResources)
                .fixtures(fixtures)
                .junctionType(junctionType)
                .reservedVlans(reservedVlans)
                .build();
    }

    /**
     * Create a reserved fixture, given the input parameters.
     *
     * @param urn          - The fixture's URN
     * @param pssResources - The fixture's PSS Resources
     * @param rsvVlans     - The fixture's assigned VLAN IDs
     * @param rsvBw        - The fixture's assigned bandwidth
     * @param fixtureType  - The fixture's type
     * @return The Reserved VLAN Fixture
     */
    public ReservedVlanFixtureE createReservedFixture(String urn, Set<ReservedPssResourceE> pssResources,
                                                      Set<ReservedVlanE> rsvVlans, ReservedBandwidthE rsvBw,
                                                      EthFixtureType fixtureType) {
        return ReservedVlanFixtureE.builder()
                .ifceUrn(urn)
                .reservedPssResources(pssResources)
                .reservedVlans(rsvVlans)
                .reservedBandwidth(rsvBw)
                .fixtureType(fixtureType)
                .build();
    }


    /**
     * Create the reserved bandwidth given the input parameters.
     *
     * @param urn    - The URN associated with this bandwidth
     * @param inMbps - The ingress bandwidth
     * @param egMbps - The egress bandwidth
     * @param start  - The requested start
     * @param end    - The requested end
     * @return A reserved bandwidth object
     */
    public ReservedBandwidthE createReservedBandwidth(String urn, Integer inMbps, Integer egMbps, Date start, Date end, String connectionId) {
        return ReservedBandwidthE.builder()
                .urn(urn)
                .inBandwidth(inMbps)
                .egBandwidth(egMbps)
                .beginning(start.toInstant())
                .ending(end.toInstant())
                .containerConnectionId(connectionId)
                .build();
    }

    /**
     * Create the reserved VLAN ID given the input parameters.
     *
     * @param urn    - The URN associated with this VLAN
     * @param vlanId - The ID value for the VLAN tag
     * @param start  - The requested start time
     * @param end    - The requested end time
     * @return The reserved VLAN objct
     */
    public ReservedVlanE createReservedVlan(String urn, Integer vlanId, Date start, Date end) {
        return ReservedVlanE.builder()
                .urn(urn)
                .vlan(vlanId)
                .beginning(start.toInstant())
                .ending(end.toInstant())
                .build();
    }


    public void testBandwidthRequirements(RequestedVlanPipeE reqPipe, List<TopoEdge> azERO, List<TopoEdge> zaERO,
                                          Map<String, UrnE> urnMap, Integer azMbps, Integer zaMbps,
                                          Map<String, Map<String, Integer>> availBwMap,
                                          Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap,
                                          List<ReservedBandwidthE> reservedBandwidths) throws PCEException {
        // Confirm that there is sufficient bandwidth to meet the request (given what has been reserved so far)

        // Evaluate the fixtures
        boolean sufficientBwFixtures = bwService.evaluateBandwidthFixtures(reqPipe, availBwMap);
        if(!sufficientBwFixtures){
            throw new PCEException("Insufficient bandwidth to meet requested fixtures for pipe " + reqPipe.toString());
        }
        // Palindromic EROs -- evaluate both directions at each port - traffic flows both ways
        if (pceAssistant.palindromicEros(azERO, zaERO)) {
            boolean sufficientBw = bwService.evaluateBandwidthEROBi(urnMap, azMbps, zaMbps, azERO, zaERO, availBwMap);
            if (!sufficientBw)
                throw new PCEException("Insufficient Bandwidth to meet requested pipe" + reqPipe.toString() +
                        " given previous reservations in flow");
        }
        // Non-Palindromic EROs -- evaluate the ports in each ERO just for traffic in the AZ or ZA direction
        else {
            // Consider A->Z ERO with bwAZ
            boolean sufficientBwAZ = bwService.evaluateBandwidthEROUni(azERO, urnMap, availBwMap, azMbps);
            // Consider Z->A ERO with bwZA
            boolean sufficientBwZA = bwService.evaluateBandwidthEROUni(zaERO, urnMap, availBwMap, zaMbps);
            if (!sufficientBwAZ) {
                throw new PCEException("Insufficient Bandwidth to meet requested A->Z pipe" + reqPipe.toString() +
                        " given previous reservations in flow");
            }
            if (!sufficientBwZA) {
                throw new PCEException("Insufficient Bandwidth to meet requested Z->A pipe" + reqPipe.toString() +
                        " given previous reservations in flow");
            }

            // Now consider those ports which are shared by both A->Z and Z->A -- they must be checked for both directions.
            List<TopoVertex> bidirectionalPorts = requestedBandwidthMap.keySet()
                    .stream()
                    .filter(v -> v.getVertexType().equals(VertexType.PORT))
                    .filter(v -> requestedBandwidthMap.get(v).get("Ingress") > 0)
                    .filter(v -> requestedBandwidthMap.get(v).get("Egress") > 0)
                    .collect(Collectors.toList());

            for (TopoVertex biPort : bidirectionalPorts) {
                if (!urnMap.containsKey(biPort.getUrn())) {
                    assert false;
                }

                if (!bwService.evaluateBandwidthURN(biPort.getUrn(), availBwMap, azMbps, zaMbps)) {
                    throw new PCEException("Insufficient Bandwidth to meet requested pipe" + reqPipe.toString() +
                            " given previous reservations in flow");
                }
            }

            /* Support fringe case of identical link traversal in both A->Z and Z->A direction */
            List<TopoEdge> sharedEdges = azERO.stream().collect(Collectors.toList());
            sharedEdges.retainAll(zaERO);

            if (!sharedEdges.isEmpty()) {
                List<TopoVertex> sharedPorts = new ArrayList<>();
                List<String> sharedPortNames = new ArrayList<>();
                sharedPorts.add(sharedEdges.get(0).getA());
                sharedPortNames.add(sharedEdges.get(0).getA().getUrn());
                for (TopoEdge e : sharedEdges) {
                    sharedPorts.add(e.getZ());
                    sharedPortNames.add(e.getZ().getUrn());
                }

                // Filter out devices
                sharedPorts.removeIf(p -> !p.getVertexType().equals(VertexType.PORT));

                List<ReservedBandwidthE> sharedBandwidths = reservedBandwidths.stream()
                        .filter(sBW -> sharedPortNames.contains(sBW.getUrn()))
                        .collect(Collectors.toList());

                Map<String, Map<String, Integer>> sharedBwMap;
                sharedBwMap = bwService.buildBandwidthAvailabilityMap(sharedBandwidths);

                for (TopoVertex sharedPort : sharedPorts) {
                    if (!urnMap.containsKey(sharedPort.getUrn())) {
                        assert false;
                    }

                    if (!bwService.evaluateBandwidthSharedURN(sharedPort.getUrn(), sharedBwMap, azMbps, azMbps, zaMbps, zaMbps)) {
                        throw new PCEException("Insufficient Bandwidth to meet requested pipe" + reqPipe.toString() + " given previous reservations in flow");
                    }
                }
            }
            /* * */
        }
    }

}
