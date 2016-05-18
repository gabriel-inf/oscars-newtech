package net.es.oscars.resv.ent;

import lombok.*;

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
    private ReservedBlueprintE reserved;

}
