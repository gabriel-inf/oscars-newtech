package net.es.oscars.dto.spec;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestedBlueprint {

    private Long id;

    @NonNull
    private RequestedVlanFlow vlanFlow;

    @NonNull
    private Layer3Flow layer3Flow;
}