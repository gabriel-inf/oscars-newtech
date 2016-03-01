package net.es.oscars.ds.topo.ent;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class IntRange {
    private Integer floor;
    private Integer ceiling;
    public IntRange() {

    }
}
