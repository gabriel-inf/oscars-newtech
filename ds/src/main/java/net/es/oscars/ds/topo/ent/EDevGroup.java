package net.es.oscars.ds.topo.ent;

import lombok.Data;
import lombok.NonNull;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
@Entity
@Audited
public class EDevGroup {

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

    public EDevGroup(String name, Set<EDevice> devices) {
        this.name = name;
        this.devices = devices;
    }

    public Optional<EDevice> byName(String devName) {
        for (EDevice dev :devices) {
            if (dev.getName().equals(devName)) {
                return Optional.of(dev);
            }
        }
        return Optional.empty();
    }

    public EDevGroup(String name) {
        this.name = name;
    }

    public EDevGroup() {

    }

}
