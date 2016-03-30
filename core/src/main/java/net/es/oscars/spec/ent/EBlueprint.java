package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EBlueprint {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany
    @NonNull
    private Set<EFlow> flows;

}
