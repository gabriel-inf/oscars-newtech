package net.es.oscars.pss.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthValveType;
import net.es.oscars.resv.ent.EReservedResource;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EEthValve {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private Integer mbps;

    @NonNull
    private EthValveType valveType;

    private Boolean limited;

    @ElementCollection
    private Set<String> resourceIds;

}
