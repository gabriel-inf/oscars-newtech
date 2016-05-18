package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.topo.ent.UrnE;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservedVlanJunctionE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    private UrnE deviceUrn;

    @NonNull
    private EthJunctionType junctionType;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedVlanFixtureE> fixtures;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedPssResourceE> reservedPssResources;


}

