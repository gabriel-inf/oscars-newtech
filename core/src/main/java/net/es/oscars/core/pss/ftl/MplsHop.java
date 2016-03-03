package net.es.oscars.core.pss.ftl;

import lombok.Data;
import lombok.NonNull;


@Data
public class MplsHop {
    public MplsHop() {

    }

    @NonNull
    private Integer order = 0;
    @NonNull
    private String address = "protect";

}
