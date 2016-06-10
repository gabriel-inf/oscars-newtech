package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.enums.*;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.beans.UrnEdge;
import net.es.oscars.topo.dao.ReservableBandwidthRepository;
import net.es.oscars.topo.dao.ReservableVlanRepository;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.*;
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
                    VertexType type = null;
                    if(u.getDeviceType() == null && u.getIfceType() != null){
                        type = VertexType.PORT;
                    }
                    else{
                        switch(u.getDeviceType()){
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
                    Long metric = adj.getMetrics().get(layer);
                    Optional<TopoVertex> a = topo.getVertexByUrn(adj.getA().getUrn());
                    Optional<TopoVertex> z = topo.getVertexByUrn(adj.getZ().getUrn());
                    if(a.isPresent() && z.isPresent()){
                        TopoEdge edge = TopoEdge.builder()
                                .a(a.get())
                                .z(z.get())
                                .metric(metric)
                                .layer(layer)
                                .build();
                        topo.getEdges().add(edge);
                    }
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
