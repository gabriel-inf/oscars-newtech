package net.es.oscars.dto.pss.params.mx;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxVpls {

    private Integer vcId;

    private List<MxIfce> ifces;

    private String serviceName;

    private String description;


    private String loopback;

    private String communityName;


    private String policyName;

    private Map<String, String> lspNeighbors;


}
