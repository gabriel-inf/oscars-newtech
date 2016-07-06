package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.pce.TopoAssistant;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PCEAssistant {


    public static List<Map<Layer, List<TopoEdge>>> decompose(List<TopoEdge> edges, Map<String, DeviceModel> deviceModels) {
        List<Map<Layer, List<TopoEdge>>> result = new ArrayList<>();

        /*
        let's remember: pipes go from device URN to device URN
        the shortest possible pipe we could possibly be decomposing looks like this:

        alpha -- INTERNAL -- alpha:1/1/1
        alpha:1/1/1 -- MPLS or ETHERNET -- beta:1/1/1
        beta:1/1/1 -- INTERNAL -- beta

        in general, this would continue on as follows:

        beta            -- INTERNAL -- beta:2/1/1
        beta:2/1/1      -- ETHERNET / MPLS-- charlie:1/1/1
        charlie:1/1/1   -- INTERNAL -- charlie
        charlie         -- INTERNAL -- charlie:2/1/1
        charlie:2/1/1   -- ETHERNET / MPLS-- delta:1/1/1
        delta:1/1/1     -- INTERNAL -- delta

        and so on and so forth. very first one and very last one is always INTERNAL, from and to devices specifically

        the size of the path is always a multiple of 3

        offset mod 3 == 0 : an INTERNAL device-to-port
        offset mod 3 == 1 : an ETHERNET / MPLS port-to-port
        offset mod 3 == 2 : an INTERNAL port-to-device

       */

        log.info(edges.toString());

        assert edges.size() >= 3;

        assert Math.floorMod(edges.size(), 3) == 0;


        for (int i = 0; i < edges.size(); i++) {
            log.info(edges.get(i).toString());
            Integer mod = Math.floorMod(i, 3);
            if (mod == 0) {
                // this is device - to - port
                assert edges.get(i).getLayer().equals(Layer.INTERNAL);
                assert deviceModels.containsKey(edges.get(i).getA().getUrn());

            } else if (mod == 1) {
                // this is port - to - port
                assert !edges.get(i).getLayer().equals(Layer.INTERNAL);
                assert !deviceModels.containsKey(edges.get(i).getA().getUrn());
                assert !deviceModels.containsKey(edges.get(i).getZ().getUrn());
            } else if (mod == 2) {
                // this is port - to - device
                assert edges.get(i).getLayer().equals(Layer.INTERNAL);
                assert deviceModels.containsKey(edges.get(i).getZ().getUrn());
            }


        }

        // this is the very first meaningful layer (edge offset 0 is INTERNAL)
        Layer currentLayer = edges.get(1).getLayer();

        Map<Layer, List<TopoEdge>> segment = new HashMap<>();
        List<TopoEdge> segmentEdges = new ArrayList<>();

        segment.put(currentLayer, segmentEdges);
        result.add(segment);


        for (int i = 0; i < edges.size(); i++) {
            Integer mod = Math.floorMod(i, 3);
            // we are at the port-to-device connection (the third one, so offset 2)
            // this needs belong to a new segment IFF the layer of the NEXT port-to-port connection is different
            if (mod == 2) {
                // at the very last one, there's no next port-to-port, so skip that
                if (i + 1 != edges.size()) {
                    TopoEdge nextPortToPort = edges.get(i + 2);
                    if (!nextPortToPort.getLayer().equals(currentLayer)) {
                        log.info("switching layers to " + nextPortToPort.getLayer());
                        currentLayer = nextPortToPort.getLayer();

                        segment = new HashMap<>();
                        segmentEdges = new ArrayList<>();
                        segment.put(currentLayer, segmentEdges);
                        result.add(segment);
                    }

                }
            }
            segmentEdges.add(edges.get(i));

        }

        return result;

    }

    public List<ReservedVlanJunctionE> makeEthernetJunctions(List<TopoEdge> edges,
                                                              Integer azMbps, Integer zaMbps, Integer vlanId,
                                                              Optional<ReservedVlanJunctionE> mergeA,
                                                              Optional<ReservedVlanJunctionE> mergeZ,
                                                              ScheduleSpecificationE sched,
                                                              Map<String, UrnE> urnMap,
                                                              Map<String, DeviceModel> deviceModels)
            throws PSSException {
        List<ReservedVlanJunctionE> result = new ArrayList<>();
        if (mergeA.isPresent()) {
            // we will find the device URN at the A of the first edge
            String aaUrn = edges.get(0).getA().getUrn();
            UrnE deviceUrn = mergeA.get().getDeviceUrn();
            assert aaUrn.equals(deviceUrn.getUrn());
        }
        if (mergeZ.isPresent()) {
            // we will find the device URN at Z of the last edge
            String zzUrn = edges.get(edges.size() - 1).getZ().getUrn();
            UrnE deviceUrn = mergeZ.get().getDeviceUrn();
            assert zzUrn.equals(deviceUrn.getUrn());
        }

        /*
        a few different cases to handle:

        if we need to mergeA for the first device at the first edge we will only get:
        edge.a -> device
        edge.a -> port

        if we need to mergeZ for the second device for the second edge we will only get:
        edge.a -> port
        edge.a -> device

        otherwise, per device, there will be TWO edges to look at:
        edge1.a -> port
        edge1.a -> device

        edge2.a -> device
        edge2.a -> port

        */


        Integer startAt = 0;
        Integer limit = edges.size();
        if (mergeA.isPresent()) {
            startAt = 1;

            ReservedVlanJunctionE mergeThis = mergeA.get();
            DeviceModel model = deviceModels.get(mergeThis.getDeviceUrn().getUrn());
            EthFixtureType fixtureType = decideFixtureType(model);

            String zUrn = edges.get(0).getZ().getUrn();
            log.info("making fixture for z urn: "+zUrn);

            UrnE urn = urnMap.get(zUrn);

            assert urn != null;

            // Create new Reserved Bandwidth
            ReservedBandwidthE rsvBw = createReservedBandwidth(urn, azMbps, zaMbps, sched);

            ReservedVlanE rsvVlan = createReservedVlan(urn, vlanId, sched);

            ReservedVlanFixtureE fx = createReservedFixture(urn, new HashSet<>(), rsvVlan, rsvBw, fixtureType);

            ReservedVlanJunctionE copy = ReservedVlanJunctionE.copyFrom(mergeThis);
            copy.setJunctionType(decideJunctionType(model));
            copy.getFixtures().add(fx);
            for (ReservedVlanFixtureE f : copy.getFixtures()) {
                f.setFixtureType(fixtureType);
            }


            result.add(copy);
        }
        if (mergeZ.isPresent()) {
            limit = limit - 1;
        }

        // now we work with pairs as described above

        for (int i = startAt; i + 2 <= limit; i += 2) {
            TopoEdge edgeOne = edges.get(i);
            TopoEdge edgeTwo = edges.get(i + 1);

            String urnAPortString = edgeOne.getA().getUrn();
            String urnDeviceString = edgeOne.getZ().getUrn();
            String urnZPortString = edgeTwo.getZ().getUrn();
            if(!urnMap.containsKey(urnAPortString) || !urnMap.containsKey(urnZPortString) || !urnMap.containsKey(urnDeviceString)){
                    throw new PSSException("URNs " + urnAPortString + " and/or " + urnZPortString + " Not found in URN map");
            }
            UrnE urnPortA = urnMap.get(urnAPortString);
            UrnE urnDevice = urnMap.get(urnDeviceString);
            UrnE urnPortZ = urnMap.get(urnZPortString);


            DeviceModel model = deviceModels.get(edgeOne.getZ().getUrn());
            EthFixtureType fixtureType = decideFixtureType(model);

            // Create new Reserved Bandwidth for URN A
            ReservedBandwidthE rsvBwA = createReservedBandwidth(urnPortA, azMbps, zaMbps, sched);

            // Create new Reserved Bandwidth for URN Z
            ReservedBandwidthE rsvBwZ = createReservedBandwidth(urnPortZ, azMbps, zaMbps, sched);

            // Create new Reserved VLAN for Urn A
            ReservedVlanE rsvVlanA = createReservedVlan(urnPortA, vlanId, sched);

            // Create new Reserved VLAN for Urn Z
            ReservedVlanE rsvVlanZ = createReservedVlan(urnPortZ, vlanId, sched);

            // Create junction for device
            ReservedVlanJunctionE newJunction = createReservedJunction(urnDevice, new HashSet<>(), new HashSet<>(),
                    decideJunctionType(model));

            // For URN Port A
            ReservedVlanFixtureE fOne = createReservedFixture(urnPortA, new HashSet<>(), rsvVlanA, rsvBwA, fixtureType);


            // For URN Port Z
            ReservedVlanFixtureE fTwo = createReservedFixture(urnPortZ, new HashSet<>(), rsvVlanZ, rsvBwZ, fixtureType);

            newJunction.getFixtures().add(fOne);
            newJunction.getFixtures().add(fTwo);


            result.add(newJunction);

        }

        if (mergeZ.isPresent()) {
            ReservedVlanJunctionE mergeThis = mergeZ.get();
            DeviceModel model = deviceModels.get(mergeThis.getDeviceUrn().getUrn());
            EthFixtureType fixtureType = decideFixtureType(model);

            String urnZString = edges.get(edges.size()-1).getZ().getUrn();
            if(!urnMap.containsKey(urnZString)){
                throw new PSSException(("URN Map does not contain: " + urnZString));
            }
            UrnE urnZ = urnMap.get(urnZString);
            // Create new Reserved Bandwidth for URN Z
            ReservedBandwidthE rsvBw = createReservedBandwidth(urnZ, azMbps, zaMbps, sched);

            // Create new Reserved VLAN for Urn Z
            ReservedVlanE rsvVlan = createReservedVlan(urnZ, vlanId, sched);

            // Create fixture for UrnZ
            ReservedVlanFixtureE fx = createReservedFixture(urnZ, new HashSet<>(), rsvVlan, rsvBw, fixtureType);

            ReservedVlanJunctionE copy = ReservedVlanJunctionE.copyFrom(mergeThis);
            copy.setJunctionType(decideJunctionType(model));
            copy.getFixtures().add(fx);
            for (ReservedVlanFixtureE f : copy.getFixtures()) {
                f.setFixtureType(fixtureType);
            }
            result.add(copy);
        }
        return result;

    }

    public ReservedEthPipeE makeVplsPipe(List<TopoEdge> azEdges, List<TopoEdge> zaEdges,
                                         Integer azMbps, Integer zaMbps,
                                         Integer vlanId,
                                         Optional<ReservedVlanJunctionE> mergeA,
                                         Optional<ReservedVlanJunctionE> mergeZ,
                                         Map<String, UrnE> urnMap,
                                         Map<String, DeviceModel> deviceModels,
                                         ScheduleSpecificationE sched) throws PSSException {
        /*
        a few different cases to handle:
        if we need to merge either A or Z junction from the originally Reserved pipe, we just copy the junction over

        if at either end of the pipe we are not provided a junction to merge, we will be provided the port URN
        if it's at A, it will be the A of the first edge
        if it's at Z, it will be the Z of the last edge

        in either case, we will need to create a new junction for our pipe and add that single port as a fixture

        */
        ReservedVlanJunctionE aJunction;
        ReservedVlanJunctionE zJunction;

        if (mergeA.isPresent()) {
            DeviceModel model = deviceModels.get(mergeA.get().getDeviceUrn().getUrn());
            aJunction = mergeJunction(mergeA.get(), model);


        } else {
            TopoEdge aEdge = azEdges.get(0);
            UrnE deviceUrn = urnMap.get(aEdge.getZ().getUrn());
            UrnE portUrn = urnMap.get(aEdge.getA().getUrn());
            DeviceModel model = deviceUrn.getDeviceModel();

            aJunction = makeEdgeJunction(deviceUrn, portUrn, model, azMbps, zaMbps, vlanId, sched);

        }
        if (mergeZ.isPresent()) {
            DeviceModel model = deviceModels.get(mergeZ.get().getDeviceUrn().getUrn());
            zJunction = mergeJunction(mergeZ.get(), model);

        } else {
            TopoEdge zEdge = azEdges.get(azEdges.size() - 1);

            UrnE deviceUrn = urnMap.get(zEdge.getZ().getUrn());
            UrnE portUrn = urnMap.get(zEdge.getA().getUrn());
            DeviceModel model = deviceUrn.getDeviceModel();

            zJunction = makeEdgeJunction(deviceUrn, portUrn, model, azMbps, zaMbps, vlanId, sched);

        }

        /* generating the ERO
        basically we are given edges in a sequence like so:

        0. port_A_1 - device_A
        1. device_A - port_A_2
        2. port_A_2 - port_B_1
        3. port_B_1 - device_B
        4. device_B - port_B_2

          etc, etc. exceptions:
              at the very first segment, if merging, we would start at a device not a port
              at the very last segment,  if merging, we would finish at a device not a port

        what we will save as the ERO is
        device - port - port - device - port - port - device - port - port - device

        we will be adding the A edge to everything plus the Z of the last one

        if we are NOT merging A, we should skip the first edge
        if we are NOT merging Z, we skip the last one


        and we just flip the ERO to reverse for the z-a direction (for now!)

         */
        int firstIdx = 0;
        int lastIdxExclusive = azEdges.size();
        if (!mergeA.isPresent()) {
            firstIdx = 1;
        }
        if (!mergeZ.isPresent()) {
            lastIdxExclusive = lastIdxExclusive - 1;
        }

        List<TopoEdge> azSubList = azEdges.subList(firstIdx, lastIdxExclusive);
        List<TopoEdge> zaSubList = zaEdges.subList(firstIdx, lastIdxExclusive);
        List<String> azEro = TopoAssistant.makeEro(azSubList, false);
        List<String> zaEro = zaSubList.equals(azSubList) ?
                TopoAssistant.makeEro(zaSubList, true) : TopoAssistant.makeEro(zaSubList, false);


        DeviceModel aModel = deviceModels.get(aJunction.getDeviceUrn().getUrn());
        DeviceModel zModel = deviceModels.get(zJunction.getDeviceUrn().getUrn());

        // Create Reserved Bandwidth
        // NOTE: Has null urn (for now)
        Set<ReservedBandwidthE> rsvBws = createBandwidthForEros(azEro, azMbps, zaMbps, sched, urnMap);
        rsvBws.addAll(createBandwidthForEros(zaEro, azMbps, zaMbps, sched, urnMap));
        return ReservedEthPipeE.builder()
                .pipeType(decidePipeType(aModel, zModel))
                .aJunction(aJunction)
                .zJunction(zJunction)
                .reservedPssResources(new HashSet<>())
                .reservedBandwidths(rsvBws)
                .azERO(azEro)
                .zaERO(zaEro)
                .build();

    }


    private ReservedVlanJunctionE mergeJunction(ReservedVlanJunctionE junction, DeviceModel model) throws PSSException {
        ReservedVlanJunctionE result = ReservedVlanJunctionE.copyFrom(junction);

        result.setJunctionType(decideJunctionType(model));
        EthFixtureType fixtureType = decideFixtureType(model);

        for (ReservedVlanFixtureE f : result.getFixtures()) {
            f.setFixtureType(fixtureType);
        }
        return result;
    }

    private ReservedVlanJunctionE makeEdgeJunction(UrnE deviceUrn, UrnE portUrn, DeviceModel model, Integer azMbps,
                                                   Integer zaMbps, Integer vlanId, ScheduleSpecificationE sched) throws PSSException {
        // TODO: verify urns exist / pass them in method

        // Create new Reserved Bandwidth
        ReservedBandwidthE rsvBw = createReservedBandwidth(portUrn, azMbps, zaMbps, sched);

        ReservedVlanE rsvVlan = createReservedVlan(portUrn, vlanId, sched);

        ReservedVlanJunctionE result = createReservedJunction(deviceUrn, new HashSet<>(), new HashSet<>(),
                decideJunctionType(model));

        ReservedVlanFixtureE fx = createReservedFixture(portUrn, new HashSet<>(), rsvVlan, rsvBw,
                decideFixtureType(model));

        result.getFixtures().add(fx);
        return result;
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

    private Set<ReservedBandwidthE> createBandwidthForEros(List<String> ero, Integer azMbps, Integer zaMbps,
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
