package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestedVlanFlowE {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany (cascade = CascadeType.ALL)
    private Set<RequestedVlanJunctionE> junctions;

    @OneToMany (cascade = CascadeType.ALL)
    private Set<RequestedVlanPipeE> pipes;

    @NonNull
    private Integer minPipes;

    @NonNull
    private Integer maxPipes;

    @NonNull
    private String containerConnectionId;       // Unique ID of the containing Connection
}
