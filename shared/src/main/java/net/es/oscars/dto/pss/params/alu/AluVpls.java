package net.es.oscars.dto.pss.params.alu;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AluVpls {

    private Integer vcId;

    private List<AluSap> saps;

    private String serviceName;

    private String description;

    private String endpointName;

    private AluSdp sdp;

    private Integer protectVcId;

    private AluSdp protectSdp;



}
