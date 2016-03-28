package net.es.oscars.topo.ent;

import lombok.Data;
import lombok.NonNull;
import net.es.oscars.common.topo.Layer;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;


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
