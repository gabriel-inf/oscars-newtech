package net.es.oscars.pss.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthValveType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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


}
