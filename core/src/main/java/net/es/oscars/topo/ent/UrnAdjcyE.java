package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.topo.enums.Layer;

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
    @ManyToOne
    private UrnE a;

    @NonNull
    @ManyToOne
    private UrnE z;

    @ElementCollection
    private Map<Layer, Long> metrics = new HashMap<>();

}
