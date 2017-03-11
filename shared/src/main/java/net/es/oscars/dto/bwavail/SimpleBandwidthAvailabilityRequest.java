package net.es.oscars.dto.bwavail;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleBandwidthAvailabilityRequest {

    @NonNull
    private String startDate;
    @NonNull
    private String endDate;

    @NonNull
    private Integer minAzBandwidth;
    @NonNull
    private Integer minZaBandwidth;

    private List<List<String>> azEros;
    private List<List<String>> zaEros;

    private String srcDevice;
    private List<String> srcPorts;
    private String dstDevice;
    private List<String> dstPorts;

    private Boolean disjointPaths;
    private Integer numPaths;
}
