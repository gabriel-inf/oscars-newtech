package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class JuniperExCommandGenerator {

    @Autowired
    private MiscHelper help;


    public String isolatedJunction(ReservedVlanJunctionE rvj, ConnectionE conn) throws PSSException {
        return "";

    }
    public String ethPipe(ReservedVlanJunctionE rvj, ReservedEthPipeE rep, List<String> ero) throws PSSException {
        return "";

    }
    public String mplsPipe(ConnectionE conn, ReservedVlanJunctionE from, ReservedVlanJunctionE to,
                           ReservedMplsPipeE rmp, List<String> ero) throws PSSException {
        throw new PSSException("MPLS not supported on Juniper EX platform");

    }

}
