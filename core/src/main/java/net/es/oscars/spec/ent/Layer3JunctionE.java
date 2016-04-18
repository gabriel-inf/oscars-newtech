package net.es.oscars.spec.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.Layer3JunctionType;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Layer3JunctionE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String deviceUrn;

    @NonNull
    private Layer3JunctionType junctionType;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Layer3FixtureE> fixtures;

    @ElementCollection
    private Set<String> resourceIds;

}
