package net.es.oscars.pce.exc;


import java.util.ArrayList;
import java.util.List;

public class VlanNotFoundException extends PCEException {
    private List<String> badUrns = new ArrayList<>();
    public List<String> getBadUrns() {
        return badUrns;
    }

    public VlanNotFoundException(String msg) {
        super (msg);
    }
    public VlanNotFoundException(String msg, List<String> badUrns) {
        super (msg);
        this.badUrns = badUrns;
    }
    public VlanNotFoundException() {super(); }
}
