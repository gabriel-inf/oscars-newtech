package net.es.oscars.pss.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AluGenerationParams {

    private List<AluQos> qoses;

    private List<Lsp> lsps;

    private List<MplsPath> paths;

    private List<AluSdp> sdps;

    private AluVpls aluVpls;
    private String loopbackInterface;
    private String loopbackAddress;
    private Boolean applyQos;


}
