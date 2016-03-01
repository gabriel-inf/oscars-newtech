package net.es.oscars.dto.topo;

import lombok.Data;
import net.es.oscars.common.topo.Layer;

import java.util.HashSet;
import java.util.Set;

@Data
public class Topology {
    public Layer layer;
    private Set<TopoEdge> edges = new HashSet<>();
    private Set<TopoVertex> vertices = new HashSet<>();

    public Topology() {
        
    }
}
