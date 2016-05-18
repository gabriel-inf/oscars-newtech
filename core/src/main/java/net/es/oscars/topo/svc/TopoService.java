package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.UrnEdge;
import net.es.oscars.topo.dao.ReservableBandwidthRepository;
import net.es.oscars.topo.dao.ReservableVlanRepository;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.*;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.UrnType;
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
    private UrnRepository urnRepo;

    @Autowired
    private ReservableVlanRepository vlanRepo;

    @Autowired
    private ReservableBandwidthRepository bwRepo;

    public Topology layer(Layer layer) throws NoSuchElementException {

        log.info("topology for layer " + layer);
        Topology topo = new Topology();
        topo.setLayer(layer);
        List<UrnE> urns = urnRepo.findAll();
        List<UrnAdjcyE> adjcies = adjcyRepo.findAll();

        urns.stream()
                .filter(u -> u.getCapabilities().contains(layer))
                .forEach(u -> {
                    log.info("added urn " + u.getUrn() + " to topo for " + layer);

                    TopoVertex dev = new TopoVertex(u.getUrn());
                    topo.getVertices().add(dev);
                });

        adjcies.stream()
                .filter(adj -> adj.getMetrics().containsKey(layer))
                .forEach(adj -> {
                    Long metric = adj.getMetrics().get(layer);
                    UrnEdge edge = UrnEdge.builder()
                            .a(adj.getA().getUrn())
                            .z(adj.getZ().getUrn())
                            .metrics(new HashMap<>()).build();
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


        List<ReservableVlanE> vlans = vlanRepo.findAll();
        return vlans;


    }


    public List<ReservableBandwidthE> reservableBandwidths() {
        return bwRepo.findAll();

    }

    public List<String> edges(Layer layer) {
        log.info("finding edges for "+layer);

        return urnRepo.findAll().stream()
                .filter( u -> u.getCapabilities().contains(layer) && u.getUrnType().equals(UrnType.IFCE))
                .map(UrnE::getUrn)
                .collect(Collectors.toList());

    }




    public List<String> devices() {
        log.info("retrieving all devices");

        return urnRepo.findAll().stream()
                .filter(u -> u.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toList());
    }


}
