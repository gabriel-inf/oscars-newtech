package net.es.oscars.common.pss;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
public class Lsp {

    @NonNull
    private String name;

    @NonNull
    private String pathName;

    @NonNull
    private Integer setupPriority;

    @NonNull
    private Integer holdPriority;

    @NonNull
    private Integer metric;

    @NonNull
    private String to;
}
