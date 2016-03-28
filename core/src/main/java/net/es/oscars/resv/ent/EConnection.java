package net.es.oscars.resv.ent;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;

@Data
@Entity
public class EConnection {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String connectionId;
    
    @Embedded
    private EStates states;

    public EConnection(String connectionId) {
        this.connectionId = connectionId;
    }

    public EConnection() {

    }
}
