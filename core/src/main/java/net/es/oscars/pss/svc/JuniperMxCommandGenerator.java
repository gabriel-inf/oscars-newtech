package net.es.oscars.pss.svc;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.cmd.*;
import net.es.oscars.pss.dao.TemplateRepository;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
import net.es.oscars.resv.ent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j

public class JuniperMxCommandGenerator {

    @Autowired
    private Stringifier stringifier;

    @Autowired
    private Assembler assembler;

    @Autowired
    private TemplateRepository tpr;

    @Autowired
    private MiscHelper help;

    public String isolatedJunction(ReservedVlanJunctionE rvj, ConnectionE conn) throws PSSException {
        return "";

    }
    public String ethPipe(ReservedVlanJunctionE rvj, ReservedEthPipeE rep, List<String> ero) throws PSSException {
        return "";

    }
    public String mplsPipe(ConnectionE conn, ReservedVlanJunctionE from, ReservedVlanJunctionE to, ReservedMplsPipeE rmp, List<String> ero) throws PSSException {
        return "";

    }


}
