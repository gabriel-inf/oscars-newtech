package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlueprintE {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany (cascade = CascadeType.ALL)
    @NonNull
    private Set<VlanFlowE> vlanFlows;

    @OneToMany (cascade = CascadeType.ALL)
    @NonNull
    private Set<Layer3FlowE> layer3Flows;


}
