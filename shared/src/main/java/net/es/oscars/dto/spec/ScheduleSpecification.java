package net.es.oscars.dto.spec;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSpecification {

    @NonNull
    private Date notBefore;

    @NonNull
    private Date notAfter;

    @NonNull
    private Long durationMinutes;

}
