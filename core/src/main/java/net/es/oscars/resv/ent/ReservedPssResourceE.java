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
public class ReservedPssResourceE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @ManyToOne
    private UrnE urn;

    @NonNull
    private ResourceType resourceType;

    private Integer resource;

    private Instant beginning;

    private Instant ending;


}
