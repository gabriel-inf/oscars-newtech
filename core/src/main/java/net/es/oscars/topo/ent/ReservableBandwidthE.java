package net.es.oscars.topo.ent;


import lombok.*;
import net.es.oscars.topo.ent.UrnE;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity

public class ReservableBandwidthE {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @OneToOne
    private UrnE urn;


    private Integer bandwidth;

    private Integer ingressBw;

    private Integer egressBw;

}
