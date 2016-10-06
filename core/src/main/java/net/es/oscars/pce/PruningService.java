package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.resv.ent.*;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.topo.svc.TopoService;
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

    @Autowired
    private TopoService topoService;


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

        Set<String> urnBlacklist = pipe.getUrnBlacklist();

        return pruneTopology(topo, azBw, zaBw, vlans, urns, rsvBwList, rsvVlanList, urnBlacklist);
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

        Set<String> urnBlacklist = pipe.getUrnBlacklist();

        return pruneTopologyUni(topo, azBw, vlans, urns, rsvBwList, rsvVlanList, urnBlacklist);
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

        Set<String> urnBlacklist = pipe.getUrnBlacklist();

        return pruneTopologyUni(topo, zaBw, vlans, urns, rsvBwList, rsvVlanList, urnBlacklist);
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

        Set<String> urnBlacklist = pipe.getUrnBlacklist();

        return pruneTopology(topo, azBw, zaBw, vlans, urns,
                bwSvc.getReservedBandwidthFromRepo(start, end), vlanSvc.getReservedVlansFromRepo(start, end), urnBlacklist);
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
     * Prune the topology Called by all outward facing methods to actually perform the pruning. Given the parameters,
     * filter out the edges where the terminating nodes (a/z) do not meet the bandwidth/vlan requirements.
     * @param topo - The topology to be pruned.
     * @param azBw - The required bandwidth that must be supported in one direction on each edge.
     * @param zaBw - The required bandwidth that must be supported in the other direction on each edge.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param urns - The URNs that will be matched to elements in the topology.
     * @param rsvBwList - A list of Reserved Bandwidth to be considered when pruning.
     * @param rsvVlanList - A list of Reserved VLAN tags to be considered when pruning.
     * @param urnBlacklist - URNs which are to be explicitly pruned out of the topology, regardless of availability.
     * @return The topology with ineligible edges removed.
     */
    private Topology pruneTopology(Topology topo, Integer azBw, Integer zaBw, List<IntRange> vlans, List<UrnE> urns, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList, Set<String> urnBlacklist)
    {
        //Build map of URN name to UrnE
        Map<String, UrnE> urnMap = buildUrnMap(urns);

        // Build map of URN to available bandwidth
        Map<UrnE, Map<String, Integer>> availBwMap = bwSvc.buildBandwidthAvailabilityMapWithUrns(rsvBwList, urns);

        // Get map of parent device vertex -> set of port vertices
        Map<String, Set<String>> deviceToPortMap = topoService.buildDeviceToPortMap();
        Map<String, String> portToDeviceMap = topoService.buildPortToDeviceMap(deviceToPortMap);

        // Build map of URN to resvVlan
        Map<UrnE, Set<Integer>> availVlanMap = vlanSvc.buildAvailableVlanIdMap(urnMap, rsvVlanList, portToDeviceMap);

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
        if(pruned.getLayer() != Layer.MPLS && pruned.getLayer() != null && !availableEdges.isEmpty()){
            availableEdges = vlanSvc.findMaxValidEdgeSet(availableEdges, urnMap, vlans, availVlanMap);
        }

        // Prune out blacklisted edges
        if(urnBlacklist != null && !urnBlacklist.isEmpty() && !availableEdges.isEmpty())
        {
            Set<TopoEdge> blacklistedEdges = pruneBlacklist(topo, urnBlacklist);
            availableEdges.removeAll(blacklistedEdges);
        }

        pruned.setEdges(availableEdges);

        return pruned;
    }


    /**
     * Prune the topology for bandwidth in a single direction. Called by all outward facing methods to actually perform the pruning. Given the parameters,
     * filter out the edges where the terminating nodes (a/z) do not meet the unidirectional bandwidth/vlan requirements.
     * @param topo - The topology to be pruned.
     * @param theBw - The required bandwidth that must be supported in one direction on each edge.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param urns - The URNs that will be matched to elements in the topology.
     * @param rsvBwList - A list of Reserved Bandwidth to be considered when pruning.
     * @param rsvVlanList - A list of Reserved VLAN tags to be considered when pruning.
     * @param urnBlacklist - URNs which are to be explicitly pruned out of the topology, regardless of availability.
     */
    private Topology pruneTopologyUni(Topology topo, Integer theBw, List<IntRange> vlans, List<UrnE> urns, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList, Set<String> urnBlacklist)
    {
        //Build map of URN name to UrnE
        Map<String, UrnE> urnMap = buildUrnMap(urns);

        // Build map of URN to available bandwidth
        Map<UrnE, Map<String, Integer>> availBwMap = bwSvc.buildBandwidthAvailabilityMapWithUrns(rsvBwList, urns);

        // Get map of parent device vertex -> set of port vertices
        Map<String, Set<String>> deviceToPortMap = topoService.buildDeviceToPortMap();
        Map<String, String> portToDeviceMap = topoService.buildPortToDeviceMap(deviceToPortMap);

        // Build map of URN to resvVlan
        Map<UrnE, Set<Integer>> availVlanMap = vlanSvc.buildAvailableVlanIdMap(urnMap, rsvVlanList, portToDeviceMap);

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
        if(pruned.getLayer() == Layer.MPLS || pruned.getLayer() == null || availableEdges.isEmpty())
        {
            pruned.setEdges(availableEdges);
        }
        else
        {
            pruned.setEdges(vlanSvc.findMaxValidEdgeSet(availableEdges, urnMap, vlans, availVlanMap));
        }

        // Prune out blacklisted edges
        if(urnBlacklist != null)
        {
            if(!urnBlacklist.isEmpty())
            {
                Set<TopoEdge> blacklistedEdges = pruneBlacklist(topo, urnBlacklist);
                pruned.getEdges().removeAll(blacklistedEdges);
            }
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
     * Identifies topology edges that correspond to a given set of topology URNs, for removal from the topology.
     * @param topology - Full un-pruned topology containing all edges and vertices in the network
     * @param urnBlacklist - Set of URN strings corresponding to blacklisted devices/ports.
     * @return Set of blacklisted edges to be pruned from the topology.
     */
    public Set<TopoEdge> pruneBlacklist(Topology topology, Set<String> urnBlacklist)
    {
        Set<TopoEdge> topoEdges = topology.getEdges();
        Set<TopoVertex> topoVertices = topology.getVertices();

        Set<TopoEdge> blacklistedEdges = new HashSet<>();

        // Map URN Strings to TopoVertices
        Set<TopoVertex> badVertices = topoVertices.stream()
                .filter(urn -> urnBlacklist.contains(urn.getUrn()))
                .collect(Collectors.toSet());

        Set<TopoVertex> badPorts = new HashSet<>();

        // Determine if any ports need to be added to the blacklist based on blacklisted devices
        for(TopoVertex badVertex : badVertices)
        {
            // Devices: Prune out all edges that have badVertex OR ITS PORTS as an endpoint
            if(!badVertex.getVertexType().equals(VertexType.PORT))
            {
                for(TopoEdge oneEdge : topoEdges)
                {
                    TopoVertex badPort = null;

                    if(oneEdge.getA().equals(badVertex))
                    {
                        badPort = oneEdge.getZ();
                    }
                    else if(oneEdge.getZ().equals(badVertex))
                    {
                        badPort = oneEdge.getA();
                    }

                    if(badPort != null)
                    {
                        badPorts.add(badPort);
                    }
                }
            }
        }

        // Complete the set of blacklisted URNs
        badVertices.addAll(badPorts);

        // Identify edges to blacklist
        for(TopoVertex badVertex : badVertices)
        {
            for(TopoEdge oneEdge : topoEdges)
            {
                if(oneEdge.getA().equals(badVertex) || oneEdge.getZ().equals(badVertex))
                {
                    blacklistedEdges.add(oneEdge);
                }
            }
        }

        return blacklistedEdges;
    }
}
