package net.es.oscars.dto.topo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.topo.VertexType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopoVertex {
    private String urn;
    private VertexType vertexType;
}
