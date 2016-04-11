package net.es.oscars.spec.ent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany (cascade = CascadeType.ALL)
    private Set<EVlanJunction> junctions;

    @OneToMany (cascade = CascadeType.ALL)
    private Set<EVlanPipe> pipes;


}
