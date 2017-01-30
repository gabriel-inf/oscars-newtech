package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.dao.RouterCommandsRepository;
import net.es.oscars.pss.ent.RouterCommandsE;
import net.es.oscars.resv.dao.ConnectionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@Transactional
public class RouterCommandGenerator {

    @Autowired
    private RouterCommandsRepository rcRepo;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private TopoService topoService;

    @Autowired
    private JuniperExCommandGenerator exCommandGen;

    @Autowired
    private JuniperMxCommandGenerator mxCommandGen;

    @Autowired
    private AluCommandGenerator aluCommandGen;

    public void generateConfig(ConnectionE conn) throws PSSException {
        log.info("generating config");
        conn.getStates().setProv(ProvState.GENERATING);
        connRepo.save(conn);
        ReservedVlanFlowE rvf = conn.getReserved().getVlanFlow();

        Set<ReservedVlanJunctionE> rvj_set = new HashSet<>();
        rvj_set.addAll(rvf.getJunctions());
        Map<String, List<String>> isolatedJunctionCommands = isolatedJunctionCommands(rvj_set, conn);

        Map<String, List<String>> ethCommands = new HashMap<>();
        Map<String, List<String>> mplsCommands = new HashMap<>();
        for (ReservedEthPipeE rep : rvf.getEthPipes()) {
            log.info("generating config for eth pipe " + rep.getId());
            Map<String, List<String>> thisPipeCommands = this.ethPipeCommands(rep);
            thisPipeCommands.forEach((urn, commands) -> {
                if (!ethCommands.keySet().contains(urn)) {
                    ethCommands.put(urn, new ArrayList<>());
                }
                ethCommands.get(urn).addAll(commands);

            });

        }

        for (ReservedMplsPipeE rmp : rvf.getMplsPipes()) {
            log.info("generating config for mpls pipe " + rmp.getId());
            Map<String, List<String>> thisPipeCommands = this.mplsPipeCommands(conn, rmp);
            thisPipeCommands.forEach((urn, commands) -> {
                if (!mplsCommands.keySet().contains(urn)) {
                    mplsCommands.put(urn, new ArrayList<>());
                }
                mplsCommands.get(urn).addAll(commands);

            });
        }
        Map<String, List<String>> commandsByDevice = new HashMap<>();

        isolatedJunctionCommands.forEach((urn, commands) -> {
            log.info("adding isolated junction commands for " + urn);
            commandsByDevice.put(urn, commands);

        });

        ethCommands.forEach((urn, commands) -> {
            log.info("adding ethernet commands for " + urn);
            if (!commandsByDevice.keySet().contains(urn)) {
                commandsByDevice.put(urn, new ArrayList<>());
            }
            commandsByDevice.get(urn).addAll(commands);
        });

        mplsCommands.forEach((urn, commands) -> {
            log.info("adding mpls commands for " + urn);
            if (!commandsByDevice.keySet().contains(urn)) {
                commandsByDevice.put(urn, new ArrayList<>());
            }
            commandsByDevice.get(urn).addAll(commands);
        });

        for (String deviceUrn : commandsByDevice.keySet()) {
            List<String> commands = commandsByDevice.get(deviceUrn);
            RouterCommandsE rc_e = RouterCommandsE.builder()
                    .connectionId(conn.getConnectionId())
                    .deviceUrn(deviceUrn)
                    .contents(String.join("\n", commands))
                    .build();

            rcRepo.save(rc_e);
            log.info("saved router commands: " + rc_e.toString());
        }


        conn.getStates().setProv(ProvState.DISMANTLED_AUTO);
        connRepo.save(conn);
        log.info(" done generating config");
    }

