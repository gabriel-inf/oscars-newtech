package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthFixtureType;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestedVlanFixtureE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String portUrn;

    private String vlanExpression;

    @NonNull
    private EthFixtureType fixtureType;

    @NonNull
    private Integer inMbps;

    @NonNull
    private Integer egMbps;

}
