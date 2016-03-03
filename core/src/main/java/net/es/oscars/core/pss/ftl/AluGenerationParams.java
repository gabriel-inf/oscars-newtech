package net.es.oscars.core.pss.ftl;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AluGenerationParams {
    public AluGenerationParams() {

    }

    private AluVpls aluVpls;
    private List<AluQos> qosList = new ArrayList<>();
    private List<Lsp> lsps = new ArrayList<>();
    private List<MplsPath> paths = new ArrayList<>();
    private List<AluSdp> sdps = new ArrayList<>();
    private String loopbackInterface;
    private String loopbackAddress;
    private Boolean applyQos;


}
