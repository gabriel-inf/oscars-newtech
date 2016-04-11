package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.topo.enums.DeviceModel;


import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    private DeviceModel model;
    private DeviceType type;

    @ElementCollection
    @CollectionTable
    private List<EIntRange> reservableVlans;


    @OneToMany(cascade = CascadeType.ALL)
    @NonNull
    private Set<EIfce> ifces = new HashSet<>();

    public EDevice(String urn) {
        this.urn = urn;
    }

}
