package net.es.oscars.dto.pss.params.alu;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.es.oscars.dto.pss.params.Policing;


@Data
@Builder
public class AluQos {

    @NonNull
    private AluQosType type;

    @NonNull
    private Policing policing;

    @NonNull
    private Integer mbps;

    @NonNull
    private Integer policyId;

    @NonNull
    private String policyName;

    @NonNull
    private String description;

}
