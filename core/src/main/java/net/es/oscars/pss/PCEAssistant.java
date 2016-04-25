package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.spec.VlanPipe;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.spec.ent.VlanJunctionE;
import net.es.oscars.spec.ent.VlanPipeE;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.enums.DeviceModel;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class PCEAssistant {


    public static Set<VlanPipeE> decompose(VlanPipeE req_p, List<TopoEdge> edges, Map<String, DeviceModel> deviceModels) {
        Set<VlanPipeE> result = new HashSet<>();

        /* why 3? this the shortest possible pipe; otherwise it's the same device and it's a junction. i.e.:

        alpha -- INTERNAL -- alpha:1/1/1
        alpha:1/1/1 -- ETHERNET or MPLS-- beta:1/1/1
        beta:1/1/1 -- INTERNAL -- beta

        in general, this would continues on as follows, with

        beta            -- INTERNAL -- beta:2/1/1
        beta:2/1/1      -- ETHERNET / MPLS-- charlie:1/1/1
        charlie:1/1/1   -- INTERNAL -- charlie
        charlie         -- INTERNAL -- charlie:2/1/1
        charlie:2/1/1   -- ETHERNET / MPLS-- delta:1/1/1
        delta:1/1/1     -- INTERNAL -- delta

        and so on and so forth. very first one and very last one is always INTERNAL, from and to devices specifically

        the size of the path is always a multiple of 3.
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

            } else if ( mod == 1) {
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

        Layer currentLayer = edges.get(1).getLayer();

        for (int i = 0; i < edges.size(); i++) {
            Integer mod = Math.floorMod(i, 3);
            // for each device-to-port edge, decide if now is the time to start a new pipe.
            if (mod == 0) {
                // except for the very last one, of course
                if (i + 1 < edges.size()) {
                    TopoEdge nextEdge = edges.get(i + 1);
                    if (!nextEdge.getLayer().equals(currentLayer)) {
                        log.info("switching layers to " + nextEdge.getLayer());
                        currentLayer = nextEdge.getLayer();
                    }
                }
            }

        }

        return result;

    }

    public void reservePipeResources(VlanPipeE vp) throws PSSException {
        switch (vp.getPipeType()) {
            case ALU_TO_ALU_VPLS:
                return;
            case ALU_TO_JUNOS_VPLS:
                return;
            case JUNOS_TO_JUNOS_VPLS:
                return;
            case ETHERNET_TRUNK:
                return;
            case REQUESTED:
                throw new PSSException("Invalid pipe type (REQUESTED)!");
        }
        throw new PSSException("Could not reserve pipe resources");

    }

    public void reserveJunctionResources(VlanJunctionE vj) throws PSSException {
        switch (vj.getJunctionType()) {
            case ALU_VPLS:
                // name
                // vc-id

                return;
            case JUNOS_SWITCH:
                // TODO: what?
                return;
            case JUNOS_VPLS:
                // TODO: what to reserve?
                return;
        }
        throw new PSSException("Could not reserve junction resources");
    }

    public EthJunctionType decideJunctionType(EDevice device) throws PSSException {
        DeviceModel model = device.getModel();
        switch (model) {
            case ALCATEL_SR7750:
                return EthJunctionType.ALU_VPLS;
            case JUNIPER_EX:
                return EthJunctionType.JUNOS_SWITCH;
            case JUNIPER_MX:
                return EthJunctionType.JUNOS_VPLS;
        }
        throw new PSSException("Could not determine junction type for "+device);

    }

    public EthFixtureType decideFixtureType(EDevice device) throws PSSException {
        DeviceModel model = device.getModel();
        switch (model) {
            case ALCATEL_SR7750:
                return EthFixtureType.ALU_SAP;
            case JUNIPER_EX:
                return EthFixtureType.JUNOS_IFCE;
            case JUNIPER_MX:
                return EthFixtureType.JUNOS_IFCE;
        }
        throw new PSSException("Could not determine fixture type for "+device);

    }

    public EthPipeType decidePipeType(EDevice a, EDevice z) throws PSSException {
        DeviceModel aModel = a.getModel();
        DeviceModel zModel = z.getModel();

        switch (aModel) {
            case ALCATEL_SR7750:
                switch (zModel) {
                    case ALCATEL_SR7750:
                        return EthPipeType.ALU_TO_ALU_VPLS;
                    case JUNIPER_EX:
                        return EthPipeType.ETHERNET_TRUNK;
                    case JUNIPER_MX:
                        return EthPipeType.ALU_TO_JUNOS_VPLS;
                }

                break;
            case JUNIPER_MX:
                switch (zModel) {
                    case ALCATEL_SR7750:
                        return EthPipeType.ALU_TO_JUNOS_VPLS;
                    case JUNIPER_EX:
                        return EthPipeType.ETHERNET_TRUNK;
                    case JUNIPER_MX:
                        return EthPipeType.JUNOS_TO_JUNOS_VPLS;
                }
            case JUNIPER_EX:
                switch (zModel) {
                    case ALCATEL_SR7750:
                        return EthPipeType.ETHERNET_TRUNK;
                    case JUNIPER_MX:
                        return EthPipeType.ETHERNET_TRUNK;
                    case JUNIPER_EX:
                        return EthPipeType.ETHERNET_TRUNK;
                }

        }
        throw new PSSException("Could not determine pipe type");
    }

}
