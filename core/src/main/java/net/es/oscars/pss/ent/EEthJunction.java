package net.es.oscars.pss.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthJunctionType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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

    @NonNull
    private String deviceUrn;

    @NonNull
    private EthJunctionType junctionType;

    @OneToMany
    private Set<EEthFixture> fixtures;

}
