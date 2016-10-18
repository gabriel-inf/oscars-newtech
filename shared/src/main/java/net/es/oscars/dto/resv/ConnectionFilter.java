package net.es.oscars.dto.resv;

import lombok.*;
import net.es.oscars.dto.spec.ReservedBlueprint;
import net.es.oscars.dto.spec.Specification;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;

import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionFilter {

    private String connectionId;

    private Set<ResvState> resvStates;


}
