package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ScheduleSpecificationE {


    @NonNull
    private Date notBefore;

    @NonNull
    private Date notAfter;

    @NonNull
    private Long durationMinutes;

}
