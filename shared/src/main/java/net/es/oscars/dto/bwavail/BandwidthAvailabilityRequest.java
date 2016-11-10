package net.es.oscars.dto.bwavail;

import lombok.*;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;

import java.util.Date;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandwidthAvailabilityRequest {

    @NonNull
    private Date startDate;
    @NonNull
    private Date endDate;

    @NonNull
    private Integer minAzBandwidth;
    @NonNull
    private Integer minZaBandwidth;

    @NonNull
    private List<List<String>> azEros;
    @NonNull
    private List<List<String>> zaEros;

    private String srcDevice;
    private List<String> srcPorts;
    private String dstDevice;
    private List<String> dstPorts;

    private Boolean disjointPaths;
    private Integer numPaths;
}
