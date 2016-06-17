package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.resv.ent.RequestedVlanFixtureE;
import net.es.oscars.resv.ent.RequestedVlanJunctionE;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.ReservableVlanE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
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
     * @param vlans - The set of required VLANs.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBwVlans(Topology topo, Integer Bw, Set<Integer> vlans, List<UrnE> urns){
        return pruneTopology(topo, Bw, Bw, vlans, urns);
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth, and set of VLANs. The URNs are pulled
     * from the URN repository.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param vlans - The set of required VLANs.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBwVlans(Topology topo, Integer Bw, Set<Integer> vlans){
        return pruneTopology(topo, Bw, Bw, vlans, urnRepo.findAll());
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths, set of VLANs, and a list of URNs to
     * match to the topology.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @param vlans - The set of required VLANs.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBwVlans(Topology topo, Integer azBw, Integer zaBw, Set<Integer> vlans, List<UrnE> urns){
        return pruneTopology(topo, azBw, zaBw, vlans, urns);
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths, and set of VLANs. The URNs are pulled
     * from the URN repository.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @param vlans - The set of required VLANs.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBwVlans(Topology topo, Integer azBw, Integer zaBw, Set<Integer> vlans){
        return pruneTopology(topo, azBw, zaBw, vlans, urnRepo.findAll());
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
        return pruneTopology(topo, Bw, Bw, new HashSet<>(), urns);
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
        return pruneTopology(topo, Bw, Bw, new HashSet<>(), urnRepo.findAll());
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
        return pruneTopology(topo, azBw, zaBw, new HashSet<>(), urns);
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
        return pruneTopology(topo, azBw, zaBw, new HashSet<>(), urnRepo.findAll());
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
        Set<Integer> vlans = new HashSet<>();
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
     * @param vlans - The required VLANs (can be a set of one) that must be supported by the nodes terminating each edge.
     * @param urns - The URNs that will be matched to elements in the topology.
     * @return The topology with ineligible edges removed.
     */
    private Topology pruneTopology(Topology topo, Integer azBw, Integer zaBw, Set<Integer> vlans, List<UrnE> urns){
        // Copy the original topology's layer and set of vertices.
        Topology pruned = new Topology();
        pruned.setLayer(topo.getLayer());
        pruned.setVertices(topo.getVertices());
        // Filter out edges from the topology that do not have sufficient bandwidth available on both terminating nodes.
        Set<TopoEdge> availableEdges = topo.getEdges().stream()
                .filter(e -> bwAvailable(e, azBw, zaBw, urns))
                .collect(Collectors.toSet());
        // If this is an MPLS topology, or there are no edges left, just take the bandwidth-pruned set of edges.
        // Otherwise, find all the remaining edges that can support the requested VLAN(s).
        if(pruned.getLayer() == Layer.MPLS || availableEdges.isEmpty()){
            pruned.setEdges(availableEdges);
        }else {
            pruned.setEdges(findEdgesWithAvailableVlans(availableEdges, urns, vlans));
        }
        return pruned;
    }

    /**
     * Evaluate an edge to determine if the nodes on either end of the edge support the requested
     * az and za bandwidths. An edge will only fail the test if one or both URNs (corresponding to the nodes):
     * (1) have valid reservable bandwidth fields, and (2) the URN(s) do not have sufficient reservable bandwidth
     * available in both the az and za directions (egress and ingress).
     * @param edge - The edge to be evaluated.
     * @param azBw - The requested bandwidth in one direction.
     * @param zaBw - The requested bandwidth in the other direction.
     * @param urns - The list of URNs to match to the nodes on either end of the edge.
     * @return True if there is sufficient reservable bandwidth, False otherwise.
     */
    private boolean bwAvailable(TopoEdge edge, Integer azBw, Integer zaBw, List<UrnE> urns){
        // Get the reservable bandwidth for the URN matching the node on the "a" side of the edge.
        // This list should only have one or zero elements in it
        List<ReservableBandwidthE> aMatching = urns.stream()
                .filter(u -> u.getUrn().equals(edge.getA().getUrn()))
                .filter(u -> u.getReservableBandwidth() != null)
                .map(UrnE::getReservableBandwidth)
                .collect(Collectors.toList());
        // Get the reservable bandwidth for the URN matching the node on the "z" side of the edge.
        // This list should only have one or zero elements in it
        List<ReservableBandwidthE> zMatching = urns.stream()
                .filter(u -> u.getUrn().equals(edge.getZ().getUrn()))
                .filter(u -> u.getReservableBandwidth() != null)
                .map(UrnE::getReservableBandwidth)
                .collect(Collectors.toList());

        // Verify that there is no more than one reservable bandwidth that matched the a/z URNs.
        // This would imply that there are duplicate elements in the list of URNs
        assert aMatching.size() <= 1 && zMatching.size() <=1;
        // If both are empty, then neither node has a reservable bandwidth field, so there is no need to remove the edge
        if (aMatching.isEmpty() && zMatching.isEmpty()) {
            return true;
        } else {
            // At least one of the two has reservable bandwidth, so check the valid nodes to determine
            // If they have sufficient bandwidth. In the case of a (device, port) edge, only the port must be checked.
            // If it is a (port, port) edge, then both must be checked.
            boolean aPasses = true;
            boolean zPasses = true;
            if(!aMatching.isEmpty()){
                aPasses = aMatching.get(0).getEgressBw() >= azBw && aMatching.get(0).getIngressBw() >= zaBw;
            }
            if(!zMatching.isEmpty()){
                zPasses = zMatching.get(0).getIngressBw() >= azBw && zMatching.get(0).getEgressBw() >= zaBw;
            }

            return aPasses && zPasses;

        }
    }

    /**
     * Return a modified set of edges where the nodes on either end of the edge support the specified VLANs.
     * A list of URNs are used to retrive the reservable VLAN sets from nodes (where applicable). If no VLANs are
     * requested (set is empty), find all open VLAN tags available across all input edges, then filter out the edges
     * using that set.
     * @param availableEdges - The set of currently available edges, which will be pruned further using VLAN tags.
     * @param urns - The list of URNs which will be mapped to the nodes in the topology.
     * @param vlans - The desired set of VLANs (empty if any VLANs can work).
     * @return The input edges, pruned using the input set of VLAN tags.
     */
    private Set<TopoEdge> findEdgesWithAvailableVlans(Set<TopoEdge> availableEdges, List<UrnE> urns, Set<Integer> vlans) {
        // If no VLAN tags are specified
        if(vlans.isEmpty()){
            // Find all VLAN tags that are available across the input edges
            Set<Integer> open = findOpenVlans(availableEdges, urns);
            // If none were found, then no edges are viable.
            if(open.isEmpty()){
                return new HashSet<>();
            }
            // Otherwise, there is at least one VLAN tag available across all input edges, so all of these
            // edges are viable.
            return availableEdges;
        }
        // If there are specified VLANs, filter out the currently available edges using the specified VLAN tags..
        return availableEdges.stream().filter(e -> vlansAvailable(e, vlans, urns))
                    .collect(Collectors.toSet());
    }

    /**
     * Determine if the specified VLAN tags are supported on the two nodes terminating this edge. An edge only
     * fails the test if: (1) One or both of the URNs matching the terminating nodes have valid sets of reservable
     * VLAN rangs, and (2) Of those that do have valid ranges, the specified VLAN tags are not contained in those
     * ranges (the tag(s) are not currently reservable).
     * @param edge - The edge to be evaluated.
     * @param vlans - The set of VLAN tags requested.
     * @param urns - The list of URNs, used to find the matching UrnE objects for the terminating nodes.
     * @return True, if the edge can support the desired VLAN tags. False, otherwise.
     */
    private boolean vlansAvailable(TopoEdge edge, Set<Integer> vlans, List<UrnE> urns) {
        // Get the set of IntRanges supported at node A
        Set<IntRange> aRanges = getVlanRangesFromUrn(urns, edge.getA().getUrn());
        // Get the set of IntRanges supported at node Z
        Set<IntRange> zRanges = getVlanRangesFromUrn(urns, edge.getZ().getUrn());

        // If neither node supports IntRanges, the edge does not need to be removed.
        if(aRanges.isEmpty() && zRanges.isEmpty()){
            return true;
        } else{
            // Otherwise, go through all requested VLAN tags
            for(Integer requestedVlan : vlans){

                // Check if any of the ranges supported at A contain the requested tag
                boolean aContainsVlan = true;
                if(!aRanges.isEmpty()) {
                    aContainsVlan = aRanges.stream().anyMatch(vr -> vr.contains(requestedVlan));
                }
                // Check if any of the ranges supported at Z contain the requested tag
                boolean zContainsVlan = true;
                if(!zRanges.isEmpty()) {
                    zContainsVlan = zRanges.stream().anyMatch(vr -> vr.contains(requestedVlan));
                }
                // If neither contain the requested tag, remove the edge
                if(!aContainsVlan || !zContainsVlan){
                    return false;
                }
            }
            // If all requested tags are available at both nodes if (port, port), or just at the port if (device, port),
            // then this edge is valid.
            return true;
        }
    }

    /**
     * Using the list of URNs and the URN string, find the matching UrnE object and retrieve all of its
     * IntRanges.
     * @param urns - The list of possible URNs.
     * @param matchingUrn - String representation of the desired URN.
     * @return - All IntRanges supported at the URN.
     */
    private Set<IntRange> getVlanRangesFromUrn(List<UrnE> urns, String matchingUrn){
        return urns.stream()
                .filter(u -> u.getUrn().equals(matchingUrn))
                .filter(u -> u.getReservableVlans() != null)
                .map(UrnE::getReservableVlans)
                .map(ReservableVlanE::getVlanRanges)
                .flatMap(Collection::stream)
                .map(IntRangeE::toDtoIntRange)
                .collect(Collectors.toSet());
    }

    /**
     * Traverse the set of edges, and find all VLAN tags that are available across every edge in the set.
     * @param edges - The set of edges.
     * @param urns - The list of URNs that are used to retrieve the available VLANs at each node.
     * @return A (possibly empty) set of VLAN tags that are available across every edge.
     */
    private Set<Integer> findOpenVlans(Set<TopoEdge> edges, List<UrnE> urns){
        // Overlap is used to track all VLAN tags that are available across every edge.
        Set<Integer> overlap = new HashSet<>();
        Iterator<TopoEdge> iter = edges.iterator();
        // If there are no edges, there is no overlapping set of VLAN tags.
        if(!iter.hasNext()){
            return overlap;
        }
        // Pick a first edge
        TopoEdge e = iter.next();

        // Get the VLAN ranges available the a and z ends of the edge
        Set<IntRange> aRanges = getVlanRangesFromUrn(urns, e.getA().getUrn());
        Set<IntRange> zRanges = getVlanRangesFromUrn(urns, e.getZ().getUrn());


        // Find the intersection of those two set of VLAN ranges
        overlap = addToOverlap(overlap, aRanges);
        overlap = addToOverlap(overlap, zRanges);

        //Now, we have a set of all integers(VLAN tags) that are available on both node A and node Z of an edge
        //Next, we have to go through the other edges in the network, and for each one, find the vlans available
        //On both the A and Z ends of the edge. Remove VLAN tags from our overlap set if they are not contained
        //Within the available VLAN ranges on each edge.
        while(iter.hasNext()){
            TopoEdge edge = iter.next();
            aRanges = getVlanRangesFromUrn(urns, edge.getA().getUrn());
            zRanges = getVlanRangesFromUrn(urns, edge.getZ().getUrn());

            overlap = removeIfNotInRange(overlap, aRanges);
            overlap = removeIfNotInRange(overlap, zRanges);
        }
        return overlap;
    }

    /**
     * Iterate through a set of VLAN ranges, and remove elements from the overlap set if they are not contained
     * in all of the ranges.
     * @param overlap - The set of overlapping VLAN tags
     * @param ranges - The set of available VLAN ranges
     * @return The filtered set of overlapping VLAN tags
     */
    private Set<Integer> removeIfNotInRange(Set<Integer> overlap, Set<IntRange> ranges){
        // Go through each IntRange, filter the overlap set to remove VLAN tags that
        // are not contained within the IntRange.
        for(IntRange range : ranges){
            overlap = overlap.stream().filter(range::contains).collect(Collectors.toSet());
        }
        return overlap;
    }

    /**
     * Iterate through the set of overlapping VLAN tags, keep the elements in that set which are also
     * contained in every IntRange passed in.
     * @param overlap - The set of overlapping VLAN tags.
     * @param ranges - The set of IntRanges, corresponding to the VLANs available on a node.
     * @return The (possibly reduced) set of overlapping VLAN tags.
     */
    private Set<Integer> addToOverlap(Set<Integer> overlap, Set<IntRange> ranges){
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
    private Set<Integer> getVlansFromJunction(RequestedVlanJunctionE junction){
        // Stream through the junction's fixtures, map the requested VLAN expression to a set of Integers
        return junction.getFixtures().stream().map(RequestedVlanFixtureE::getVlanExpression)
                .map(Integer::parseInt).collect(Collectors.toSet());
    }

}
