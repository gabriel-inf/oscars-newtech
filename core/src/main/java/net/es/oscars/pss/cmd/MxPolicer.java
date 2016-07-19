package net.es.oscars.pss.cmd;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class MxPolicer {

    @NonNull
    private String name;

    @NonNull
    private Integer mbps;

    @NonNull
    private Policing policing;
}
