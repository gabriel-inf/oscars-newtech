package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EJunction {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String deviceUrn;

    @OneToMany
    private Set<EFixture> fixtures;

}
