package net.es.oscars.core.pss.ftl;

import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;


@Data
public class Lsp {
    public Lsp() {

    }

    @NonNull
    private String name = "primary";

    @NonNull
    private String pathName = "primary";

    @NonNull
    private Integer setupPriority;

    @NonNull
    private Integer holdPriority;

    @NonNull
    private Integer metric;

    @NonNull
    private String to = "loopback";
}
