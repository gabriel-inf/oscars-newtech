package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.pce.TopoAssistant;
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
     * @param edges
     * @return
     */
    public static List<Map<Layer, List<TopoVertex>>> decompose(List<TopoEdge> edges) {
        List<Map<Layer, List<TopoVertex>>> result = new ArrayList<>();


        log.info(edges.toString());

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
                if(nextPortToPortLayer.equals(Layer.MPLS)){
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
        log.info(result.toString());
        return result;

    }

    /**
     * Build a junction for each device in the input set of vertices. Each junction has two fixtures (made from the vertex
     * prior to the device, and the vertex after the device).
     * @param vertices - A list of vertices, comprised of ports and devices.
     * @param urnMap - A map of URN string to URN object
     * @param deviceModels - A map of URN string to device model
     * @param azMbps - Requested AZ bandwidth
     * @param zaMbps - Requested ZA bandwidth
     * @param vlanId - The requested VLAN ID
     * @param sched - The requested schedule (start/end date)
     * @param reqJunctionA - Requested junction A
     * @param reqJunctionZ - Requested junction Z
     * @return A list of reserved VLAN junctions, one per device.
     * @throws PSSException
     */
    public List<ReservedVlanJunctionE> createJunctions(List<TopoVertex> vertices, Map<String, UrnE> urnMap,
                                                        Map<String, DeviceModel> deviceModels, Integer azMbps, Integer zaMbps,
                                                        Integer vlanId, ScheduleSpecificationE sched,
                                                       RequestedVlanJunctionE reqJunctionA,
                                                       RequestedVlanJunctionE reqJunctionZ) throws PSSException{
        assert(vertices.size() % 3 == 0);


        List<ReservedVlanJunctionE> rsvJunctions = new ArrayList<>();
        // Maintain a sublist of vertices, that together can be made into junctions
        // Sublist is reset after a junction is made
        List<TopoVertex> junctionBuilder = new ArrayList<>();
        for(TopoVertex vertice : vertices) {
            junctionBuilder.add(vertice);
            // Build a junction
            if (junctionBuilder.size() == 3) {
                // Retrieve the two port vertices and the device vertex
                TopoVertex portOneVertex = junctionBuilder.get(0);
                TopoVertex deviceVertex = junctionBuilder.get(1);
                TopoVertex portTwoVertex = junctionBuilder.get(2);

                Set<TopoVertex> ports = new HashSet<>();
                ports.add(portOneVertex);
                ports.add(portTwoVertex);

                // Add in requested ports for this junction if they are not already included
                Set<TopoVertex> extraPortsA = getExtraRequestedPorts(reqJunctionA, deviceVertex, ports);
                Set<TopoVertex> extraPortsZ = getExtraRequestedPorts(reqJunctionZ, deviceVertex, ports);
                ports.addAll(extraPortsA);
                ports.addAll(extraPortsZ);

                // Build the junction
                ReservedVlanJunctionE rsvJunction = createJunctionAndFixtures(deviceVertex, ports, urnMap,
                        deviceModels, azMbps, zaMbps, vlanId, sched);

                // Add it to the set of reserved junctions
                rsvJunctions.add(rsvJunction);

                // Reset the current list of vertices, so the next junction can be collected together
                junctionBuilder = new ArrayList<>();
            }
        }
        return rsvJunctions;
    }


    public ReservedEthPipeE createPipe(List<TopoVertex> azVertices, List<TopoVertex> zaVertices,
                                        Map<String, DeviceModel> deviceModels, Map<String, UrnE> urnMap, Integer azMbps,
                                        Integer zaMbps, Integer vlanId, ScheduleSpecificationE sched,
                                       RequestedVlanJunctionE reqJunctionA, RequestedVlanJunctionE reqJunctionZ)
            throws PSSException{
        // Pull out the first and last element of each vertex list
        // This will get you the starting/ending port
        // Also retrieve the starting and ending device (but do not remove them from the list, they are included in
        // the AZ and ZA EROs).
        // Build a junction for the starting and ending device
        // Note: Junctions only need to be made for the AZ path, not both

        // Ingress into the pipe
        // Get and remove the ingress port
        TopoVertex ingressPort = azVertices.remove(0);
        // Get (and do not remove) the ingress device
        TopoVertex ingressDevice = azVertices.get(0);

        Set<TopoVertex> ingressPorts = new HashSet<>();
        ingressPorts.add(ingressPort);

        // Get extra ports that were requested for ingress junction (if applicable)
        Set<TopoVertex> extraIngressPortsA = getExtraRequestedPorts(reqJunctionA, ingressDevice, ingressPorts);
        Set<TopoVertex> extraIngressPortsZ = getExtraRequestedPorts(reqJunctionZ, ingressDevice, ingressPorts);
        ingressPorts.addAll(extraIngressPortsA);
        ingressPorts.addAll(extraIngressPortsZ);

        DeviceModel ingressModel = deviceModels.get(ingressDevice.getUrn());

        // Egress from the pipe
        // Get and remove the egress port
        TopoVertex egressPort = azVertices.remove(azVertices.size()-1);
        // Get (and do not remove) the egress device
        TopoVertex egressDevice = azVertices.get(azVertices.size()-1);

        Set<TopoVertex> egressPorts = new HashSet<>();
        egressPorts.add(egressPort);

        // Get extra ports that were requested for egress junction (if applicable)
        Set<TopoVertex> extraEgressPortsA = getExtraRequestedPorts(reqJunctionA, egressDevice, egressPorts);
        Set<TopoVertex> extraEgressPortsZ = getExtraRequestedPorts(reqJunctionZ, egressDevice, egressPorts);
        egressPorts.addAll(extraEgressPortsA);
        egressPorts.addAll(extraEgressPortsZ);

        DeviceModel egressModel = deviceModels.get(egressDevice.getUrn());

        // Should be at least two ports left in between, if not several ports/devices
        assert(azVertices.size() >= 2);

        // Just remove from ZA path, do not need to be saved
        // Remove ingress port
        zaVertices.remove(0);
        // Remove egress port
        zaVertices.remove(zaVertices.size()-1);

        assert(zaVertices.size() >= 2);

        ReservedVlanJunctionE ingressJunction = createJunctionAndFixtures(ingressDevice, ingressPorts,
                urnMap, deviceModels, azMbps, zaMbps, vlanId, sched);

        ReservedVlanJunctionE egressJunction = createJunctionAndFixtures(egressDevice, egressPorts,
                urnMap, deviceModels, azMbps, zaMbps, vlanId, sched);

        // Make the ERO for AZ
        List<String> azStrings = azVertices.stream().map(TopoVertex::getUrn).collect(Collectors.toList());
        // Make the ERO for ZA
        List<String> zaStrings = zaVertices.stream().map(TopoVertex::getUrn).collect(Collectors.toList());

        // Build a reserved bandwidth for each intermediate port
        Set<ReservedBandwidthE> rsvBws = createBandwidthForEros(azStrings, azMbps, zaMbps, sched, urnMap);

        return ReservedEthPipeE.builder()
                .aJunction(ingressJunction)
                .zJunction(egressJunction)
                .azERO(azStrings)
                .zaERO(zaStrings)
                .reservedBandwidths(rsvBws)
                .reservedPssResources(new HashSet<>())
                .pipeType(decidePipeType(ingressModel, egressModel))
                .build();
    }

    /**
     * Retrieve all requested fixtures at a junction where those ports are not already included in the input list
     * of ports
     * @param reqJunction - The requested junction, containing the requested fixtures
     * @param deviceVertex - The device corresponding to that junction
     * @param ports - The input list of ports (at that device)
     * @return All requested ports that are not already in the input list of ports.
     */
    public Set<TopoVertex> getExtraRequestedPorts(RequestedVlanJunctionE reqJunction, TopoVertex deviceVertex,
                                                   Set<TopoVertex> ports) {
        Set<TopoVertex> extraPorts = new HashSet<>();
        if(reqJunction.getDeviceUrn().getUrn().equals(deviceVertex.getUrn())){
            extraPorts = reqJunction.getFixtures()
                    .stream()
                    .map(fx -> fx.getPortUrn().getUrn())
                    .map(s -> TopoVertex.builder().urn(s).vertexType(VertexType.PORT).build())
                    .filter(v -> !ports.contains(v))
                    .collect(Collectors.toSet());
        }
        return extraPorts;
    }


    public ReservedVlanJunctionE createJunctionAndFixtures(TopoVertex device, Set<TopoVertex> ports,
                                                            Map<String, UrnE> urnMap, Map<String, DeviceModel> deviceModels,
                                                            Integer azMbps, Integer zaMbps, Integer vlanId,
                                                            ScheduleSpecificationE sched) throws PSSException {
        String deviceString = device.getUrn();
        assert(urnMap.containsKey(deviceString));
        UrnE deviceUrn = urnMap.get(deviceString);

        DeviceModel model = deviceModels.get(deviceString);

        Set<ReservedVlanFixtureE> fixtures = new HashSet<>();

        for(TopoVertex port : ports){
            String portString = port.getUrn();
            assert(urnMap.containsKey(portString));
            UrnE portUrn = urnMap.get(portString);
            ReservedVlanFixtureE fix = createFixtureAndResources(portUrn, model, azMbps, zaMbps, vlanId, sched);
            fixtures.add(fix);
        }

        return ReservedVlanJunctionE.builder()
                .deviceUrn(deviceUrn)
                .fixtures(fixtures)
                .reservedPssResources(new HashSet<>())
                .junctionType(decideJunctionType(model))
                .build();
    }

    public ReservedVlanFixtureE createFixtureAndResources(UrnE portUrn, DeviceModel model, Integer azMbps,
                                                           Integer zaMbps, Integer vlanId,
                                                           ScheduleSpecificationE sched) throws PSSException{
        EthFixtureType fixtureType = decideFixtureType(model);

        // Create reserved resources for Fixture
        ReservedBandwidthE rsvBw = createReservedBandwidth(portUrn, azMbps, zaMbps, sched);
        ReservedVlanE rsvVlan = createReservedVlan(portUrn, vlanId, sched);
        // Create Fixture
        return createReservedFixture(portUrn, new HashSet<>(), rsvVlan, rsvBw, fixtureType);
    }

    public ReservedVlanJunctionE createReservedJunction(UrnE urn, Set<ReservedPssResourceE> pssResources,
                                                         Set<ReservedVlanFixtureE> fixtures, EthJunctionType junctionType){
        return ReservedVlanJunctionE.builder()
                .deviceUrn(urn)
                .reservedPssResources(pssResources)
                .fixtures(fixtures)
                .junctionType(junctionType)
                .build();
    }

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


    public ReservedBandwidthE createReservedBandwidth(UrnE urn, Integer azMbps, Integer zaMbps, ScheduleSpecificationE sched){
        return ReservedBandwidthE.builder()
                .urn(urn)
                .egBandwidth(azMbps)
                .inBandwidth(zaMbps)
                .beginning(sched.getNotBefore().toInstant())
                .ending(sched.getNotAfter().toInstant())
                .build();
    }

    public ReservedVlanE createReservedVlan(UrnE urn, Integer vlanId, ScheduleSpecificationE sched){
        return ReservedVlanE.builder()
                .urn(urn)
                .vlan(vlanId)
                .beginning(sched.getNotBefore().toInstant())
                .ending(sched.getNotAfter().toInstant())
                .build();
    }

    public Set<ReservedBandwidthE> createBandwidthForEros(List<String> ero, Integer azMbps, Integer zaMbps,
                                                           ScheduleSpecificationE sched, Map<String, UrnE> urnMap) {
        return ero
                .stream()
                .filter(urnMap::containsKey)
                .map(urnMap::get)
                .filter(urn -> urn.getUrnType().equals(UrnType.IFCE))
                .map(urn -> createReservedBandwidth(urn, azMbps, zaMbps, sched))
                .collect(Collectors.toSet());
    }

    // TODO: fix this
    public Map<String, ResourceType> neededPipeResources(ReservedEthPipeE vp) throws PSSException {
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

    public EthPipeType decidePipeType(DeviceModel aModel, DeviceModel zModel) throws PSSException {

        switch (aModel) {
            case ALCATEL_SR7750:
                switch (zModel) {
                    case ALCATEL_SR7750:
                        return EthPipeType.ALU_TO_ALU_VPLS;
                    case JUNIPER_MX:
                        return EthPipeType.ALU_TO_JUNOS_VPLS;
                }

                break;
            case JUNIPER_MX:
                switch (zModel) {
                    case ALCATEL_SR7750:
                        return EthPipeType.ALU_TO_JUNOS_VPLS;
                    case JUNIPER_MX:
                        return EthPipeType.JUNOS_TO_JUNOS_VPLS;
                }

        }
        throw new PSSException("Could not determine pipe type");
    }

}
