package net.es.oscars.whatif.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeRequestSpecification {

    String startDate;
    String endDate;
    Integer volume;
    String srcDevice;
    Set<String> srcPorts;
    String dstDevice;
    Set<String> dstPorts;
}
