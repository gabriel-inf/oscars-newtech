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
    private Date startTime;

    @NonNull
    private Date endTime;

    @NonNull
    private Integer minBandwidth;

    @NonNull
    private Integer maxBandwidth;

    @NonNull
    private List<Map<Date, Integer>> bwAvailMap;


}
