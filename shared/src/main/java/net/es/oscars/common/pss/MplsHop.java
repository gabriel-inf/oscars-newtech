package net.es.oscars.common.pss;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;


@Data
@Builder
public class MplsHop {

    @NonNull
    private Integer order;

    @NonNull
    private String address;

}
