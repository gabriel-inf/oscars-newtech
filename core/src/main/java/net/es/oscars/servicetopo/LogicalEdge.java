package net.es.oscars.servicetopo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.enums.Layer;

import java.util.List;

/**
 * Created by jeremy on 6/15/16.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogicalEdge extends TopoEdge
{
    private TopoVertex a;

    private TopoVertex z;

    private Long metric;

    private Layer layer;

    private List<TopoEdge> correspondingTopoEdges;
}
