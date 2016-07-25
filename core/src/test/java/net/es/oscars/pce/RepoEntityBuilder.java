package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.*;
import net.es.oscars.topo.enums.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class RepoEntityBuilder {

    @Autowired
    UrnRepository urnRepo;

    @Autowired
    UrnAdjcyRepository adjcyRepo;


    public void populateRepos(Collection<TopoVertex> vertices, Collection<TopoEdge> edges, Map<TopoVertex,TopoVertex> portToDeviceMap){
        log.info("Populating URN Repo and Adjcy Repo");

        urnRepo.deleteAll();
        adjcyRepo.deleteAll();

        List<Integer> floors = Arrays.asList(1, 10, 20, 30);
        List<Integer> ceilings = Arrays.asList(9, 19, 29, 39);

        List<UrnE> urnList = new ArrayList<>();
        List<UrnAdjcyE> adjcyList = new ArrayList<>();
        for(TopoEdge edge : edges){
            TopoVertex a = edge.getA();

            TopoVertex z = edge.getZ();


            UrnE aUrn = addUrnToList(a, urnList, portToDeviceMap, floors, ceilings);

            UrnE zUrn = addUrnToList(z, urnList, portToDeviceMap, floors, ceilings);

            UrnAdjcyE adj = buildUrnAdjcy(edge, aUrn, zUrn);
            adjcyList.add(adj);
        }
        urnRepo.save(urnList);
        adjcyRepo.save(adjcyList);
    }

    public void populateRepos(Collection<TopoVertex> vertices, Collection<TopoEdge> edges, Map<TopoVertex,TopoVertex> portToDeviceMap,
                              Map<TopoVertex, List<Integer>> portBWs){
        log.info("Populating URN Repo and Adjcy Repo");

        urnRepo.deleteAll();
        adjcyRepo.deleteAll();

        List<Integer> floors = Arrays.asList(1, 10, 20, 30);
        List<Integer> ceilings = Arrays.asList(9, 19, 29, 39);

        List<UrnE> urnList = new ArrayList<>();
        List<UrnAdjcyE> adjcyList = new ArrayList<>();
        for(TopoEdge edge : edges){
            TopoVertex a = edge.getA();

            TopoVertex z = edge.getZ();


            UrnE aUrn = addUrnToList(a, urnList, portToDeviceMap, portBWs, floors, ceilings);

            UrnE zUrn = addUrnToList(z, urnList, portToDeviceMap, portBWs, floors, ceilings);

            UrnAdjcyE adj = buildUrnAdjcy(edge, aUrn, zUrn);
            adjcyList.add(adj);
        }
        urnRepo.save(urnList);
        adjcyRepo.save(adjcyList);
    }

    public void populateRepos(Collection<TopoVertex> vertices, Collection<TopoEdge> edges, Map<TopoVertex,TopoVertex> portToDeviceMap,
                              Map<TopoVertex, List<Integer>> floorMap, Map<TopoVertex, List<Integer>> ceilingMap){
        log.info("Populating URN Repo and Adjcy Repo");

        urnRepo.deleteAll();
        adjcyRepo.deleteAll();

        List<UrnE> urnList = new ArrayList<>();
        List<UrnAdjcyE> adjcyList = new ArrayList<>();
        for(TopoEdge edge : edges){
            TopoVertex a = edge.getA();

            TopoVertex z = edge.getZ();


            UrnE aUrn = addUrnToList(a, urnList, portToDeviceMap, floorMap.get(a), ceilingMap.get(a));

            UrnE zUrn = addUrnToList(z, urnList, portToDeviceMap, floorMap.get(z), ceilingMap.get(z));

            UrnAdjcyE adj = buildUrnAdjcy(edge, aUrn, zUrn);
            adjcyList.add(adj);
        }
        urnRepo.save(urnList);
        adjcyRepo.save(adjcyList);
    }

    public void populateRepos(Collection<TopoVertex> vertices, Collection<TopoEdge> edges, Map<TopoVertex,TopoVertex> portToDeviceMap,
                              Map<TopoVertex, List<Integer>> portBWs, Map<TopoVertex, List<Integer>> floorMap,
                              Map<TopoVertex, List<Integer>> ceilingMap){
        log.info("Populating URN Repo and Adjcy Repo");

        urnRepo.deleteAll();
        adjcyRepo.deleteAll();

        List<UrnE> urnList = new ArrayList<>();
        List<UrnAdjcyE> adjcyList = new ArrayList<>();
        for(TopoEdge edge : edges){
            TopoVertex a = edge.getA();

            TopoVertex z = edge.getZ();


            UrnE aUrn = addUrnToList(a, urnList, portToDeviceMap, portBWs, floorMap.get(a), ceilingMap.get(a));

            UrnE zUrn = addUrnToList(z, urnList, portToDeviceMap, portBWs, floorMap.get(z), ceilingMap.get(z));

            UrnAdjcyE adj = buildUrnAdjcy(edge, aUrn, zUrn);
            adjcyList.add(adj);
        }
        urnRepo.save(urnList);
        adjcyRepo.save(adjcyList);
    }




    public UrnE addUrnToList(TopoVertex v, List<UrnE> urnList, Map<TopoVertex,TopoVertex> portToDeviceMap, List<Integer> floors,
                             List<Integer> ceilings){
        UrnE urn = getFromUrnList(v.getUrn(), urnList);
        if(urn == null){
            Set<Layer> capabilities = new HashSet<>();
            capabilities.add(Layer.INTERNAL);
            capabilities.add(Layer.ETHERNET);

            if (!v.getVertexType().equals(VertexType.PORT)) {
                if(v.getVertexType().equals(VertexType.ROUTER)) {
                    capabilities.add(Layer.MPLS);
                    urn = buildUrn(v, null, capabilities, floors, ceilings);
                }
                else {
                    urn = buildUrn(v, null, capabilities, floors, ceilings);
                }
            } else {
                TopoVertex deviceVertex = portToDeviceMap.get(v);
                VertexType deviceVertexType = deviceVertex.getVertexType();
                if(deviceVertexType.equals(VertexType.ROUTER)) {
                    capabilities.add(Layer.MPLS);
                    urn = buildUrn(v, determineDeviceModel(deviceVertexType), capabilities, floors, ceilings);
                }
                else {
                    urn = buildUrn(v, determineDeviceModel(deviceVertexType), capabilities, floors, ceilings);
                }
            }
            urnList.add(urn);
        }
        return urn;
    }

    public UrnE addUrnToList(TopoVertex v, List<UrnE> urnList, Map<TopoVertex,TopoVertex> portToDeviceMap,
                             Map<TopoVertex, List<Integer>> portBWs, List<Integer> floors, List<Integer> ceilings){
        UrnE urn = getFromUrnList(v.getUrn(), urnList);
        if(urn == null){
            Set<Layer> capabilities = new HashSet<>();
            capabilities.add(Layer.INTERNAL);
            capabilities.add(Layer.ETHERNET);

            if (!v.getVertexType().equals(VertexType.PORT)) {
                if(v.getVertexType().equals(VertexType.ROUTER)) {
                    capabilities.add(Layer.MPLS);
                    urn = buildUrn(v, null, capabilities, floors, ceilings);
                }
                else {
                    urn = buildUrn(v, null, capabilities, floors, ceilings);
                }
            } else {
                TopoVertex deviceVertex = portToDeviceMap.get(v);
                VertexType deviceVertexType = deviceVertex.getVertexType();
                List<Integer> portInEgBw = portBWs.get(v);
                if(deviceVertexType.equals(VertexType.ROUTER)) {
                    capabilities.add(Layer.MPLS);
                    urn = buildUrn(v, determineDeviceModel(deviceVertexType), capabilities, portInEgBw.get(0),
                            portInEgBw.get(1), floors, ceilings);
                }
                else {
                    urn = buildUrn(v, determineDeviceModel(deviceVertexType), capabilities, portInEgBw.get(0),
                            portInEgBw.get(1), floors, ceilings);
                }
            }
            urnList.add(urn);
        }
        return urn;
    }

    public UrnE buildUrn(TopoVertex vertex, DeviceModel parentModel, Set<Layer> capabilities,
                         List<Integer> floors, List<Integer> ceilings){
        VertexType vertexType = vertex.getVertexType();
        UrnType urnType = determineUrnType(vertexType);
        DeviceType deviceType = determineDeviceType(vertexType);
        IfceType ifceType = determineIfceType(vertexType);
        DeviceModel model = parentModel == null ? determineDeviceModel(vertexType) : parentModel;

        UrnE urn =  UrnE.builder()
                .urn(vertex.getUrn())
                .urnType(urnType)
                .deviceType(deviceType)
                .ifceType(ifceType)
                .deviceModel(model)
                .reservablePssResources(new HashSet<>())
                .valid(true)
                .capabilities(capabilities)
                .build();

        Integer ingressBw = 1000;
        Integer egressBw = 1000;
        if(vertexType.equals(VertexType.PORT)){
            ReservableBandwidthE resvBw = buildReservableBandwidth(ingressBw, egressBw);
            ReservableVlanE resvVlan = buildReservableVlan(buildIntRanges(floors, ceilings));
            urn.setReservableBandwidth(resvBw);
            urn.setReservableVlans(resvVlan);
        }
        return urn;
    }

    public UrnE buildUrn(TopoVertex vertex, DeviceModel parentModel, Set<Layer> capabilities, Integer inBW, Integer egBW,
                         List<Integer> floors, List<Integer> ceilings){
        VertexType vertexType = vertex.getVertexType();
        UrnType urnType = determineUrnType(vertexType);
        DeviceType deviceType = determineDeviceType(vertexType);
        IfceType ifceType = determineIfceType(vertexType);
        DeviceModel model = parentModel == null ? determineDeviceModel(vertexType) : parentModel;

        UrnE urn =  UrnE.builder()
                .urn(vertex.getUrn())
                .urnType(urnType)
                .deviceType(deviceType)
                .ifceType(ifceType)
                .deviceModel(model)
                .reservablePssResources(new HashSet<>())
                .valid(true)
                .capabilities(capabilities)
                .build();

        if(vertexType.equals(VertexType.PORT)){
            ReservableBandwidthE resvBw = buildReservableBandwidth(inBW, egBW);
            ReservableVlanE resvVlan = buildReservableVlan(buildIntRanges(floors, ceilings));
            urn.setReservableBandwidth(resvBw);
            urn.setReservableVlans(resvVlan);
        }
        return urn;
    }



    public UrnAdjcyE buildUrnAdjcy(TopoEdge edge, UrnE a, UrnE z){
        HashMap<Layer, Long> metrics = new HashMap<>();
        metrics.put(edge.getLayer(), edge.getMetric());
        return UrnAdjcyE.builder()
                .a(a)
                .z(z)
                .metrics(metrics)
                .build();
    }

    public TopoEdge buildTopoEdge(TopoVertex a, TopoVertex z, Layer layer, Long metric){
        return TopoEdge.builder()
                .a(a)
                .z(z)
                .layer(layer)
                .metric(metric)
                .build();
    }

    public TopoVertex buildTopoVertex(String name, VertexType type){
        return TopoVertex.builder()
                .vertexType(type)
                .urn(name)
                .build();
    }

    public Set<IntRangeE> buildIntRanges(List<Integer> floors, List<Integer> ceilings){
        Set<IntRangeE> ranges = new HashSet<>();
        for(int i = 0; i < floors.size(); i++){
            Integer floor = floors.get(i);
            Integer ceiling = ceilings.get(i);
            ranges.add(buildIntRange(floor, ceiling));
        }
        return ranges;
    }

    public IntRangeE buildIntRange(Integer floor, Integer ceiling){
        return IntRangeE.builder()
                .floor(floor)
                .ceiling(ceiling)
                .build();
    }

    public ReservableBandwidthE buildReservableBandwidth(Integer azMbps, Integer zaMbps){
        return ReservableBandwidthE.builder()
                .bandwidth(Math.max(azMbps, zaMbps))
                .egressBw(zaMbps)
                .ingressBw(azMbps)
                .build();
    }

    public ReservableVlanE buildReservableVlan(Set<IntRangeE> ranges){
        return ReservableVlanE.builder()
                .vlanRanges(ranges)
                .build();
    }

    public UrnType determineUrnType(VertexType type){
        switch(type){
            case ROUTER:
            case SWITCH: return UrnType.DEVICE;
            case PORT: return UrnType.IFCE;
            default: return UrnType.DEVICE;
        }
    }

    public DeviceType determineDeviceType(VertexType type){
        switch(type){
            case ROUTER: return DeviceType.ROUTER;
            case SWITCH: return DeviceType.SWITCH;
            default: return null;
        }
    }

    public IfceType determineIfceType(VertexType type){
        switch(type){
            case PORT: return IfceType.PORT;
            default: return null;
        }
    }

    public DeviceModel determineDeviceModel(VertexType type){
        switch(type){
            case ROUTER: return DeviceModel.JUNIPER_MX;
            case SWITCH: return DeviceModel.JUNIPER_EX;
            default: return null;
        }
    }

    public UrnE getFromUrnList(String name, List<UrnE> urns){
        Optional<UrnE> optUrn = urns.stream().filter(u -> u.getUrn().equals(name)).findFirst();
        if(optUrn.isPresent()){
            return optUrn.get();
        }
        else{
            return null;
        }
    }


    public TopoVertex getFromSet(String name, Set<TopoVertex> vertices){
        Optional<TopoVertex> optVertex = vertices.stream().filter(v -> v.getUrn().equals(name)).findFirst();
        if(optVertex.isPresent()){
            return optVertex.get();
        }
        return null;
    }

}
