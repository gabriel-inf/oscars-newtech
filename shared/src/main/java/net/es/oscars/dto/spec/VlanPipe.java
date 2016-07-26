package net.es.oscars.dto.spec;

import lombok.*;
import net.es.oscars.dto.pss.EthPipeType;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanPipe {

    private Long id;

    @NonNull
    private VlanJunction aJunction;

    @NonNull
    private VlanJunction zJunction;

    @NonNull
    private Integer azMbps;

    @NonNull
    private Integer zaMbps;

    @NonNull
    private List<String> azERO;

    @NonNull
    private List<String> zaERO;

    @NonNull
    private EthPipeType pipeType;

    @NonNull
    private PalindromicType palindromic;

    private Set<String> resourceIds;

}
