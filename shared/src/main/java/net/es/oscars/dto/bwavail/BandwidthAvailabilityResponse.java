package net.es.oscars.dto.bwavail;


import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandwidthAvailabilityResponse {

    @NonNull
    private Long requestID;

    @NonNull
    private Date startDate;

    @NonNull
    private Date endDate;

    @NonNull
    private Integer minRequestedBandwidth;

    @NonNull
    private String source;

    @NonNull
    private String destination;

    @NonNull
    private Integer minAvailableBandwidth;

    @NonNull
    private Integer maxAvailableBandwidth;

    @NonNull
    private List<Map<Date, Integer>> bwAvailMap;


}
