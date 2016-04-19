package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.spec.ent.BlueprintE;
import net.es.oscars.spec.ent.SpecificationE;

import javax.persistence.*;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String connectionId;
    
    @Embedded
    private StatesE states;

    @Embedded
    private ScheduleE schedule;

    @OneToOne (cascade = CascadeType.ALL)
    private SpecificationE specification;


    @OneToOne (cascade = CascadeType.ALL)
    private BlueprintE reserved;

}
