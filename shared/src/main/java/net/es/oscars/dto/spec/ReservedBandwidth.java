package net.es.oscars.dto.spec;

import lombok.*;
import net.es.oscars.dto.topo.Urn;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservedBandwidth {


    @NonNull
    private String urn;

    //private Integer bandwidth;

    private Integer inBandwidth;

    private Integer egBandwidth;


    private Instant beginning;

    private Instant ending;


}
