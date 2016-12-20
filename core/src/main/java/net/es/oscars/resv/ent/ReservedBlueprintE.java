package net.es.oscars.resv.ent;


import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservedBlueprintE {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @NonNull
    private ReservedVlanFlowE vlanFlow;

    @NonNull
    private String containerConnectionId;       // Unique ID of the containing Connection
}
