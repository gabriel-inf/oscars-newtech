package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestedBlueprintE {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany (cascade = CascadeType.ALL)
    @NonNull
    private Set<RequestedVlanFlowE> vlanFlows;

    @OneToMany (cascade = CascadeType.ALL)
    @NonNull
    private Set<Layer3FlowE> layer3Flows;


}
