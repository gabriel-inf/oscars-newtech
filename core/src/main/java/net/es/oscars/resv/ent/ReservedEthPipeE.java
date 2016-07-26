package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthPipeType;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ReservedEthPipeE {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private ReservedVlanJunctionE aJunction;

    @OneToOne(cascade = CascadeType.ALL)
    private ReservedVlanJunctionE zJunction;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedBandwidthE> reservedBandwidths;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedVlanE> reservedVlans;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedPssResourceE> reservedPssResources;

    @NonNull
    @ElementCollection
    private List<String> azERO;

    @NonNull
    @ElementCollection
    private List<String> zaERO;

    @NonNull
    private EthPipeType pipeType;

    @NonNull
    private final String uniqueID = UUID.randomUUID().toString();
}
