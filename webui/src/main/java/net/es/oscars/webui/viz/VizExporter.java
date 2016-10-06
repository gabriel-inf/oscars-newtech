package net.es.oscars.webui.viz;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.viz.Position;
import net.es.oscars.dto.viz.VizEdge;
import net.es.oscars.dto.viz.VizGraph;
import net.es.oscars.dto.viz.VizNode;
import net.es.oscars.webui.ipc.TopologyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

@Component
@Slf4j
public class VizExporter {

    @Autowired
    private TopologyProvider topologyProvider;

    public VizGraph multilayerGraph() {

        VizGraph g = VizGraph.builder().edges(new ArrayList<>()).nodes(new ArrayList<>()).build();
        
        Topology multilayer = topologyProvider.getTopology();

        for (TopoEdge topoEdge : multilayer.getEdges()) {
            String a = topoEdge.getA().getUrn();
            String z = topoEdge.getZ().getUrn();
            VizEdge ve = VizEdge.builder()
                    .from(a).to(z).title("").label("").value(1)
                    .id(null)
                    .arrows(null).arrowStrikethrough(false).color(null)
                    .build();

            g.getEdges().add(ve);
        }

        for (TopoVertex vertex : multilayer.getVertices()) {
            this.makeNode(vertex.getUrn(), g);
        }

        return g;

    }


    private void makeNode(String node, VizGraph g) {

        Map<String, Set<String>> hubs = topologyProvider.getHubs();
        Map<String, Position> positions = topologyProvider.getPositions();
        String hub = null;
        for (String h : hubs.keySet()) {
            if (hubs.get(h).contains(node)) {
                hub = h;
            }
        }

        VizNode n = VizNode.builder().id(node).label(node).title(node).value(1).build();
        if (hub != null) {
            n.setGroup(hub);
        }

        if (positions.keySet().contains(node)) {
            n.setFixed(new HashMap<>());
            n.getFixed().put("x", true);
            n.getFixed().put("y", true);
            n.setX(positions.get(node).getX());
            n.setY(positions.get(node).getY());
        }

        g.getNodes().add(n);
    }

    private String toWeb(Color c) {
        String rgb = Integer.toHexString(c.getRGB());
        rgb = "#" + rgb.substring(2, rgb.length());
        return rgb;
    }

    private String shorten(Double mbps) {

        BigDecimal bd = new BigDecimal(mbps);
        bd = bd.round(new MathContext(3));
        double rounded = bd.doubleValue();

        if (mbps < 1000.0) {
            return rounded + "M";
        } else if (mbps < 1000.0 * 1000) {
            return rounded / 1000 + "G";
        } else if (mbps < 1000.0 * 1000000) {
            return rounded / 1000000 + "T";
        } else if (mbps < 1000.0 * 1000000000) {
            return rounded / 1000000000 + "P";
        } else {
            return ">1000P";
        }


    }

}
