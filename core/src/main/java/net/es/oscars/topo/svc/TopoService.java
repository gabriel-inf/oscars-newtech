package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.dto.topo.enums.DeviceType;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.topo.dao.ReservableBandwidthRepository;
import net.es.oscars.topo.dao.ReservableVlanRepository;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.ReservableVlanE;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class TopoService {
    private UrnAdjcyRepository adjcyRepo;

    private UrnRepository urnRepo;

    private ReservableVlanRepository vlanRepo;

    private ReservableBandwidthRepository bwRepo;

    public UrnE getUrn(String urn) throws NoSuchElementException {
        return urnRepo.findByUrn(urn).orElseThrow(NoSuchElementException::new);
    }


    @Autowired
    public TopoService(UrnAdjcyRepository adjcyRepo, UrnRepository urnRepo,
                       ReservableVlanRepository vlanRepo, ReservableBandwidthRepository bwRepo) {
        this.adjcyRepo = adjcyRepo;
        this.urnRepo = urnRepo;
        this.vlanRepo = vlanRepo;
        this.bwRepo = bwRepo;
    }

    public Topology layer(Layer layer) throws NoSuchElementException {

        //log.info("topology for layer " + layer);
        Topology topo = new Topology();
        topo.setLayer(layer);
        List<UrnE> urns = urnRepo.findAll();
        List<UrnAdjcyE> adjcies = adjcyRepo.findAll();

        urns.stream()
                .filter(u -> u.getCapabilities().contains(layer) || layer.equals(Layer.INTERNAL))
                .forEach(u -> {
                    VertexType type = null;
                    if (u.getDeviceType() == null && u.getIfceType() != null) {
                        type = VertexType.PORT;
                    } else {
                        switch (u.getDeviceType()) {
                            case ROUTER:
                                type = VertexType.ROUTER;
                                break;
                            case SWITCH:
                                type = VertexType.SWITCH;
                                break;
                        }
                    }
                    TopoVertex dev = new TopoVertex(u.getUrn(), type);
                    topo.getVertices().add(dev);
                });

        adjcies.stream()
                .filter(adj -> adj.getMetrics().containsKey(layer))
                .forEach(adj -> {
                    if (adj.getA() == null || adj.getZ() == null) {
                        log.error("error in adjacency!");
                        log.error(adj.toString());
                    } else {
                        Long metric = adj.getMetrics().get(layer);
                        Optional<TopoVertex> a = topo.getVertexByUrn(adj.getA().getUrn());
                        Optional<TopoVertex> z = topo.getVertexByUrn(adj.getZ().getUrn());

                        if (a.isPresent() && z.isPresent()) {
                            TopoEdge edge = TopoEdge.builder()
                                    .a(a.get())
                                    .z(z.get())
                                    .metric(metric)
                                    .layer(layer)
                                    .build();
                            topo.getEdges().add(edge);
                        }
                    }
                });

        String pretty = null;
        try {
            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(topo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        //log.info(pretty);     //commented for output readability

        return topo;
    }

    public Topology getMultilayerTopology() {
        Topology multiLayerTopo = new Topology();

        Topology ethTopo = layer(Layer.ETHERNET);
        Topology intTopo = layer(Layer.INTERNAL);
        Topology mplsTopo = layer(Layer.MPLS);

        multiLayerTopo.getVertices().addAll(ethTopo.getVertices());
        multiLayerTopo.getVertices().addAll(intTopo.getVertices());
        multiLayerTopo.getVertices().addAll(mplsTopo.getVertices());
        multiLayerTopo.getEdges().addAll(ethTopo.getEdges());
        multiLayerTopo.getEdges().addAll(intTopo.getEdges());
        multiLayerTopo.getEdges().addAll(mplsTopo.getEdges());

        return multiLayerTopo;
    }

    public Map<String, DeviceModel> deviceModels() {
        Map<String, DeviceModel> modelMap = new HashMap<>();
        urnRepo.findAll().stream().filter(u -> u.getUrnType().equals(UrnType.DEVICE)).forEach(u -> {
            modelMap.put(u.getUrn(), u.getDeviceModel());
        });

        return modelMap;

    }

    public UrnE device(String urn) throws NoSuchElementException {
        UrnE device = urnRepo.findByUrn(urn).orElseThrow(NoSuchElementException::new);

        if (!device.getUrnType().equals(UrnType.DEVICE)) {
            throw new NoSuchElementException();
        }
        return device;

    }

    public List<ReservableVlanE> reservableVlans() {
        return vlanRepo.findAll();

    }


    public List<ReservableBandwidthE> reservableBandwidths() {
        return bwRepo.findAll();

    }

    public List<String> edges(Layer layer) {
        log.info("finding edges for " + layer);

        return urnRepo.findAll().stream()
                .filter(u -> u.getCapabilities().contains(layer) && u.getUrnType().equals(UrnType.IFCE))
                .map(UrnE::getUrn)
                .collect(Collectors.toList());

    }

    public List<String> edgesWithCapability(String device, Layer layer) {
        log.info("finding edges with capability " + layer + " for device " + device);
        List<UrnAdjcyE> adjcies = adjcyRepo.findAll();

        return adjcies.stream()
                .filter(adj ->
                        adj.getMetrics().containsKey(Layer.INTERNAL)
                        && adj.getA().getUrn().equals(device)
                        && adj.getZ().getCapabilities().contains(layer))
                .map(adj -> adj.getZ().getUrn())
                .collect(Collectors.toList());

    }

    public List<String> devices() {
        log.info("retrieving all devices");

        return urnRepo.findAll().stream()
                .filter(u -> u.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toList());
    }


    public VertexType getVertexTypeFromDeviceType(DeviceType deviceType) {
        if (deviceType.equals(DeviceType.SWITCH))
            return VertexType.SWITCH;
        else
            return VertexType.ROUTER;
    }

    public Map<String, Set<String>> buildDeviceToPortMap() {
        Topology topo = getMultilayerTopology();
        Map<String, Set<String>> deviceToPortMap = new HashMap<>();
        Map<TopoVertex, Boolean> checkedMap = topo.getVertices().stream().collect(Collectors.toMap(v -> v, v -> false));
        for (TopoEdge edge : topo.getEdges()) {
            TopoVertex a = edge.getA();
            TopoVertex z = edge.getZ();
            // If either a or z is a device
            if (!a.getVertexType().equals(VertexType.PORT) || !z.getVertexType().equals(VertexType.PORT)) {
                // and if either a or z has not been checked
                if (!checkedMap.get(a) || !checkedMap.get(z)) {
                    // Add A (if it is a device) to the map if it has not been already
                    if (!a.getVertexType().equals(VertexType.PORT)) {
                        deviceToPortMap.putIfAbsent(a.getUrn(), new HashSet<>());
                        // If the other node (port) has not been checked, add it to the device's set of ports
                        if (!checkedMap.get(z) && z.getVertexType().equals(VertexType.PORT)) {
                            deviceToPortMap.get(a.getUrn()).add(z.getUrn());
                        }
                    }
                    // Add Z (if it is a device) to the map if it has not been already
                    if (!z.getVertexType().equals(VertexType.PORT)) {
                        deviceToPortMap.putIfAbsent(z.getUrn(), new HashSet<>());
                        // If the other node (port) has not been checked, add it to the device's set of ports
                        if (!checkedMap.get(a) && a.getVertexType().equals(VertexType.PORT)) {
                            deviceToPortMap.get(z.getUrn()).add(a.getUrn());
                        }
                    }
                    // Mark both as checked
                    checkedMap.put(a, true);
                    checkedMap.put(z, true);
                }
            }
        }
        return deviceToPortMap;
    }

    public Map<String, String> buildPortToDeviceMap(Map<String, Set<String>> deviceToPortMap) {
        Map<String, String> portToDeviceMap = new HashMap<>();
        for (String device : deviceToPortMap.keySet()) {
            deviceToPortMap.get(device).forEach(port -> portToDeviceMap.put(port, device));
        }
        return portToDeviceMap;
    }
}
