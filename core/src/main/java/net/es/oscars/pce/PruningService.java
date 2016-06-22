package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.resv.ent.RequestedVlanFixtureE;
import net.es.oscars.resv.ent.RequestedVlanJunctionE;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/***
 * Pruning Service. This class can take in a variety of inputs (Bidrectional bandwidth, a pair of unidirectional
 * bandwidths (AZ/ZA), specified set of desired VLAN tags, or a logical pipe (containing those other elements)).
 * With any/all of those inputs, edges are removed from the passed in topology that do not meet the specified
 * bandwidth/VLAN requirements.
 */
@Slf4j
@Service
@Component
public class PruningService {

    @Autowired
    private UrnRepository urnRepo;


    /**
     * Prune the topology using a specified bidirectional bandwidth, set of VLANs, and a list of URNs to
     * match to the topology.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBwVlans(Topology topo, Integer Bw, String vlans, List<UrnE> urns){
        return pruneTopology(topo, Bw, Bw, getIntRangesFromString(vlans), urns);
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth, and set of VLANs. The URNs are pulled
     * from the URN repository.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBwVlans(Topology topo, Integer Bw, String vlans){
        return pruneTopology(topo, Bw, Bw, getIntRangesFromString(vlans), urnRepo.findAll());
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths, set of VLANs, and a list of URNs to
     * match to the topology.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBwVlans(Topology topo, Integer azBw, Integer zaBw, String vlans, List<UrnE> urns){
        return pruneTopology(topo, azBw, zaBw, getIntRangesFromString(vlans), urns);
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths, and set of VLANs. The URNs are pulled
     * from the URN repository.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBwVlans(Topology topo, Integer azBw, Integer zaBw, String vlans){
        return pruneTopology(topo, azBw, zaBw, getIntRangesFromString(vlans), urnRepo.findAll());
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth, set of VLANs, and a list of URNs to
     * match to the topology.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBw(Topology topo, Integer Bw, List<UrnE> urns){
        return pruneTopology(topo, Bw, Bw, new ArrayList<>(), urns);
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth. With no VLAN specified, the
     * pruning service will search for at least one VLAN (if not several) that is available across the topology,
     * and prune using that set of VLANs. The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBw(Topology topo, Integer Bw){
        return pruneTopology(topo, Bw, Bw, new ArrayList<>(), urnRepo.findAll());
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths, and a list of URNs.
     * With no VLAN specified, the pruning service will search for at least one VLAN (if not several) that is
     * available across the topology, and prune using that set of VLANs.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBw(Topology topo, Integer azBw, Integer zaBw, List<UrnE> urns){
        return pruneTopology(topo, azBw, zaBw, new ArrayList<>(), urns);
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths. With no VLAN specified, the
     * pruning service will search for at least one VLAN (if not several) that is available across the topology,
     * and prune using that set of VLANs. The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBw(Topology topo, Integer azBw, Integer zaBw){
        return pruneTopology(topo, azBw, zaBw, new ArrayList<>(), urnRepo.findAll());
    }


    /**
     * Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe){
        assert(pipe != null);
        assert(topo != null);
        assert(urnRepo != null);
        assert(urnRepo.findAll() != null);
        return pruneWithPipe(topo, pipe, urnRepo.findAll());
    }

    /**
     * Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). A list of URNs is passed into match devices/interfaces to
     * topology elements.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, List<UrnE> urns){
        Integer azBw = pipe.getAzMbps();
        Integer zaBw = pipe.getZaMbps();
        List<IntRange> vlans = new ArrayList<>();
        vlans.addAll(getVlansFromJunction(pipe.getAJunction()));
        vlans.addAll(getVlansFromJunction(pipe.getZJunction()));
        return pruneTopology(topo, azBw, zaBw, vlans, urns);
    }

    /**
     * Prune the topology. Called by all outward facing methods to actually perform the pruning. Given the parameters,
     * filter out the edges where the terminating nodes (a/z) do not meet the bandwidth/vlan requirements.
     * @param topo - The topology to be pruned.
     * @param azBw - The required bandwidth that must be supported in one direction on each edge.
     * @param zaBw - The required bandwidth that must be supported in the other direction on each edge.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param urns - The URNs that will be matched to elements in the topology.
     * @return The topology with ineligible edges removed.
     */
    private Topology pruneTopology(Topology topo, Integer azBw, Integer zaBw, List<IntRange> vlans, List<UrnE> urns){
        //Build map of URN name to UrnE
        Map<String, UrnE> urnMap = buildUrnMap(urns);
        // Copy the original topology's layer and set of vertices.
        Topology pruned = new Topology();
        pruned.setLayer(topo.getLayer());
        pruned.setVertices(topo.getVertices());
        // Filter out edges from the topology that do not have sufficient bandwidth available on both terminating nodes.
        // Also filters out all edges where either terminating node is not present in the URN map.
        Set<TopoEdge> availableEdges = topo.getEdges().stream()
                .filter(e -> urnMap.get(e.getA().getUrn()) != null)
                .filter(e -> urnMap.get(e.getZ().getUrn()) != null)
                .filter(e -> bwAvailable(e, azBw, zaBw, urnMap))
                .collect(Collectors.toSet());
        // If this is an MPLS topology, or there are no edges left, just take the bandwidth-pruned set of edges.
        // Otherwise, find all the remaining edges that can support the requested VLAN(s).
        if(pruned.getLayer() == Layer.MPLS || availableEdges.isEmpty()){
            pruned.setEdges(availableEdges);
        }else {
            pruned.setEdges(findEdgesWithAvailableVlans(availableEdges, urnMap, vlans));
        }
        return pruned;
    }

