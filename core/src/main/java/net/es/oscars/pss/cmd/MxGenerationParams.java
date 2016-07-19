package net.es.oscars.pss.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MxGenerationParams {


    private Map<Lsp, MxFilter> lsps;

    private List<MplsPath> paths;


    private Map<MxFilter, MxPolicer> policing;

    private MxVpls mxVpls;

    private String loopbackInterface;
    private String loopbackAddress;
    private Boolean applyQos;


}
