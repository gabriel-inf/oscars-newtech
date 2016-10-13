package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReservedVlanE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String urn;

    private Integer vlan;

    private Instant beginning;

    private Instant ending;


}
