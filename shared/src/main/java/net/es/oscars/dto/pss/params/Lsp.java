package net.es.oscars.dto.pss.params;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;


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
