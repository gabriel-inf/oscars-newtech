package net.es.oscars.dto.resv;

import lombok.Data;
import lombok.NonNull;

@Data
public class Reservation {
    public Reservation() {

    }

    @NonNull
    private String gri;

    private States states;

}
