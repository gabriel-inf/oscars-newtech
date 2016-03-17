package net.es.oscars.common.plumb;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.es.oscars.dto.resv.ReservedResource;

import java.util.Set;

@Data
@Builder
public class VplsPlumbing {

    @NonNull
    private String deviceUrn;


    @NonNull
    private Set<ReservedResource> reservedResources;




}
