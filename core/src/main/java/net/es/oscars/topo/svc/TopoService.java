package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.UrnEdge;
import net.es.oscars.topo.dao.DeviceRepository;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.ent.*;
import net.es.oscars.topo.enums.DeviceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class TopoService {

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private DeviceRepository devRepo;

    public EDevice device(String urn) throws NoSuchElementException {
        return devRepo.findByUrn(urn).orElseThrow(NoSuchElementException::new);
    }

    public Topology layer(Layer layer) throws NoSuchElementException {

        log.info("topology for layer " + layer);
        Topology topo = new Topology();
        topo.setLayer(layer);
        List<EDevice> devices = devRepo.findAll();
        List<EUrnAdjcy> adjcies = adjcyRepo.findAll();

        devices.stream()
                .filter(d -> d.getCapabilities().contains(layer))
                .forEach(d -> {
                    log.info("added device "+d.getUrn()+" to topo for "+layer);

                    TopoVertex dev = new TopoVertex(d.getUrn());
                    topo.getVertices().add(dev);

                    d.getIfces().stream()
                            .filter(i -> i.getCapabilities().contains(layer))
                            .forEach(i -> {
                                TopoVertex ifce = new TopoVertex(i.getUrn());
                                topo.getVertices().add(ifce);

                                UrnEdge edge = UrnEdge.builder().a(d.getUrn()).z(i.getUrn()).metrics(new HashMap<>()).build();
                                edge.getMetrics().put(Layer.INTERNAL, 1L);
                                topo.getEdges().add(edge);

                                UrnEdge reverseEdge = UrnEdge.builder().z(d.getUrn()).a(i.getUrn()).metrics(new HashMap<>()).build();
                                reverseEdge.getMetrics().put(Layer.INTERNAL, 1L);
                                topo.getEdges().add(reverseEdge);


                            });
                });

        adjcies.stream()
                .filter(adj -> adj.getMetrics().containsKey(layer))
                .forEach(adj -> {
                    Long metric = adj.getMetrics().get(layer);
                    UrnEdge edge = UrnEdge.builder().a(adj.getA()).z(adj.getZ()).metrics(new HashMap<>()).build();
                    edge.getMetrics().put(layer, metric);
                    topo.getEdges().add(edge);
                });

        String pretty = null;
        try {
            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(topo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        log.info(pretty);

        return topo;
    }

    public List<TopoResource> constraining() {
        log.info("starting constraining");
        List<TopoResource> resources = new ArrayList<>();
        List<EDevice> devices = devRepo.findAll();

        devices.stream()
                .forEach(d -> {
                    // vlans for switches: global to the device; one vlan strResource w urns for device, all ifces
                    // no bandwidth strResource for switches
                    if (d.getType().equals(DeviceType.SWITCH)) {
                        Set<IntRange> dtoVlans = d.getReservableVlans().stream().map(EIntRange::toDtoIntRange).collect(Collectors.toSet());

                        TopoResource dtoVlanResource = TopoResource.builder()
                                .reservableQties(new HashMap<>())
                                .reservableRanges(new HashMap<>())
                                .topoVertexUrns(new ArrayList<>())
                                .build();


                        dtoVlanResource.getReservableRanges().put(ResourceType.VLAN, dtoVlans);

                        dtoVlanResource.getTopoVertexUrns().add(d.getUrn());
                        for (EIfce switchIfce : d.getIfces()) {
                            dtoVlanResource.getTopoVertexUrns().add(switchIfce.getUrn());
                        }
                        resources.add(dtoVlanResource);
                        log.info("added switch vlan strResource: " + dtoVlanResource.toString());

                    }
                    // now handle bandwidth for everything matched, and vlans for routers
                    d.getIfces().stream()
                            .forEach(i -> {
                                List<String> ifceUrns = new ArrayList<>();
                                ifceUrns.add(i.getUrn());

                                IntRange bwRange = IntRange.builder().floor(0).ceiling(i.getReservableBw()).build();

                                TopoResource dtoResource = TopoResource.builder()
                                        .reservableQties(new HashMap<>())
                                        .reservableRanges(new HashMap<>())
                                        .topoVertexUrns(ifceUrns)
                                        .build();

                                dtoResource.getReservableQties().put(ResourceType.BANDWIDTH, bwRange);
                                resources.add(dtoResource);

                                if (d.getType().equals(DeviceType.ROUTER)) {
                                    Set<IntRange> dtoVlans = i.getReservableVlans().stream().map(EIntRange::toDtoIntRange).collect(Collectors.toSet());

                                    dtoResource.getReservableRanges().put(ResourceType.VLAN, dtoVlans);

                                }
                                log.info("added router ifce strResource: " + dtoResource.toString());
                            });
                });


        return resources;
    }

    public Map<String, DeviceModel> deviceModels() {
        Map<String, DeviceModel> result = new HashMap<>();
        devRepo.findAll().stream().forEach(t -> {
            result.put(t.getUrn(), t.getModel());
        });
        return result;

    }

    public List<String> edges(Layer layer) {
        log.info("finding edges for "+layer);

        List<String> edges = new ArrayList<>();
        List<EDevice> devices = devRepo.findAll();

        devices.stream()
                .filter(d -> d.getCapabilities().contains(layer))
                .forEach(d -> {
                    log.info("found device "+d.getUrn()+" for "+layer);
                    d.getIfces().stream()
                            .filter(i -> i.getCapabilities().contains(layer))
                            .forEach(i -> {
                                log.info("found ifce "+i.getUrn()+" for "+layer);

                                edges.add(i.getUrn());
                            });
                });
        return edges;
    }

    public List<String> devices() {
        log.info("retrieving all devices");

        return devRepo.findAll().stream().map(EDevice::getUrn).collect(Collectors.toList());

    }



}
