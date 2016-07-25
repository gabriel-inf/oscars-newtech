package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
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

    @Autowired
    private VlanService vlanSvc;

    @Autowired
    private BandwidthService bwSvc;


    /**
     * Prune the topology using a specified bidirectional bandwidth, set of VLANs, and a list of URNs to
     * match to the topology.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBwVlans(Topology topo, Integer Bw, String vlans, List<UrnE> urns,
                                     List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        return pruneTopology(topo, Bw, Bw, vlanSvc.getIntRangesFromString(vlans), urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth, and set of VLANs. The URNs are pulled
     * from the URN repository.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBwVlans(Topology topo, Integer Bw, String vlans, Date start, Date end){
        return pruneTopology(topo, Bw, Bw, vlanSvc.getIntRangesFromString(vlans), urnRepo.findAll(),
                bwSvc.getReservedBandwidthFromRepo(start, end), vlanSvc.getReservedVlansFromRepo(start, end));
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
    public Topology pruneWithAZBwVlans(Topology topo, Integer azBw, Integer zaBw, String vlans, List<UrnE> urns,
                                       List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        return pruneTopology(topo, azBw, zaBw, vlanSvc.getIntRangesFromString(vlans), urns, rsvBwList, rsvVlanList);
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
    public Topology pruneWithAZBwVlans(Topology topo, Integer azBw, Integer zaBw, String vlans, Date start, Date end){
        return pruneTopology(topo, azBw, zaBw, vlanSvc.getIntRangesFromString(vlans), urnRepo.findAll(),
                bwSvc.getReservedBandwidthFromRepo(start, end), vlanSvc.getReservedVlansFromRepo(start, end));
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth, set of VLANs, and a list of URNs to
     * match to the topology.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBw(Topology topo, Integer Bw, List<UrnE> urns,
                                List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        return pruneTopology(topo, Bw, Bw, new ArrayList<>(), urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth. With no VLAN specified, the
     * pruning service will search for at least one VLAN (if not several) that is available across the topology,
     * and prune using that set of VLANs. The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBw(Topology topo, Integer Bw, Date start, Date end){
        return pruneTopology(topo, Bw, Bw, new ArrayList<>(), urnRepo.findAll(),
                bwSvc.getReservedBandwidthFromRepo(start, end), vlanSvc.getReservedVlansFromRepo(start, end));
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
    public Topology pruneWithAZBw(Topology topo, Integer azBw, Integer zaBw, List<UrnE> urns,
                                  List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        return pruneTopology(topo, azBw, zaBw, new ArrayList<>(), urns, rsvBwList, rsvVlanList);
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
    public Topology pruneWithAZBw(Topology topo, Integer azBw, Integer zaBw, Date start, Date end){
        return pruneTopology(topo, azBw, zaBw, new ArrayList<>(), urnRepo.findAll(),
                bwSvc.getReservedBandwidthFromRepo(start, end), vlanSvc.getReservedVlansFromRepo(start, end));
    }


    /**
     * Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, ScheduleSpecificationE sched){
        Date start = sched.getNotBefore();
        Date end = sched.getNotAfter();
        return pruneWithPipe(topo, pipe, urnRepo.findAll(),
                bwSvc.getReservedBandwidthFromRepo(start, end), vlanSvc.getReservedVlansFromRepo(start, end));
    }

    /**
     * Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param sched - The requested schedule, containing the start and end Dates
     * @param rsvBwList - A list of Reserved Bandwidth to be considered when pruning (along with Bandwidth in the Repo)
     * @param rsvVlanList - A list of Reserved VLAN tags to be considered when pruning (along with VLANs in the Repo)
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, ScheduleSpecificationE sched,
                                  List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Date start = sched.getNotBefore();
        Date end = sched.getNotAfter();


        return pruneWithPipe(topo, pipe, urnRepo.findAll(),
                rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology based on A->Z bandwidth using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param sched - The requested schedule, containing the start and end Dates
     * @param rsvBwList - A list of Reserved Bandwidth to be considered when pruning (along with Bandwidth in the Repo)
     * @param rsvVlanList - A list of Reserved VLAN tags to be considered when pruning (along with VLANs in the Repo)
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipeAZ(Topology topo, RequestedVlanPipeE pipe, ScheduleSpecificationE sched,
                                  List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Date start = sched.getNotBefore();
        Date end = sched.getNotAfter();


        return pruneWithPipeAZ(topo, pipe, urnRepo.findAll(), rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology based on Z->A bandwidth using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param sched - The requested schedule, containing the start and end Dates
     * @param rsvBwList - A list of Reserved Bandwidth to be considered when pruning (along with Bandwidth in the Repo)
     * @param rsvVlanList - A list of Reserved VLAN tags to be considered when pruning (along with VLANs in the Repo)
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipeZA(Topology topo, RequestedVlanPipeE pipe, ScheduleSpecificationE sched,
                                    List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Date start = sched.getNotBefore();
        Date end = sched.getNotAfter();

        return pruneWithPipeZA(topo, pipe, urnRepo.findAll(), rsvBwList, rsvVlanList);
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
    public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, List<UrnE> urns,
                                  List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Integer azBw = pipe.getAzMbps();
        Integer zaBw = pipe.getZaMbps();
        List<IntRange> vlans = new ArrayList<>();
        vlans.addAll(vlanSvc.getVlansFromJunction(pipe.getAJunction()));
        vlans.addAll(vlanSvc.getVlansFromJunction(pipe.getZJunction()));
        return pruneTopology(topo, azBw, zaBw, vlans, urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology based on A->Z bandwidth using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). A list of URNs is passed into match devices/interfaces to
     * topology elements.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    private Topology pruneWithPipeAZ(Topology topo, RequestedVlanPipeE pipe, List<UrnE> urns,
                                  List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Integer azBw = pipe.getAzMbps();
        List<IntRange> vlans = new ArrayList<>();
        vlans.addAll(vlanSvc.getVlansFromJunction(pipe.getAJunction()));
        vlans.addAll(vlanSvc.getVlansFromJunction(pipe.getZJunction()));
        return pruneTopologyUni(topo, azBw, vlans, urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology based on Z->A bandwidth  using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). A list of URNs is passed into match devices/interfaces to
     * topology elements.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    private Topology pruneWithPipeZA(Topology topo, RequestedVlanPipeE pipe, List<UrnE> urns,
                                     List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Integer zaBw = pipe.getZaMbps();
        List<IntRange> vlans = new ArrayList<>();
        vlans.addAll(vlanSvc.getVlansFromJunction(pipe.getAJunction()));
        vlans.addAll(vlanSvc.getVlansFromJunction(pipe.getZJunction()));
        return pruneTopologyUni(topo, zaBw, vlans, urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). A list of URNs is passed into match devices/interfaces to
     * topology elements.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @param sched - The requested schedule, containing the start and end Dates
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, List<UrnE> urns, ScheduleSpecificationE sched){
        Integer azBw = pipe.getAzMbps();
        Integer zaBw = pipe.getZaMbps();
        List<IntRange> vlans = new ArrayList<>();
        vlans.addAll(vlanSvc.getVlansFromJunction(pipe.getAJunction()));
        vlans.addAll(vlanSvc.getVlansFromJunction(pipe.getZJunction()));

        Date start = sched.getNotBefore();
        Date end = sched.getNotAfter();

        return pruneTopology(topo, azBw, zaBw, vlans, urns,
                bwSvc.getReservedBandwidthFromRepo(start, end), vlanSvc.getReservedVlansFromRepo(start, end));
    }

    /**
     * Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param sched - The requested schedule, containing the start and end Dates
     * @param rsvBwList - A list of Reserved Bandwidth to be considered when pruning (along with Bandwidth in the Repo)
     * @param rsvVlanList - A list of Reserved VLAN tags to be considered when pruning (along with VLANs in the Repo)
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, ScheduleSpecificationE sched, List<UrnE> urns,
                                  List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Date start = sched.getNotBefore();
        Date end = sched.getNotAfter();


        return pruneWithPipe(topo, pipe, urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology based on A->Z bandwidth using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param sched - The requested schedule, containing the start and end Dates
     * @param rsvBwList - A list of Reserved Bandwidth to be considered when pruning (along with Bandwidth in the Repo)
     * @param rsvVlanList - A list of Reserved VLAN tags to be considered when pruning (along with VLANs in the Repo)
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipeAZ(Topology topo, RequestedVlanPipeE pipe, ScheduleSpecificationE sched, List<UrnE> urns,
                                  List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Date start = sched.getNotBefore();
        Date end = sched.getNotAfter();

        return pruneWithPipeAZ(topo, pipe, urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology based on Z->A bandwidth using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param sched - The requested schedule, containing the start and end Dates
     * @param rsvBwList - A list of Reserved Bandwidth to be considered when pruning (along with Bandwidth in the Repo)
     * @param rsvVlanList - A list of Reserved VLAN tags to be considered when pruning (along with VLANs in the Repo)
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipeZA(Topology topo, RequestedVlanPipeE pipe, ScheduleSpecificationE sched, List<UrnE> urns,
                                    List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Date start = sched.getNotBefore();
        Date end = sched.getNotAfter();

        return pruneWithPipeZA(topo, pipe, urns, rsvBwList, rsvVlanList);
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
    private Topology pruneTopology(Topology topo, Integer azBw, Integer zaBw, List<IntRange> vlans, List<UrnE> urns,
                                   List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        //Build map of URN name to UrnE
        Map<String, UrnE> urnMap = buildUrnMap(urns);

        // Build map of URN to available bandwidth
        Map<UrnE, Map<String, Integer>> availBwMap = bwSvc.buildBandwidthAvailabilityMapWithUrns(rsvBwList, urns);

        // Build map of URN to resvVlan
        Map<UrnE, Set<Integer>> availVlanMap = vlanSvc.buildAvailableVlanIdMap(urnMap, rsvVlanList);

        // Copy the original topology's layer and set of vertices.
        Topology pruned = new Topology();
        pruned.setLayer(topo.getLayer());
        pruned.setVertices(topo.getVertices());
        // Filter out edges from the topology that do not have sufficient bandwidth available on both terminating nodes.
        // Also filters out all edges where either terminating node is not present in the URN map.
        Set<TopoEdge> availableEdges = topo.getEdges().stream()
                .filter(e -> bwSvc.evaluateBandwidthEdge(e, azBw, zaBw, urnMap, availBwMap))
                .collect(Collectors.toSet());
        // If this is an MPLS topology, or there are no edges left, just take the bandwidth-pruned set of edges.
        // Otherwise, find all the remaining edges that can support the requested VLAN(s).
        if(pruned.getLayer() == Layer.MPLS || pruned.getLayer() == null || availableEdges.isEmpty()){
            pruned.setEdges(availableEdges);
        }else {
            pruned.setEdges(vlanSvc.findMaxValidEdgeSet(availableEdges, urnMap, vlans, availVlanMap));
        }
        return pruned;
    }

    /**
     * Prune the topology for bandwidth in a single direction. Called by all outward facing methods to actually perform the pruning. Given the parameters,
     * filter out the edges where the terminating nodes (a/z) do not meet the unidirectional bandwidth/vlan requirements.
     * @param topo - The topology to be pruned.
     * @param theBw - The required bandwidth that must be supported in one direction on each edge.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param urns - The URNs that will be matched to elements in the topology.
     * @return The topology with ineligible edges removed.
     */
    private Topology pruneTopologyUni(Topology topo, Integer theBw, List<IntRange> vlans, List<UrnE> urns,
                                   List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        //Build map of URN name to UrnE
        Map<String, UrnE> urnMap = buildUrnMap(urns);

        // Build map of URN to available bandwidth
        Map<UrnE, Map<String, Integer>> availBwMap = bwSvc.buildBandwidthAvailabilityMapWithUrns(rsvBwList, urns);

        // Build map of URN to resvVlan
        Map<UrnE, Set<Integer>> availVlanMap = vlanSvc.buildAvailableVlanIdMap(urnMap, rsvVlanList);

        // Copy the original topology's layer and set of vertices.
        Topology pruned = new Topology();
        pruned.setLayer(topo.getLayer());
        pruned.setVertices(topo.getVertices());
        // Filter out edges from the topology that do not have sufficient bandwidth available on both terminating nodes.
        // Also filters out all edges where either terminating node is not present in the URN map.
        Set<TopoEdge> availableEdges = topo.getEdges().stream()
                .filter(e -> bwSvc.evaluateBandwidthEdgeUni(e, theBw, urnMap, availBwMap))
                .collect(Collectors.toSet());
        // If this is an MPLS topology, or there are no edges left, just take the bandwidth-pruned set of edges.
        // Otherwise, find all the remaining edges that can support the requested VLAN(s).
        if(pruned.getLayer() == Layer.MPLS || pruned.getLayer() == null || availableEdges.isEmpty()){
            pruned.setEdges(availableEdges);
        }else {
            pruned.setEdges(vlanSvc.findMaxValidEdgeSet(availableEdges, urnMap, vlans, availVlanMap));
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

}
