package net.es.oscars.dto.spec;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanFlow {
    private Long id;


    private Set<VlanJunction> junctions;

    private Set<VlanPipeB> pipes;


}
