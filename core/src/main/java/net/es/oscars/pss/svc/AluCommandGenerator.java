package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.resv.ent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class AluCommandGenerator {

    @Autowired
    private UrnAddressRepository addrRepo;

    @Autowired
    private MiscHelper help;



    private Optional<Integer> aluFixtureInQos(ReservedVlanFixtureE rvf) {
        return help.pssResourceOfType(rvf.getReservedPssResources(), ResourceType.ALU_INGRESS_POLICY_ID);

    }

    private Optional<Integer> aluFixtureEgQos(ReservedVlanFixtureE rvf) {
        return help.pssResourceOfType(rvf.getReservedPssResources(), ResourceType.ALU_EGRESS_POLICY_ID);
    }

    public String isolatedJunction(ReservedVlanJunctionE rvj, ConnectionE conn) throws PSSException {
       return "";

    }
    public String ethPipe(ReservedVlanJunctionE rvj, ReservedEthPipeE rep, List<String> ero) throws PSSException {
        return "";

    }
    public String mplsPipe(ConnectionE conn, ReservedVlanJunctionE from, ReservedVlanJunctionE to,
                           ReservedMplsPipeE rmp, List<String> ero) throws PSSException {
        return "";



    }



}
