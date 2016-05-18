package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.topo.ent.UrnE;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReservedVlanE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    private UrnE urn;

    private Integer vlan;

    private Instant beginning;

    private Instant ending;


}
