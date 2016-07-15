package net.es.oscars.pce;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Junction{
    String urn;
    List<String> fixtures;
    List<Integer> ingressBWs;
    List<Integer> egressBWs;
    String vlanExpression;
    boolean firstJunction;
}
