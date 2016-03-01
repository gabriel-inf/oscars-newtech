package net.es.oscars.ds.topo.ent;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.List;


@Data
@Entity
public class EIfce {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String name;

    private Integer reservableBw;

    @ElementCollection
    @CollectionTable
    private List<IntRange> reservableVlans;

    public EIfce() {

    }

    public EIfce(String name) {
        this.name = name;
    }

}
