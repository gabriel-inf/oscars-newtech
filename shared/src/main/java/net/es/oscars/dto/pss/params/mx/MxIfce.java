package net.es.oscars.dto.pss.params.mx;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class MxIfce {

    @NonNull
    private String port;

    @NonNull
    private Integer vlan;

    @NonNull
    private String description;


}
