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

    @OneToOne (cascade = CascadeType.ALL)
    @NonNull
    private RequestedVlanFlowE vlanFlow;

    @OneToOne (cascade = CascadeType.ALL)
    @NonNull
    private Layer3FlowE layer3Flow;


}
