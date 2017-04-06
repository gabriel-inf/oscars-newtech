package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PSSParamsAdapter {

    private TopoService topoService;
    private AluParamsAdapter aluParamsAdapter;

    @Autowired
    public PSSParamsAdapter(TopoService topoService, AluParamsAdapter aluParamsAdapter) {
        this.aluParamsAdapter = aluParamsAdapter;
        this.topoService = topoService;
    }

    public Command buildIsolatedJunction(ConnectionE c, ReservedVlanJunctionE rvj) throws PSSException {
        log.info("making build command for isolated junction ");

        Command cmd = makeCmd(c.getConnectionId(), CommandType.BUILD, rvj.getDeviceUrn());

        // TODO: add apply qos switch to connection

        switch (cmd.getModel()) {
            case ALCATEL_SR7750:
                AluParams aluParams = aluParamsAdapter.isolatedJunction(c, rvj);
                cmd.setAlu(aluParams);
                break;
            case JUNIPER_EX:
                break;
            case JUNIPER_MX:
                break;
        }
        return cmd;
    }

    public List<Command> buildEthPipe(ConnectionE c, ReservedEthPipeE ethPipe) {
        List<Command> commands = new ArrayList<>();
        return commands;
    }
    public List<Command> buildMplsPipe(ConnectionE c, ReservedMplsPipeE mplsPipe) {
        List<Command> commands = new ArrayList<>();
        return commands;
    }



    public Command dismantleIsolatedJunction(ConnectionE c, ReservedVlanJunctionE rvj) throws PSSException {

        Command cmd = makeCmd(c.getConnectionId(), CommandType.DISMANTLE, rvj.getDeviceUrn());

        switch (cmd.getModel()) {
            case ALCATEL_SR7750:
                AluParams aluParams = aluParamsAdapter.isolatedJunction(c, rvj);
                cmd.setAlu(aluParams);
                break;
            case JUNIPER_EX:
                break;
            case JUNIPER_MX:
                break;
        }
        return cmd;
    }

    public List<Command> dismantleEthPipe(ConnectionE c, ReservedEthPipeE ethPipe) {
        List<Command> commands = new ArrayList<>();
        return commands;
    }
    public List<Command> dismantleMplsPipe(ConnectionE c, ReservedMplsPipeE mplsPipe) {
        List<Command> commands = new ArrayList<>();
        return commands;
    }

    private Command makeCmd(String connId, CommandType type, String device) {
        UrnE devUrn = topoService.device(device);

        return Command.builder()
                .connectionId(connId)
                .type(type)
                .model(devUrn.getDeviceModel())
                .device(devUrn.getUrn())
                .build();
    }

}
