package net.es.oscars.topo.pop;

import lombok.*;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.dto.topo.enums.IfceType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ifce {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String urn;

    private Integer reservableBw;
    private IfceType type = IfceType.PORT;

    @ElementCollection
    @CollectionTable
    private Set<IntRangeE> reservableVlans;


    @ElementCollection
    @CollectionTable
    private Set<Layer> capabilities = new HashSet<>();


}