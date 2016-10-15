package net.es.oscars.dto.spec;

import lombok.*;
import net.es.oscars.dto.pss.EthPipeType;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestedVlanPipe {

    private Long id;

    @NonNull
    private RequestedVlanJunction aJunction;

    @NonNull
    private RequestedVlanJunction zJunction;

    @NonNull
    private Integer azMbps;

    @NonNull
    private Integer zaMbps;

    @NonNull
    private List<String> azERO;

    @NonNull
    private List<String> zaERO;

    @NonNull
    private Set<String> urnBlacklist;

    @NonNull
    private EthPipeType pipeType;

    @NonNull
    private PalindromicType eroPalindromic;

    @NonNull
    private SurvivabilityType eroSurvivability;

    @NonNull
    private Integer numDisjoint;
}
