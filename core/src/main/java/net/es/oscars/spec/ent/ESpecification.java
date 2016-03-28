package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESpecification {

    @Id
    @GeneratedValue
    private Long id;

    private Integer version;

    @NonNull
    @Column(unique = true)
    private String specificationId;

    private Date submitted;
    private Date notBefore;
    private Date notAfter;

    private Long durationMinutes;

    private String username;

    @OneToOne
    private EBlueprint blueprint;


}
