package net.es.oscars.ds.resv.ent;

import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EUrnReserved {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String urn;

    @NonNull
    private String gri;

    private Long bandwidth;

    @OneToMany
    @NonNull
    @Cascade(CascadeType.ALL)
    private Set<EReservedIdentifier> identifiers;

}
