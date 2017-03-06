package net.es.oscars.pss.ent;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrnAddressE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String urn;

    @NonNull
    private String ipv4Address;

    private String ipv6Address;


}
