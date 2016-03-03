package net.es.oscars.core.pss.ftl;

import lombok.Data;
import lombok.NonNull;


@Data
public class AluQos {
    public AluQos() {

    }

    @NonNull
    private AluQosType type = AluQosType.SAP_INGRESS;

    @NonNull
    private Policing policing = Policing.STRICT;
    @NonNull
    private Integer mbps  = 1;
    @NonNull
    private Integer policyId = 0;
    @NonNull
    private String policyName = "default name";
    @NonNull
    private String description = "default description";

}
