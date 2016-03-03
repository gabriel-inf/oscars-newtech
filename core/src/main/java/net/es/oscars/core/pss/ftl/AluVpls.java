package net.es.oscars.core.pss.ftl;

import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class AluVpls {
    public AluVpls() {

    }
    @NonNull
    private Integer vcId;

    @NonNull
    private List<AluSap> saps = new ArrayList<>();

    @NonNull
    private String serviceName;

    @NonNull
    private String description;

    @NonNull
    private boolean endpoint;

    private String endpointName;

    @NonNull
    private boolean hasProtect;

    private AluSdp sdp;

    private Integer protectVcId;

    private AluSdp protectSdp;



}
