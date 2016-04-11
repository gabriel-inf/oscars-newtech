package net.es.oscars.pss;

import net.es.oscars.pss.enums.EthFixtureType;
import net.es.oscars.pss.enums.EthJunctionType;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.enums.DeviceModel;
import org.springframework.stereotype.Component;

@Component
public class PCEAssistant {

    public EthJunctionType decideJunctionType(EDevice device) throws PSSException {
        DeviceModel model = device.getModel();
        switch (model) {
            case ALCATEL_SR7750:
                return EthJunctionType.ALU_SDP;
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

}
