package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
    private String urn;

    private Integer bandwidth;

    private Instant beginning;

    private Instant ending;


}
