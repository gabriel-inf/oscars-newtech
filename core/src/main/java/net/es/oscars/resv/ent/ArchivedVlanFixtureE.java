package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthFixtureType;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedVlanFixtureE
{
    @Id
    private Long id;

    @NonNull
    private String ifceUrn;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ArchivedVlanE> reservedVlans;

    @OneToOne(cascade = CascadeType.ALL)
    private ArchivedBandwidthE reservedBandwidth;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ArchivedPssResourceE> reservedPssResources;

    @NonNull
    private EthFixtureType fixtureType;
}
