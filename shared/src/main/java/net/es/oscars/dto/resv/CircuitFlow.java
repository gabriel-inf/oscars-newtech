package net.es.oscars.dto.resv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitFlow {

    Set<String> sourcePorts;

    String sourceDevice;

    Set<String> destPorts;

    String destDevice;

    Integer azMbps;

    Integer zaMbps;

    String sourceVlan;

    String destVlan;

    List<String> azRoute;

    List<String> zaRoute;

    Set<String> blacklist;

    String palindromic;

    String survivability;

    Integer numDisjointPaths;
}
