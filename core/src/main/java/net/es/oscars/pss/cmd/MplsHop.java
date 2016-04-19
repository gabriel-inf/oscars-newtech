package net.es.oscars.pss.cmd;

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
