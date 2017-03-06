package net.es.oscars.dto.pss.params.ex;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ExIfce {

    @NonNull
    private String port;

    @NonNull
    private String vlan_name;


}


