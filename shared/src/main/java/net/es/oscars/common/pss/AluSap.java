package net.es.oscars.common.pss;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class AluSap {

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
