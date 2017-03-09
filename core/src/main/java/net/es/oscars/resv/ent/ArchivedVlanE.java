package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ArchivedVlanE
{
    @Id
    private Long id;

    @NonNull
    private String urn;

    private Integer vlan;

    private Instant beginning;

    private Instant ending;
}
