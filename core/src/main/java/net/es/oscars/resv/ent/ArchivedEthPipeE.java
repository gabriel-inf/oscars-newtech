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
public class ArchivedEthPipeE
{
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private ArchivedVlanJunctionE aJunction;

    @OneToOne(cascade = CascadeType.ALL)
    private ArchivedVlanJunctionE zJunction;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ArchivedBandwidthE> reservedBandwidths;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ArchivedVlanE> reservedVlans;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ArchivedPssResourceE> reservedPssResources;

    @NonNull
    @ElementCollection
    private List<String> azERO;

    @NonNull
    @ElementCollection
    private List<String> zaERO;

    @NonNull
    private EthPipeType pipeType;

    @NonNull
    private String uniqueID;
}
