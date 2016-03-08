package net.es.oscars.ds.spec.ent;

import lombok.*;
import net.es.oscars.ds.resv.ent.EStates;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESpecification {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String specificationId;

    private Instant submitted;
    private Instant reserveBegin;
    private Instant reserveEnd;

    private Integer mbps;
    private Integer vlanId;
    private String aUrn;
    private String zUrn;

    private String username;

}
