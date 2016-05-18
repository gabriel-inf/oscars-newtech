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
public class RequestedVlanJunctionE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    private UrnE deviceUrn;

    @NonNull
    private EthJunctionType junctionType;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<RequestedVlanFixtureE> fixtures;

    public static RequestedVlanJunctionE copyFrom(RequestedVlanJunctionE junction) {
        Set<RequestedVlanFixtureE> fixtures = new HashSet<>();

        junction.getFixtures().stream().forEach(f -> {
            RequestedVlanFixtureE copy = RequestedVlanFixtureE.builder()
                    .fixtureType(f.getFixtureType())
                    .egMbps(f.getEgMbps())
                    .inMbps(f.getInMbps())
                    .vlanExpression(f.getVlanExpression())
                    .portUrn(f.getPortUrn())
                    .build();
            fixtures.add(copy);
        });


        return RequestedVlanJunctionE.builder()
                .deviceUrn(junction.getDeviceUrn())
                .junctionType(junction.getJunctionType())
                .fixtures(fixtures)
                .build();


    }



}
