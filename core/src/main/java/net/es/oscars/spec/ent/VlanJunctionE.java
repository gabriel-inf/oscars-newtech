package net.es.oscars.spec.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthJunctionType;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanJunctionE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String deviceUrn;

    @NonNull
    private EthJunctionType junctionType;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<VlanFixtureE> fixtures;

    @ElementCollection
    private Set<String> resourceIds;

}
