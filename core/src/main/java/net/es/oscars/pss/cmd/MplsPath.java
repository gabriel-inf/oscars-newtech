package net.es.oscars.pss.cmd;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;


@Data
@Builder
public class MplsPath {

    @NonNull
    private String name;

    @NonNull
    private List<MplsHop> hops;
}
