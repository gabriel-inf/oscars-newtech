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
public class EReservedString implements IReserved<String> {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String urn;

    @NonNull
    private ResourceType resourceType;

    @NonNull
    private String resource;

    private Instant validFrom;

    private Instant validUntil;


}
