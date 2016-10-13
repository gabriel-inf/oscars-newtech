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
public class ReservedVlanFixtureE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String ifceUrn;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedVlanE> reservedVlans;

    @OneToOne(cascade = CascadeType.ALL)
    private ReservedBandwidthE reservedBandwidth;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedPssResourceE> reservedPssResources;

    @NonNull
    private EthFixtureType fixtureType;


}
