package net.es.oscars.dto.spec;

import lombok.*;
import net.es.oscars.dto.pss.EthJunctionType;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanJunction {

    private Long id;

    @NonNull
    private String deviceUrn;

    @NonNull
    private EthJunctionType junctionType;

    private Set<VlanFixture> fixtures;

    private Set<String> resourceIds;

}
