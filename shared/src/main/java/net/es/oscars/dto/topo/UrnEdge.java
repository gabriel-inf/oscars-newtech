package net.es.oscars.dto.topo;

import lombok.Data;
import net.es.oscars.common.topo.Layer;

import java.util.HashMap;
import java.util.Map;

@Data
public class UrnEdge {
    private String a;

    private String z;

    private Map<Layer, Long> metrics = new HashMap<>();

    public UrnEdge() {
        
    }
    public UrnEdge(String a, String z) {
        this.a = a;
        this.z = z;

    }

}
