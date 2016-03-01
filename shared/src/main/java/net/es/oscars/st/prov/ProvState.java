package net.es.oscars.st.prov;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ProvState {


    INITIAL("INITIAL"),

    DISMANTLED_MANUAL("DISMANTLED_MANUAL"),
    DISMANTLED_AUTO("DISMANTLED_AUTO"),

    BUILDING_MANUAL("BUILDING_MANUAL"),
    BUILDING_AUTO("BUILDING_AUTO"),

    BUILT_MANUAL("BUILT_MANUAL"),
    BUILT_AUTO("BUILT_AUTO"),

    DISMANTLING_MANUAL("DISMANTLING_MANUAL"),
    DISMANTLING_AUTO("DISMANTLING_AUTO"),

    FAILED("FAILED");

    private String code;

    ProvState(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }


    private static final Map<String, ProvState> lookup = new HashMap<String, ProvState>();

    static {
        for (ProvState pc : EnumSet.allOf(ProvState.class)) {
            lookup.put(pc.getCode(), pc);
        }
    }

    public static Optional<ProvState> get(String code) {
        Optional<ProvState> result;
        if (lookup.containsKey(code)) {
            return Optional.of(lookup.get(code));
        } else {
            return Optional.empty();
        }
    }
}
