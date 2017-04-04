package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.resv.ent.ReservedEthPipeE;
import net.es.oscars.resv.ent.ReservedMplsPipeE;
import net.es.oscars.resv.ent.ReservedVlanFixtureE;
import net.es.oscars.resv.ent.ReservedVlanJunctionE;
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

    @Autowired
    public PSSParamsAdapter(TopoService topoService) {
        this.topoService = topoService;
    }

    public Command isolatedJunctionSetup(String connectionId, ReservedVlanJunctionE rvj) {

        Command cmd = makeCmd(connectionId, CommandType.SETUP, rvj.getDeviceUrn());

        switch (cmd.getModel()) {
            case ALCATEL_SR7750:
                break;
            case JUNIPER_EX:
                break;
            case JUNIPER_MX:
                break;
        }
        return cmd;
    }

    public List<Command> ethPipeSetup(String connectionId, ReservedEthPipeE ethPipe) {
        List<Command> commands = new ArrayList<>();
        return commands;
    }
    public List<Command> mplsPipeSetup(String connectionId, ReservedMplsPipeE mplsPipe) {
        List<Command> commands = new ArrayList<>();
        return commands;
    }



    public Command isolatedJunctionTeardown(String connectionId, ReservedVlanJunctionE rvj) {

        Command cmd = makeCmd(connectionId, CommandType.TEARDOWN, rvj.getDeviceUrn());

        switch (cmd.getModel()) {
            case ALCATEL_SR7750:
                break;
            case JUNIPER_EX:
                break;
            case JUNIPER_MX:
                break;
        }
        return cmd;
    }

    public List<Command> ethPipeTeardown(String connectionId, ReservedEthPipeE ethPipe) {
        List<Command> commands = new ArrayList<>();
        return commands;
    }
    public List<Command> mplsPipeTeardown(String connectionId, ReservedMplsPipeE mplsPipe) {
        List<Command> commands = new ArrayList<>();
        return commands;
    }


    public AluParams setupAluParams(String connectionId, ReservedVlanJunctionE rvj) {
        AluParams aluParams = AluParams.builder().build();
        rvj.getReservedPssResources();
        for (ReservedVlanFixtureE rvf : rvj.getFixtures()) {

        }
        return aluParams;
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
