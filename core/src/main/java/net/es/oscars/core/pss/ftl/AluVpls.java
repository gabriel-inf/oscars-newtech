package net.es.oscars.core.pss.ftl;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class AluVpls {

    @NonNull
    private Integer vcId;

    @NonNull
    private List<AluSap> saps = new ArrayList<>();

    @NonNull
    private String serviceName;

    @NonNull
    private String description;

    private Optional<String> endpointName;

    private Optional<AluSdp> sdp;

    private Optional<Integer> protectVcId;

    private Optional<AluSdp> protectSdp;



}
