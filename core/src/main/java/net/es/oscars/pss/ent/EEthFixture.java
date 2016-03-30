package net.es.oscars.pss.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthFixtureType;
import net.es.oscars.resv.ent.EReservedResource;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EEthFixture {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String portUrn;

    private EthFixtureType fixtureType;

    @NonNull
    private Integer vlanId;

    @OneToOne
    private EEthValve inValve;

    @OneToOne
    private EEthValve outValve;

    @ElementCollection
    private Set<String> resourceIds;

}
