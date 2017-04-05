package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.dao.RouterCommandsRepository;
import net.es.oscars.pss.ent.RouterCommandsE;
import net.es.oscars.resv.ent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


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

    public void setup(ConnectionE conn) throws PSSException {
        log.info("setting up "+conn.getConnectionId());
        List<Command> commands = this.setupCommands(conn);
        try {
            List<CommandResponse> responses = parallelSubmit(commands);
            List<String> commandIds = responses.stream()
                    .map(CommandResponse::getCommandId).collect(Collectors.toList());

        } catch (InterruptedException | ExecutionException ex) {
            throw new PSSException("interrupted");
        }
    }

    public void teardown(ConnectionE conn) throws PSSException {
        log.info("tearing down "+conn.getConnectionId());
        List<Command> commands = this.teardownCommands(conn);
        try {
            List<CommandResponse> responses = parallelSubmit(commands);
            List<String> commandIds = responses.stream()
                    .map(CommandResponse::getCommandId).collect(Collectors.toList());
            List<CommandStatus> statuses = pollUntilStable(commandIds);

        } catch (InterruptedException | ExecutionException ex) {
            throw new PSSException("interrupted");
        }
    }


    public List<CommandStatus> pollUntilStable(List<String> commandIds)
            throws PSSException {

        boolean allDone = false;
        boolean timedOut = false;
        Integer timeoutMillis = 60000;
        Integer elapsed = 0;
        List<CommandStatus> statuses = new ArrayList<>();

        try {
            while (!allDone && !timedOut) {
                log.info("polling PSS.. ");
                statuses = pollStatuses(commandIds);
                allDone = areAllDone(statuses);

                if (!allDone) {
                    Thread.sleep(1000);
                    elapsed = elapsed + 1000;
                    if (elapsed > timeoutMillis) {
                        timedOut = true;
                    }
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            log.error("interrupted!", ex);
            throw new PSSException("PSS thread interrupted");
        }

        if (timedOut) {
            throw new PSSException("timed out waiting for all routers to be stable");
        }

        return statuses;
    }

    public boolean areAllDone(List<CommandStatus> statuses) {
        boolean allDone = true;
        for (int j = 0; j < statuses.size(); j++) {
            if (!statuses.get(j).getLifecycleStatus().equals(LifecycleStatus.DONE)) {
                allDone = false;
            }
        }
        return allDone;
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
            log.info("got response " + futureTask.get().getCommandId());
        }
        executor.shutdown();
        return responses;
    }


    public List<CommandStatus> pollStatuses(List<String> commandIds)
            throws InterruptedException, ExecutionException {
        List<CommandStatus> statuses = new ArrayList<>();
        int threadNum = commandIds.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        List<FutureTask<CommandStatus>> taskList = new ArrayList<>();
        for (String commandId : commandIds) {
            FutureTask<CommandStatus> task = new FutureTask<>(() -> pssProxy.status(commandId));
            taskList.add(task);
            executor.execute(task);
        }

        for (int j = 0; j < threadNum; j++) {
            FutureTask<CommandStatus> futureTask = taskList.get(j);
            statuses.add(taskList.get(j).get());
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
