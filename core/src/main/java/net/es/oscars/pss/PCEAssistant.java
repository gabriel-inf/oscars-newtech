package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.pss.MplsPipeType;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.rsrc.ReservableBandwidth;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.enums.VertexType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PCEAssistant {


    /**
     * Given a list of edges, convert that list to into a number of segments, based on layer.
     * An ETHERNET segment is made entirely of switches and their ports, while a MPLS segment consists
     * of routers and their ports.
     * @param edges - The edges to be decomposed into segments.
     * @return A list of <Layer, List<TopoVertex>> pairs (segments).
     */
    public static List<Map<Layer, List<TopoVertex>>> decompose(List<TopoEdge> edges) {
        List<Map<Layer, List<TopoVertex>>> result = new ArrayList<>();

        // We have List of edges like this
        // Port -INTERNAL- Device -INTERNAL- Port -ETHERNET/MPLS- Port -INTERNAL- Device -INTERNAL- Port, etc
        // We want to make several lists of vertices
        assert(edges.size() > 2);

        // Find the first applicable non-INTERNAL edge on the path, and set it as the current layer
        Layer currentLayer = edges.get(2).getLayer();

        // Initialize the containers for holding vertices per segment
        List<TopoVertex> segmentVertices = new ArrayList<>();
        HashMap<Layer, List<TopoVertex>> segment = new HashMap<>();
        segment.put(currentLayer, segmentVertices);
        result.add(segment);

        // Loop through each edge, add the nodes connected to that edge to different segments based
        // on certain conditions
        for(int i = 0; i < edges.size(); i++){
            // Retrieve the current edge and nodes
            TopoEdge currentEdge = edges.get(i);
            TopoVertex nodeA = currentEdge.getA();
            TopoVertex nodeZ = currentEdge.getZ();
            // If this is the first edge, add the node on the "A" side of the edge
            if(i==0){
                segmentVertices.add(nodeA);
            }
            // If we've been in a MPLS segment, check if the currently considered edge is ETHERNET
            // If so, switch the layer to ETHERNET and start a new segment
            if(currentLayer.equals(Layer.MPLS)){
                Layer currentEdgeLayer = edges.get(i).getLayer();
                if(currentEdgeLayer.equals(Layer.ETHERNET)){
                    currentLayer = currentEdgeLayer;
                    segment = new HashMap<>();
                    segmentVertices = new ArrayList<>();
                    segment.put(currentLayer, segmentVertices);
                    result.add(segment);
                }
            }
            // If we've been in an ETHERNET segment, check if next non-internal edge is MPLS
            // If so, switch the layer to MPLS and start a new segment
            else if(currentLayer.equals(Layer.ETHERNET) && i % 3 == 2 && i + 3 != edges.size()){
                Layer nextPortToPortLayer = edges.get(i+3).getLayer();
                if(currentLayer != nextPortToPortLayer){
                    currentLayer = nextPortToPortLayer;
                    segment = new HashMap<>();
                    segmentVertices = new ArrayList<>();
                    segment.put(currentLayer, segmentVertices);
                    result.add(segment);
                }
            }
            // Add the node on the "Z" end of the current edge
            segmentVertices.add(nodeZ);

        }
        return result;
    }


    /**
     * Create a Junction and it's associated fixtures given the input parameters.
     * @param device - The device vertex associated with the junction
     * @param urnMap - A mapping of URN strings to URN objects
     * @param deviceModels - A mapping of URN strings to Device Models
     * @param requestedJunctions - A set of requested junctions - used to determine attributes of junction/fixtures
     * @param vlanId - The assigned VLAN ID for the fixtures
     * @param sched - The requested schedule
     * @return A Reserved Vlan Junction with Reserved Fixtures (if contained in a matching requested junction)
     * @throws PSSException
     */
    public ReservedVlanJunctionE createJunctionAndFixtures(TopoVertex device, Map<String, UrnE> urnMap,
                                                            Map<String, DeviceModel> deviceModels,
                                                           Set<RequestedVlanJunctionE> requestedJunctions,
                                                           Integer vlanId, ScheduleSpecificationE sched) throws PSSException {
        // Get this junction's URN, Device Model, and Typing for its Fixtures
        UrnE aUrn = urnMap.get(device.getUrn());
        DeviceModel model = deviceModels.get(aUrn.getUrn());
        EthFixtureType fixType = decideFixtureType(model);

        // Create the set of Reserved Fixtures by filtering for requested junctions that match the URN
        Set<ReservedVlanFixtureE> reservedVlanFixtures = requestedJunctions
                .stream()
                .filter(reqJunction -> reqJunction.getDeviceUrn().equals(aUrn))
                .map(RequestedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(reqFix -> createFixtureAndResources(reqFix.getPortUrn(), fixType,
                        reqFix.getInMbps(), reqFix.getEgMbps(), vlanId, sched))
                .collect(Collectors.toSet());

        // Return the Reserved Junction (with the set of reserved VLAN fixtures).
        return createReservedJunction(aUrn, new HashSet<>(),
                reservedVlanFixtures, decideJunctionType(model));
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

    // TODO: fix this
    /**
     * Determine what resources are needed for a reserved MPLS pipe
     * @param vp - The reserved pipe
     * @return A mapping of the needed resources
     * @throws PSSException
     */
    public Map<String, ResourceType> neededPipeResources(ReservedMplsPipeE vp) throws PSSException {
        Map<String, ResourceType> result = new HashMap<>();
        switch (vp.getPipeType()) {
            case ALU_TO_ALU_VPLS:
                return result;
            case ALU_TO_JUNOS_VPLS:
                return result;
            case JUNOS_TO_JUNOS_VPLS:
                return result;
            case REQUESTED:
                throw new PSSException("Invalid pipe type (Reserved)!");
        }
        throw new PSSException("Could not reserve pipe resources");

    }

    /**
     * Determine what resources are needed for provisioning this reserved junction.
     * @param vj - The reserved junction
     * @return A mapping of the needed resources
     * @throws PSSException
     */
    public Map<ResourceType, List<String>> neededJunctionResources(ReservedVlanJunctionE vj) throws PSSException {
        Map<ResourceType, List<String>> result = new HashMap<>();

        List<String> deviceScope = new ArrayList<>();
        deviceScope.add(vj.getDeviceUrn().getUrn());
        List<String> global = new ArrayList<>();
        global.add(ResourceType.GLOBAL);

        List<String> ports = new ArrayList<>();
        vj.getFixtures().stream().forEach(t -> {
            ports.add(t.getIfceUrn().getUrn());
        });


        switch (vj.getJunctionType()) {
            case ALU_VPLS:

                result.put(ResourceType.ALU_INGRESS_POLICY_ID, deviceScope);
                result.put(ResourceType.ALU_EGRESS_POLICY_ID, deviceScope);
                result.put(ResourceType.VC_ID, global);

                return result;
            case JUNOS_SWITCH:

                return result;
            case JUNOS_VPLS:

                result.put(ResourceType.VC_ID, global);
                return result;
        }
        throw new PSSException("Could not decide needed junction resources");
    }

    /**
     * Given a junction's device model, determine the junction's type
     * @param model - The  device model
     * @return The junction's type
     * @throws PSSException
     */
    public EthJunctionType decideJunctionType(DeviceModel model) throws PSSException {
        switch (model) {
            case ALCATEL_SR7750:
                return EthJunctionType.ALU_VPLS;
            case JUNIPER_EX:
                return EthJunctionType.JUNOS_SWITCH;
            case JUNIPER_MX:
                return EthJunctionType.JUNOS_VPLS;
        }
        throw new PSSException("Could not determine junction type for " + model);

    }

    /**
     * Given the device model of the associated device, determine the fixture's type
     * @param model - The device's model
     * @return The fixture's type
     * @throws PSSException
     */
    public EthFixtureType decideFixtureType(DeviceModel model) throws PSSException {
        switch (model) {
            case ALCATEL_SR7750:
                return EthFixtureType.ALU_SAP;
            case JUNIPER_EX:
                return EthFixtureType.JUNOS_IFCE;
            case JUNIPER_MX:
                return EthFixtureType.JUNOS_IFCE;
        }
        throw new PSSException("Could not determine fixture type for " + model);

    }

    /**
     * Given the models of the starting/ending devices of a MPLS pipe, determine the pipe's type
     * @param aModel - The A junction's device model
     * @param zModel - The Z junction's device model
     * @return The MPLS pipe's type
     * @throws PSSException
     */
    public MplsPipeType decideMplsPipeType(DeviceModel aModel, DeviceModel zModel) throws PSSException {

        switch (aModel) {
            case ALCATEL_SR7750:
                switch (zModel) {
                    case ALCATEL_SR7750:
                        return MplsPipeType.ALU_TO_ALU_VPLS;
                    case JUNIPER_MX:
                        return MplsPipeType.ALU_TO_JUNOS_VPLS;
                }

                break;
            case JUNIPER_MX:
                switch (zModel) {
                    case ALCATEL_SR7750:
                        return MplsPipeType.ALU_TO_JUNOS_VPLS;
                    case JUNIPER_MX:
                        return MplsPipeType.JUNOS_TO_JUNOS_VPLS;
                }

        }
        throw new PSSException("Could not determine MPLS pipe type");
    }


    /**
     * Given the models of the starting/ending devices of an Ethernet pipe, determine the pipe's type
     * @param aModel - The A junction's model
     * @param zModel - The Z junction's model
     * @return The ethernet pipe's type
     * @throws PSSException
     */
    public EthPipeType decideEthPipeType(DeviceModel aModel, DeviceModel zModel) throws PSSException {

        switch (aModel) {
            case ALCATEL_SR7750:
                switch (zModel) {
                    case JUNIPER_EX:
                        return EthPipeType.ALU_VPLS_TO_JUNOS_SWITCH;
                }
            case JUNIPER_MX:
                switch (zModel) {
                    case JUNIPER_EX:
                        return EthPipeType.JUNOS_VPLS_TO_JUNOS_SWITCH;
                }
            case JUNIPER_EX:
                switch (zModel) {
                    case JUNIPER_EX:
                        return EthPipeType.JUNOS_SWITCH_TO_JUNOS_SWITCH;
                    case JUNIPER_MX:
                        return EthPipeType.JUNOS_SWITCH_TO_JUNOS_VPLS;
                    case ALCATEL_SR7750:
                        return EthPipeType.JUNOS_SWITCH_TO_ALU_VPLS;
                }
        }
        throw new PSSException("Could not determine Ethernet pipe type");
    }

    /**
     * Construct a mapping of Junction Pairs (two vertices) to the AZ/ZA listing of vertices (the pipe) between those
     * two junctions. Updates the pased in junctionPairToPipeEROMap and the allJunctionPairs map to keep track
     * of which junction pairs have been created.
     * @param junctionPairToPipeEROMap - A mapping between junction pairs and pipe vertices.
     * @param allJunctionPairs - A mapping between Junction pairs and layer (determines what kind of pipe to create)
     * @param azSegments - The path segments in the AZ direction
     * @param zaSegments - The path segments in the ZA direction
     */
    public void constructJunctionPairToPipeEROMap(Map<List<TopoVertex>, Map<String, List<TopoVertex>>> junctionPairToPipeEROMap,
                                                  Map<List<TopoVertex>, Layer> allJunctionPairs,
                                                  List<Map<Layer, List<TopoVertex>>> azSegments,
                                                  List<Map<Layer, List<TopoVertex>>> zaSegments) {

        // Containers for the junction pairs and pipes
        List<List<TopoVertex>> junctionPairs = new ArrayList<>();
        junctionPairs.add(new ArrayList<>());
        List<List<TopoVertex>> azEros = new ArrayList<>();
        azEros.add(new ArrayList<>());
        List<List<TopoVertex>> zaEros = new ArrayList<>();
        zaEros.add(new ArrayList<>());

        // Containers for the junction pairs that connect two segments (ETHERNET -> MPLS, or MPLS -> ETHERNET)
        List<List<TopoVertex>> interJunctionPairs = new ArrayList<>();
        interJunctionPairs.add(new ArrayList<>());
        List<List<TopoVertex>> interAzEROs = new ArrayList<>();
        interAzEROs.add(new ArrayList<>());
        List<List<TopoVertex>> interZaEROs = new ArrayList<>();
        interZaEROs.add(new ArrayList<>());

        // The indices of the current junction pair / inter-segment junction pair
        Integer currJunctionPairIndex = 0;
        Integer currInterJunctionPairIndex = 0;


        for (int i = 0; i < azSegments.size(); i++) {
            // Get az segment and za segment
            Map<Layer, List<TopoVertex>> azSegment = azSegments.get(i);
            Map<Layer, List<TopoVertex>> zaSegment = zaSegments.get(zaSegments.size() - i - 1);
            assert (azSegment.keySet().equals(zaSegment.keySet()));

            // Determine the segment's type
            Layer layer = azSegment.containsKey(Layer.ETHERNET) ? Layer.ETHERNET : Layer.MPLS;

            // Get the vertices from the segments
            List<TopoVertex> azVertices = azSegment.get(layer);
            List<TopoVertex> zaVertices = zaSegment.get(layer);


            // Retrieve and remove the AZ ingress and egress ports
            TopoVertex azIngress = azVertices.remove(0);
            TopoVertex azEgress = azVertices.remove(azVertices.size() - 1);

            // Remove the ZA ingress and egress ports
            zaVertices.remove(0);
            zaVertices.remove(zaVertices.size() - 1);


            // Store the first device and last device in the segment
            TopoVertex currentVertex = azVertices.get(0);
            TopoVertex lastDevice = azVertices.get(azVertices.size()-1);

            // Add the current vertex to the current intersegment junction pair
            // The intersegment pipe can now be completed by adding the ingress point
            // To the intersegment pipe
            if (interJunctionPairs.get(currInterJunctionPairIndex).size() == 1) {
                // Add the starting device to the junction pair
                interJunctionPairs.get(currInterJunctionPairIndex).add(currentVertex);

                // Add to the list of all junction pairs
                allJunctionPairs.put(interJunctionPairs.get(currInterJunctionPairIndex), Layer.ETHERNET);


                // Add the ingress point to the intersegment AZ ERO
                interAzEROs.get(currInterJunctionPairIndex).add(azIngress);
                // Add the ingress port to the front of the intersegment ZA ERO
                // This should reverse the order
                interZaEROs.get(currInterJunctionPairIndex).add(0, azIngress);


                // Reset the collections of intersection junction pairs and pipes
                interJunctionPairs.add(new ArrayList<>());
                interAzEROs.add(new ArrayList<>());
                interZaEROs.add(new ArrayList<>());
                currInterJunctionPairIndex += 1;
            }

            // If we're in a MPLS segment:
            // Create a junction pair from first and last device
            // Create the AZ pipe from all vertices in between
            // Create the ZA pipe from all vertices in between
            if(azSegment.containsKey(Layer.MPLS)) {

                // Remove the first and last device from the ZA direction
                zaVertices.remove(0);
                zaVertices.remove(zaVertices.size()-1);

                // Retrieve and remove the first and last device from the AZ direction
                TopoVertex firstDevice = azVertices.remove(0);
                lastDevice = azVertices.remove(azVertices.size()-1);

                junctionPairs.get(currJunctionPairIndex).add(firstDevice);
                junctionPairs.get(currJunctionPairIndex).add(lastDevice);


                // Add to the list of all junction pairs
                allJunctionPairs.put(junctionPairs.get(currJunctionPairIndex), layer);

                // Store the ports/device in between the current junction pair
                azEros.get(currJunctionPairIndex).addAll(azVertices);
                zaEros.get(currJunctionPairIndex).addAll(zaVertices);

                // Reset the current junction pair and pipe ERO lists
                junctionPairs.add(new ArrayList<>());
                azEros.add(new ArrayList<>());
                zaEros.add(new ArrayList<>());
                currJunctionPairIndex += 1;
            }
            else {
                for (Integer v = 0; v < azVertices.size(); v++) {
                    currentVertex = azVertices.get(v);
                    TopoVertex zaVertex = zaVertices.get(zaVertices.size() - 1 - v);

                    if (!currentVertex.getVertexType().equals(VertexType.PORT)) {
                        junctionPairs.get(currJunctionPairIndex).add(currentVertex);

                        if (junctionPairs.get(currJunctionPairIndex).size() == 2) {
                            // Add to the list of all junction pairs
                            allJunctionPairs.put(junctionPairs.get(currJunctionPairIndex), layer);

                            // Reset the current junction pair and pipe ERO lists
                            junctionPairs.add(new ArrayList<>());
                            azEros.add(new ArrayList<>());
                            zaEros.add(new ArrayList<>());
                            currJunctionPairIndex += 1;

                            // If this is not the last vertex, or there is not another segment
                            // Add to the current junction pair
                            if (v != azVertices.size() - 1)
                                junctionPairs.get(currJunctionPairIndex).add(currentVertex);
                        }
                    }
                    // This is a port
                    else {
                        azEros.get(currJunctionPairIndex).add(currentVertex);
                        zaEros.get(currJunctionPairIndex).add(0, zaVertex);
                    }
                }
            }

            // Clear out any leftover vertices put into the current vertex containers
            if(junctionPairs.get(currJunctionPairIndex).size() == 1){
                junctionPairs.set(currJunctionPairIndex, new ArrayList<>());
                azEros.set(currJunctionPairIndex, new ArrayList<>());
                zaEros.set(currJunctionPairIndex, new ArrayList<>());
            }
            // If this is not the last segment
            // Start a new intersegment junction pair, and add the last device in the segment
            // Add the egress point of this segment to a new intersegment pipe ERO
            if (i < azSegments.size() - 1) {
                interJunctionPairs.get(currInterJunctionPairIndex).add(lastDevice);
                interAzEROs.get(currInterJunctionPairIndex).add(azEgress);
                interZaEROs.get(currInterJunctionPairIndex).add(azEgress);
            }
        }

        // Store the pipes/junction pairs
        for(Integer jp = 0; jp < junctionPairs.size(); jp++){
            List<TopoVertex> junctionPair = junctionPairs.get(jp);
            if(junctionPair.size() == 2) {
                List<TopoVertex> azERO = azEros.get(jp);
                List<TopoVertex> zaEro = zaEros.get(jp);
                junctionPairToPipeEROMap.put(junctionPair, makeDirectionalEROMap(azERO, zaEro));
            }
        }

        // Store the inter-segment pipes/junction pairs
        for(Integer jp = 0; jp < interJunctionPairs.size(); jp++){
            List<TopoVertex> junctionPair = interJunctionPairs.get(jp);
            if(junctionPair.size() == 2) {
                List<TopoVertex> azERO = interAzEROs.get(jp);
                List<TopoVertex> zaEro = interZaEROs.get(jp);
                junctionPairToPipeEROMap.put(junctionPair, makeDirectionalEROMap(azERO, zaEro));
            }
        }

    }

    /**
     * Make a mapping to store the AZ and ZA Explicit Route Objects (ERO).
     * @param azERO - The AZ path
     * @param zaERO - The ZA path
     * @return A mapping between "AZ" and "ZA" to the associated path
     */
    private Map<String, List<TopoVertex>> makeDirectionalEROMap(List<TopoVertex> azERO, List<TopoVertex> zaERO) {
        Map<String, List<TopoVertex>> directionalEROMap = new HashMap<>();
        directionalEROMap.put("AZ", azERO);
        directionalEROMap.put("ZA", zaERO);
        return directionalEROMap;
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
}
