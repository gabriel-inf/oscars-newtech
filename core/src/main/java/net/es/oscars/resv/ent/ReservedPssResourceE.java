package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.resv.ResourceType;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReservedPssResourceE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String urn;

    @NonNull
    private ResourceType resourceType;

    private Integer resource;

    private Instant beginning;

    private Instant ending;
}
