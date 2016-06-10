package net.es.oscars.servicetopo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceLayerTopology
{
    List<SLDevice> servicelayerDevices;
    List<SLPort> servicelayerPorts;
    List<SLVertex> servicelayerURNs;
    List<LogicalMPLSDevice> logicalSrcDst;
    List<LogicalMPLSPort> logicalSrcdstPort;
    List<LogicalMPLSVertex> logicalURNs;

}
