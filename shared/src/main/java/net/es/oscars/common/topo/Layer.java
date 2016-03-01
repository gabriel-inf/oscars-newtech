package net.es.oscars.common.topo;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Layer {
    LAYER_ONE("LAYER_ONE"),
    ETHERNET("ETHERNET"),
    MPLS("MPLS");

    private String code;

    Layer(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }


    private static final Map<String, Layer> lookup = new HashMap<String, Layer>();

    static {
        for (Layer pc : EnumSet.allOf(Layer.class)) {
            lookup.put(pc.getCode(), pc);
        }
    }

    public static Layer get(String code) {
        return lookup.get(code);
    }

}
