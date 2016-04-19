package net.es.oscars.topo.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.IntRange;
import net.es.oscars.common.resv.ResourceType;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.dto.rsrc.ReservableQty;
import net.es.oscars.dto.rsrc.ReservableRanges;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.UrnEdge;
import net.es.oscars.topo.dao.DeviceRepository;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.ent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
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

    public Topology layer(String layer) throws NoSuchElementException {

        log.info("topology for layer " + layer);
        Layer eLayer = Layer.get(layer).orElseThrow(NoSuchElementException::new);
        Topology topo = new Topology();
        topo.setLayer(eLayer);
        List<EDevice> devices = devRepo.findAll();
        List<EUrnAdjcy> adjcies = adjcyRepo.findAll();

        devices.stream()
                .filter(d -> d.getCapabilities().contains(eLayer))
                .forEach(d -> {

                    TopoVertex dev = new TopoVertex(d.getUrn());
                    topo.getVertices().add(dev);

                    d.getIfces().stream()
                            .filter(i -> i.getCapabilities().contains(eLayer))
                            .forEach(i -> {
                                TopoVertex ifce = new TopoVertex(i.getUrn());
                                topo.getVertices().add(ifce);

                                UrnEdge edge = new UrnEdge(d.getUrn(), i.getUrn());
                                edge.getMetrics().put(eLayer, 1L);
                                topo.getEdges().add(edge);
                            });
                });

        adjcies.stream()
                .filter(adj -> adj.getMetrics().containsKey(eLayer))
                .forEach(adj -> {
                    Long metric = adj.getMetrics().get(eLayer);
                    UrnEdge edge = new UrnEdge(adj.getA(), adj.getZ());
                    edge.getMetrics().put(eLayer, metric);
                    topo.getEdges().add(edge);
                });


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
                        ReservableRanges dtoVlans = ReservableRanges.builder()
                                .type(ResourceType.VLAN)
                                .ranges(d.getReservableVlans().stream().map(EIntRange::toDtoIntRange).collect(Collectors.toList()))
                                .build();

                        TopoResource dtoVlanResource = TopoResource.builder()
                                .reservableQties(new HashSet<>())
                                .reservableRanges(new HashSet<>())
                                .topoVertexUrns(new ArrayList<>())
                                .build();

                        dtoVlanResource.getReservableRanges().add(dtoVlans);

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
                                ReservableQty dtoBw = ReservableQty.builder()
                                        .type(ResourceType.BANDWIDTH)
                                        .range(bwRange)
                                        .build();

                                TopoResource dtoResource = TopoResource.builder()
                                        .reservableQties(new HashSet<>())
                                        .reservableRanges(new HashSet<>())
                                        .topoVertexUrns(ifceUrns)
                                        .build();

                                dtoResource.getReservableQties().add(dtoBw);
                                resources.add(dtoResource);

                                if (d.getType().equals(DeviceType.ROUTER)) {
                                    ReservableRanges dtoVlans = ReservableRanges.builder()
                                            .type(ResourceType.VLAN)
                                            .ranges(i.getReservableVlans().stream().map(EIntRange::toDtoIntRange).collect(Collectors.toList()))
                                            .build();
                                    dtoResource.getReservableRanges().add(dtoVlans);

                                }
                                log.info("added router ifce strResource: " + dtoResource.toString());
                            });
                });


        return resources;
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
