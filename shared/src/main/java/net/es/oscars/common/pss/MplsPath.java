package net.es.oscars.common.pss;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
public class MplsPath {

    @NonNull
    private String name;

    @NonNull
    private List<MplsHop> hops;
}
