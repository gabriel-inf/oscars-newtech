package net.es.oscars.dto.bwavail;


import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BandwidthAvailabilityResponse {

    @NonNull
    private Map<List<String>, Integer> minAvailableBwMap;

    @NonNull
    private Map<List<String>, Map<Instant, Integer>> bwAvailabilityMap;

    @NonNull
    private Map<List<String>, List<String>> pathPairMap;
}
