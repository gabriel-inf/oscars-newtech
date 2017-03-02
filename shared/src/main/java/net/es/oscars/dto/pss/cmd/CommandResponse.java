package net.es.oscars.dto.pss.cmd;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.pss.st.OperationalStatus;
import net.es.oscars.dto.pss.st.RollbackStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandResponse {
    private String device;
    private String connectionId;
    private String commandId;


}
