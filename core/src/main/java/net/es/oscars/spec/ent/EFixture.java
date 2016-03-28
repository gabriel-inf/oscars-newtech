package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EFixture {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String portUrn;

    @NonNull
    private Integer vlanId;

    @OneToOne
    private EValve inValve;

    @OneToOne
    private EValve outValve;

}
