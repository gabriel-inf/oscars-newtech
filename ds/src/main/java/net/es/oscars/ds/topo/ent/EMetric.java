package net.es.oscars.ds.topo.ent;

import lombok.Data;
import lombok.NonNull;
import net.es.oscars.common.topo.Layer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
public class EMetric {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private Layer layer;

    @NonNull
    private Long value;

    public EMetric() {

    }

}