    /***
     * Build a mapping of URN names to UrnE objects.
     * @param urns - A list of URNs, used to create the name -> UrnE map.
     * @return A map of URN names to UrnE objects.
     */
    private Map<String,UrnE> buildUrnMap(List<UrnE> urns) {
        return urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
    }

    /**
     * Evaluate an edge to determine if the nodes on either end of the edge support the requested
     * az and za bandwidths. An edge will only fail the test if one or both URNs (corresponding to the nodes):
     * (1) have valid reservable bandwidth fields, and (2) the URN(s) do not have sufficient reservable bandwidth
     * available in both the az and za directions (egress and ingress).
     * @param edge - The edge to be evaluated.
     * @param azBw - The requested bandwidth in one direction.
     * @param zaBw - The requested bandwidth in the other direction.
     * @param urnMap - Map of URN name to UrnE object.
     * @return True if there is sufficient reservable bandwidth, False otherwise.
     */
    private boolean bwAvailable(TopoEdge edge, Integer azBw, Integer zaBw, Map<String, UrnE> urnMap){
        // Get the reservable bandwidth for the URN matching the node on the "a" side of the edge.
        ReservableBandwidthE aBandwidth = urnMap.get(edge.getA().getUrn()).getReservableBandwidth();
        // Get the reservable bandwidth for the URN matching the node on the "z" side of the edge.
        ReservableBandwidthE zBandwidth = urnMap.get(edge.getZ().getUrn()).getReservableBandwidth();

        // If both are empty, then neither node has a reservable bandwidth field, so there is no need to remove the edge
        if (aBandwidth == null && zBandwidth == null) {
            return true;
        } else {
            // At least one of the two has reservable bandwidth, so check the valid nodes to determine
            // If they have sufficient bandwidth. In the case of a (device, port) edge, only the port must be checked.
            // If it is a (port, port) edge, then both must be checked.
            boolean aPasses = true;
            boolean zPasses = true;
            if(aBandwidth != null){
                aPasses = aBandwidth.getEgressBw() >= azBw && aBandwidth.getIngressBw() >= zaBw;
            }
            if(zBandwidth != null){
                zPasses = zBandwidth.getIngressBw() >= azBw && zBandwidth.getEgressBw() >= zaBw;
            }

            return aPasses && zPasses;

        }
    }

    /**
     * Return a pruned set of edges where the nodes on either end of the edge support at least one of the specified VLANs.
     * A map of URNs are used to retrive the reservable VLAN sets from nodes (where applicable). Builds a mapping from
     * each available VLAN id to the set of edges that support that ID. Using this mapping, the largest set of edges
     * that supports a requested VLAN id (or any VLAN id if none are specified) is returned.
     * @param availableEdges - The set of currently available edges, which will be pruned further using VLAN tags.
     * @param urnMap - Map of URN name to UrnE object.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The input edges, pruned using the input set of VLAN tags.
     */
    private Set<TopoEdge> findEdgesWithAvailableVlans(Set<TopoEdge> availableEdges, Map<String, UrnE> urnMap, List<IntRange> vlans) {
        // Find a set of matching edges for each available VLAN id
        Map<Integer, Set<TopoEdge>> edgesPerId = findEdgesPerVlanId(availableEdges, urnMap);
        // Get a set of the requested VLAN ids
        Set<Integer> idsInRanges = getIntegersFromRanges(vlans);
        // Find the largest set of TopoEdges that meet the request
        Set<TopoEdge> bestSet = new HashSet<>();
        for(Integer id : edgesPerId.keySet()){
            // Ignore the set of edges where both terminating nodes do not have reservable VLAN fields
            // Add them to the best set of edges after this loop
            if(id == -1){
                continue;
            }
            // If the currently considered ID matches the request (or there are no VLANs requested)
            // and the set of edges supporting this ID are larger than the current best
            // choose this set of edges
            if((idsInRanges.contains(id) || idsInRanges.isEmpty()) && edgesPerId.get(id).size() > bestSet.size()){
                bestSet = edgesPerId.get(id);
            }
        }
        // Add all edges where neither terminating node has reservable VLAN attributes
        bestSet.addAll(edgesPerId.get(-1));
        return bestSet;
    }

