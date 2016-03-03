package net.es.oscars.core.pss.ftl;

import lombok.Data;
import lombok.NonNull;


@Data
public class AluSdp {
    public AluSdp() {

    }

    @NonNull
    private Integer sdpId = 1;

    @NonNull
    private String lspName = "primary";

    @NonNull
    private String description = "primary";


    @NonNull
    private String farEnd = "loopback";
}
