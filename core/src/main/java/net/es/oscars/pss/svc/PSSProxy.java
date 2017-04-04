package net.es.oscars.pss.svc;


import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.GenerateResponse;

public interface PSSProxy {

    CommandResponse submitCommand(Command cmd);

    GenerateResponse generate(Command cmd);

    CommandStatus status(String commandId);

}
