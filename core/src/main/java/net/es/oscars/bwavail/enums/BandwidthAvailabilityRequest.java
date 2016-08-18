package net.es.oscars.bwavail.enums;

import lombok.*;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandwidthAvailabilityRequest {

    @NonNull
    private Long requestID;

    @NonNull
    private Date startDate;

    @NonNull
    private Date endDate;

    @NonNull
    private Integer minAzBandwidth;

    @NonNull
    private Integer minZaBandwidth;

    @NonNull
    private PalindromicType palindromicType;

    @NonNull
    private SurvivabilityType survivabilityType;

    @NonNull
    private String srcDevice;

    @NonNull
    private String srcPort;

    @NonNull
    private String dstDevice;

    @NonNull
    private String dstPort;
}
