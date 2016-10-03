package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.dto.topo.enums.DeviceType;
import net.es.oscars.dto.topo.enums.IfceType;
import net.es.oscars.dto.topo.enums.UrnType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UrnE {

    public String toString() {
        return this.getUrn();
    }

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String urn;


    // TODO: validity periods? but this will do for now
    @NonNull
    private Boolean valid;


    @NonNull
    private UrnType urnType;

    // these may be null
    private DeviceModel deviceModel;

    private DeviceType deviceType;

    private IfceType ifceType;




    @OneToOne (cascade = CascadeType.ALL)
    private ReservableBandwidthE reservableBandwidth;

    @OneToOne (cascade = CascadeType.ALL)
    private ReservableVlanE reservableVlans;

    @OneToMany (cascade = CascadeType.ALL)
    private Set<ReservablePssResourceE> reservablePssResources;

    @ElementCollection
    @CollectionTable
    private Set<Layer> capabilities = new HashSet<>();


}
