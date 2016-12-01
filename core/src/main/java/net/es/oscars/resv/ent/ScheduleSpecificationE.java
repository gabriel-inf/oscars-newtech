package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ScheduleSpecificationE {


    @NonNull
    @ElementCollection(targetClass=Date.class)
    private List<Date> startDates;

    @NonNull
    @ElementCollection(targetClass=Date.class)
    private List<Date> endDates;

    private Long minimumDuration;

}
