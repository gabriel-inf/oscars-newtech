package net.es.oscars.dto.resv;

import lombok.Data;
import lombok.NonNull;


@Data
public class Connection {
    public Connection() {

    }

    @NonNull
    private String connectionId;


    private States states;

}
