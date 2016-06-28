package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.topo.ent.UrnE;

import javax.persistence.*;
import java.util.HashSet;
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

    public static ReservedVlanJunctionE copyFrom(ReservedVlanJunctionE junction) {
        Set<ReservedVlanFixtureE> fixtures = new HashSet<>();

        junction.getFixtures().stream().forEach(f -> {
            ReservedVlanFixtureE copy = ReservedVlanFixtureE.builder()
                    .reservedBandwidth(f.getReservedBandwidth())
                    .reservedVlan(f.getReservedVlan())
                    .reservedPssResources(f.getReservedPssResources())
                    .fixtureType(f.getFixtureType())
                    .ifceUrn(f.getIfceUrn())
                    .build();
            fixtures.add(copy);
        });


        return ReservedVlanJunctionE.builder()
                .deviceUrn(junction.getDeviceUrn())
                .junctionType(junction.getJunctionType())
                .fixtures(fixtures)
                .reservedPssResources(junction.getReservedPssResources())
                .build();


    }

}

