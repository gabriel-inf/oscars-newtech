package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EFlow {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany
    @NonNull
    private Set<EJunction> junctions;

    @OneToMany
    @NonNull
    private Set<EPipe> pipes;

    @OneToMany
    @NonNull
    private Set<EValve> valves;

}
