package net.es.oscars.dto.spec;

import lombok.*;
import net.es.oscars.dto.pss.MplsPipeType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservedMplsPipe {
    private Long id;

    private ReservedVlanJunction aJunction;

    private ReservedVlanJunction zJunction;

    private Set<ReservedBandwidth> reservedBandwidths;

    private Set<ReservedPssResource> reservedPssResources;

    @NonNull
    private List<String> azERO;

    @NonNull
    private List<String> zaERO;

    @NonNull
    private MplsPipeType pipeType;

    @NonNull
    private final String uniqueID = UUID.randomUUID().toString();
}
