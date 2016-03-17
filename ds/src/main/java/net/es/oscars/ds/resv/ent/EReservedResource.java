package net.es.oscars.ds.resv.ent;

import lombok.*;
import net.es.oscars.common.resv.ResourceType;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EReservedResource {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @ElementCollection
    private List<String> urns;

    @NonNull
    private ResourceType resourceType;

    private String strResource;

    private Integer intResource;

    private Instant beginning;

    private Instant ending;


}
