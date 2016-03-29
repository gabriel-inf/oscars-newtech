package net.es.oscars.pss.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthFixtureType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

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

}
