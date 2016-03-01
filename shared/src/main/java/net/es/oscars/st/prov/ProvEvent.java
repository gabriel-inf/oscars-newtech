package net.es.oscars.st.prov;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ProvEvent {

    SWITCH_TO_AUTO("SWITCH_TO_AUTO"),
    SWITCH_TO_MANUAL("SWITCH_TO_MANUAL"),

    BUILD_TIME_REACHED("BUILD_TIME_REACHED"),
    DISMANTLE_TIME_REACHED("DISMANTLE_TIME_REACHED"),

    BUILD_COMMAND("BUILD_COMMAND"),
    DISMANTLE_COMMAND("DISMANTLE_COMMAND"),

    BUILD_OK("BUILD_OK"),
    BUILD_FL("BUILD_FL"),

    DISMANTLE_OK("DISMANTLE_OK"),
    DISMANTLE_FL("DISMANTLE_FL");



    private String code;

    ProvEvent(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }


    private static final Map<String, ProvEvent> lookup = new HashMap<String, ProvEvent>();

    static {
        for (ProvEvent pc : EnumSet.allOf(ProvEvent.class)) {
            lookup.put(pc.getCode(), pc);
        }
    }

    public static Optional<ProvEvent> get(String code) {
        Optional<ProvEvent> result;
        if (lookup.containsKey(code)) {
            return Optional.of(lookup.get(code));
        } else {
            return Optional.empty();
        }
    }
}
