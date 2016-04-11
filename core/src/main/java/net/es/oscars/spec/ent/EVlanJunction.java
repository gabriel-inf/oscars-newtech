package net.es.oscars.spec.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthJunctionType;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EVlanJunction {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String deviceUrn;

    @NonNull
    private EthJunctionType junctionType;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<EVlanFixture> fixtures;

    @ElementCollection
    private Set<String> resourceIds;

}
