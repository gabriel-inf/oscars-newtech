package net.es.oscars.servicetopo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.enums.Layer;

import java.util.List;

/**
 * Created by jeremy on 7/28/16.
 *
 * Class to represent a non-physical edge on the service-layer topology. Identical to LogicalEdge, however it also includes both a primary and backup list of physical TopoEdges that comprise this SurvivableLogicalEdge.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class SurvivableLogicalEdge extends TopoEdge
{
    private TopoVertex a;

    private TopoVertex z;

    private Long metricPrimary;
    private Long metricSecondary;

    private Layer layer;

    private List<TopoEdge> correspondingPrimaryTopoEdges;
    private List<TopoEdge> correspondingSecondaryTopoEdges;
}
