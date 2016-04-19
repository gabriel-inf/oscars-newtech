package net.es.oscars.dto.topo;

import lombok.Data;

@Data
public class TopoEdge {
    private TopoVertex a;

    private TopoVertex z;

    private Long metric;

    private Layer layer;

    public TopoEdge() {

    }
    public TopoEdge(TopoVertex a, TopoVertex z) {
        this.a = a;
        this.z = z;

    }

}
