package net.es.oscars.helpers.test;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.dto.topo.enums.*;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.*;
import net.es.oscars.topo.pop.TopoFileImporter;
import net.es.oscars.topo.pop.TopoImporter;
import net.es.oscars.topo.prop.TopoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class RepoEntityBuilder {

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private ReservedBandwidthRepository reservedBandwidthRepo;


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
        return addUrnToList(v, urnList, portToDeviceMap, null, floors, ceilings);
    }

    public UrnE addUrnToList(TopoVertex v, List<UrnE> urnList, Map<TopoVertex,TopoVertex> portToDeviceMap,
                             Map<TopoVertex, List<Integer>> portBWs, List<Integer> floors, List<Integer> ceilings){
        UrnE urn = getFromUrnList(v.getUrn(), urnList);
        if(urn == null){
            Set<Layer> capabilities = new HashSet<>();
            capabilities.add(Layer.INTERNAL);
            capabilities.add(Layer.ETHERNET);
            // If vertex is a port, get it's parent's type instead
            VertexType typeOfParentOrSelf = v.getVertexType().equals(VertexType.PORT) ? portToDeviceMap.get(v).getVertexType() : v.getVertexType();
            if(typeOfParentOrSelf.equals(VertexType.ROUTER)){
                capabilities.add(Layer.MPLS);
            }

            if(v.getVertexType().equals(VertexType.PORT)){
                List<Integer> portInEgBw = portBWs != null ? portBWs.get(v) : Arrays.asList(1000, 1000);
                urn = buildPortUrn(v, typeOfParentOrSelf, capabilities, portInEgBw.get(0), portInEgBw.get(1), floors, ceilings);
            }
            else{
                urn = buildUrn(v, typeOfParentOrSelf, capabilities, floors, ceilings);
            }

            urnList.add(urn);
        }
        return urn;
    }

    public UrnE buildUrn(TopoVertex vertex, VertexType vertexType, Set<Layer> capabilities,
                         List<Integer> floors, List<Integer> ceilings){

        DeviceType deviceType = determineDeviceType(vertexType);
        UrnType urnType = determineUrnType(vertexType);
        DeviceModel deviceModel = determineDeviceModel(vertexType);
        IfceType ifceType = determineIfceType(vertexType);

        UrnE urn =  UrnE.builder()
                .urn(vertex.getUrn())
                .urnType(urnType)
                .deviceType(deviceType)
                .ifceType(ifceType)
                .deviceModel(deviceModel)
                .reservablePssResources(new HashSet<>())
                .valid(true)
                .capabilities(capabilities)
                .build();

        if(deviceType.equals(DeviceType.SWITCH)) {
            ReservableVlanE resvVlan = buildReservableVlan(buildIntRanges(floors, ceilings));
            urn.setReservableVlans(resvVlan);
        }
        return urn;
    }

    public UrnE buildPortUrn(TopoVertex vertex, VertexType parentType, Set<Layer> capabilities, Integer inBW, Integer egBW,
                         List<Integer> floors, List<Integer> ceilings){
        VertexType vertexType = vertex.getVertexType();
        UrnType urnType = determineUrnType(vertexType);
        DeviceType deviceType = determineDeviceType(vertexType);
        IfceType ifceType = determineIfceType(vertexType);
        DeviceModel parentModel = determineDeviceModel(parentType);

        UrnE urn =  UrnE.builder()
                .urn(vertex.getUrn())
                .urnType(urnType)
                .deviceType(deviceType)
                .ifceType(ifceType)
                .deviceModel(parentModel)
                .reservablePssResources(new HashSet<>())
                .valid(true)
                .capabilities(capabilities)
                .build();

        ReservableBandwidthE resvBw = buildReservableBandwidth(inBW, egBW);
        urn.setReservableBandwidth(resvBw);
        if(parentType.equals(VertexType.ROUTER)){
            ReservableVlanE resvVlan = buildReservableVlan(buildIntRanges(floors, ceilings));
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

    public void importEsnet() {
        TopoProperties topoProperties = new TopoProperties();
        topoProperties.setDevicesFilename("config/esnet-devices.json");
        topoProperties.setAdjciesFilename("config/esnet-adjacencies.json");
        log.info("Building ESnet topology");
        TopoFileImporter topoImporter = new TopoFileImporter(urnRepo, adjcyRepo, topoProperties);
    }

    public void reserveBandwidth(List<String> reservedPortNames, List<Instant> reservedStartTimes,
                                  List<Instant> reservedEndTimes, List<Integer> inBandwidths, List<Integer> egBandwidths) {
        List<ReservedBandwidthE> reservedBandwidths = new ArrayList<>();
        for(Integer index = 0; index < reservedPortNames.size(); index++){
            reservedBandwidths.add(ReservedBandwidthE.builder()
                    .urn(reservedPortNames.get(index))
                    .beginning(reservedStartTimes.get(index))
                    .ending(reservedEndTimes.get(index))
                    .inBandwidth(inBandwidths.get(index))
                    .egBandwidth(egBandwidths.get(index))
                    .containerConnectionId("testConnectionID")
                    .build());
        }
        reservedBandwidthRepo.save(reservedBandwidths);
    }
}