    /**
     * Return all of the VLAN ids contained within the list of VLAN ranges.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The set of VLAN ids making up the input ranges.
     */
    private Set<Integer> getIntegersFromRanges(List<IntRange> vlans){
        return vlans
                .stream()
                .map(this::getSetOfNumbersInRange)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Using the list of URNs and the URN string, find the matching UrnE object and retrieve all of its
     * IntRanges.
     * @param urnMap - Map of URN name to UrnE object.
     * @param matchingUrn - String representation of the desired URN.
     * @return - All IntRanges supported at the URN.
     */
    private Set<IntRange> getVlanRangesFromUrn(Map<String, UrnE> urnMap, String matchingUrn){
        if(urnMap.get(matchingUrn) == null || urnMap.get(matchingUrn).getReservableVlans() == null)
            return new HashSet<>();
        return urnMap.get(matchingUrn)
                .getReservableVlans()
                .getVlanRanges()
                .stream()
                .map(IntRangeE::toDtoIntRange)
                .collect(Collectors.toSet());
    }

    /**
     * Traverse the set of edges, and create a map of VLAN ID to the edges where that ID is available
     * @param edges - The set of edges.
     * @param urnMap - Map of URN name to UrnE object.
     * @return A (possibly empty) set of VLAN tags that are available across every edge.
     */
    private Map<Integer, Set<TopoEdge>> findEdgesPerVlanId(Set<TopoEdge> edges, Map<String, UrnE> urnMap){
        // Overlap is used to track all VLAN tags that are available across every edge.
        Map<Integer, Set<TopoEdge>> edgesPerId = new HashMap<>();
        edgesPerId.put(-1, new HashSet<>());
        for(TopoEdge edge : edges){
            // Overlap is used to track all VLAN tags that are available across both endpoints of an edge
            Set<Integer> overlap = new HashSet<>();
            // Get the VLAN ranges available the a and z ends of the edge
            Set<IntRange> aRanges = getVlanRangesFromUrn(urnMap, edge.getA().getUrn());
            Set<IntRange> zRanges = getVlanRangesFromUrn(urnMap, edge.getZ().getUrn());


            // If neither edge has reservable VLAN fields, add the edge to the "-1" VLAN tag list.
            // These edges do not need to be pruned, and will be added at the end to the best set of edges
            if(aRanges.isEmpty() && zRanges.isEmpty()){
                edgesPerId.get(-1).add(edge);
            }
            // Otherwise, find the intersection between the VLAN ranges (if any), and add the edge to the list
            // matching each overlapping VLAN ID.
            else{
                // Find the intersection of those two set of VLAN ranges
                overlap = addToOverlap(overlap, aRanges);
                overlap = addToOverlap(overlap, zRanges);


                // For overlapping IDs, put that edge into the map
                for(Integer id: overlap){
                    if(!edgesPerId.containsKey(id)){
                        edgesPerId.put(id, new HashSet<>());
                    }
                    edgesPerId.get(id).add(edge);
                }
            }

        }
        return edgesPerId;
    }

    /**
     * Iterate through the set of overlapping VLAN tags, keep the elements in that set which are also
     * contained in every IntRange passed in.
     * @param overlap - The set of overlapping VLAN tags.
     * @param ranges - The set of IntRanges, corresponding to the VLANs available on a node.
     * @return The (possibly reduced) set of overlapping VLAN tags.
     */
    private Set<Integer> addToOverlap(Set<Integer> overlap, Set<IntRange> ranges){
        // If there are no ranges available, just return the current overlap set
        if(ranges.isEmpty()){
            return overlap;
        }
        // Iterate through all passed in IntRanges
        for(IntRange range : ranges){
            // Get the set of VLAN tags within that range
            Set<Integer> numbersInRange = getSetOfNumbersInRange(range);
            // If the overlap does not already have elements, add all of the tags retrieved from this range
            if(overlap.isEmpty()){
                overlap.addAll(numbersInRange);
            }else{
                // Otherwise, find the intersection between the current overlap and the tags retrieved from this range
                overlap.retainAll(numbersInRange);
            }
        }
        return overlap;
    }

    /**
     * Retrieve a set of all Integers that fall within the specified IntRange.
     * @param aRange - The specified IntRange
     * @return The set of Integers contained within the range.
     */
    private Set<Integer> getSetOfNumbersInRange(IntRange aRange) {
        Set<Integer> numbers = new HashSet<>();
        for(Integer num = aRange.getFloor(); num <= aRange.getCeiling(); num++){
            numbers.add(num);
        }
        return numbers;
    }

    /**
     * Get the requested set of VLAN tags from a junction by streaming through the fixtures in the junction.
     * @param junction - The requested VLAN junction.
     * @return The set of VLAN tags (Integers) requested for fixtures at that junction.
     */
    private List<IntRange> getVlansFromJunction(RequestedVlanJunctionE junction){
        // Stream through the junction's fixtures, map the requested VLAN expression to a set of Integers
        return junction.getFixtures().stream()
                .map(RequestedVlanFixtureE::getVlanExpression)
                .map(this::getIntRangesFromString)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Given a list of strings, convert all valid strings into IntRanges.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return A list of IntRanges, each representing a range of VLAN ID values parsed from a string.
     */
    private List<IntRange> getIntRangesFromString(String vlans){
        if(IntRangeParsing.isValidIntRangeInput(vlans)){
            try {
                return IntRangeParsing.retrieveIntRanges(vlans);
            }catch(Exception e){
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

}
