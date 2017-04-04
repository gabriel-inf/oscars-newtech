package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.dto.pss.params.alu.AluParams;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.dao.RouterCommandsRepository;
import net.es.oscars.pss.ent.RouterCommandsE;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Component
@Slf4j
public class PSSAdapter {
    private PSSProxy pssProxy;
    private RouterCommandsRepository rcr;
    private PSSParamsAdapter paramsAdapter;

    @Autowired
    public PSSAdapter(PSSProxy pssProxy, RouterCommandsRepository rcr, PSSParamsAdapter paramsAdapter) {
        this.pssProxy = pssProxy;
        this.rcr = rcr;
        this.paramsAdapter = paramsAdapter;
    }

    public void setup(ConnectionE conn) {
        log.info("setting up");
        List<Command> commands = this.setupCommands(conn);


    }

    public List<CommandResponse> parallelSubmit(List<Command> commands)
            throws InterruptedException, ExecutionException {
        List<CommandResponse> responses = new ArrayList<>();

        int threadNum = commands.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        List<FutureTask<CommandResponse>> taskList = new ArrayList<>();
        for (Command cmd : commands) {
            FutureTask<CommandResponse> task = new FutureTask<>(() -> pssProxy.submitCommand(cmd));
            taskList.add(task);
            executor.execute(task);
        }
        for (int j = 0; j < threadNum; j++) {
            FutureTask<CommandResponse> futureTask = taskList.get(j);
            responses.add(taskList.get(j).get());
            log.info("got response "+futureTask.get().getCommandId());
        }
        executor.shutdown();
        return responses;
    }

    public List<CommandStatus> parallelStatus(List<String> commandIds)
            throws InterruptedException, ExecutionException {

        int threadNum = commandIds.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        List<FutureTask<CommandStatus>> taskList = new ArrayList<>();
        for (String commandId : commandIds) {
            FutureTask<CommandStatus> task = new FutureTask<>(() -> pssProxy.status(commandId));
            taskList.add(task);
            executor.execute(task);
        }

        List<CommandStatus> statuses = new ArrayList<>();

        for (int j = 0; j < threadNum; j++) {
            FutureTask<CommandStatus> futureTask = taskList.get(j);
            statuses.add(taskList.get(j).get());
            log.info("got command status for device "+futureTask.get().getDevice());
        }
        executor.shutdown();
        return statuses;
    }


    public void generateConfig(ConnectionE conn) {
        log.info("generating config");

        // TODO: mapping of device urn <-> pss device ?
        List<Command> commands = new ArrayList<>();
        commands.addAll(this.setupCommands(conn));
        commands.addAll(this.teardownCommands(conn));
        for (Command cmd : commands) {
            GenerateResponse resp = pssProxy.generate(cmd);
            RouterCommandsE rce = RouterCommandsE.builder()
                    .connectionId(conn.getConnectionId())
                    .deviceUrn(cmd.getDevice())
                    .contents(resp.getGenerated())
                    .type(resp.getCommandType())
                    .build();
            rcr.save(rce);
        }
    }


    public List<Command> setupCommands(ConnectionE conn) {
        List<Command> commands = new ArrayList<>();

        ReservedVlanFlowE flow = conn.getReserved().getVlanFlow();
        for (ReservedVlanJunctionE rvj : flow.getJunctions()) {
            commands.add(paramsAdapter.isolatedJunctionSetup(conn.getConnectionId(), rvj));
        }
        for (ReservedEthPipeE ethPipe : flow.getEthPipes()) {
            commands.addAll(paramsAdapter.ethPipeSetup(conn.getConnectionId(), ethPipe));
        }
        for (ReservedMplsPipeE mplsPipe : flow.getMplsPipes()) {
            commands.addAll(paramsAdapter.mplsPipeSetup(conn.getConnectionId(), mplsPipe));
        }

        return commands;
    }


    public List<Command> teardownCommands(ConnectionE conn) {
        List<Command> commands = new ArrayList<>();

        ReservedVlanFlowE flow = conn.getReserved().getVlanFlow();
        for (ReservedVlanJunctionE rvj : flow.getJunctions()) {
            commands.add(paramsAdapter.isolatedJunctionTeardown(conn.getConnectionId(), rvj));
        }
        for (ReservedEthPipeE ethPipe : flow.getEthPipes()) {
            commands.addAll(paramsAdapter.ethPipeTeardown(conn.getConnectionId(), ethPipe));
        }
        for (ReservedMplsPipeE mplsPipe : flow.getMplsPipes()) {
            commands.addAll(paramsAdapter.mplsPipeTeardown(conn.getConnectionId(), mplsPipe));
        }

        return commands;
    }
}
