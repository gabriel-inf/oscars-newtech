package net.es.oscars.ds.topo.ent;

import lombok.Data;
import lombok.NonNull;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;


@Data
@Entity
public class EUrnAdjcy {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String a;

    @NonNull
    private String z;

    @OneToMany
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<EMetric> metrics = new HashSet<>();

    public EUrnAdjcy() {

    }

    public EUrnAdjcy(String a, String z) {
        this.a = a;
        this.z = z;
    }

}
