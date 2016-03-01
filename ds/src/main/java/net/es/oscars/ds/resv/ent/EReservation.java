package net.es.oscars.ds.resv.ent;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;

@Data
@Entity
public class EReservation {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String gri;
    
    @Embedded
    private EStates states;

    public EReservation(String gri) {
        this.gri = gri;
    }

    public EReservation() {

    }
}
