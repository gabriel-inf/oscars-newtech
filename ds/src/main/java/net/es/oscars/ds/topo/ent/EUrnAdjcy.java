package net.es.oscars.ds.topo.ent;

import lombok.Data;
import lombok.NonNull;
import net.es.oscars.common.topo.Layer;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    @ElementCollection
    private Map<Layer, Long> metrics = new HashMap<>();

    public EUrnAdjcy() {

    }

    public EUrnAdjcy(String a, String z) {
        this.a = a;
        this.z = z;
    }

}
