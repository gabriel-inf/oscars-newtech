package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EValve {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String valveId;

    @NonNull
    private String deviceUrn;

    @NonNull
    private Integer mbps;

}
