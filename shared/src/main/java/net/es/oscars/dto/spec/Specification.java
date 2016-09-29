package net.es.oscars.dto.spec;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Specification {

    private Long id;

    @NonNull
    private Integer version;

    @NonNull
    private String username;

    @NonNull
    private String description;

    @NonNull
    private String connectionId;

    @NonNull
    private ScheduleSpecification scheduleSpec;

    @NonNull
    private RequestedBlueprint requested;


}
