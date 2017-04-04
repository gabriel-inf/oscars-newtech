package net.es.oscars.pss.help;

import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.dto.pss.st.*;
import net.es.oscars.pss.svc.PSSProxy;
import org.hashids.Hashids;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MockPssServer implements PSSProxy {

    public GenerateResponse generate(Command cmd) {
        return GenerateResponse.builder()
                .commandType(cmd.getType())
                .connectionId(cmd.getConnectionId())
                .device(cmd.getDevice())
                .generated("")
                .build();
    }

    public CommandStatus status(String commandId) {
        return this.statuses.get(commandId);
    }

    private Map<String, CommandStatus> statuses = new HashMap<>();

    public CommandResponse submitCommand(Command cmd) {
        String cmdId = this.randHashId();

        CommandStatus status = CommandStatus.builder()
                .configStatus(ConfigStatus.NONE)
                .lifecycleStatus(LifecycleStatus.INITIAL_STATE)
                .operationalStatus(OperationalStatus.NONE)
                .controlPlaneStatus(ControlPlaneStatus.NONE)
                .connectionId(cmd.getConnectionId())
                .device(cmd.getDevice())
                .lastUpdated(new Date())
                .commands("")
                .output("")
                .type(cmd.getType())
                .build();
        statuses.put(cmdId, status);

        return CommandResponse.builder()
                .connectionId(cmd.getConnectionId())
                .device(cmd.getDevice())
                .commandId(cmdId)
                .build();
    }



    public void initial(String commandId) {
        StatusCollection sc = new StatusCollection();
        sc.lf = LifecycleStatus.INITIAL_STATE;
        sc.os = OperationalStatus.NONE;
        sc.cp = ControlPlaneStatus.NONE;
        sc.cf = ConfigStatus.NONE;
        updateStatus(sc, commandId);
    }

    public void waiting(String commandId) {
        StatusCollection sc = new StatusCollection();
        sc.lf = LifecycleStatus.WAITING;
        sc.os = OperationalStatus.NONE;
        sc.cp = ControlPlaneStatus.NONE;
        sc.cf = ConfigStatus.NONE;
        updateStatus(sc, commandId);
    }

    public void working(String commandId) {
        StatusCollection sc = new StatusCollection();
        sc.lf = LifecycleStatus.PROCESSING;
        sc.os = OperationalStatus.NONE;
        sc.cp = ControlPlaneStatus.NONE;
        sc.cf = ConfigStatus.NONE;
        updateStatus(sc, commandId);
    }

    public void controlPlaneError(String commandId) {
        StatusCollection sc = new StatusCollection();
        sc.lf = LifecycleStatus.DONE;
        sc.os = OperationalStatus.NONE;
        sc.cp = ControlPlaneStatus.ERROR;
        sc.cf = ConfigStatus.NONE;
        updateStatus(sc, commandId);
    }

    public void configStatusIs(ConfigStatus cf, String commandId) {
        StatusCollection sc = new StatusCollection();
        sc.lf = LifecycleStatus.DONE;
        sc.os = OperationalStatus.NONE;
        sc.cp = ControlPlaneStatus.OK;
        sc.cf = cf;
        updateStatus(sc, commandId);
    }

    public void opStatusIs(OperationalStatus os, String commandId) {
        StatusCollection sc = new StatusCollection();
        sc.lf = LifecycleStatus.DONE;
        sc.os = os;
        sc.cp = ControlPlaneStatus.OK;
        sc.cf = ConfigStatus.NONE;
        updateStatus(sc, commandId);
    }


    private void updateStatus(StatusCollection sc, String commandId) {
        CommandStatus status = this.statuses.get(commandId);
        status.setOperationalStatus(sc.os);
        status.setLifecycleStatus(sc.lf);
        status.setConfigStatus(sc.cf);
        status.setControlPlaneStatus(sc.cp);
        status.setLastUpdated(new Date());
        this.statuses.put(commandId, status);
    }

    private class StatusCollection {
        ControlPlaneStatus cp;
        ConfigStatus cf;
        LifecycleStatus lf;
        OperationalStatus os;
    }

    private String randHashId() {
        Hashids hashids = new Hashids("oscars");

        Random rand = new Random();
        Integer id = rand.nextInt();
        if (id < 0) {
            id = -1 * id;
        }
        return hashids.encode(id);
    }
}
