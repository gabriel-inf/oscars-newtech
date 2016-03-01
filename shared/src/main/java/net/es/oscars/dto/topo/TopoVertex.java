package net.es.oscars.dto.topo;

import lombok.Data;

@Data
public class TopoVertex {
    private String urn;

    public TopoVertex() {
        
    }
    public TopoVertex(String urn) {
        this.urn = urn;
    }
}
