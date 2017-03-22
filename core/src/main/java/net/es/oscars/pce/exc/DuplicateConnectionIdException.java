package net.es.oscars.pce.exc;


import java.util.ArrayList;
import java.util.List;

public class DuplicateConnectionIdException extends PCEException {

    public DuplicateConnectionIdException(String msg) {
        super (msg);
    }
    public DuplicateConnectionIdException() {super(); }
}
