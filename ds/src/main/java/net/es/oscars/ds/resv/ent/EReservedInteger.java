package net.es.oscars.ds.resv.ent;

import lombok.*;
import net.es.oscars.common.resv.IReserved;
import net.es.oscars.common.resv.ResourceType;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EReservedInteger implements IReserved<Integer> {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String urn;

    @NonNull
    private ResourceType resourceType;

    @NonNull
    private Integer resource;

    private Instant validFrom;

    private Instant validUntil;


}
