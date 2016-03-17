package net.es.oscars.common.plumb;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.es.oscars.dto.resv.ReservedResource;

import java.util.Set;

@Data
@NoArgsConstructor
public class EthBridgePlumbing {

    @NonNull
    private String deviceUrn;


    @NonNull
    private Set<ReservedResource> reservedResources;



}
