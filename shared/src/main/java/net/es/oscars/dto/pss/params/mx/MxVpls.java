package net.es.oscars.dto.pss.params.mx;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MxVpls {

    @NonNull
    private Integer vcId;

    @NonNull
    private List<MxIfce> ifces;

    @NonNull
    private String serviceName;

    @NonNull
    private String description;


    private String loopback;

    private String communityName;


    private String policyName;

    private Map<String, String> lspNeighbors;


}
