package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.topo.ent.BidirectionalPathE;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservedVlanFlowE {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedVlanJunctionE> junctions;

    @OneToMany (cascade = CascadeType.ALL)
    private Set<ReservedEthPipeE> ethPipes;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedMplsPipeE> mplsPipes;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<BidirectionalPathE> allPaths;

    @NonNull
    private String containerConnectionId;       // Unique ID of the containing Connection

}
