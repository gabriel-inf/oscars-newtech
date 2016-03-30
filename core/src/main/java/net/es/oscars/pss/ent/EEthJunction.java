package net.es.oscars.pss.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthJunctionType;
import net.es.oscars.resv.ent.EReservedResource;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EEthJunction {

    @Id
    @GeneratedValue
    private Long id;

    private String junctionId;

    @NonNull
    private String deviceUrn;

    @NonNull
    private EthJunctionType junctionType;

    @OneToMany
    private Set<EEthFixture> fixtures;

    @ElementCollection
    private Set<String> resourceIds;

}
