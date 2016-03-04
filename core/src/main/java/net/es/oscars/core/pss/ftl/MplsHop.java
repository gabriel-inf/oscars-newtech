package net.es.oscars.core.pss.ftl;

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
