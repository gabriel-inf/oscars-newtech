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
    private Set<EJunction> junctions;

    @OneToMany
    private Set<EPipe> pipes;


}
