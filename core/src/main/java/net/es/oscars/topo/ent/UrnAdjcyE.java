package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.dto.topo.Layer;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UrnAdjcyE {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @OneToOne (cascade = CascadeType.ALL)
    private UrnE a;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    private UrnE z;

    @ElementCollection
    private Map<Layer, Long> metrics = new HashMap<>();

}
