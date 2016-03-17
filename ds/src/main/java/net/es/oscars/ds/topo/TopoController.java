package net.es.oscars.ds.topo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.IntRange;
import net.es.oscars.common.resv.ResourceType;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.ds.helpers.EIntRange;
import net.es.oscars.ds.topo.dao.DeviceRepository;
import net.es.oscars.ds.topo.dao.UrnAdjcyRepository;
import net.es.oscars.ds.topo.ent.*;
import net.es.oscars.dto.rsrc.ReservableQty;
import net.es.oscars.dto.rsrc.ReservableRanges;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.topo.UrnEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class TopoController {

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private DeviceRepository devRepo;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn(ex.getMessage(), ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn(ex.getMessage(), ex);
    }


    @RequestMapping(value = "/device/{urn}", method = RequestMethod.GET)
    @ResponseBody
    public EDevice device(@PathVariable("urn") String urn) {
        EDevice eDevice = devRepo.findByUrn(urn).orElseThrow(NoSuchElementException::new);
        log.info(eDevice.toString());
        return eDevice;
    }

    @RequestMapping(value = "/topo/layer/{layer}", method = RequestMethod.GET)
    @ResponseBody
    public Topology layer(@PathVariable("layer") String layer) {

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

    @RequestMapping(value = "/constraining", method = RequestMethod.GET)
    @ResponseBody
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



}