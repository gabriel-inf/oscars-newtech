package net.es.oscars.pss.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthPipeType;
import net.es.oscars.resv.ent.EReservedResource;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EEthPipe {

    @Id
    @GeneratedValue
    private Long id;

    private String aJunctionId;

    private String zJunctionId;

    @OneToOne
    private EEthValve azValve;

    @NonNull
    private EthPipeType pipeType;

    @NonNull
    @ElementCollection
    private List<String> azPath;

    @ElementCollection
    private Set<String> resourceIds;

}
