package net.es.oscars.core.pss.ftl;

import lombok.Data;
import lombok.NonNull;

@Data
public class AluSap {
    public AluSap() {

    }
    @NonNull
    private String port;
    @NonNull
    private Integer vlan;
    @NonNull
    private Integer ingressQosId;
    @NonNull
    private Integer egressQosId;
    @NonNull
    private String description;



}
