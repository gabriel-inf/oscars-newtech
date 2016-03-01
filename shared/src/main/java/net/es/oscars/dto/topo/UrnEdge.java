package net.es.oscars.dto.topo;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UrnEdge {
    private String a;

    private String z;

    private Set<Metric> metrics = new HashSet<>();

    public UrnEdge() {
        
    }
    public UrnEdge(String a, String z) {
        this.a = a;
        this.z = z;

    }

}
