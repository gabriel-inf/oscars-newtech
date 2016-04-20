package net.es.oscars.pss;

import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.spec.ent.VlanJunctionE;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.enums.DeviceModel;
import org.springframework.stereotype.Component;

@Component
public class PCEAssistant {

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
                        return EthPipeType.HYBRID;
                    case JUNIPER_MX:
                        return EthPipeType.ALU_TO_JUNOS_VPLS;
                }

                break;
            case JUNIPER_MX:
                switch (zModel) {
                    case ALCATEL_SR7750:
                        return EthPipeType.JUNOS_TO_ALU_VPLS;
                    case JUNIPER_EX:
                        return EthPipeType.HYBRID;
                    case JUNIPER_MX:
                        return EthPipeType.JUNOS_TO_JUNOS_VPLS;
                }
            case JUNIPER_EX:
                return EthPipeType.HYBRID;


        }
        throw new PSSException("Could not determine pipe type");
    }

}
