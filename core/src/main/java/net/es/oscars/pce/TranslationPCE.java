package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.dao.ReservedPssResourceRepository;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.ReservableBandwidthE;
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
                                                       Set<ReservedVlanJunctionE> simpleJunctions,
                                                       List<ReservedBandwidthE> reservedBandwidths)
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

        // Select a VLAN ID for this junction
        Integer vlanId = selectVLANForJunction(req_j, sched, simpleJunctions);
        if(vlanId == -1){
            return null;
        }

        // Confirm that there is sufficient available bandwidth
        boolean sufficientBandwidth = confirmSufficientBandwidth(req_j, reservedBandwidths);
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

        // Retrieve requested fixtures
        Set<RequestedVlanJunctionE> reqPipeJunctions = new HashSet<>();
        reqPipeJunctions.add(reqPipe.getAJunction());
        reqPipeJunctions.add(reqPipe.getZJunction());


        // Get map of "Ingress" and "Egress" bandwidth availability
        Map<UrnE, Map<String, Integer>> availBwMap;
        availBwMap = createBandwidthAvailabilityMap(reservedBandwidths);

        // Returns a mapping from topovertices (ports) to an "Ingress"/"Egress" map of the total Ingress/Egress
        // Requested bandwidth at that port across both the azERO and the zaERO
        Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap = createRequestedBandwidthMap(azERO, zaERO, azMbps, zaMbps);

        // Confirm that there is sufficient bandwidth to meet the request (given what has been reserved so far)
        // Palindromic EROs -- evaluate both directions at each port - traffic flows both ways
        if(this.palindromicEros(azERO, zaERO)){
            boolean sufficientBw = checkForSufficientBw(urnMap, azMbps, zaMbps, azERO, zaERO, availBwMap);
            if(!sufficientBw)
                throw new PCEException("Insufficient Bandwidth to meet requested pipe" + reqPipe.toString() +
                        " given previous reservations in flow");
        }
        // Non-Palindromic EROs -- evaluate the ports in each ERO just for traffic in the AZ or ZA direction
        else{
            // Consider A->Z ERO with bwAZ
            boolean sufficientBwAZ = checkForSufficientBwUni(urnMap, azMbps, azERO, availBwMap);
            // Consider Z->A ERO with bwZA
            boolean sufficientBwZA = checkForSufficientBwUni(urnMap, zaMbps, zaERO, availBwMap);
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

                if(!sufficientBandwidthAtUrn(biUrn, availBwMap, azMbps, zaMbps)){
                    throw new PCEException("Insufficient Bandwidth to meet requested pipe" + reqPipe.toString() +
                            " given previous reservations in flow");
                }
            }


        }


        // Confirm that there is at least one VLAN ID that can support every segment (given what has been reserved so far)
        Set<Integer> validVlanIds = selectVlanIds(urnMap, reqPipe, azERO, zaERO, reservedVlans);
        if(validVlanIds.isEmpty()){
            throw new PCEException("Insufficient VLANs to meet requested pipe " +
                    reqPipe.toString() + " given previous reservations in flow");
        }

        // now, decompose the path
        List<Map<Layer, List<TopoVertex>>> azSegments = PCEAssistant.decompose(azERO);
        List<Map<Layer, List<TopoVertex>>> zaSegments = PCEAssistant.decompose(zaERO);
        assert(azSegments.size() == zaSegments.size());

        // for each segment:
        // if it is an Ethernet segment, make junctions, one per device
        // if it is an MPLS segment, make a pipe
        // all the while, make sure to merge in the current first and last junctions as needed


        // Get Chosen VLAN ID
        Integer vlanId = validVlanIds.iterator().next();


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

        log.info("0: All Junction Pairs - " + allJunctionPairs.toString());
        for(List<TopoVertex> junctionPair : allJunctionPairs.keySet()){
            Layer thisLayer = allJunctionPairs.get(junctionPair);
            log.info("1: Layer " + thisLayer);
            Map<String, List<TopoVertex>> pipeEroMap = junctionPairToPipeEROMap.get(junctionPair);
            log.info("2: Pipe ERO Map " + pipeEroMap.toString());
            List<String> azPipeEro = pipeEroMap.get("AZ")
                    .stream()
                    .map(TopoVertex::getUrn)
                    .collect(Collectors.toList());
            log.info("3: AZ ERO " + azPipeEro);
            List<String> zaPipeEro = pipeEroMap.get("ZA")
                    .stream()
                    .map(TopoVertex::getUrn)
                    .collect(Collectors.toList());
            log.info("4: ZA ERO " + zaPipeEro);

            TopoVertex aVertex = junctionPair.get(0);
            TopoVertex zVertex = junctionPair.get(1);

            ReservedVlanJunctionE aJunction;
            ReservedVlanJunctionE zJunction;
            // Create or retrieve A Junction
            if(!junctionMap.containsKey(aVertex)){

                aJunction = pceAssistant.createJunctionAndFixtures(aVertex, urnMap, deviceModels, reqPipeJunctions,
                        vlanId, sched);
                junctionMap.put(aVertex, aJunction);
            }
            else{
                aJunction = junctionMap.get(aVertex);
            }
            log.info("5: A Junction " + aJunction.toString());
            // Create Z Junction
            if(!junctionMap.containsKey(zVertex)){
                zJunction = pceAssistant.createJunctionAndFixtures(zVertex, urnMap, deviceModels, reqPipeJunctions,
                        vlanId, sched);
                junctionMap.put(zVertex, zJunction);
            }
            else{
                zJunction = junctionMap.get(zVertex);
            }
            log.info("6: Z Junction " + zJunction.toString());

            DeviceModel aModel = deviceModels.get(aJunction.getDeviceUrn().getUrn());
            log.info("7: A Model " + aModel);
            DeviceModel zModel = deviceModels.get(zJunction.getDeviceUrn().getUrn());
            log.info("8: Z Model " + zModel);

            Set<ReservedBandwidthE> pipeBandwidths = pceAssistant.createReservedBandwidthForEROs(pipeEroMap.get("AZ"),
                    pipeEroMap.get("ZA"), urnMap, requestedBandwidthMap, sched);

            log.info("9: Pipe bandwidths" + pipeBandwidths.toString());

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
                log.info("10-1: MPLS Pipe " + mplsPipe.toString());
                reservedMplsPipes.add(mplsPipe);
            }
            // ETHERNET
            else{
                ReservedEthPipeE ethPipe = ReservedEthPipeE.builder()
                        .aJunction(aJunction)
                        .zJunction(zJunction)
                        .azERO(azPipeEro)
                        .zaERO(zaPipeEro)
                        .reservedBandwidths(pipeBandwidths)
                        .reservedVlan(vlanId)
                        .reservedPssResources(new HashSet<>())
                        .pipeType(pceAssistant.decideEthPipeType(aModel, zModel))
                        .build();
                log.info("10-2: Ethernet Pipe " + ethPipe.toString());
                reservedEthPipes.add(ethPipe);
            }
        }

    }


    /**
     * Build a map of the available bandwidth at each URN. For each URN, there is a map of "Ingress" and "Egress"
     * bandwidth available. Only port URNs can be found in this map.
     * @param rsvBandwidths - A list of all bandwidth reserved so far
     * @return A mapping of URN to Ingress/Egress bandwidth availability
     */
    private Map<UrnE, Map<String, Integer>> createBandwidthAvailabilityMap(List<ReservedBandwidthE> rsvBandwidths){
        // Build a map, allowing us to retrieve a list of ReservedBandwidth given the associated URN
        Map<UrnE, List<ReservedBandwidthE>> resvBwMap = pruningService.buildReservedBandwidthMap(rsvBandwidths);

        // Build a map, allowing us to retrieve the available "Ingress" and "Egress" bandwidth at each associated URN
        Map<UrnE, Map<String, Integer>> availBwMap = new HashMap<>();
        urnRepository.findAll()
                .stream()
                .filter(urn -> urn.getReservableBandwidth() != null)
                .forEach(urn -> availBwMap.put(urn, pruningService.getBwAvailabilityForUrn(urn, urn.getReservableBandwidth(), resvBwMap)));

        return availBwMap;
    }

    public List<ReservedBandwidthE> createReservedBandwidthList(Set<ReservedVlanJunctionE> reservedJunctions,
                                                                 Set<ReservedMplsPipeE> reservedMplsPipes,
                                                                 Set<ReservedEthPipeE> reservedEthPipes,
                                                                 ScheduleSpecificationE sched){
        // Retrieve all bandwidth reserved so far from pipes & junctions
        List<ReservedBandwidthE> rsvBandwidths = retrieveReservedBandwidths(reservedJunctions);
        rsvBandwidths.addAll(retrieveReservedBandwidthsFromMplsPipes(reservedMplsPipes));
        rsvBandwidths.addAll(retrieveReservedBandwidthsFromEthPipes(reservedEthPipes));
        rsvBandwidths.addAll(pruningService.getReservedBandwidth(sched.getNotBefore(), sched.getNotAfter()));

        return rsvBandwidths;
    }

    public List<ReservedVlanE> createReservedVlanList(Set<ReservedVlanJunctionE> reservedJunctions,
                                                       Set<ReservedEthPipeE> reservedEthPipes,
                                                       ScheduleSpecificationE sched){

        // Retrieve all VLAN IDs reserved so far from junctions & pipes
        List<ReservedVlanE> rsvVlans = retrieveReservedVlans(reservedJunctions);
        rsvVlans.addAll(retrieveReservedVlansFromEthPipes(reservedEthPipes));

        return rsvVlans;
    }

    /**
     * Build a map of the requested bandwidth at each port TopoVertex contained within the az and za EROs.
     * @param azERO - The path in the AZ direction
     * @param zaERO - The path in the ZA direction
     * @param azMbps - The requested bandwidth in the AZ direction
     * @param zaMbps - The requested bandwidth in the ZA direction
     * @return A mapping from TopoVertex (ports only) to requested "Ingress" and "Egress" bandwidth
     */
    private Map<TopoVertex, Map<String, Integer>> createRequestedBandwidthMap(List<TopoEdge> azERO, List<TopoEdge> zaERO,
                                                                             Integer azMbps, Integer zaMbps){
        // Map a port node to a map of "Ingress" and "Egress" requested bandwidth values
        Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap = new HashMap<>();

        // Iterate through the AZ edges, update the map for each port node found in the path
        for(TopoEdge azEdge : azERO){
            updateRequestedBandwidthMap(azEdge, azMbps, requestedBandwidthMap);
        }

        // Iterate through the ZA edges, update the map for each port node in the path
        for(TopoEdge zaEdge : zaERO){
            updateRequestedBandwidthMap(zaEdge, zaMbps, requestedBandwidthMap);
        }

        return requestedBandwidthMap;
    }

    /**
     * Update the values in a mapping of port vertices to requested Ingress/Egress bandwidth.
     * @param edge - An edge, with a vertex on the "A" side and on the "Z" side
     * @param bandwidth - The requested bandwidth in one direction
     * @param requestedBandwidthMap - The soon-to-be updated map of requested bandwidth per port
     */
    private void updateRequestedBandwidthMap(TopoEdge edge, Integer bandwidth,
                                             Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap){

        // Retrieve the "A" side of the edge
        TopoVertex nodeA = edge.getA();
        // Retrieve the "Z" side of the edge
        TopoVertex nodeZ = edge.getZ();

        // Add node A to the map if it is a port and it is not already in the map
        if(nodeA.getVertexType().equals(VertexType.PORT) && !requestedBandwidthMap.containsKey(nodeA)){
            requestedBandwidthMap.put(nodeA, makeInitialBandwidthMap());
        }
        // Add node Z to the map if it is a port and it is not already in the map
        if(nodeZ.getVertexType().equals(VertexType.PORT) && !requestedBandwidthMap.containsKey(nodeZ)){
            requestedBandwidthMap.put(nodeZ, makeInitialBandwidthMap());
        }

        // All Cases: Add the requested bandwidth to the amount already stored in the map
        // Case 1: portA -> portZ -- portA = egress, portZ = ingress
        /*
        if(nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT))
        {
            Integer updatedEgress = requestedBandwidthMap.get(nodeA).get("Egress") + bandwidth;
            requestedBandwidthMap.get(nodeA).put("Egress", updatedEgress);

            Integer updatedIngress = requestedBandwidthMap.get(nodeZ).get("Ingress") + bandwidth;
            requestedBandwidthMap.get(nodeZ).put("Ingress", updatedIngress);
        }*/
        // Case 2: portA -> deviceZ -- portA = ingress
        if(nodeA.getVertexType().equals(VertexType.PORT) && !nodeZ.getVertexType().equals(VertexType.PORT))
        {
            Integer updatedIngress = requestedBandwidthMap.get(nodeA).get("Ingress") + bandwidth;
            requestedBandwidthMap.get(nodeA).put("Ingress", updatedIngress);
        }
        // Case 3: deviceA -> portZ -- portZ = egress
        else if(!nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT))
        {
            Integer updatedEgress = requestedBandwidthMap.get(nodeZ).get("Egress") + bandwidth;
            requestedBandwidthMap.get(nodeZ).put("Egress", updatedEgress);
        }

    }

    /**
     * Build an initial map of requested bandwidth in the Ingress and Egress directions
     * @return A new map with 0 Ingress and Egress bandwidth.
     */
    private Map<String, Integer> makeInitialBandwidthMap(){
        Map<String, Integer> initialMap = new HashMap<>();
        initialMap.put("Ingress", 0);
        initialMap.put("Egress", 0);
        return initialMap;
    }

    /**
     * Confirm that the two EROs are identical
     * @param azERO - A path in one direction
     * @param zaERO - A path in another direction
     * @return True if they are identical, False otherwise.
     */
    private boolean palindromicEros(List<TopoEdge> azERO, List<TopoEdge> zaERO)
    {
        Set<TopoVertex> azVertices = new HashSet<>();
        Set<TopoVertex> zaVertices = new HashSet<>();

        for(TopoEdge azEdge : azERO)
        {
            azVertices.add(azEdge.getA());
            azVertices.add(azEdge.getZ());
        }

        for(TopoEdge zaEdge : zaERO)
        {
            zaVertices.add(zaEdge.getA());
            zaVertices.add(zaEdge.getZ());
        }

        if(azVertices.size() != zaVertices.size())
            return false;

        for(TopoVertex oneVert : azVertices)
        {
            if(!zaVertices.contains(oneVert))
                return false;
        }

        Set<TopoVertex> azPorts = azVertices
                .stream()
                .filter(v -> v.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());
        Set<TopoVertex> zaPorts = zaVertices
                .stream()
                .filter(v -> v.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());

        if(azPorts.size() != zaPorts.size())
            return false;

        // Now see if all ports are traversed in both the ingress and egress directions
        Set<TopoVertex> ingressPorts = new HashSet<>();
        Set<TopoVertex> egressPorts = new HashSet<>();

        // Identify which ports are used as ingress vs the ones that are used as egress
        for(TopoEdge azEdge : azERO)
        {
            TopoVertex nodeA = azEdge.getA();
            TopoVertex nodeZ = azEdge.getZ();

            // Case 1: portA -> portZ -- portA = egress, portZ = ingress
            if(nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT))
            {
                egressPorts.add(nodeA);
                ingressPorts.add(nodeZ);
            }
            // Case 2: portA -> deviceZ -- portA = ingress
            else if(nodeA.getVertexType().equals(VertexType.PORT) && !nodeZ.getVertexType().equals(VertexType.PORT))
            {
                ingressPorts.add(nodeA);
            }
            // Case 3: deviceA -> portZ -- portZ = egress
            else if(!nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT))
            {
                egressPorts.add(nodeZ);
            }
        }

        // repeat above for Z->A
        for(TopoEdge zaEdge : zaERO)
        {
            TopoVertex nodeA = zaEdge.getA();
            TopoVertex nodeZ = zaEdge.getZ();

            // Case 1: portA -> portZ -- portA = egress, portZ = ingress
            if(nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT))
            {
                egressPorts.add(nodeA);
                ingressPorts.add(nodeZ);
            }
            // Case 2: portA -> deviceZ -- portA = ingress
            else if(nodeA.getVertexType().equals(VertexType.PORT) && !nodeZ.getVertexType().equals(VertexType.PORT))
            {
                ingressPorts.add(nodeA);
            }
            // Case 3: deviceA -> portZ -- portZ = egress
            else if(!nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT))
            {
                egressPorts.add(nodeZ);
            }
        }

        // Now check to see if all ports used are both ingress and egress -- if so, palindromic port usage!
        if(ingressPorts.size() != egressPorts.size())
            return false;

        if(ingressPorts.size() != azPorts.size())
            return false;

        for(TopoVertex onePort : azPorts)
        {
            if(!(zaPorts.contains(onePort) && ingressPorts.contains(onePort) && egressPorts.contains(onePort)))
                return false;
        }

        return true;
    }

    /**
     * Examine all AZ and ZA edges, confirm that the requested bandwidth can be supported given the available bandwidth.
     * @param urnMap - A map of URN string to URN objects
     * @param azERO - The path in the A->Z direction
     * @param zaERO - The path in the Z->A direction
     * @param availBwMap - A map of bandwidth availability
     * @return True, if there is sufficient bandwidth across all edges. False, otherwise.
     */
    private boolean checkForSufficientBw(Map<String, UrnE> urnMap, Integer azMbps, Integer zaMbps, List<TopoEdge> azERO,
                                         List<TopoEdge> zaERO, Map<UrnE, Map<String, Integer>> availBwMap) {

        // For the AZ direction, fail the test if there is insufficient bandwidth
        if(!sufficientBandwidthForERO(azERO, urnMap, availBwMap, azMbps, zaMbps)){
            return false;
        }

        // For each ZA direction, fail the test if there is insufficient bandwidth
        return sufficientBandwidthForERO(zaERO, urnMap, availBwMap, azMbps, zaMbps);
    }

    /**
     * Examine all edges, confirm that the requested bandwidth can be supported given the available bandwidth.
     * @param urnMap - A map of URN string to URN objects
     * @param bwMbps - The requested bandwidth (in one direction)
     * @param availBwMap - A map of bandwidth availability
     * @return True, if there is sufficient bandwidth across all edges. False, otherwise.
     */
    private boolean checkForSufficientBwUni(Map<String, UrnE> urnMap, Integer bwMbps, List<TopoEdge> ERO,
                                            Map<UrnE, Map<String, Integer>> availBwMap){
        // For the AZ direction, fail the test if there is insufficient bandwidth
        return sufficientBandwidthForEROUni(ERO, urnMap, availBwMap, bwMbps);
    }

    /**
     * Given a particular list of edges, iterate and confirm that there is sufficient bandwidth available in both directions
     * @param ERO - A series of edges
     * @param urnMap - A mapping of URN strings to URN objects
     * @param availBwMap - A mapping of URN objects to lists of available bandwidth for that URN
     * @param azMbps - The bandwidth in the AZ direction
     * @param zaMbps - The bandwidth the ZA direction
     * @return True, if the segment can support the requested bandwidth. False, otherwise.
     */
    private boolean sufficientBandwidthForERO(List<TopoEdge> ERO, Map<String, UrnE> urnMap,
                                              Map<UrnE, Map<String, Integer>> availBwMap, Integer azMbps,
                                              Integer zaMbps){
        // For each edge in that list
        for(TopoEdge edge : ERO){
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
                if(!sufficientBandwidthAtUrn(urnA, availBwMap, azMbps, zaMbps)){
                    return false;
                }
            }

            // If URN Z has reservable bandwidth, confirm that there is enough available
            if(urnZ.getReservableBandwidth() != null){
                if(!sufficientBandwidthAtUrn(urnZ, availBwMap, azMbps, zaMbps)){
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Given a list of edges, confirm that there is sufficient bandwidth available in the one direction
     * @param ERO - A series of edges
     * @param urnMap - A mapping of URN strings to URN objects
     * @param availBwMap - A mapping of URN objects to lists of reserved bandwidth for that URN
     * @param bwMbps - The bandwidth in the specified direction
     * @return True, if the segment can support the requested bandwidth. False, otherwise.
     */
    private boolean sufficientBandwidthForEROUni(List<TopoEdge> ERO, Map<String, UrnE> urnMap,
                                                 Map<UrnE, Map<String, Integer>> availBwMap, Integer bwMbps)
    {
        // For each edge in that list
        for(TopoEdge edge : ERO)
        {
            Map<UrnE, Boolean> urnIngressDirectionMap = new HashMap<>();
            TopoVertex nodeA = edge.getA();
            TopoVertex nodeZ = edge.getZ();
            String urnStringA = nodeA.getUrn();
            String urnStringZ = nodeZ.getUrn();

            if(!urnMap.containsKey(urnStringA) || !urnMap.containsKey(urnStringZ))
            {
                return false;
            }

            UrnE urnA = urnMap.get(urnStringA);
            UrnE urnZ = urnMap.get(urnStringZ);

            // From Port to Device -- Consider Ingress direction for this Port
            if(nodeA.getVertexType().equals(VertexType.PORT) && !nodeZ.getVertexType().equals(VertexType.PORT))
            {
                urnIngressDirectionMap.put(urnA, true);
            }
            // From Device to Port -- Consider Egress direction for this Port
            else if(!nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT))
            {
                urnIngressDirectionMap.put(urnZ, false);
            }
            // From Port to Port -- Consider Egress for portA, and Ingress for portZ
            else if(nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT))
            {
                urnIngressDirectionMap.put(urnA, false);
                urnIngressDirectionMap.put(urnZ, true);
            }

            // If URN A has reservable bandwidth, confirm that there is enough available
            if(urnA.getReservableBandwidth() != null)
            {
                boolean ingressDirection = urnIngressDirectionMap.get(urnA);

                if(!sufficientBandwidthAtUrnUni(urnA, availBwMap, bwMbps, ingressDirection))
                {
                    return false;
                }
            }

            // If URN Z has reservable bandwidth, confirm that there is enough available
            if(urnZ.getReservableBandwidth() != null)
            {
                boolean ingressDirection = urnIngressDirectionMap.get(urnZ);

                if(!sufficientBandwidthAtUrnUni(urnZ, availBwMap, bwMbps, ingressDirection))
                {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Given a specific URN, determine if there is enough bandwidth available to support the requested bandwidth
     * @param urn - The URN
     * @param availBwMap - Map of URNs to available Bandwidth
     * @param inMbps - Requested ingress Mbps
     * @param egMbps - Requested egress Mbps
     * @return True, if there is enough available bandwidth at the URN. False, otherwise
     */
    private boolean sufficientBandwidthAtUrn(UrnE urn, Map<UrnE, Map<String, Integer>> availBwMap,
                                             Integer inMbps, Integer egMbps){
        Map<String, Integer> bwAvail = availBwMap.get(urn);
        if(bwAvail.get("Ingress") < inMbps || bwAvail.get("Egress") < egMbps){
            log.error("Insufficient Bandwidth at " + urn.toString() + ". Requested: " +
                    inMbps + " In and " + egMbps + " Out. Available: " + bwAvail.get("Ingress") +
                    " In and " + bwAvail.get("Egress") + " Out.");
            return false;
        }
        return true;
    }

    /**
     * Given a specific URN, determine if there is enough bandwidth available to support the requested bandwidth in a specific (ingress/egress) direction
     * @param urn - The URN
     * @param availBwMap - Map of URNs to Available Bandwidth
     * @param bwMbps - Requested Mbps
     * @param ingressDirection - True if pruning is done based upon port ingress b/w, false if done by egress b/w
     * @return True, if there is enough available bandwidth at the URN. False, otherwise
     */
    private boolean sufficientBandwidthAtUrnUni(UrnE urn, Map<UrnE, Map<String, Integer>> availBwMap,
                                                Integer bwMbps, boolean ingressDirection){
        Map<String, Integer> bwAvail = availBwMap.get(urn);

        Integer unidirectionalBW;
        String direction = "";

        if(ingressDirection)
        {
            unidirectionalBW = bwAvail.get("Ingress");
            direction = " In.";
        }
        else
        {
            unidirectionalBW = bwAvail.get("Egress");
            direction = " Out.";
        }

        if(unidirectionalBW < bwMbps)
        {
            log.error("Insufficient Bandwidth at " + urn.toString() + ". Requested: " + bwMbps + direction);
            return false;
        }

        return true;
    }

    /**
     * Retrieve a list of VLAN IDs that can be used given the AZ and ZA segments, the requested VLAN ranges, and
     * the VLAN IDs reserved so far.
     * @param urnMap - Mapping of URN string to URN object
     * @param reqPipe - The requested pipe
     * @param azERO - The AZ path.
     * @param zaERO - The ZA path.
     * @param rsvVlans - The reserved VLAN IDs
     * @return A set of all viable VLAN IDs to meet the demand (set may be empty)
     */
    private Set<Integer> selectVlanIds(Map<String, UrnE> urnMap, RequestedVlanPipeE reqPipe, List<TopoEdge> azERO,
                                       List<TopoEdge> zaERO, List<ReservedVlanE> rsvVlans) {


        Set<Integer> overlappingVlanIds = new HashSet<>();
        // Map of URN to associated list of reserved VLANs
        Map<UrnE, List<ReservedVlanE>> rsvVlanMap = pruningService.buildReservedVlanMap(rsvVlans);

        // The requested VLAN Expression
        String vlanExpression = reqPipe.getAJunction().getFixtures().iterator().next().getVlanExpression();
        // Convert that expression into a set of IDs
        Set<Integer> requestedVlanIds = pruningService
                .getIntegersFromRanges(pruningService.getIntRangesFromString(vlanExpression));

        log.info("Requested VLAN IDs: " + requestedVlanIds.toString());
        // Find all valid IDs for the AZ path
        // Find all valid IDs for the ETHERNET segment
        Set<Integer> azValidIds = getValidIdsForPath(azERO, requestedVlanIds, urnMap, rsvVlanMap);
        log.info("Valid AZ VLAN IDs: " + azValidIds.toString());
        // If that segment has no valid IDs return an empty set
        if(azValidIds.isEmpty()){
            return overlappingVlanIds;
        }


        // Find all valid IDs for the ZA segments
        Set<Integer> zaValidIds = getValidIdsForPath(zaERO, requestedVlanIds, urnMap, rsvVlanMap);
        log.info("Valid ZA VLAN IDs: " + azValidIds.toString());
        // If that segment has no valid IDs return an empty set
        if(zaValidIds.isEmpty()){
            return overlappingVlanIds;
        }

        //TODO: VLAN Translation
        //For now: Just use same VLAN ID on all segments
        // Find the intersection between the AZ and ZA valid VLAN IDs
        overlappingVlanIds = pruningService.addToOverlap(overlappingVlanIds, azValidIds);
        overlappingVlanIds = pruningService.addToOverlap(overlappingVlanIds, zaValidIds);
        return overlappingVlanIds;
    }

    /**
     * Find all valid VLAN iDs for a given path.
     * @param ERO - The given path
     * @param requestedVlanIds - The requested VLAN IDs
     * @param urnMap - Map of URN string to URN objects
     * @param rsvVlanMap - Map of URN objects to list of reserved VLAN IDs at that URN
     * @return The set of vlaid VLAN IDs for this path
     */
    private Set<Integer> getValidIdsForPath(List<TopoEdge> ERO, Set<Integer> requestedVlanIds,
                                               Map<String, UrnE> urnMap, Map<UrnE, List<ReservedVlanE>> rsvVlanMap){
        // Set for holding valid VLAN IDs
        Set<Integer> validIds = new HashSet<>();

        // Return a map of VLAN ID to sets of edges that support that ID
        Map<Integer, Set<TopoEdge>> edgesPerVlanId = pruningService.findEdgesPerVlanId(new HashSet<>(ERO), urnMap, rsvVlanMap);

        Set<TopoEdge> mplsEdges = edgesPerVlanId.get(-1);
        // For each VLAN ID
        for (Integer id : edgesPerVlanId.keySet()) {
            // Get the edges
            Set<TopoEdge> edgesForId = edgesPerVlanId.get(id);
            // Confirm that all edges in the segment support this id
            // If they do, add this ID as a valid ID
            if (edgesForId.size() + mplsEdges.size() == ERO.size()) {
                if (requestedVlanIds.contains(id) || requestedVlanIds.isEmpty()) {
                    validIds.add(id);
                }
            }
        }
        return validIds;
    }


    /**
     * Confirm that a requested VLAN junction supports the requested bandwidth. Checks each fixture of the junction.
     * @param req_j - The requested junction.
     * @param reservedBandwidths - The reserved bandwidth.
     * @return True, if there is enough bandwidth at every fixture. False, otherwise.
     */
    public boolean confirmSufficientBandwidth(RequestedVlanJunctionE req_j, List<ReservedBandwidthE> reservedBandwidths){

        // All requested fixtures on this junction
        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();


        // Get map of "Ingress" and "Egress" bandwidth availability
        Map<UrnE, Map<String, Integer>> availBwMap = createBandwidthAvailabilityMap(reservedBandwidths);

        // For each requested fixture,
        for(RequestedVlanFixtureE reqFix: reqFixtures){
            // Confirum that there is enough available bandwidth at that URN
            if(!sufficientBandwidthAtUrn(reqFix.getPortUrn(), availBwMap, reqFix.getInMbps(), reqFix.getEgMbps())){
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
            Set<Integer> availableVlans = getAvailableVlanIds(reqFix, rsvVlans);

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
        Set<Integer> reservedVlanIds = rsvVlansAtFixture.stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());

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
     * Retrieve all Reserved Bandwidth from a set of reserved MPLS pipes.
     * @param reservedPipes - Set of reserved pipes
     * @return A list of all reserved bandwidth within the set of reserved MPLS pipes.
     */
    public List<ReservedBandwidthE> retrieveReservedBandwidthsFromMplsPipes(Set<ReservedMplsPipeE> reservedPipes) {
        return reservedPipes
                .stream()
                .map(ReservedMplsPipeE::getReservedBandwidths)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all Reserved Bandwidth from a set of reserved Ethernet pipes.
     * @param reservedPipes - Set of reserved pipes
     * @return A list of all reserved bandwidth within the set of reserved Ethernet pipes.
     */
    public List<ReservedBandwidthE> retrieveReservedBandwidthsFromEthPipes(Set<ReservedEthPipeE> reservedPipes) {
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
    public List<ReservedVlanE> retrieveReservedVlansFromEthPipes(Set<ReservedEthPipeE> reservedPipes) {
        Set<ReservedVlanJunctionE> junctions = new HashSet<>();
        for(ReservedEthPipeE pipe : reservedPipes){
            junctions.add(pipe.getAJunction());
            junctions.add(pipe.getZJunction());
        }
        return retrieveReservedVlans(junctions);
    }
}
