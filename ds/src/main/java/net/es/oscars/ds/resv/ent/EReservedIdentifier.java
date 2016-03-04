package net.es.oscars.ds.resv.ent;

import lombok.*;
import net.es.oscars.common.resv.IdentifierType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EReservedIdentifier {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String identifier;

    @NonNull
    private IdentifierType type;

}
