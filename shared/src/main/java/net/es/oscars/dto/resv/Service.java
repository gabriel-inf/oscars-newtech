package net.es.oscars.dto.resv;

import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
public class Service {
    public Service() {

    }

    @NonNull
    private String serviceId;

    private List<Connection> connections = new ArrayList<>();

}
