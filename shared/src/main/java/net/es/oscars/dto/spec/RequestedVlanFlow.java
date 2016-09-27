package net.es.oscars.dto.spec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestedVlanFlow {
    private Long id;


    private Set<RequestedVlanJunction> junctions;

    private Set<RequestedVlanPipe> pipes;


}
