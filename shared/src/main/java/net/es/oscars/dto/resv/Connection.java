package net.es.oscars.dto.resv;

import lombok.*;
import net.es.oscars.dto.spec.Blueprint;
import net.es.oscars.dto.spec.Specification;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Connection {

    private Long id;

    @NonNull
    private String connectionId;

    @NonNull
    private Specification specification;

    @NonNull
    private Schedule schedule;

    @NonNull
    private Blueprint reserved;

    @NonNull
    private States states;

}
