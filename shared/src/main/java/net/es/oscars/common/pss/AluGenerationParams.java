package net.es.oscars.common.pss;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
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
