package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.resv.ent.RequestedVlanFixtureE;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.ent.ReservedVlanE;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.enums.VertexType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class VlanSelectionService {

    @Autowired
    private PruningService pruningService;

    public Map<UrnE, Integer> selectVlansForPipe(RequestedVlanPipeE reqPipe, Map<String, UrnE> urnMap, List<ReservedVlanE> reservedVlans,
                                                 List<TopoEdge> azERO, List<TopoEdge> zaERO){

        // Confirm that there is at least one VLAN ID that can support every segment (given what has been reserved so far)
        // Get the available VLANs at all URNs
        Map<UrnE, Set<Integer>> availableVlanMap = buildAvailableVlanIdMap(urnMap, reservedVlans);
        // Get the requested VLANs per Fixture URN
        Map<UrnE, Set<Integer>> requestedVlanMap = buildRequestedVlanIdMap(reqPipe, availableVlanMap);
        // Get the "valid" VLANs per Fixture URN
        Map<UrnE, Set<Integer>> validVlanMap = buildValidVlanIdMap(requestedVlanMap, availableVlanMap);
        // Create a map of URNs to chosen VLAN IDs
        return chooseVlanForPaths(azERO, zaERO, urnMap, availableVlanMap, validVlanMap);

    }


    private Map<UrnE,Integer> chooseVlanForPaths(List<TopoEdge> azERO, List<TopoEdge> zaERO, Map<String, UrnE> urnMap,
                                                 Map<UrnE, Set<Integer>> availableVlanMap,
                                                 Map<UrnE, Set<Integer>> validVlanMap) {
        Map<UrnE, Integer> chosenVlanMap = new HashMap<>();

        // Retrive port URNs for pipe from the AZ and ZA EROs
        Set<UrnE> pipeUrns = retrieveUrnsFromERO(azERO, urnMap);
        pipeUrns.addAll(retrieveUrnsFromERO(zaERO, urnMap));
        pipeUrns = pipeUrns.stream().filter(u -> u.getUrnType().equals(UrnType.IFCE)).collect(Collectors.toSet());

        // Get the VLANs available across the AZ/ZA path
        Set<Integer> availableVlansAcrossPath = findAvailableVlansBidirectional(azERO, zaERO, availableVlanMap, urnMap);
        log.info("Available Vlans Across Path: " + availableVlansAcrossPath.toString());
        // Get the valid VLANs across the fixtures
        Set<Integer> availableVlansAcrossFixtures = getVlansAcrossMap(validVlanMap);
        log.info("Available Vlans Across Fixtures: " + availableVlansAcrossFixtures.toString());

        // If there is any overlap between these two sets, use this ID for everything
        Set<Integer> availableEverywhere = new HashSet<>(availableVlansAcrossFixtures);
        availableEverywhere.retainAll(availableVlansAcrossPath);
        log.info("Available Vlans Everywhere: " + availableEverywhere.toString());
        if(!availableEverywhere.isEmpty()){
            Integer chosenVlan = availableEverywhere.iterator().next();
            chosenVlanMap = pipeUrns.stream().collect(Collectors.toMap(u -> u, u -> chosenVlan));
        }
        // Otherwise, iterate through the valid vlans per fixture
        // For each valid VLAN, see if it is available across the path
        // If so, use that for the path
        // Either way, choose an arbitrary VLAN for the fixture
        else {
            // Remove all fixtures from the set of pipe URNs
            pipeUrns = pipeUrns.stream().filter(urn -> !validVlanMap.containsKey(urn)).collect(Collectors.toSet());
            boolean pipeVlansAssigned = false;

            // For each fixture, find if there are any overlapping VLANs between the path and the fixture
            for (UrnE fixUrn : validVlanMap.keySet()) {
                Set<Integer> overlappingVlans = new HashSet<>(validVlanMap.get(fixUrn));
                overlappingVlans.retainAll(availableVlansAcrossPath);
                log.info("Overlapping VLANs for Path and Fixture " + fixUrn + ": " + overlappingVlans.toString());
                // If there is at least one VLAN ID in common between this fixture and the other ports in the path
                if (!overlappingVlans.isEmpty() && !pipeVlansAssigned) {
                    pipeVlansAssigned = true;
                    // Choose VLAN ID
                    Integer chosenVlan = overlappingVlans.iterator().next();
                    // Assign this VLAN to every URN in the pipe
                    for(UrnE pipeUrn : pipeUrns){
                        chosenVlanMap.put(pipeUrn, chosenVlan);
                    }
                }
                if(!validVlanMap.get(fixUrn).isEmpty()) {
                    chosenVlanMap.put(fixUrn, validVlanMap.get(fixUrn).iterator().next());
                }
                else{
                    chosenVlanMap.put(fixUrn, -1);
                }
            }

            // If no VLAN tags available at fixtures could be used, assign any VLAN available across the path
            // To every port in the path (excluding fixtures)
            if(!pipeVlansAssigned){
                // If there's at least one VLAN ID available across the path
                if(!availableVlansAcrossPath.isEmpty()){
                    // Choose VLAN ID
                    Integer chosenVlan = availableVlansAcrossPath.iterator().next();
                    // Assign this VLAN to every URN in the pipe
                    for(UrnE pipeUrn : pipeUrns){
                        chosenVlanMap.put(pipeUrn, chosenVlan);
                    }
                }
                else{
                    for(UrnE pipeUrn : pipeUrns){
                        chosenVlanMap.put(pipeUrn, -1);
                    }
                }
            }
        }

        return chosenVlanMap;
    }

    private Set<UrnE> retrieveUrnsFromERO(List<TopoEdge> edges, Map<String, UrnE> urnMap){
        Set<UrnE> urns = new HashSet<>();
        for(TopoEdge edge : edges){
            String aUrnString = edge.getA().getUrn();
            UrnE aUrn = urnMap.getOrDefault(aUrnString, null);

            String zUrnString = edge.getZ().getUrn();
            UrnE zUrn = urnMap.getOrDefault(zUrnString, null);

            if(aUrn != null){
                urns.add(aUrn);
            }
            if(zUrn != null){
                urns.add(zUrn);
            }
        }
        return urns;
    }

    private Set<Integer> getVlansAcrossMap(Map<UrnE, Set<Integer>> vlanMap) {
        Set<Integer> overlappingVlans = null;
        for(UrnE urn: vlanMap.keySet()){
            Set<Integer> vlans = vlanMap.get(urn);
            if(overlappingVlans == null){
                overlappingVlans = new HashSet<>(vlans);
            }
            else{
                overlappingVlans.retainAll(vlans);
            }
        }
        return overlappingVlans;
    }

    private Map<UrnE,Set<Integer>> buildRequestedVlanIdMap(RequestedVlanPipeE reqPipe,
                                                           Map<UrnE, Set<Integer>> availableVlanMap) {
        Map<UrnE, Set<Integer>> requestedVlanIdMap = new HashMap<>();

        Set<RequestedVlanFixtureE> fixtures = new HashSet<>(reqPipe.getAJunction().getFixtures());
        fixtures.addAll(reqPipe.getZJunction().getFixtures());

        for(RequestedVlanFixtureE fix : fixtures){
            Set<Integer> requestedVlans = pruningService.getIntegersFromRanges(pruningService.getIntRangesFromString(fix.getVlanExpression()));
            if(requestedVlans.isEmpty()){
                requestedVlanIdMap.put(fix.getPortUrn(), availableVlanMap.get(fix.getPortUrn()));
            }
            else {
                requestedVlanIdMap.put(fix.getPortUrn(), requestedVlans);
            }
        }
        return requestedVlanIdMap;

    }

    private Map<UrnE, Set<Integer>> buildAvailableVlanIdMap(Map<String, UrnE> urnMap, List<ReservedVlanE> reservedVlans) {
        // Build empty map of available VLAN IDs per URN
        Map<UrnE, Set<Integer>> availableVlanIdMap = new HashMap<>();

        // Get map of all reserved VLAN IDs per URN
        Map<UrnE, Set<Integer>> reservedVlanIdMap = buildReservedVlanIdMap(urnMap, reservedVlans);
        log.info("Reserved VLAN ID Map: " + reservedVlanIdMap.toString());

        // Get map of all reservable VLAN IDs per URN
        Map<UrnE, Set<Integer>> reservableVlanIdMap = buildReservableVlanIdMap(urnMap);
        log.info("Reservable VLAN ID Map: " + reservableVlanIdMap.toString());

        urnMap.values().stream().filter(urn -> urn.getUrnType().equals(UrnType.IFCE)).forEach(urn -> {
            Set<Integer> availableIds = reservableVlanIdMap.get(urn);
            availableIds.removeAll(reservedVlanIdMap.get(urn));
            availableVlanIdMap.put(urn, availableIds);
        });
        log.info("Available VLAN ID Map: " + availableVlanIdMap.toString());

        return availableVlanIdMap;
    }

    private Map<UrnE,Set<Integer>> buildReservableVlanIdMap(Map<String, UrnE> urnMap) {
        Map<UrnE, Set<Integer>> reservableVlanIdMap = new HashMap<>();

        urnMap.values().stream().filter(urn -> urn.getUrnType().equals(UrnType.IFCE)).forEach(urn -> {
            List<IntRange> ranges = urn.getReservableVlans().getVlanRanges().stream().map(IntRangeE::toDtoIntRange).collect(Collectors.toList());
            reservableVlanIdMap.put(urn, pruningService.getIntegersFromRanges(ranges));
        });
        return reservableVlanIdMap;
    }

    private Map<UrnE, Set<Integer>> buildReservedVlanIdMap(Map<String, UrnE> urnMap, List<ReservedVlanE> reservedVlans){
        Map<UrnE, Set<Integer>> reservedVlanIdMap = new HashMap<>();
        urnMap.values().stream().filter(urn -> urn.getUrnType().equals(UrnType.IFCE)).forEach(urn -> reservedVlanIdMap.put(urn, new HashSet<>()));

        for(ReservedVlanE rsvVlan : reservedVlans){
            Integer vlanId = rsvVlan.getVlan();
            UrnE urn = rsvVlan.getUrn();
            reservedVlanIdMap.get(urn).add(vlanId);
        }
        return reservedVlanIdMap;
    }

    private Map<UrnE, Set<Integer>> buildValidVlanIdMap(Map<UrnE, Set<Integer>> requestedVlanMap,
                                                        Map<UrnE, Set<Integer>> availableVlanMap) {
        Map<UrnE, Set<Integer>> validVlanIdMap = new HashMap<>();
        for(UrnE urn : requestedVlanMap.keySet()){
            Set<Integer> requestedVlans = requestedVlanMap.get(urn);
            Set<Integer> availableVlans = availableVlanMap.get(urn);

            availableVlans.retainAll(requestedVlans);
            validVlanIdMap.put(urn, availableVlans);
        }
        return validVlanIdMap;
    }

    private Set<Integer> findAvailableVlansBidirectional(List<TopoEdge> azERO, List<TopoEdge> zaERO,
                                                         Map<UrnE, Set<Integer>> availableVlanMap, Map<String, UrnE> urnMap) {
        // Ignore the first and last edges of each ERO
        // These edges connect to fixtures
        Set<Integer> availableVlans = null;

        // Get a list of all non-fixture to device edges

        List<TopoEdge> combinedEdges = azERO.subList(1, azERO.size()-1)
                .stream()
                .filter(e -> !e.getLayer().equals(Layer.MPLS))
                .collect(Collectors.toList());
        combinedEdges.addAll(
                zaERO.subList(1, zaERO.size()-1)
                        .stream()
                        .filter(e -> !e.getLayer().equals(Layer.MPLS))
                        .collect(Collectors.toList()));

        // Loop through all edges in the AZ / ZA combined path
        // Ignore MPLS edges
        for(TopoEdge edge : combinedEdges){
            if(edge.getA().getVertexType().equals(VertexType.PORT)){
                Set<Integer> vlans = retrieveAvailableVlans(edge.getA(), availableVlanMap, urnMap);
                if(availableVlans == null){
                    availableVlans = new HashSet<>(vlans);
                }
                else{
                    availableVlans.retainAll(vlans);
                }
            }
            if(edge.getZ().getVertexType().equals(VertexType.PORT)){
                Set<Integer> vlans = retrieveAvailableVlans(edge.getZ(), availableVlanMap, urnMap);
                if(availableVlans == null){
                    availableVlans = new HashSet<>(vlans);
                }
                else{
                    availableVlans.retainAll(vlans);
                }
            }
        }
        return availableVlans != null ? availableVlans : new HashSet<>();
    }

    private Set<Integer> retrieveAvailableVlans(TopoVertex v, Map<UrnE, Set<Integer>> availableVlanMap,
                                                Map<String, UrnE> urnMap){
        String aUrnString = v.getUrn();
        UrnE urn = urnMap.getOrDefault(aUrnString, null);
        return availableVlanMap.getOrDefault(urn, new HashSet<>());
    }
}
