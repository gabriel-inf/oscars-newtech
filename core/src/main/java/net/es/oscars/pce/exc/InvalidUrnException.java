package net.es.oscars.pce.exc;


import java.util.ArrayList;
import java.util.List;

public class InvalidUrnException extends PCEException {
    private List<String> badUrns = new ArrayList<>();
    public List<String> getBadUrns() {
        return badUrns;
    }

    public InvalidUrnException(String msg) {
        super (msg);
    }
    public InvalidUrnException(String msg, List<String> badUrns) {
        super (msg);
        this.badUrns = badUrns;
    }
    public InvalidUrnException() {super(); }
}
