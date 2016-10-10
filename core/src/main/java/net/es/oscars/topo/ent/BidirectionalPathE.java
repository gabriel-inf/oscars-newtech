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
    @OneToMany (cascade = CascadeType.ALL)
    private List<EdgeE> azPath;

    @ElementCollection
    @OneToMany (cascade = CascadeType.ALL)
    private List<EdgeE> zaPath;
}
