package net.es.oscars.common.resv;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ComponentType {
    PATH("PATH"),
    ETHERNET_VLAN("ETHERNET_VLAN");

    private String code;

    ComponentType(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }


    private static final Map<String, ComponentType> lookup = new HashMap<String, ComponentType>();

    static {
        for (ComponentType pc : EnumSet.allOf(ComponentType.class)) {
            lookup.put(pc.getCode(), pc);
        }
    }

    public static Optional<ComponentType> get(String code) {
        Optional<ComponentType> result;
        if (lookup.containsKey(code)) {
            return Optional.of(lookup.get(code));
        } else {
            return Optional.empty();
        }
    }

}
