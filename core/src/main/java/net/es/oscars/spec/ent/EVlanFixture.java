package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EVlanFixture {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String portUrn;

    private Integer vlanId;

    private String vlanExpression;

    @NonNull
    private Integer inMbps;

    @NonNull
    private Integer egMbps;

}
