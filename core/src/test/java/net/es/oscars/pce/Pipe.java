package net.es.oscars.pce;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.topo.enums.PalindromicType;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pipe{
    Junction aJunction;
    Junction zJunction;
    Integer azMbps;
    Integer zaMbps;
    PalindromicType palindromic;

    // For Expected Pipes
    List<String> potentialAZEROs;
    List<String> potentialZAEROs;
    List<Integer> expectedAZInBandwidths;
    List<Integer> expectedAZEgBandwidths;
    List<Integer> expectedZAInBandwidths;
    List<Integer> expectedZAEgBandwidths;
}
