package net.es.oscars.spec.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthJunctionType;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanJunctionE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String deviceUrn;

    @NonNull
    private EthJunctionType junctionType;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<VlanFixtureE> fixtures;

    @ElementCollection
    private Set<String> resourceIds;


    public static VlanJunctionE copyFrom(VlanJunctionE junction) {
        Set<VlanFixtureE> fixtures = new HashSet<>();

        junction.getFixtures().stream().forEach(f -> {
            VlanFixtureE copy = VlanFixtureE.builder()
                    .fixtureType(f.getFixtureType())
                    .egMbps(f.getEgMbps())
                    .inMbps(f.getInMbps())
                    .vlanExpression(f.getVlanExpression())
                    .vlanId(f.getVlanId())
                    .portUrn(f.getPortUrn())
                    .build();
            fixtures.add(copy);
        });

        Set<String> resourceIds =  new HashSet<>();
        resourceIds.addAll(junction.getResourceIds());

        return VlanJunctionE.builder()
                .deviceUrn(junction.getDeviceUrn())
                .junctionType(junction.getJunctionType())
                .fixtures(fixtures)
                .resourceIds(resourceIds)
                .build();


    }



}
