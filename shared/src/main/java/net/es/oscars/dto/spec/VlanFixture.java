package net.es.oscars.dto.spec;

import lombok.*;
import net.es.oscars.dto.pss.EthFixtureType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanFixture {
    private Long id;


    @NonNull
    private String portUrn;

    private Integer vlanId;

    private String vlanExpression;

    @NonNull
    private EthFixtureType fixtureType;

    @NonNull
    private Integer inMbps;

    @NonNull
    private Integer egMbps;

}
