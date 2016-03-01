package net.es.oscars.st.oper;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum OperEvent {

    ADMIN_UP("ADMIN_UP"),
    ADMIN_DOWN("ADMIN_DOWN"),

    OPER_UP("OPER_UP"),
    OPER_DOWN("OPER_DOWN");


    private String code;

    OperEvent(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }


    private static final Map<String, OperEvent> lookup = new HashMap<String, OperEvent>();

    static {
        for (OperEvent pc : EnumSet.allOf(OperEvent.class)) {
            lookup.put(pc.getCode(), pc);
        }
    }

    public static OperEvent get(String code) {
        return lookup.get(code);
    }
}
