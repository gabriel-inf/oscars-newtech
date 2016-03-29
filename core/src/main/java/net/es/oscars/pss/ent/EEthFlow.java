package net.es.oscars.pss.ent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class EEthFlow {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany
    private Set<EEthJunction> junctions;

    @OneToMany
    private Set<EEthPipe> pipes;


}
