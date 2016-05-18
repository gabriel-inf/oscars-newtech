package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private Integer version;

    @NonNull
    private String username;

    @NonNull
    private String description;

    @NonNull
    private String connectionId;

    @Embedded
    private ScheduleSpecificationE scheduleSpec;

    @OneToOne (cascade = CascadeType.ALL)
    private RequestedBlueprintE requested;



}
