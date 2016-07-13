package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.topo.enums.PalindromicType;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestedVlanPipeE {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private RequestedVlanJunctionE aJunction;

    @OneToOne(cascade = CascadeType.ALL)
    private RequestedVlanJunctionE zJunction;

    @NonNull
    private Integer azMbps;

    @NonNull
    private Integer zaMbps;

    @NonNull
    @ElementCollection
    private List<String> azERO;

    @NonNull
    @ElementCollection
    private List<String> zaERO;

    @NonNull
    private EthPipeType pipeType;

    @NonNull
    private PalindromicType eroPalindromic;
}
