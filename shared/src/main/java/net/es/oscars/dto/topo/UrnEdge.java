package net.es.oscars.dto.topo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrnEdge {
    private String a;

    private String z;

    private Map<Layer, Long> metrics = new HashMap<>();

}
