package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedBlueprintE
{
    @Id
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @NonNull
    private ArchivedVlanFlowE vlanFlow;

    @NonNull
    private String containerConnectionId;       // Unique ID of the containing Connection
}
