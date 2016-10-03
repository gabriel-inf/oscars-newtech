package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    @ElementCollection
    private Set<String> urnBlacklist;

    @NonNull
    private EthPipeType pipeType;

    @NonNull
    private PalindromicType eroPalindromic;

    @NonNull
    private SurvivabilityType eroSurvivability;

    @NonNull
    private Integer numDisjoint;

    @NonNull
    private final String uniqueID = UUID.randomUUID().toString();
}
