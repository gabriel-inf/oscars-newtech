package net.es.oscars.servicetopo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.enums.Layer;

import java.util.List;

/**
 * Created by jeremy on 6/15/16.
 *
 * Class to represent a non-physical edge on the service-layer topology. Identical to TopoEdge, however it also includes a list of physical TopoEdges that comprise this LogicalEdge.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class LogicalEdge extends TopoEdge
{
    private TopoVertex a;

    private TopoVertex z;

    private Long metric;
    private Long metricAZ;
    private Long metricZA;

    private Layer layer;

    private List<TopoEdge> correspondingTopoEdges;
    private List<TopoEdge> correspondingAZTopoEdges;
    private List<TopoEdge> correspondingZATopoEdges;
}
