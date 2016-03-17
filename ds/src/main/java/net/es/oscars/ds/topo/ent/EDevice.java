package net.es.oscars.ds.topo.ent;

import lombok.Data;
import lombok.NonNull;
import net.es.oscars.common.IntRange;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.ds.helpers.EIntRange;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class EDevice {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String urn;

    @ElementCollection
    @CollectionTable
    private Set<Layer> capabilities = new HashSet<>();

    private String model;
    private String vendor;
    private DeviceType type;

    @ElementCollection
    @CollectionTable
    private List<EIntRange> reservableVlans;


    @OneToMany
    @NonNull
    @Cascade(CascadeType.ALL)
    private Set<EIfce> ifces = new HashSet<>();

    public EDevice() {

    }


    public EDevice(String urn) {
        this.urn = urn;
    }

}
