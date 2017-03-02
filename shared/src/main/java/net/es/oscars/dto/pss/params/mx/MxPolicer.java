package net.es.oscars.dto.pss.params.mx;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.es.oscars.dto.pss.params.Policing;

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
