package net.es.oscars.dto.resv;

import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class Connection {
    public Connection() {

    }

    @NonNull
    private String connectionId;

    private List<ReservedComponent> components = new ArrayList<>();

    private States states;

}
