package net.es.oscars.topo.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.topo.enums.VertexType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopoVertex {
    private String urn;
    private VertexType vertexType;
}
