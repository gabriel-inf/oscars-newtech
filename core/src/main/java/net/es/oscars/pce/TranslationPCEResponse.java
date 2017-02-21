package net.es.oscars.pce;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.resv.ent.ReservedEthPipeE;
import net.es.oscars.resv.ent.ReservedMplsPipeE;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranslationPCEResponse {
    private Set<ReservedEthPipeE> ethPipes;
    private Set<ReservedMplsPipeE> mplsPipes;
}
