package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.spec.ReservedVlanFixture;
import net.es.oscars.pss.dao.RouterCommandsRepository;
import net.es.oscars.pss.dao.TemplateRepository;
import net.es.oscars.pss.ent.RouterCommandsE;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
import net.es.oscars.resv.dao.ConnectionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private Stringifier stringifier;

    @Autowired
    private Assembler assembler;

    @Autowired
    private TemplateRepository tpr;


    public void generateConfig(ConnectionE conn) {
        log.info("generating config");
        conn.getStates().setProv(ProvState.GENERATING);
        connRepo.save(conn);
        ReservedVlanFlowE rvf = conn.getReserved().getVlanFlow();

        Set<ReservedVlanJunctionE> rvj_set = new HashSet<>();
        rvj_set.addAll(rvf.getJunctions());
        Map<String, List<String>> isolatedJunctionCommands = isolatedJunctionCommands(rvj_set);

        Map<String, List<String>> ethCommands = new HashMap<>();
        Map<String, List<String>> mplsCommands = new HashMap<>();
        for (ReservedEthPipeE rep : rvf.getEthPipes()) {
            log.info("generating config for eth pipe "+rep.getId());
            Map<String, List<String>> thisPipeCommands = this.ethPipeCommands(rep);
            thisPipeCommands.forEach((urn, commands) -> {
                if (!ethCommands.keySet().contains(urn)) {
                    ethCommands.put(urn, new ArrayList<>());
                }
                ethCommands.get(urn).addAll(commands);

            });

        }

        for (ReservedMplsPipeE rep : rvf.getMplsPipes()) {
            log.info("generating config for mpls pipe "+rep.getId());
            Map<String, List<String>> thisPipeCommands  = this.mplsPipeCommands(rep);
            thisPipeCommands.forEach((urn, commands) -> {
                if (!mplsCommands.keySet().contains(urn)) {
                    mplsCommands.put(urn, new ArrayList<>());
                }
                mplsCommands.get(urn).addAll(commands);

            });
        }
        Map<String, List<String>> commandsByDevice = new HashMap<>();

        isolatedJunctionCommands.forEach((urn, commands) -> {
            log.info("adding isolated junction commands for "+urn);
            commandsByDevice.put(urn, commands);

        });

        ethCommands.forEach((urn, commands) -> {
            log.info("adding ethernet commands for "+urn);
            if (!commandsByDevice.keySet().contains(urn)) {
                commandsByDevice.put(urn, new ArrayList<>());
            }
            commandsByDevice.get(urn).addAll(commands);
        });

        mplsCommands.forEach((urn, commands) -> {
            log.info("adding mpls commands for "+urn);
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

    private Map<String, List<String>> isolatedJunctionCommands(Collection<ReservedVlanJunctionE> rvj_set) {
        Map<String, List<String>> result = new HashMap<>();
        for (ReservedVlanJunctionE rvj : rvj_set) {
            String deviceUrn = rvj.getDeviceUrn();
            log.info("generating junction commands for "+deviceUrn);
            if (!result.containsKey(deviceUrn)) {
                result.put(deviceUrn, new ArrayList<>());
            }
            UrnE device = topoService.device(rvj.getDeviceUrn());
            switch (device.getDeviceModel()) {
                case ALCATEL_SR7750:
                    result.get(deviceUrn).add(alcatelJunction(rvj));
                    break;
                case JUNIPER_EX:
                    result.get(deviceUrn).add(juniperExJunction(rvj));
                    break;
                case JUNIPER_MX:
                    result.get(deviceUrn).add(juniperMxJunction(rvj));

                    break;
            }
        }
        return result;

    }


    private Map<String, List<String>> ethPipeCommands(ReservedEthPipeE rep) {
        Set<ReservedVlanJunctionE> rvjs = new HashSet<>();
        rvjs.add(rep.getAJunction());
        rvjs.add(rep.getZJunction());
        // TODO: make this correct
        return isolatedJunctionCommands(rvjs);
    }

    private Map<String, List<String>> mplsPipeCommands(ReservedMplsPipeE rep) {
        Set<ReservedVlanJunctionE> rvjs = new HashSet<>();
        rvjs.add(rep.getAJunction());
        rvjs.add(rep.getZJunction());
        // TODO: make this correct
        return isolatedJunctionCommands(rvjs);
    }

    private String alcatelJunction(ReservedVlanJunctionE rvj) {
        // TODO: no samples
        String out = "Sample Alcatel router config for junction ; " + rvj.getDeviceUrn() + " \n";
        for (ReservedVlanFixtureE f : rvj.getFixtures()) {
            out += f.getIfceUrn();
            out += "  bw : " + f.getReservedBandwidth().getInBandwidth() + " / " + f.getReservedBandwidth().getEgBandwidth() + "\n";
            out += "  vlan : " + vlanString(f);
        }
        return out;

    }

    private String juniperMxJunction(ReservedVlanJunctionE rvj) {
        // TODO: no samples
        String out = "Sample Juniper MX router config for junction ; " + rvj.getDeviceUrn() + " \n";
        for (ReservedVlanFixtureE f : rvj.getFixtures()) {
            out += f.getIfceUrn();
            out += "  bw : " + f.getReservedBandwidth().getInBandwidth() + " / " + f.getReservedBandwidth().getEgBandwidth() + "\n";
            out += "  vlan : " + vlanString(f);
        }
        return out;
    }

    private String juniperExJunction(ReservedVlanJunctionE rvj) {
        // TODO: no samples
        String out = "Sample Juniper EX switch config for junction ; " + rvj.getDeviceUrn() + " \n";
        for (ReservedVlanFixtureE f : rvj.getFixtures()) {
            out += f.getIfceUrn();
            out += "  bw : " + f.getReservedBandwidth().getInBandwidth() + " / " + f.getReservedBandwidth().getEgBandwidth() + "\n";
            out += "  vlan : " + vlanString(f);
        }
        return out;
    }
    private String vlanString(ReservedVlanFixtureE f) {
        List<String> vlans = f.getReservedVlans()
                .stream()
                .map(ReservedVlanE::getVlan)
                .map(Object::toString)
                .collect(Collectors.toList());
        return String.join(",", vlans);
    }

}
