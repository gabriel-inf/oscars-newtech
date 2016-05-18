package net.es.oscars.topo.ent;


import lombok.*;
import net.es.oscars.dto.resv.ResourceType;

import javax.persistence.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ReservablePssResourceE {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @ManyToOne
    private UrnE urn;

    @NonNull
    private ResourceType type;

    @ElementCollection
    @CollectionTable
    private Set<IntRangeE> reservableRanges;

}
