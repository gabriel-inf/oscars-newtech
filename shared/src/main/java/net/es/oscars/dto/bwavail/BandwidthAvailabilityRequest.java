package net.es.oscars.dto.bwavail;

import lombok.*;

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
    private Integer minBandwidth;

    @NonNull
    private String source;

    @NonNull
    private String destination;
}
