package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.Embeddable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ScheduleE {

    @NonNull
    private Date submitted;

    @NonNull
    private Date setup;

    @NonNull
    private Date teardown;


}
