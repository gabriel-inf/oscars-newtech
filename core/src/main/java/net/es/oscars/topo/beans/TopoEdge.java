package net.es.oscars.topo.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.topo.enums.Layer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopoEdge {
    private TopoVertex a;

    private TopoVertex z;

    private Long metric;

    private Layer layer;


}
