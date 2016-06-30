package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthPipeType;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

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
    private Set<ReservedPssResourceE> reservedPssResources;

    @NonNull
    @ElementCollection
    private List<String> azERO;

    @NonNull
    @ElementCollection
    private List<String> zaERO;

    @NonNull
    private EthPipeType pipeType;


}
