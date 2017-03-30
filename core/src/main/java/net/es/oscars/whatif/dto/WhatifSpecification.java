package net.es.oscars.whatif.dto;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatifSpecification {

    String startDate;

    String endDate;

    // Duration in minutes
    Long durationMinutes;

    // Currently assumed to be in Megabits
    Integer volume;

    // Symmetric bandwidth in Mbps: A->Z = Z->A
    Integer bandwidthMbps;

    @NonNull
    String srcDevice;
    @NonNull
    Set<String> srcPorts;
    @NonNull
    String dstDevice;
    @NonNull
    Set<String> dstPorts;
}
