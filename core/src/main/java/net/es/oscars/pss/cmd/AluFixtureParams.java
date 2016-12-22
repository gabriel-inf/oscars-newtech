package net.es.oscars.pss.cmd;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AluFixtureParams {
    private AluQos inQos;
    private AluQos egQos;
    private List<AluSap> saps;
}