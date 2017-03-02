package net.es.oscars.dto.pss.cmd;


import lombok.*;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.dto.pss.st.OperationalStatus;
import net.es.oscars.dto.pss.st.RollbackStatus;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandStatus {
    @NonNull
    private String device;
    @NonNull
    private CommandType type;

    @NonNull
    private String commands;
    @NonNull
    private String output;
    @NonNull
    private Date lastUpdated;

    private String connectionId;

    private LifecycleStatus lifecycleStatus;
    private ConfigStatus configStatus;
    private OperationalStatus operationalStatus;
    private RollbackStatus rollbackStatus;


}
