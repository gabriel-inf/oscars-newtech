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

    private VlanJunction aJunction;

    private VlanJunction zJunction;

    @NonNull
    private Integer azMbps;


    @NonNull
    private List<String> azERO;

    @NonNull
    private EthPipeType pipeType;

    private Set<String> resourceIds;

}
