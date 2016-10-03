package net.es.oscars.topo.serialization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.topo.enums.Layer;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrnAdjcy {
    private String a;
    private String z;

    private Map<Layer, Long> metrics = new HashMap<>();

}
