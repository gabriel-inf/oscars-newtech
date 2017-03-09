package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthJunctionType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedVlanJunctionE
{
    @Id
    private Long id;

    @NonNull
    private String deviceUrn;

    @NonNull
    private EthJunctionType junctionType;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ArchivedVlanFixtureE> fixtures;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ArchivedPssResourceE> reservedPssResources;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ArchivedVlanE> reservedVlans;
}
