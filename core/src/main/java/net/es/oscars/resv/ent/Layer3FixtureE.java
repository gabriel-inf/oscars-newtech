package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.Layer3FixtureType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Layer3FixtureE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String portUrn;

    private Integer vlanId;

    private String vlanExpression;

    @NonNull
    private Layer3FixtureType fixtureType;

    @NonNull
    private Integer inMbps;

    @NonNull
    private Integer egMbps;

}
