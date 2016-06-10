package net.es.oscars.topo.beans;

import lombok.Data;
import net.es.oscars.topo.enums.Layer;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
public class Topology {
    public Layer layer;
    private Set<TopoEdge> edges = new HashSet<>();
    private Set<TopoVertex> vertices = new HashSet<>();

    public Topology() {
        
    }

    public Optional<TopoVertex> getVertexByUrn(String urn){
        return this.vertices.stream()
                .filter(v -> v.getUrn().equals(urn))
                .findFirst();
    }
}
