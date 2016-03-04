package net.es.oscars.common.resv;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum IdentifierType {
    VC_ID("VC_ID"),
    ALU_INGRESS_POLICY_ID("ALU_INGRESS_POLICY_ID"),
    ALU_EGRESS_POLICY_ID("ALU_EGRESS_POLICY_ID"),
    ALU_SDP_ID("ALU_SDP_ID"),
    ALU_MPLS_LSP_NAME("ALU_MPLS_LSP_NAME"),
    ALU_MPLS_PATH_NAME("ALU_MPLS_PATH_NAME"),
    IPV4_ADDRESS("IPV4_ADDRESS"),
    INTERFACE_NAME("INTERFACE_NAME"),
    VLAN("VLAN");

    private String code;

    IdentifierType(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }


    private static final Map<String, IdentifierType> lookup = new HashMap<String, IdentifierType>();

    static {
        for (IdentifierType pc : EnumSet.allOf(IdentifierType.class)) {
            lookup.put(pc.getCode(), pc);
        }
    }

    public static Optional<IdentifierType> get(String code) {
        Optional<IdentifierType> result;
        if (lookup.containsKey(code)) {
            return Optional.of(lookup.get(code));
        } else {
            return Optional.empty();
        }
    }

}
