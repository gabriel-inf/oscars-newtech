package net.es.oscars.dto.topo;

import lombok.Data;
import lombok.NonNull;
import net.es.oscars.common.topo.Layer;

@Data
public class Metric {

    @NonNull
    private Layer layer;

    @NonNull
    private Long value;

    public Metric() {

    }
}