    private Map<String, List<String>> isolatedJunctionCommands(Collection<ReservedVlanJunctionE> rvj_set, ConnectionE conn)
            throws PSSException {

        Map<String, List<String>> result = new HashMap<>();
        for (ReservedVlanJunctionE rvj : rvj_set) {
            String deviceUrn = rvj.getDeviceUrn();
            log.info("generating junction commands for " + deviceUrn);
            if (!result.containsKey(deviceUrn)) {
                result.put(deviceUrn, new ArrayList<>());
            }
            UrnE device = topoService.device(rvj.getDeviceUrn());
            switch (device.getDeviceModel()) {
                case ALCATEL_SR7750:
                    result.get(deviceUrn).add(aluCommandGen.isolatedJunction(rvj, conn));
                    break;
                case JUNIPER_EX:
                    result.get(deviceUrn).add(exCommandGen.isolatedJunction(rvj, conn));
                    break;
                case JUNIPER_MX:
                    result.get(deviceUrn).add(mxCommandGen.isolatedJunction(rvj, conn));
                    break;
            }
        }
        return result;

    }


    private Map<String, List<String>> ethPipeCommands(ReservedEthPipeE rep) throws PSSException {
        Map<String, List<String>> result = new HashMap<>();
        ReservedVlanJunctionE aj = rep.getAJunction();

        String deviceUrn = aj.getDeviceUrn();
        log.info("generating eth pipe commands for " + deviceUrn);
        UrnE device = topoService.device(aj.getDeviceUrn());
        result.put(deviceUrn, new ArrayList<>());

        switch (device.getDeviceModel()) {
            case ALCATEL_SR7750:
                result.get(deviceUrn).add(aluCommandGen.ethPipe(aj, rep, rep.getAzERO()));
                break;
            case JUNIPER_EX:
                result.get(deviceUrn).add(exCommandGen.ethPipe(aj, rep, rep.getAzERO()));
                break;
            case JUNIPER_MX:
                result.get(deviceUrn).add(mxCommandGen.ethPipe(aj, rep, rep.getAzERO()));
                break;
        }

        ReservedVlanJunctionE zj = rep.getZJunction();
        log.info("generating eth pipe commands for " + deviceUrn);
        device = topoService.device(zj.getDeviceUrn());
        result.put(deviceUrn, new ArrayList<>());
        switch (device.getDeviceModel()) {
            case ALCATEL_SR7750:
                result.get(deviceUrn).add(aluCommandGen.ethPipe(zj, rep, rep.getZaERO()));
                break;
            case JUNIPER_EX:
                result.get(deviceUrn).add(exCommandGen.ethPipe(zj, rep, rep.getZaERO()));
                break;
            case JUNIPER_MX:
                result.get(deviceUrn).add(mxCommandGen.ethPipe(zj, rep, rep.getZaERO()));
                break;
        }
        return result;
    }

    private Map<String, List<String>> mplsPipeCommands(ConnectionE conn, ReservedMplsPipeE rmp) throws PSSException {
        Map<String, List<String>> result = new HashMap<>();
        ReservedVlanJunctionE aj = rmp.getAJunction();
        ReservedVlanJunctionE zj = rmp.getZJunction();

        String deviceUrn = aj.getDeviceUrn();
        log.info("generating mpls pipe commands for " + deviceUrn);
        UrnE device = topoService.device(aj.getDeviceUrn());
        result.put(deviceUrn, new ArrayList<>());
        switch (device.getDeviceModel()) {
            case ALCATEL_SR7750:
                result.get(deviceUrn).add(aluCommandGen.mplsPipe(conn, aj, zj, rmp, rmp.getAzERO()));
                break;
            case JUNIPER_EX:
                throw new PSSException("Cannot make MPLS pipe for Juniper EX!");
            case JUNIPER_MX:
                result.get(deviceUrn).add(mxCommandGen.mplsPipe(conn, aj, zj, rmp, rmp.getAzERO()));
                break;
        }

        deviceUrn = zj.getDeviceUrn();
        log.info("generating mpls pipe commands for " + deviceUrn);
        device = topoService.device(zj.getDeviceUrn());
        result.put(deviceUrn, new ArrayList<>());
        switch (device.getDeviceModel()) {
            case ALCATEL_SR7750:
                result.get(deviceUrn).add(aluCommandGen.mplsPipe(conn, zj, aj, rmp, rmp.getZaERO()));
                break;
            case JUNIPER_EX:
                throw new PSSException("Cannot make MPLS pipe for Juniper EX!");
            case JUNIPER_MX:
                result.get(deviceUrn).add(mxCommandGen.mplsPipe(conn, zj, aj, rmp, rmp.getZaERO()));
                break;
        }
        return result;
    }



}
