package net.es.oscars.dto.pss.params.alu;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
public class AluVpls {

    @NonNull
    private Integer vcId;

    @NonNull
    private List<AluSap> saps;

    @NonNull
    private String serviceName;

    @NonNull
    private String description;

    private String endpointName;

    private AluSdp sdp;

    private Integer protectVcId;

    private AluSdp protectSdp;



}
