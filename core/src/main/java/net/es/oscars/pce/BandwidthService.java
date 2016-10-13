package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.dto.topo.VertexType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class BandwidthService {

    @Autowired
    private ReservedBandwidthRepository resvBwRepo;

    @Autowired
    private UrnRepository urnRepository;


    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * BUILD RESERVED/AVAILABLE/REQUESTED BANDWIDTH COLLECTIONS/MAPS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Given a set of reserved junctions and reserved MPLS / ETHERNET pipes, extract the reserved bandwidth objects
     * which fall within the requested schedule period and return them all together as a list
     *
     * @param reservedJunctions - Set of reserved ethernet junctions
     * @param reservedMplsPipes - Set of reserved MPLS pipes
     * @param reservedEthPipes  - Set of reserved Ethernet pipes
     * @param sched             - The requested schedule (start and end date)
     * @return List of all reserved bandwidth contained within reserved pipes and the reserved repository.
     */
    public List<ReservedBandwidthE> createReservedBandwidthList(Set<ReservedVlanJunctionE> reservedJunctions,
                                                                Set<ReservedMplsPipeE> reservedMplsPipes,
                                                                Set<ReservedEthPipeE> reservedEthPipes,
                                                                ScheduleSpecificationE sched) {
        // Retrieve all bandwidth reserved so far from pipes & junctions
        List<ReservedBandwidthE> rsvBandwidths = getReservedBandwidthsFromJunctions(reservedJunctions);
        rsvBandwidths.addAll(getReservedBandwidthsFromMplsPipes(reservedMplsPipes));
        rsvBandwidths.addAll(getReservedBandwidthsFromEthPipes(reservedEthPipes));
        rsvBandwidths.addAll(getReservedBandwidthFromRepo(sched.getNotBefore(), sched.getNotAfter()));

        return rsvBandwidths;
    }


    /**
     * Build a mapping of UrnE objects to ReservedBandwidthE objects.
     *
     * @param rsvBwList A list of all bandwidth reserved.
     * @return A map of UrnE to ReservedBandwidthE objects
     */
    public Map<UrnE, List<ReservedBandwidthE>> buildReservedBandwidthMap(List<ReservedBandwidthE> rsvBwList) {
        Map<UrnE, List<ReservedBandwidthE>> map = new HashMap<>();
        for (ReservedBandwidthE resv : rsvBwList) {
            UrnE resvUrn = resv.getUrn();
            if (!map.containsKey(resvUrn)) {
                map.put(resvUrn, new ArrayList<>());
            }
            map.get(resvUrn).add(resv);
        }
        return map;
    }

    /**
     * Build a map of the available bandwidth at each URN. For each URN, there is a map of "Ingress" and "Egress"
     * bandwidth available. Only port URNs can be found in this map.
     *
     * @param rsvBandwidths - A list of all bandwidth reserved so far
     * @return A mapping of URN to Ingress/Egress bandwidth availability
     */
    public Map<UrnE, Map<String, Integer>> buildBandwidthAvailabilityMap(List<ReservedBandwidthE> rsvBandwidths) {
        // Build a map, allowing us to retrieve a list of ReservedBandwidth given the associated URN
        Map<UrnE, List<ReservedBandwidthE>> resvBwMap = buildReservedBandwidthMap(rsvBandwidths);

        // Build a map, allowing us to retrieve the available "Ingress" and "Egress" bandwidth at each associated URN
        Map<UrnE, Map<String, Integer>> availBwMap = new HashMap<>();
        urnRepository.findAll()
                .stream()
                .filter(urn -> urn.getReservableBandwidth() != null)
                .forEach(urn -> availBwMap.put(urn, buildBandwidthAvailabilityMapUrn(urn, urn.getReservableBandwidth(), resvBwMap)));

        return availBwMap;
    }

    /**
     * Build a map of the available bandwidth at each URN. For each URN, there is a map of "Ingress" and "Egress"
     * bandwidth available. Only port URNs can be found in this map.
     *
     * @param rsvBandwidths - A list of all bandwidth reserved so far
     * @param urns          - A list of UrnE objects
     * @return A mapping of URN to Ingress/Egress bandwidth availability
     */
    public Map<UrnE, Map<String, Integer>> buildBandwidthAvailabilityMapWithUrns(List<ReservedBandwidthE> rsvBandwidths,
                                                                                 List<UrnE> urns) {
        // Build a map, allowing us to retrieve a list of ReservedBandwidth given the associated URN
        Map<UrnE, List<ReservedBandwidthE>> resvBwMap = buildReservedBandwidthMap(rsvBandwidths);

        // Build a map, allowing us to retrieve the available "Ingress" and "Egress" bandwidth at each associated URN
        Map<UrnE, Map<String, Integer>> availBwMap = new HashMap<>();
        urns.stream()
                .filter(urn -> urn.getReservableBandwidth() != null)
                .forEach(urn -> availBwMap.put(urn, buildBandwidthAvailabilityMapUrn(urn, urn.getReservableBandwidth(), resvBwMap)));

        return availBwMap;
    }

    /**
     * Determine how much Ingress/Egress bandwidth is still available at a URN. If the list of reserved bandwidths
     * is empty, then all of the Reservable Bandwidth at that URN is available. Otherwise,
     * subtract the sum Ingress/Egress bandwidth from the maximum reservable bandwidth at that URN.
     *
     * @param bandwidth - ReservableBandwidthE object, which contains the maximum Ingress/Egress bandwidth for a given URN
     * @param resvBwMap - A Mapping from a URN to a list of Reserved Bandwidths at that URN.
     * @return A map containing the net available ingress/egress bandwidth at a URN
     */
    public Map<String, Integer> buildBandwidthAvailabilityMapUrn(UrnE urn, ReservableBandwidthE bandwidth,
                                                                 Map<UrnE, List<ReservedBandwidthE>> resvBwMap) {
        Map<String, Integer> availBw = new HashMap<>();
        availBw.put("Ingress", bandwidth.getIngressBw());
        availBw.put("Egress", bandwidth.getEgressBw());
        if (resvBwMap.containsKey(urn)) {
            List<ReservedBandwidthE> resvBwList = resvBwMap.get(urn);
            Integer sumIngress = 0;
            Integer sumEgress = 0;
            for (ReservedBandwidthE resv : resvBwList) {
                sumIngress += resv.getInBandwidth();
                sumEgress += resv.getEgBandwidth();
            }
            availBw.put("Ingress", Math.max(bandwidth.getIngressBw() - sumIngress, 0));
            availBw.put("Egress", Math.max(bandwidth.getEgressBw() - sumEgress, 0));
        }
        return availBw;
    }

    /**
     * Build a map of the requested bandwidth at each port TopoVertex contained within the passed in EROs
     *
     * @param EROs       - List of paths
     * @param bandwidths - List of bandwidths
     * @return A mapping from TopoVertex (ports only) to requested "Ingress" and "Egress" bandwidth
     */
    public Map<TopoVertex, Map<String, Integer>> buildRequestedBandwidthMap(List<List<TopoEdge>> EROs,
                                                                            List<Integer> bandwidths) {
        // Map a port node to a map of "Ingress" and "Egress" requested bandwidth values
        Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap = new HashMap<>();

        // Iterate through the AZ edges, update the map for each port node found in the path
        for (Integer i = 0; i < EROs.size(); i++) {
            List<TopoEdge> edges = EROs.get(i);
            Integer bandwidth = bandwidths.get(i);
            for (TopoEdge edge : edges) {
                updateRequestedBandwidthMap(edge, bandwidth, requestedBandwidthMap);
            }
        }

        return requestedBandwidthMap;
    }

    /**
     * Build an initial map of requested bandwidth in the Ingress and Egress directions
     *
     * @return A new map with 0 Ingress and Egress bandwidth.
     */
    private Map<String, Integer> buildZeroRequestedBandwidthMap() {
        Map<String, Integer> initialMap = new HashMap<>();
        initialMap.put("Ingress", 0);
        initialMap.put("Egress", 0);
        return initialMap;
    }

    /**
     * Update the values in a mapping of port vertices to requested Ingress/Egress bandwidth.
     *
     * @param edge                  - An edge, with a vertex on the "A" side and on the "Z" side
     * @param bandwidth             - The requested bandwidth in one direction
     * @param requestedBandwidthMap - The soon-to-be updated map of requested bandwidth per port
     */
    private void updateRequestedBandwidthMap(TopoEdge edge, Integer bandwidth,
                                             Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap) {

        // Retrieve the "A" side of the edge
        TopoVertex nodeA = edge.getA();
        // Retrieve the "Z" side of the edge
        TopoVertex nodeZ = edge.getZ();

        // Add node A to the map if it is a port and it is not already in the map
        if (nodeA.getVertexType().equals(VertexType.PORT) && !requestedBandwidthMap.containsKey(nodeA)) {
            requestedBandwidthMap.put(nodeA, buildZeroRequestedBandwidthMap());
        }
        // Add node Z to the map if it is a port and it is not already in the map
        if (nodeZ.getVertexType().equals(VertexType.PORT) && !requestedBandwidthMap.containsKey(nodeZ)) {
            requestedBandwidthMap.put(nodeZ, buildZeroRequestedBandwidthMap());
        }

        // All Cases: Add the requested bandwidth to the amount already stored in the map
        // Case 1: portA -> deviceZ -- portA = ingress
        if (nodeA.getVertexType().equals(VertexType.PORT) && !nodeZ.getVertexType().equals(VertexType.PORT)) {
            Integer updatedIngress = requestedBandwidthMap.get(nodeA).get("Ingress") + bandwidth;
            requestedBandwidthMap.get(nodeA).put("Ingress", updatedIngress);
        }
        // Case 2: deviceA -> portZ -- portZ = egress
        else if (!nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT)) {
            Integer updatedEgress = requestedBandwidthMap.get(nodeZ).get("Egress") + bandwidth;
            requestedBandwidthMap.get(nodeZ).put("Egress", updatedEgress);
        }

    }

    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * GET RESERVED BANDWIDTH COLLECTIONS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Retrieve all reserved bandwidths from a set of reserved junctions.
     *
     * @param junctions - Set of reserved junctions.
     * @return A list of all bandwidth reserved at those junctions.
     */
    public List<ReservedBandwidthE> getReservedBandwidthsFromJunctions(Set<ReservedVlanJunctionE> junctions) {
        return junctions
                .stream()
                .map(ReservedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(ReservedVlanFixtureE::getReservedBandwidth)
                .collect(Collectors.toList());
    }


    /**
     * Retrieve all Reserved Bandwidth from a set of reserved MPLS pipes.
     *
     * @param reservedPipes - Set of reserved pipes
     * @return A list of all reserved bandwidth within the set of reserved MPLS pipes.
     */
    public List<ReservedBandwidthE> getReservedBandwidthsFromMplsPipes(Set<ReservedMplsPipeE> reservedPipes) {
        return reservedPipes
                .stream()
                .map(ReservedMplsPipeE::getReservedBandwidths)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all Reserved Bandwidth from a set of reserved Ethernet pipes.
     *
     * @param reservedPipes - Set of reserved pipes
     * @return A list of all reserved bandwidth within the set of reserved Ethernet pipes.
     */
    public List<ReservedBandwidthE> getReservedBandwidthsFromEthPipes(Set<ReservedEthPipeE> reservedPipes) {
        return reservedPipes
                .stream()
                .map(ReservedEthPipeE::getReservedBandwidths)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get a list of all bandwidth reserved between a start and end date/time.
     *
     * @param start - The start of the time range
     * @param end   - The end of the time range
     * @return A list of reserved bandwidth
     */
    public List<ReservedBandwidthE> getReservedBandwidthFromRepo(Date start, Date end) {
        // Get all Reserved Bandwidth between start and end
        Optional<List<ReservedBandwidthE>> optResvBw = resvBwRepo.findOverlappingInterval(start.toInstant(), end.toInstant());
        return optResvBw.isPresent() ? optResvBw.get() : new ArrayList<>();
    }

    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * EVALUATE EDGES/NODES/URNs/ETC to determine if they have sufficient BW
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Examine all AZ and ZA edges, confirm that the requested bandwidth can be supported given the available bandwidth.
     *
     * @param urnMap     - A map of URN string to URN objects
     * @param azERO      - The path in the A->Z direction
     * @param zaERO      - The path in the Z->A direction
     * @param availBwMap - A map of bandwidth availability
     * @return True, if there is sufficient bandwidth across all edges. False, otherwise.
     */
    public boolean evaluateBandwidthEROBi(Map<String, UrnE> urnMap, Integer azMbps, Integer zaMbps, List<TopoEdge> azERO,
                                          List<TopoEdge> zaERO, Map<UrnE, Map<String, Integer>> availBwMap) {

        // For the AZ direction, fail the test if there is insufficient bandwidth
        if (!evaluateBandwidthERO(azERO, urnMap, availBwMap, azMbps, zaMbps)) {
            return false;
        }

        // For each ZA direction, fail the test if there is insufficient bandwidth
        return evaluateBandwidthERO(zaERO, urnMap, availBwMap, azMbps, zaMbps);
    }

    /**
     * Given a particular list of edges, iterate and confirm that there is sufficient bandwidth available in both directions
     *
     * @param ERO        - A series of edges
     * @param urnMap     - A mapping of URN strings to URN objects
     * @param availBwMap - A mapping of URN objects to lists of available bandwidth for that URN
     * @param azMbps     - The bandwidth in the AZ direction
     * @param zaMbps     - The bandwidth the ZA direction
     * @return True, if the segment can support the requested bandwidth. False, otherwise.
     */
    private boolean evaluateBandwidthERO(List<TopoEdge> ERO, Map<String, UrnE> urnMap,
                                         Map<UrnE, Map<String, Integer>> availBwMap, Integer azMbps,
                                         Integer zaMbps) {
        // For each edge in that list
        for (TopoEdge edge : ERO) {
            // Retrieve the URNs
            String urnStringA = edge.getA().getUrn();
            String urnStringZ = edge.getZ().getUrn();
            if (!urnMap.containsKey(urnStringA) || !urnMap.containsKey(urnStringZ)) {
                return false;
            }
            UrnE urnA = urnMap.get(urnStringA);
            UrnE urnZ = urnMap.get(urnStringZ);

            // If URN A has reservable bandwidth, confirm that there is enough available
            if (urnA.getReservableBandwidth() != null) {
                if (!evaluateBandwidthURN(urnA, availBwMap, azMbps, zaMbps)) {
                    return false;
                }
            }

            // If URN Z has reservable bandwidth, confirm that there is enough available
            if (urnZ.getReservableBandwidth() != null) {
                if (!evaluateBandwidthURN(urnZ, availBwMap, azMbps, zaMbps)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Given a list of edges, confirm that there is sufficient bandwidth available in the one direction
     *
     * @param ERO        - A series of edges
     * @param urnMap     - A mapping of URN strings to URN objects
     * @param availBwMap - A mapping of URN objects to lists of reserved bandwidth for that URN
     * @param bwMbps     - The bandwidth in the specified direction
     * @return True, if the segment can support the requested bandwidth. False, otherwise.
     */
    public boolean evaluateBandwidthEROUni(List<TopoEdge> ERO, Map<String, UrnE> urnMap,
                                           Map<UrnE, Map<String, Integer>> availBwMap, Integer bwMbps) {
        // For each edge in that list
        for (TopoEdge edge : ERO) {
            Map<UrnE, Boolean> urnIngressDirectionMap = new HashMap<>();
            TopoVertex nodeA = edge.getA();
            TopoVertex nodeZ = edge.getZ();
            String urnStringA = nodeA.getUrn();
            String urnStringZ = nodeZ.getUrn();

            if (!urnMap.containsKey(urnStringA) || !urnMap.containsKey(urnStringZ)) {
                return false;
            }

            UrnE urnA = urnMap.get(urnStringA);
            UrnE urnZ = urnMap.get(urnStringZ);

            // From Port to Device -- Consider Ingress direction for this Port
            if (nodeA.getVertexType().equals(VertexType.PORT) && !nodeZ.getVertexType().equals(VertexType.PORT)) {
                urnIngressDirectionMap.put(urnA, true);
            }
            // From Device to Port -- Consider Egress direction for this Port
            else if (!nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT)) {
                urnIngressDirectionMap.put(urnZ, false);
            }
            // From Port to Port -- Consider Egress for portA, and Ingress for portZ
            else if (nodeA.getVertexType().equals(VertexType.PORT) && nodeZ.getVertexType().equals(VertexType.PORT)) {
                urnIngressDirectionMap.put(urnA, false);
                urnIngressDirectionMap.put(urnZ, true);
            }

            // If URN A has reservable bandwidth, confirm that there is enough available
            if (urnA.getReservableBandwidth() != null) {
                boolean ingressDirection = urnIngressDirectionMap.get(urnA);

                if (!evaluateBandwidthURNUni(urnA, availBwMap, bwMbps, ingressDirection)) {
                    return false;
                }
            }

            // If URN Z has reservable bandwidth, confirm that there is enough available
            if (urnZ.getReservableBandwidth() != null) {
                boolean ingressDirection = urnIngressDirectionMap.get(urnZ);

                if (!evaluateBandwidthURNUni(urnZ, availBwMap, bwMbps, ingressDirection)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Given a specific URN, determine if there is enough bandwidth available to support the requested bandwidth
     *
     * @param urn        - The URN
     * @param availBwMap - Map of URNs to available Bandwidth
     * @param inMbps     - Requested ingress Mbps
     * @param egMbps     - Requested egress Mbps
     * @return True, if there is enough available bandwidth at the URN. False, otherwise
     */
    public boolean evaluateBandwidthURN(UrnE urn, Map<UrnE, Map<String, Integer>> availBwMap,
                                        Integer inMbps, Integer egMbps) {
        if (!availBwMap.keySet().contains(urn)) {
            log.error("could not locate available bw map for urn " + urn.getUrn());
            return false;
        }
        Map<String, Integer> bwAvail = availBwMap.get(urn);
        if (bwAvail.get("Ingress") < inMbps || bwAvail.get("Egress") < egMbps) {
            log.error("Insufficient Bandwidth at " + urn.toString() + ". Requested: " +
                    inMbps + " In and " + egMbps + " Out. Available: " + bwAvail.get("Ingress") +
                    " In and " + bwAvail.get("Egress") + " Out.");
            return false;
        }
        return true;
    }

    /**
     * Given a specific URN, which is traversed in the same direction by the forward and reverse path, determine if there is enough bandwidth available to support the requested bandwidth
     *
     * @param urn        - The URN
     * @param availBwMap - Map of URNs to available Bandwidth
     * @param inMbpsAZ   - Requested ingress Mbps in the A->Z direction
     * @param egMbpsAZ   - Requested egress Mbps in the A->Z direction
     * @param inMbpsZA   - Requested ingress Mbps in the Z->A direction
     * @param egMbpsZA   - Requested egress Mbps in the Z->A direction
     * @return True, if there is enough available bandwidth at the URN. False, otherwise
     */
    public boolean evaluateBandwidthSharedURN(UrnE urn, Map<UrnE, Map<String, Integer>> availBwMap, Integer inMbpsAZ, Integer egMbpsAZ, Integer inMbpsZA, Integer egMbpsZA) {
        Map<String, Integer> bwAvail = availBwMap.get(urn);
        if ((bwAvail.get("Ingress") < (inMbpsAZ + inMbpsZA)) || (bwAvail.get("Egress") < (egMbpsAZ + egMbpsZA))) {
            log.error("Insufficient Bandwidth at " + urn.toString() + ". Requested: " + (inMbpsAZ + inMbpsZA) + " In and " + (egMbpsAZ + egMbpsZA) + " Out. Available: " + bwAvail.get("Ingress") + " In and " + bwAvail.get("Egress") + " Out.");
            return false;
        }
        return true;
    }

    /**
     * Given a specific URN, determine if there is enough bandwidth available to support the requested bandwidth in a specific (ingress/egress) direction
     *
     * @param urn              - The URN
     * @param availBwMap       - Map of URNs to Available Bandwidth
     * @param bwMbps           - Requested Mbps
     * @param ingressDirection - True if pruning is done based upon port ingress b/w, false if done by egress b/w
     * @return True, if there is enough available bandwidth at the URN. False, otherwise
     */
    private boolean evaluateBandwidthURNUni(UrnE urn, Map<UrnE, Map<String, Integer>> availBwMap,
                                            Integer bwMbps, boolean ingressDirection) {
        Map<String, Integer> bwAvail = availBwMap.get(urn);

        Integer unidirectionalBW;
        String direction = "";

        if (ingressDirection) {
            unidirectionalBW = bwAvail.get("Ingress");
            direction = " In.";
        } else {
            unidirectionalBW = bwAvail.get("Egress");
            direction = " Out.";
        }

        if (unidirectionalBW < bwMbps) {
            log.error("Insufficient Bandwidth at " + urn.toString() + ". Requested: " + bwMbps + direction);
            return false;
        }

        return true;
    }


    /**
     * Confirm that a requested VLAN junction supports the requested bandwidth. Checks each fixture of the junction.
     *
     * @param req_j              - The requested junction.
     * @param reservedBandwidths - The reserved bandwidth.
     * @return True, if there is enough bandwidth at every fixture. False, otherwise.
     */
    public boolean evaluateBandwidthJunction(RequestedVlanJunctionE req_j, List<ReservedBandwidthE> reservedBandwidths) {

        // All requested fixtures on this junction
        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();


        // Get map of "Ingress" and "Egress" bandwidth availability
        Map<UrnE, Map<String, Integer>> availBwMap = buildBandwidthAvailabilityMap(reservedBandwidths);

        // For each requested fixture,
        for (RequestedVlanFixtureE reqFix : reqFixtures) {
            // Confirum that there is enough available bandwidth at that URN
            if (!evaluateBandwidthURN(reqFix.getPortUrn(), availBwMap, reqFix.getInMbps(), reqFix.getEgMbps())) {
                return false;
            }
        }
        return true;
    }


    /**
     * Evaluate an edge to determine if the nodes on either end of the edge support the requested
     * az and za bandwidths. An edge will only fail the test if one or both URNs (corresponding to the nodes):
     * (1) have valid reservable bandwidth fields, and (2) the URN(s) do not have sufficient available bandwidth
     * available in both the az and za directions (egress and ingress).
     *
     * @param edge       - The edge to be evaluated.
     * @param azBw       - The requested bandwidth in one direction.
     * @param zaBw       - The requested bandwidth in the other direction.
     * @param urnMap     - Map of URN name to UrnE object.
     * @param availBwMap - Map of UrnE objects to "Ingress" and "Egress" Available Bandwidth
     * @return True if there is sufficient reservable bandwidth, False otherwise.
     */
    public boolean evaluateBandwidthEdge(TopoEdge edge, Integer azBw, Integer zaBw, Map<String, UrnE> urnMap,
                                         Map<UrnE, Map<String, Integer>> availBwMap) {

        UrnE aUrn = urnMap.get(edge.getA().getUrn());
        UrnE zUrn = urnMap.get(edge.getZ().getUrn());
        // At least one of the two has reservable bandwidth, so check the valid nodes to determine
        // If they have sufficient bandwidth. In the case of a (device, port) edge, only the port must be checked.
        // If it is a (port, port) edge, then both must be checked.
        boolean aPasses = true;
        boolean zPasses = true;
        if (availBwMap.containsKey(aUrn)) {
            // Get a map of the available Ingress/Egress bandwidth for URN a
            Map<String, Integer> aAvailBwMap = availBwMap.get(aUrn);
            aPasses = aAvailBwMap.get("Egress") >= azBw && aAvailBwMap.get("Ingress") >= zaBw;
        }
        if (availBwMap.containsKey(zUrn)) {
            // Get a map of the available Ingress/Egress bandwidth for URN z
            Map<String, Integer> zAvailBwMap = availBwMap.get(zUrn);
            zPasses = zAvailBwMap.get("Ingress") >= azBw && zAvailBwMap.get("Egress") >= zaBw;
        }

        return aPasses && zPasses;

    }

    /**
     * Evaluate an edge to determine if the nodes on either end of the edge support the requested
     * unidriectional bandwidth. An edge will only fail the test if one or both URNs (corresponding to the nodes):
     * (1) have valid reservable bandwidth fields, and (2) the URN(s) do not have sufficient available bandwidth
     * available in the unique direction.
     *
     * @param edge       - The edge to be evaluated.
     * @param theBw      - The requested bandwidth in one direction.
     * @param urnMap     - Map of URN name to UrnE object.
     * @param availBwMap - Map of UrnE objects to "Ingress" and "Egress" Available Bandwidth
     * @return True if there is sufficient reservable bandwidth, False otherwise.
     */
    public boolean evaluateBandwidthEdgeUni(TopoEdge edge, Integer theBw, Map<String, UrnE> urnMap,
                                            Map<UrnE, Map<String, Integer>> availBwMap) {

        if (!edge.getA().getVertexType().equals(VertexType.PORT) || !edge.getZ().getVertexType().equals(VertexType.PORT))
            return true;

        UrnE aUrn = urnMap.get(edge.getA().getUrn());
        UrnE zUrn = urnMap.get(edge.getZ().getUrn());
        // At least one of the two has reservable bandwidth, so check the valid nodes to determine
        // If they have sufficient bandwidth. In the case of a (device, port) edge, only the port must be checked.
        // If it is a (port, port) edge, then both must be checked.
        boolean aPasses = true;
        boolean zPasses = true;
        if (availBwMap.containsKey(aUrn)) {
            // Get a map of the available Ingress/Egress bandwidth for URN a
            Map<String, Integer> aAvailBwMap = availBwMap.get(aUrn);
            aPasses = aAvailBwMap.get("Egress") >= theBw;
        }
        if (availBwMap.containsKey(zUrn)) {
            // Get a map of the available Ingress/Egress bandwidth for URN z
            Map<String, Integer> zAvailBwMap = availBwMap.get(zUrn);
            zPasses = zAvailBwMap.get("Ingress") >= theBw;
        }

        return aPasses && zPasses;
    }

}
