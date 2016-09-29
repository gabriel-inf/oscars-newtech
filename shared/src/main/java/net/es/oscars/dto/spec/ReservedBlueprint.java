package net.es.oscars.dto.spec;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservedBlueprint {

    private Long id;

    @NonNull
    private ReservedVlanFlow vlanFlow;
}