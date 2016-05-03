package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.pce.TopoAssistant;
import net.es.oscars.spec.ent.VlanFixtureE;
import net.es.oscars.spec.ent.VlanJunctionE;
import net.es.oscars.spec.ent.VlanPipeE;
import net.es.oscars.topo.enums.DeviceModel;
import org.springframework.stereotype.Component;

import java.util.*;

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
                if (i + 1 == edges.size()) {

                } else {
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

    public List<VlanJunctionE> makeEthernetJunctions(List<TopoEdge> edges,
                                                     Integer azMbps, Integer zaMbps,
                                                     Optional<VlanJunctionE> mergeA,
                                                     Optional<VlanJunctionE> mergeZ,
                                                     Map<String, DeviceModel> deviceModels)
            throws PSSException {
        List<VlanJunctionE> result = new ArrayList<>();
        if (mergeA.isPresent()) {
            // we will find the device URN at the A of the first edge
            String aaUrn = edges.get(0).getA().getUrn();
            String deviceUrn = mergeA.get().getDeviceUrn();
            assert aaUrn.equals(deviceUrn);
        }
        if (mergeZ.isPresent()) {
            // we will find the device URN at Z of the last edge
            String zzUrn = edges.get(edges.size() - 1).getZ().getUrn();
            String deviceUrn = mergeZ.get().getDeviceUrn();
            assert zzUrn.equals(deviceUrn);
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

            VlanJunctionE mergeThis = mergeA.get();
            DeviceModel model = deviceModels.get(mergeThis.getDeviceUrn());
            EthFixtureType fixtureType = decideFixtureType(model);

            VlanFixtureE fx = VlanFixtureE.builder()
                    .inMbps(azMbps)
                    .egMbps(zaMbps)
                    .fixtureType(EthFixtureType.REQUESTED)
                    .portUrn(edges.get(0).getZ().getUrn())
                    .vlanExpression("")
                    .vlanId(null)
                    .build();

            VlanJunctionE copy = VlanJunctionE.copyFrom(mergeThis);
            copy.setJunctionType(decideJunctionType(model));
            copy.getFixtures().add(fx);
            for (VlanFixtureE f : copy.getFixtures()) {
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


            DeviceModel model = deviceModels.get(edgeOne.getZ().getUrn());

            VlanJunctionE newJunction = VlanJunctionE.builder()
                    .junctionType(decideJunctionType(model))
                    .resourceIds(new HashSet<>())
                    .fixtures(new HashSet<>())
                    .build();
            VlanFixtureE fOne = VlanFixtureE.builder()
                    .inMbps(azMbps)
                    .egMbps(zaMbps)
                    .fixtureType(decideFixtureType(model))
                    .portUrn(edgeOne.getA().getUrn())
                    .vlanExpression("")
                    .vlanId(null)
                    .build();
            VlanFixtureE fTwo = VlanFixtureE.builder()
                    .inMbps(azMbps)
                    .egMbps(zaMbps)
                    .fixtureType(decideFixtureType(model))
                    .portUrn(edgeTwo.getZ().getUrn())
                    .vlanExpression("")
                    .vlanId(null)
                    .build();

            newJunction.getFixtures().add(fOne);
            newJunction.getFixtures().add(fTwo);


            result.add(newJunction);

        }

        if (mergeZ.isPresent()) {
            VlanJunctionE mergeThis = mergeZ.get();
            DeviceModel model = deviceModels.get(mergeThis.getDeviceUrn());
            EthFixtureType fixtureType = decideFixtureType(model);

            VlanFixtureE fx = VlanFixtureE.builder()
                    .inMbps(azMbps)
                    .egMbps(zaMbps)
                    .fixtureType(EthFixtureType.REQUESTED)
                    .portUrn(edges.get(edges.size() - 1).getZ().getUrn())
                    .vlanExpression("")
                    .vlanId(null)
                    .build();

            VlanJunctionE copy = VlanJunctionE.copyFrom(mergeThis);
            copy.setJunctionType(decideJunctionType(model));
            copy.getFixtures().add(fx);
            for (VlanFixtureE f : copy.getFixtures()) {
                f.setFixtureType(fixtureType);
            }
            result.add(copy);
        }
        return result;

    }

    // TODO: support asymmetrical case; need to change method signature
    public VlanPipeE makeVplsPipe(List<TopoEdge> edges,
                                  Integer azMbps, Integer zaMbps,
                                  Optional<VlanJunctionE> mergeA,
                                  Optional<VlanJunctionE> mergeZ,
                                  Map<String, DeviceModel> deviceModels)
            throws PSSException {
        /*
        a few different cases to handle:
        if we need to merge either A or Z junction from the originally requested pipe, we just copy the junction over

        if at either end of the pipe we are not provided a junction to merge, we will be provided the port URN
        if it's at A, it will be the A of the first edge
        if it's at Z, it will be the Z of the last edge

        in either case, we will need to create a new junction for our pipe and add that single port as a fixture

        */
        VlanJunctionE aJunction;
        VlanJunctionE zJunction;

        if (mergeA.isPresent()) {
            DeviceModel model = deviceModels.get(mergeA.get().getDeviceUrn());
            aJunction = mergeJunction(mergeA.get(), model);


        } else {
            TopoEdge aEdge = edges.get(0);
            String deviceUrn = aEdge.getZ().getUrn();
            String portUrn = aEdge.getA().getUrn();
            DeviceModel model = deviceModels.get(deviceUrn);
            aJunction = makeEdgeJunction(deviceUrn, portUrn, model, azMbps, zaMbps);

        }
        if (mergeZ.isPresent()) {
            DeviceModel model = deviceModels.get(mergeZ.get().getDeviceUrn());
            zJunction = mergeJunction(mergeZ.get(), model);

        } else {
            TopoEdge zEdge = edges.get(edges.size() - 1);
            String deviceUrn = zEdge.getA().getUrn();
            String portUrn = zEdge.getZ().getUrn();
            DeviceModel model = deviceModels.get(deviceUrn);
            zJunction = makeEdgeJunction(deviceUrn, portUrn, model, azMbps, zaMbps);

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
        int lastIdxExclusive = edges.size();
        if (!mergeA.isPresent()) {
            firstIdx = 1;
        }
        if (!mergeZ.isPresent()) {
            lastIdxExclusive = lastIdxExclusive - 1;
        }
        List<TopoEdge> subList = edges.subList(firstIdx, lastIdxExclusive);
        List<String> azEro = TopoAssistant.makeEro(subList, false);
        List<String> zaEro = TopoAssistant.makeEro(subList, true);


        DeviceModel aModel = deviceModels.get(aJunction.getDeviceUrn());
        DeviceModel zModel = deviceModels.get(zJunction.getDeviceUrn());
        return VlanPipeE.builder()
                .pipeType(decidePipeType(aModel, zModel))
                .aJunction(aJunction)
                .zJunction(zJunction)
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .resourceIds(new HashSet<>())
                .azERO(azEro)
                .zaERO(zaEro)
                .build();

    }


    private VlanJunctionE mergeJunction(VlanJunctionE junction, DeviceModel model) throws PSSException {
        VlanJunctionE result = VlanJunctionE.copyFrom(junction);

        result.setJunctionType(decideJunctionType(model));
        EthFixtureType fixtureType = decideFixtureType(model);

        for (VlanFixtureE f : result.getFixtures()) {
            f.setFixtureType(fixtureType);
        }
        return result;
    }

    private VlanJunctionE makeEdgeJunction(String deviceUrn, String portUrn, DeviceModel model, Integer azMbps, Integer zaMbps) throws PSSException {

        VlanJunctionE result = VlanJunctionE.builder()
                .resourceIds(new HashSet<>())
                .deviceUrn(deviceUrn)
                .fixtures(new HashSet<>())
                .junctionType(decideJunctionType(model))
                .build();
        VlanFixtureE fx = VlanFixtureE.builder()
                .inMbps(azMbps)
                .egMbps(zaMbps)
                .fixtureType(decideFixtureType(model))
                .portUrn(portUrn)
                .vlanExpression("")
                .vlanId(null)
                .build();
        result.getFixtures().add(fx);
        return result;
    }

    // TODO: fix this
    public Map<String, ResourceType> neededPipeResources(VlanPipeE vp) throws PSSException {
        Map<String, ResourceType> result = new HashMap<>();
        switch (vp.getPipeType()) {
            case ALU_TO_ALU_VPLS:
                return result;
            case ALU_TO_JUNOS_VPLS:
                return result;
            case JUNOS_TO_JUNOS_VPLS:
                return result;
            case REQUESTED:
                throw new PSSException("Invalid pipe type (REQUESTED)!");
        }
        throw new PSSException("Could not reserve pipe resources");

    }

    public Map<ResourceType, List<String>> neededJunctionResources(VlanJunctionE vj) throws PSSException {
        Map<ResourceType, List<String>> result = new HashMap<>();

        List<String> deviceScope = new ArrayList<>();
        deviceScope.add(vj.getDeviceUrn());
        List<String> global = new ArrayList<>();
        global.add(ResourceType.GLOBAL);

        List<String> ports = new ArrayList<>();
        vj.getFixtures().stream().forEach(t -> {
            ports.add(t.getPortUrn());
        });


        switch (vj.getJunctionType()) {
            case ALU_VPLS:

                result.put(ResourceType.ALU_INGRESS_POLICY_ID, deviceScope);
                result.put(ResourceType.ALU_EGRESS_POLICY_ID, deviceScope);
                result.put(ResourceType.VC_ID, global);
                result.put(ResourceType.VLAN, ports);

                return result;
            case JUNOS_SWITCH:
                result.put(ResourceType.VLAN, deviceScope);
                return result;
            case JUNOS_VPLS:
                result.put(ResourceType.VC_ID, global);
                result.put(ResourceType.VLAN, ports);
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
