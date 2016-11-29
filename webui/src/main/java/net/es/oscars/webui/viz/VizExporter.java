package net.es.oscars.webui.viz;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.spec.ReservedEthPipe;
import net.es.oscars.dto.spec.ReservedMplsPipe;
import net.es.oscars.dto.spec.ReservedVlanFlow;
import net.es.oscars.dto.spec.ReservedVlanJunction;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.VertexType;
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
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class VizExporter {

    @Autowired
    private TopologyProvider topologyProvider;

    public VizGraph connection(Connection c) {
        Topology multilayer = topologyProvider.getTopology();

        VizGraph g = VizGraph.builder().edges(new ArrayList<>()).nodes(new ArrayList<>()).build();

        Set<String> nodes = new HashSet<>();

        // TODO: visualize other things
        ReservedVlanFlow rvf = c.getReserved().getVlanFlow();

        String[] colors = {"blue", "red", "green", "yellow", "purple", "pink", "lightblue", "darkgreen"};
        int i = 0;

        List<Map<String, Set<String>>> ethEdgeList = this.resvViewEthPipes(rvf, nodes, multilayer);
        for (Map<String, Set<String>> ethEdges : ethEdgeList) {
            this.drawEdges(ethEdges, g, colors[i]);
            i++;

        }
        List<Map<String, Set<String>>> mplsEdgeList = this.resvViewMplsPipes(rvf, nodes, multilayer);
        for (Map<String, Set<String>> mplsEdges : mplsEdgeList) {
            this.drawEdges(mplsEdges, g, colors[i]);
            i++;
        }

        for (ReservedVlanJunction rvj : rvf.getJunctions()) {
            nodes.add(rvj.getDeviceUrn());
        }

        for (String deviceUrn : nodes) {
            this.makeNode(deviceUrn, g);
        }
        return g;
    }


    private List<Map<String, Set<String>>> resvViewMplsPipes(ReservedVlanFlow rvf,
                                                             Set<String> nodes,
                                                             Topology topology) {
        List<Map<String, Set<String>>> edgesSetList = new ArrayList<>();
        for (ReservedMplsPipe rep : rvf.getMplsPipes()) {
            Map<String, Set<String>> edges = new HashMap<>();
            String a = rep.getAJunction().getDeviceUrn();
            String z = rep.getZJunction().getDeviceUrn();
            nodes.add(a);
            nodes.add(z);
            List<String> azEro = devicesFromEro(a, rep.getAzERO(), z, topology);
            List<String> zaEro = devicesFromEro(z, rep.getZaERO(), a, topology);
            addEroToEdges(azEro, nodes, edges);
            addEroToEdges(zaEro, nodes, edges);
            edgesSetList.add(edges);
        }
        return edgesSetList;

    }

    private List<Map<String, Set<String>>> resvViewEthPipes(ReservedVlanFlow rvf,
                                                            Set<String> nodes,
                                                            Topology topology) {
        List<Map<String, Set<String>>> edgesSetList = new ArrayList<>();
        for (ReservedEthPipe rep : rvf.getEthPipes()) {
            Map<String, Set<String>> edges = new HashMap<>();
            String a = rep.getAJunction().getDeviceUrn();
            String z = rep.getZJunction().getDeviceUrn();
            nodes.add(a);
            nodes.add(z);
            List<String> azEro = devicesFromEro(a, rep.getAzERO(), z, topology);
            List<String> zaEro = devicesFromEro(z, rep.getZaERO(), a, topology);
            addEroToEdges(azEro, nodes, edges);
            addEroToEdges(zaEro, nodes, edges);
            edgesSetList.add(edges);
        }
        return edgesSetList;

    }

    private void drawEdges(Map<String, Set<String>> edges, VizGraph g, String color) {
        log.info("drawing edges " + edges.toString());
        List<String> added = new ArrayList<>();

        for (String a : edges.keySet()) {
            for (String z : edges.get(a)) {
                String e_id = a + " -- " + z;
                String r_id = z + " -- " + a;
                if (!added.contains(r_id)) {

                    added.add(e_id);
                    added.add(r_id);

                    VizEdge ve = VizEdge.builder()
                            .from(a).to(z).title("").label("").value(1)
                            .id(UUID.randomUUID().toString())
                            .arrows(null).arrowStrikethrough(false).color(color)
                            .build();

                    g.getEdges().add(ve);
                }
            }

        }
    }


    private void addEroToEdges(List<String> ero, Set<String> nodes, Map<String, Set<String>> edges) {
        log.info("adding an ero to connection " + ero.toString());
        if (ero.size() > 1) {
            for (int i = 1; i <= ero.size() - 1; i++) {
                String previous = ero.get(i - 1);
                String current = ero.get(i);
                if (!edges.containsKey(current)) {
                    edges.put(current, new HashSet<>());
                }
                if (!edges.containsKey(previous)) {
                    edges.put(previous, new HashSet<>());
                }
                edges.get(current).add(previous);
                edges.get(previous).add(current);
                nodes.add(current);
                nodes.add(previous);
            }
        }
    }


    private List<String> devicesFromEro(String a, List<String> ero, String z, Topology topology) {
        List<String> devicesInEro = new ArrayList<>();
        devicesInEro.add(a);
        for (String edgeUrn : ero) {
            Optional<TopoVertex> maybeTv = topology.getVertexByUrn(edgeUrn);
            if (maybeTv.isPresent()) {
                TopoVertex tv = maybeTv.get();
                if (tv.getVertexType().equals(VertexType.ROUTER) || tv.getVertexType().equals(VertexType.SWITCH)) {
                    devicesInEro.add(edgeUrn);
                }
            }
        }
        devicesInEro.add(z);
        log.info("found devices in ERO: " + devicesInEro.toString());

        return devicesInEro;

    }

    public VizGraph multilayerGraph() {

        VizGraph g = VizGraph.builder().edges(new ArrayList<>()).nodes(new ArrayList<>()).build();
        Map<String, Set<String>> portMap = topologyProvider.devicePortMap();
        Map<String, String> reverseMap = new HashMap<>();
        for (String d : portMap.keySet()) {
            for (String p : portMap.get(d)) {
                reverseMap.put(p, d);
            }
        }


        Topology multilayer = topologyProvider.getTopology();
        List<String> added = new ArrayList<>();

        for (TopoEdge topoEdge : multilayer.getEdges()) {
            String a = topoEdge.getA().getUrn();
            String z = topoEdge.getZ().getUrn();
            String dev_a = reverseMap.get(a);
            String dev_z = reverseMap.get(z);
            String e_id = a + " -- " + z;
            String r_id = z + " -- " + a;
            if (!added.contains(r_id)) {

                added.add(e_id);
                added.add(r_id);
                VizEdge ve = VizEdge.builder()
                        .from(dev_a).to(dev_z).title("").label("").value(1)
                        .id(e_id)
                        .arrows(null).arrowStrikethrough(false).color(null)
                        .build();

                g.getEdges().add(ve);

            }

        }

        for (String deviceUrn : portMap.keySet()) {
            this.makeNode(deviceUrn, g);
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

    public List<String> listTopologyPorts()
    {
        Topology multilayer = topologyProvider.getTopology();
        Set<TopoVertex> topoPorts = multilayer.getVertices().stream().filter(v -> v.getVertexType().equals(VertexType.PORT)).collect(Collectors.toSet());
        List<String> portURNs = new ArrayList<>();

        for(TopoVertex onePort : topoPorts)
        {
            portURNs.add(onePort.getUrn());
        }

        return portURNs;
    }

}
