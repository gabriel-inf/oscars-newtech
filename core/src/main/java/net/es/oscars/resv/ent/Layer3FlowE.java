package net.es.oscars.resv.ent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Layer3FlowE {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany (cascade = CascadeType.ALL)
    private Set<Layer3JunctionE> junctions;

    @OneToMany (cascade = CascadeType.ALL)
    private Set<Layer3PipeE> pipes;


}
