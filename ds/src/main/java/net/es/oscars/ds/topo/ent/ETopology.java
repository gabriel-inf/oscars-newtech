package net.es.oscars.ds.topo.ent;

import lombok.Data;
import lombok.NonNull;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
@Entity
public class ETopology {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String name;

    @NonNull
    @OneToMany
    @Cascade(CascadeType.ALL)
    private Set<EDevice> devices = new HashSet<>();

    @OneToMany
    @NonNull
    @Cascade(CascadeType.ALL)
    private Set<EUrnAdjcy> adjcies = new HashSet<>();

    public ETopology(String name, Set<EDevice> devices) {
        this.name = name;
        this.devices = devices;
    }

    public Optional<EDevice> byUrn(String devUrn) {
        for (EDevice dev :devices) {
            if (dev.getUrn().equals(devUrn)) {
                return Optional.of(dev);
            }
        }
        return Optional.empty();
    }

    public ETopology(String name) {
        this.name = name;
    }

    public ETopology() {

    }

}
