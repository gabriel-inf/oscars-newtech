package net.es.oscars.common.resv;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ResourceType {
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

    ResourceType(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }


    private static final Map<String, ResourceType> lookup = new HashMap<String, ResourceType>();

    static {
        for (ResourceType pc : EnumSet.allOf(ResourceType.class)) {
            lookup.put(pc.getCode(), pc);
        }
    }

    public static Optional<ResourceType> get(String code) {
        Optional<ResourceType> result;
        if (lookup.containsKey(code)) {
            return Optional.of(lookup.get(code));
        } else {
            return Optional.empty();
        }
    }

}
