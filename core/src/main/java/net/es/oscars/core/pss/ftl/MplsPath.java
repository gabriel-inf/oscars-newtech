package net.es.oscars.core.pss.ftl;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
public class MplsPath {

    @NonNull
    private String name = "primary";

    @NonNull
    private List<MplsHop> hops = new ArrayList<>();
}
