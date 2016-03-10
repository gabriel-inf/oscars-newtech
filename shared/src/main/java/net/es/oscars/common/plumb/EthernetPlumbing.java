package net.es.oscars.common.plumb;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class EthernetPlumbing {

    private Set<EthBridgePlumbing> bridgePlumbings = new HashSet<>();

    private Set<VplsPlumbing> vplsPlumbings = new HashSet<>();


}
