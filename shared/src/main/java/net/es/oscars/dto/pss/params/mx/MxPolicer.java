package net.es.oscars.dto.pss.params.mx;

import lombok.*;
import net.es.oscars.dto.pss.params.Policing;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxPolicer {

    private String name;

    private Integer mbps;

    private Policing policing;
}
