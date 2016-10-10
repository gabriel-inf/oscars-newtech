package net.es.oscars.topo.ent;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidirectionalPathE {

    @Id
    @GeneratedValue
    private Long id;

    @ElementCollection
    @CollectionTable
    private List<EdgeE> azPath;

    @ElementCollection
    @CollectionTable
    private List<EdgeE> zaPath;
}
