package net.es.oscars.dto.topo;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class VertexTree {
    private TopoVertex vertex;
    private Set<VertexTree> children = new HashSet<>();

    public VertexTree() {
        
    }
}
