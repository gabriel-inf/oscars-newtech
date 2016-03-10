package net.es.oscars.common.plumb;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.es.oscars.dto.resv.ReservedComponent;

import java.util.Set;

@Data
@NoArgsConstructor
public class EthBridgePlumbing {

    @NonNull
    private String deviceUrn;

    @NonNull
    private Set<ReservedComponent> reserved;


}
