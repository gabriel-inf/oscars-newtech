package net.es.oscars.pss.cmd;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
