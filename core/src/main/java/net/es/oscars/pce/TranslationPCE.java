package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.VertexType;
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
     * @param req_j - The requested junction
     * @param sched - The requested schedule
     * @param simpleJunctions - A set of all singular requested junctions so far
     * @return The Reserved Junction
     * @throws PCEException
     * @throws PSSException
     */
    public ReservedVlanJunctionE reserveSimpleJunction(RequestedVlanJunctionE req_j, ScheduleSpecificationE sched,
                                                       Set<ReservedVlanJunctionE> simpleJunctions,
                                                       List<ReservedBandwidthE> reservedBandwidths,
                                                       List<ReservedVlanE> reservedVlans)
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
        ReservedVlanJunctionE rsv_j = createReservedJunction(urn, new HashSet<>(), new HashSet<>(),
                pceAssistant.decideJunctionType(urn.getDeviceModel()));

        // Select a VLAN ID for this junction
        Map<RequestedVlanFixtureE, Integer> fixVlanMap = vlanService.selectVLANsForJunction(req_j, sched, simpleJunctions, reservedVlans);
        if(fixVlanMap.containsValue(-1)){
            return null;
        }

        // Confirm that there is sufficient available bandwidth
        boolean sufficientBandwidth = bwService.evaluateBandwidthJunction(req_j, reservedBandwidths);
        if(!sufficientBandwidth){
            return null;
        }

        // For each requested fixture, create a reserved bandwdith and reserved VLAN object
        // and store them in a Reserved Fixture
        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();
        for(RequestedVlanFixtureE reqFix : reqFixtures){
            ReservedBandwidthE rsvBw = createReservedBandwidth(reqFix.getPortUrn(), reqFix.getInMbps(),
                    reqFix.getEgMbps(), sched);

            ReservedVlanE rsvVlan = createReservedVlan(reqFix.getPortUrn(), fixVlanMap.get(reqFix), sched);

            ReservedVlanFixtureE rsvFix = createReservedFixture(reqFix.getPortUrn(), new HashSet<>(),
                    rsvVlan, rsvBw, pceAssistant.decideFixtureType(reqFix.getPortUrn().getDeviceModel()));

            // Add the fixtures to the Reserved Junction
            rsv_j.getFixtures().add(rsvFix);
        }

        return rsv_j;
    }

    /**
     * Create a set of reserved pipes/junctions from a requested pipe. A requested pipe can produce:
     * One pipe for each pair of Ethernet devices
     * One pipe for each MPLS segment along the path
     * This function will add to the reservedMplsPipes and reservedEthPipes sets passed in as input
     * @param reqPipe - THe requested pipe, containing details on the requested endpoints/bandwidth/VLANs
     * @param sched - The requested schedule (i.e. start/end date)
     * @param azERO - The physical path taken by the pipe in the A->Z direction
     * @param zaERO - The physical path taken by the pipe in the Z->A direction
     * @param reservedBandwidths - The list of all bandwidth reserved so far
     * @param reservedVlans - The list of all VLAN IDs reserved so far
     * @param reservedMplsPipes - The set of all reserved MPLS pipes so far
     * @param reservedEthPipes - The set of all reserved Ethernet pipes so far
     * @throws PCEException
     * @throws PSSException
     */
    public void reserveRequestedPipe(RequestedVlanPipeE reqPipe, ScheduleSpecificationE sched, List<TopoEdge> azERO,
                                     List<TopoEdge> zaERO, List<ReservedBandwidthE> reservedBandwidths,
                                     List<ReservedVlanE> reservedVlans, Set<ReservedMplsPipeE> reservedMplsPipes,
                                     Set<ReservedEthPipeE> reservedEthPipes)
    throws PCEException, PSSException{

        // Get requested bandwidth
        Integer azMbps = reqPipe.getAzMbps();
        Integer zaMbps = reqPipe.getZaMbps();

        // Retrieve a map of URN strings to device models
        Map<String, DeviceModel> deviceModels = topoService.deviceModels();

        // Build a urn map
        Map<String , UrnE> urnMap = new HashMap<>();
        urnRepository.findAll().stream().forEach(u -> {
            urnMap.put(u.getUrn(), u);
        });

        // Retrieve requested junctions
        Set<RequestedVlanJunctionE> reqPipeJunctions = new HashSet<>();
        reqPipeJunctions.add(reqPipe.getAJunction());
        reqPipeJunctions.add(reqPipe.getZJunction());

        // Get map of "Ingress" and "Egress" bandwidth availability
        Map<UrnE, Map<String, Integer>> availBwMap;
        availBwMap = bwService.buildBandwidthAvailabilityMap(reservedBandwidths);

        // Returns a mapping from topovertices (ports) to an "Ingress"/"Egress" map of the total Ingress/Egress
        // Requested bandwidth at that port across both the azERO and the zaERO
        List<List<TopoEdge>> EROs = Arrays.asList(azERO, zaERO);
        List<Integer> bandwidths = Arrays.asList(azMbps, zaMbps);
        Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap = bwService.buildRequestedBandwidthMap(EROs, bandwidths);

        // Confirm that there is sufficient bandwidth to meet the request (given what has been reserved so far)
        // Palindromic EROs -- evaluate both directions at each port - traffic flows both ways
        if(pceAssistant.palindromicEros(azERO, zaERO)){
            boolean sufficientBw = bwService.evaluateBandwidthEROBi(urnMap, azMbps, zaMbps, azERO, zaERO, availBwMap);
            if(!sufficientBw)
                throw new PCEException("Insufficient Bandwidth to meet requested pipe" + reqPipe.toString() +
                        " given previous reservations in flow");
        }
        // Non-Palindromic EROs -- evaluate the ports in each ERO just for traffic in the AZ or ZA direction
        else{
            // Consider A->Z ERO with bwAZ
            boolean sufficientBwAZ = bwService.evaluateBandwidthEROUni(azERO, urnMap, availBwMap, azMbps);
            // Consider Z->A ERO with bwZA
            boolean sufficientBwZA = bwService.evaluateBandwidthEROUni(zaERO, urnMap, availBwMap, zaMbps);
            if(!sufficientBwAZ)
            {
                throw new PCEException("Insufficient Bandwidth to meet requested A->Z pipe" + reqPipe.toString() +
                        " given previous reservations in flow");
            }
            if(!sufficientBwZA)
            {
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

            for(TopoVertex biPort : bidirectionalPorts)
            {
                if(!urnMap.containsKey(biPort.getUrn()))
                {
                    assert false;
                }
                UrnE biUrn = urnMap.get(biPort.getUrn());

                if(!bwService.evaluateBandwidthURN(biUrn, availBwMap, azMbps, zaMbps)){
                    throw new PCEException("Insufficient Bandwidth to meet requested pipe" + reqPipe.toString() +
                            " given previous reservations in flow");
                }
            }


        }

        Map<UrnE, Integer> chosenVlanMap = vlanService.selectVlansForPipe(reqPipe, urnMap, reservedVlans, azERO, zaERO);
        log.info("Chosen VLAN Map: " + chosenVlanMap);
        for(UrnE urn : chosenVlanMap.keySet()){
            if(chosenVlanMap.get(urn).equals(-1)){
                throw new PCEException(("VLAN could not not be found for URN " + urn.toString()));
            }
        }

        // now, decompose the path
        List<Map<Layer, List<TopoVertex>>> azSegments = PCEAssistant.decompose(azERO);
        List<Map<Layer, List<TopoVertex>>> zaSegments = PCEAssistant.decompose(zaERO);
        assert(azSegments.size() == zaSegments.size());

        // for each segment:
        // if it is an Ethernet segment, make junctions, one per device
        // if it is an MPLS segment, make a pipe
        // all the while, make sure to merge in the current first and last junctions as needed

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

        Map<TopoVertex, ReservedVlanJunctionE> junctionMap = new HashMap<>();

        for(List<TopoVertex> junctionPair : allJunctionPairs.keySet()){
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
            if(!junctionMap.containsKey(aVertex)){
                aJunction = createJunctionAndFixtures(aVertex, urnMap, deviceModels, reqPipeJunctions,
                        chosenVlanMap, sched);
                junctionMap.put(aVertex, aJunction);
            }
            else{
                aJunction = junctionMap.get(aVertex);
            }
            // Create Z Junction
            if(!junctionMap.containsKey(zVertex)){
                zJunction = createJunctionAndFixtures(zVertex, urnMap, deviceModels, reqPipeJunctions,
                        chosenVlanMap, sched);
                junctionMap.put(zVertex, zJunction);
            }
            else{
                zJunction = junctionMap.get(zVertex);
            }

            DeviceModel aModel = deviceModels.get(aJunction.getDeviceUrn().getUrn());
            DeviceModel zModel = deviceModels.get(zJunction.getDeviceUrn().getUrn());

            Set<ReservedBandwidthE> pipeBandwidths = createReservedBandwidthForEROs(pipeEroMap.get("AZ"),
                    pipeEroMap.get("ZA"), urnMap, requestedBandwidthMap, sched);


            if(thisLayer.equals(Layer.MPLS)){
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
            else{
                Set<ReservedVlanE> pipeVlans = createReservedVlanForEROs(pipeEroMap.get("AZ"),
                        pipeEroMap.get("ZA"), urnMap, chosenVlanMap, sched);

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



    /**
     * Given two lists of EROS in the AZ and ZA direction, a map of URNs, a map of the requested bandwidth at each URN,
     * and the requested schedule, return a combined set of reserved bandwidth objects for the AZ and ZA paths
     * @param az - The AZ vertices
     * @param za - The ZA vertices
     * @param urnMap - A mapping of URN string to URN object
     * @param requestedBandwidthMap - A mapping of Vertex objects to "Ingress"/"Egress" requested bandwidth
     * @param sched - THe requested schedule
     * @return A set of all reserved bandwidth for every port (across both paths)
     */
    public Set<ReservedBandwidthE> createReservedBandwidthForEROs(List<TopoVertex> az, List<TopoVertex> za,
                                                                  Map<String, UrnE> urnMap,
                                                                  Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap,
                                                                  ScheduleSpecificationE sched) {
        // Combine the AZ and ZA EROs
        Set<TopoVertex> combined = new HashSet<>(az);
        combined.addAll(za);

        // Store the reserved bandwidths
        Set<ReservedBandwidthE> reservedBandwidths = new HashSet<>();

        // For each vertex in the combined set, retrieve the requested Ingress/Egress bandwidth, and create a reserved
        // bandwidth object
        combined.stream().filter(requestedBandwidthMap::containsKey).forEach(vertex -> {
            UrnE urn = urnMap.get(vertex.getUrn());
            Integer reqInMbps = requestedBandwidthMap.get(vertex).get("Ingress");
            Integer reqEgMbps = requestedBandwidthMap.get(vertex).get("Egress");

            ReservedBandwidthE rsvBw = createReservedBandwidth(urn, reqInMbps, reqEgMbps, sched);
            reservedBandwidths.add(rsvBw);

        });

        // Return the set
        return reservedBandwidths;
    }

    /**
     * Given two lists of EROS in the AZ and ZA direction, a map of URNs, a chosen vlan ID,
     * and the requested schedule, return a combined set of reserved VLAN objects for the AZ and ZA paths
     * @param az - The AZ vertices
     * @param za - The ZA vertices
     * @param urnMap - A mapping of URN string to URN object
     * @param vlanMap - A mapping of URN object to assigned VLAN ID
     * @param sched - THe requested schedule
     * @return A set of all reserved VLAN objects for every port (across both paths)
     */
    public Set<ReservedVlanE> createReservedVlanForEROs(List<TopoVertex> az, List<TopoVertex> za,
                                                        Map<String, UrnE> urnMap, Map<UrnE, Integer> vlanMap,
                                                        ScheduleSpecificationE sched) {
        // Combine the AZ and ZA EROs
        Set<TopoVertex> combined = new HashSet<>(az);
        combined.addAll(za);

        // Store the reserved bandwidths
        Set<ReservedVlanE> reservedVlans = new HashSet<>();

        // For each vertex in the combined set, retrieve the requested Ingress/Egress bandwidth, and create a reserved
        // bandwidth object
        combined.stream().filter(vertex -> vertex.getVertexType().equals(VertexType.PORT)).forEach(vertex -> {
            UrnE urn = urnMap.get(vertex.getUrn());

            ReservedVlanE rsvVlan = createReservedVlan(urn, vlanMap.get(urn), sched);
            reservedVlans.add(rsvVlan);

        });

        // Return the set
        return reservedVlans;
    }

    /**
     * Create a Junction and it's associated fixtures given the input parameters.
     * @param device - The device vertex associated with the junction
     * @param urnMap - A mapping of URN strings to URN objects
     * @param deviceModels - A mapping of URN strings to Device Models
     * @param requestedJunctions - A set of requested junctions - used to determine attributes of junction/fixtures
     * @param vlanMap - Map of assigned VLANs for each fixture
     * @param sched - The requested schedule
     * @return A Reserved Vlan Junction with Reserved Fixtures (if contained in a matching requested junction)
     * @throws PSSException
     */
    public ReservedVlanJunctionE createJunctionAndFixtures(TopoVertex device, Map<String, UrnE> urnMap,
                                                           Map<String, DeviceModel> deviceModels,
                                                           Set<RequestedVlanJunctionE> requestedJunctions,
                                                           Map<UrnE, Integer> vlanMap, ScheduleSpecificationE sched)
            throws PSSException {
        // Get this junction's URN, Device Model, and Typing for its Fixtures
        UrnE aUrn = urnMap.get(device.getUrn());
        DeviceModel model = deviceModels.get(aUrn.getUrn());
        EthFixtureType fixType = pceAssistant.decideFixtureType(model);

        // Create the set of Reserved Fixtures by filtering for requested junctions that match the URN
        log.info(requestedJunctions.stream().filter(reqJunction -> reqJunction.getDeviceUrn().equals(aUrn)).collect(Collectors.toList()).toString());
        Set<ReservedVlanFixtureE> reservedVlanFixtures = requestedJunctions
                .stream()
                .filter(reqJunction -> reqJunction.getDeviceUrn().equals(aUrn))
                .map(RequestedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(reqFix -> createFixtureAndResources(reqFix.getPortUrn(), fixType,
                        reqFix.getInMbps(), reqFix.getEgMbps(), vlanMap.get(reqFix.getPortUrn()), sched))
                .collect(Collectors.toSet());

        log.info("Reserved Fixtures at " + aUrn + ": " + reservedVlanFixtures);
        // Return the Reserved Junction (with the set of reserved VLAN fixtures).
        return createReservedJunction(aUrn, new HashSet<>(),
                reservedVlanFixtures, pceAssistant.decideJunctionType(model));
    }

    /**
     * Create a Reserved Fixtures and it's associated resources (VLAN and Bandwidth) using the input.
     * @param portUrn - The URN of the desired fixture
     * @param fixtureType - The typing of the desired fixture
     * @param azMbps - The requested ingress bandwidth
     * @param zaMbps - The requested egress bandwidth
     * @param vlanId - The assigned VLAN ID
     * @param sched - The requested schedule
     * @return The reserved fixture, containing all of its reserved resources
     */
    public ReservedVlanFixtureE createFixtureAndResources(UrnE portUrn, EthFixtureType fixtureType, Integer azMbps,
                                                          Integer zaMbps, Integer vlanId,
                                                          ScheduleSpecificationE sched){


        // Create reserved resources for Fixture
        ReservedBandwidthE rsvBw = createReservedBandwidth(portUrn, azMbps, zaMbps, sched);
        ReservedVlanE rsvVlan = createReservedVlan(portUrn, vlanId, sched);
        // Create Fixture
        return createReservedFixture(portUrn, new HashSet<>(), rsvVlan, rsvBw, fixtureType);
    }

    /**
     * Create a reserved junction given the input
     * @param urn - The junction's URN
     * @param pssResources - The junction's PSS Resources
     * @param fixtures - The junction's fixtures
     * @param junctionType - The junction's type
     * @return The Reserved VLAN Junction
     */
    public ReservedVlanJunctionE createReservedJunction(UrnE urn, Set<ReservedPssResourceE> pssResources,
                                                        Set<ReservedVlanFixtureE> fixtures, EthJunctionType junctionType){
        return ReservedVlanJunctionE.builder()
                .deviceUrn(urn)
                .reservedPssResources(pssResources)
                .fixtures(fixtures)
                .junctionType(junctionType)
                .build();
    }

    /**
     * Create a reserved fixture, given the input parameters.
     * @param urn - The fixture's URN
     * @param pssResources - The fixture's PSS Resources
     * @param rsvVlan - The fixture's assigned VLAN ID
     * @param rsvBw - The fixture's assigned bandwidth
     * @param fixtureType - The fixture's type
     * @return The Reserved VLAN Fixture
     */
    public ReservedVlanFixtureE createReservedFixture(UrnE urn, Set<ReservedPssResourceE> pssResources,
                                                      ReservedVlanE rsvVlan, ReservedBandwidthE rsvBw,
                                                      EthFixtureType fixtureType){
        return ReservedVlanFixtureE.builder()
                .ifceUrn(urn)
                .reservedPssResources(pssResources)
                .reservedVlan(rsvVlan)
                .reservedBandwidth(rsvBw)
                .fixtureType(fixtureType)
                .build();
    }


    /**
     * Create the reserved bandwidth given the input parameters.
     * @param urn - The URN associated with this bandwidth
     * @param inMbps - The ingress bandwidth
     * @param egMbps - The egress bandwidth
     * @param sched - The requested schedule
     * @return A reserved bandwidth object
     */
    public ReservedBandwidthE createReservedBandwidth(UrnE urn, Integer inMbps, Integer egMbps, ScheduleSpecificationE sched){
        return ReservedBandwidthE.builder()
                .urn(urn)
                .inBandwidth(inMbps)
                .egBandwidth(egMbps)
                .beginning(sched.getNotBefore().toInstant())
                .ending(sched.getNotAfter().toInstant())
                .build();
    }

    /**
     * Create the reserved VLAN ID given the input parameters.
     * @param urn - The URN associated with this VLAN
     * @param vlanId - The ID value for the VLAN tag
     * @param sched - The requested schedule
     * @return The reserved VLAN objct
     */
    public ReservedVlanE createReservedVlan(UrnE urn, Integer vlanId, ScheduleSpecificationE sched){
        return ReservedVlanE.builder()
                .urn(urn)
                .vlan(vlanId)
                .beginning(sched.getNotBefore().toInstant())
                .ending(sched.getNotAfter().toInstant())
                .build();
    }

}
