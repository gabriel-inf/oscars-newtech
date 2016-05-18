package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.topo.ent.UrnE;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReservedBandwidthE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL)
    private UrnE urn;

    private Integer bandwidth;

    private Integer inBandwidth;

    private Integer egBandwidth;


    private Instant beginning;

    private Instant ending;


}
