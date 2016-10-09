package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.topo.TopoEdge;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidirectionalPathE {

    @NonNull
    @ElementCollection
    private List<TopoEdge> azPath;

    @NonNull
    @ElementCollection
    private List<TopoEdge> zaPath;
}
