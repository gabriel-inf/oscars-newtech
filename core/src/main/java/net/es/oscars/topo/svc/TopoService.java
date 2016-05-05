package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.rsrc.ReservableBandwidth;
import net.es.oscars.dto.rsrc.ReservableVlan;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.rsrc.ReservablePssResource;
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

    public List<ReservableVlan> reservableVlans() {

        List<ReservableVlan> vlans = new ArrayList<>();

        List<EDevice> devices = devRepo.findAll();

        // vlans for switches: one resource, per device;
        // vlans for routers: one resource per ifce for device

        devices.stream().filter(d -> d.getType().equals(DeviceType.SWITCH)).forEach(d -> {
            Set<IntRange> dtoVlans = d.getReservableVlans().stream().map(EIntRange::toDtoIntRange).collect(Collectors.toSet());
            ReservableVlan dtoVlanResource = ReservableVlan.builder()
                    .topoVertexUrn(d.getUrn())
                    .vlanRanges(dtoVlans)
                    .build();
            vlans.add(dtoVlanResource);
        });

        devices.stream().filter(d -> d.getType().equals(DeviceType.ROUTER)).forEach(d -> {
            d.getIfces().stream()
                    .forEach(i -> {
                        Set<IntRange> dtoVlans = i.getReservableVlans().stream().map(EIntRange::toDtoIntRange).collect(Collectors.toSet());
                        ReservableVlan dtoVlanResource = ReservableVlan.builder()
                                .topoVertexUrn(i.getUrn())
                                .vlanRanges(dtoVlans)
                                .build();
                        vlans.add(dtoVlanResource);
                    });
        });

        return vlans;
    }


    public List<ReservableBandwidth> reservableBandwidth() {
        List<ReservableBandwidth> bws = new ArrayList<>();
        List<EDevice> devices = devRepo.findAll();

        // vlans for switches: one resource, per device;
        // vlans for routers: one resource per ifce for device
        // bandwidth for switches or routers: one resource per ifce

        devices.stream().forEach(d -> {
                    d.getIfces().stream().forEach(i -> {
                        ReservableBandwidth rbw = ReservableBandwidth.builder()
                                .bandwidth(i.getReservableBw())
                                .topoVertexUrn(i.getUrn())
                                .build();
                        bws.add(rbw);
                    });
                });

        return bws;
    }

    public List<ReservablePssResource> reservablePssResources() {
        log.info("starting reservable");
        List<ReservablePssResource> reservable = new ArrayList<>();
        // this gives us bandwidth and VLAN already
        // now for the rest:

        devRepo.findAll().stream().forEach(d -> {

            if (d.getType().equals(DeviceType.ROUTER) && d.getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                ReservablePssResource tr = ReservablePssResource.builder()
                        .reservableRanges(new HashMap<>())
                        .topoVertexUrn(d.getUrn())
                        .build();

                Set<IntRange> inPolicyIds = new HashSet<>();
                Set<IntRange> egPolicyIds = new HashSet<>();
                Set<IntRange> sdpIds = new HashSet<>();

                // TODO: change from hardcoded to configurable
                inPolicyIds.add(IntRange.builder().floor(6000).ceiling(6999).build());
                egPolicyIds.add(IntRange.builder().floor(6000).ceiling(6999).build());
                sdpIds.add(IntRange.builder().floor(6000).ceiling(6999).build());


                tr.getReservableRanges().put(ResourceType.ALU_EGRESS_POLICY_ID, egPolicyIds);
                tr.getReservableRanges().put(ResourceType.ALU_INGRESS_POLICY_ID, inPolicyIds);
                tr.getReservableRanges().put(ResourceType.ALU_SDP_ID, sdpIds);
                reservable.add(tr);
            }

        });


        // TODO: change from hardcoded to configurable
        ReservablePssResource tr = ReservablePssResource.builder()
                .reservableRanges(new HashMap<>())
                .topoVertexUrn(ResourceType.GLOBAL)
                .build();

        Set<IntRange> vcIds = new HashSet<>();
        vcIds.add(IntRange.builder().floor(6000).ceiling(6999).build());
        tr.getReservableRanges().put(ResourceType.VC_ID, vcIds);
        reservable.add(tr);

        return reservable;
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
