package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.resv.ent.ReservedVlanE;
import net.es.oscars.resv.ent.ScheduleSpecificationE;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.dto.topo.VertexType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeremy on 7/22/16.
 */
@Slf4j
@Component
public class EroPCE {
    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    /**
     * Depends on DijkstraPCE to construct the Physical-Layer EROs for a request after pruning the topology based on requested ERO parameters
     *
     * @param requestPipe  Requested pipe with required reservation parameters, and non-empty ERO specifications
     * @param requestSched Requested schedule parameters
     * @return A two-element Map containing both the forward-direction (A->Z) ERO and the reverse-direction (Z->A) ERO
     * @throws PCEException
     */
    public Map<String, List<TopoEdge>> computeSpecifiedERO(RequestedVlanPipeE requestPipe, ScheduleSpecificationE requestSched, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList) throws PCEException {
        log.info("Entering EroPCE.");
        UrnE srcPortURN = topoService.getUrn(requestPipe.getAJunction().getFixtures().iterator().next().getPortUrn());
        UrnE dstPortURN = topoService.getUrn(requestPipe.getZJunction().getFixtures().iterator().next().getPortUrn());

        TopoVertex srcPort = new TopoVertex(srcPortURN.getUrn(), VertexType.PORT);
        TopoVertex dstPort = new TopoVertex(dstPortURN.getUrn(), VertexType.PORT);
        String srcPortName = srcPortURN.getUrn();
        String dstPortName = dstPortURN.getUrn();

        Topology multiLayerTopoAzDirection = topoService.getMultilayerTopology();
        Topology multiLayerTopoZaDirection = topoService.getMultilayerTopology();   // Same as AZ initially

        List<String> requestedAzERO = requestPipe.getAzERO().stream().collect(Collectors.toList());
        List<String> requestedZaERO = requestPipe.getZaERO().stream().collect(Collectors.toList());

        if (!requestedAzERO.get(0).equals(requestPipe.getAJunction().getDeviceUrn()) ||
                !requestedAzERO.get(requestedAzERO.size() - 1).equals(requestPipe.getZJunction().getDeviceUrn())) {
            throw new PCEException("Requested ERO must begin at source-device URN, and terminate at destination-device URN.");
        }

        if (!requestedZaERO.get(0).equals(requestPipe.getZJunction().getDeviceUrn()) ||
                !requestedZaERO.get(requestedZaERO.size() - 1).equals(requestPipe.getAJunction().getDeviceUrn())) {
            throw new PCEException("Requested ERO must begin at source-device URN, and terminate at destination-device URN.");
        }

        if (requestedAzERO.isEmpty() || requestedZaERO.isEmpty())
            throw new PCEException("Requested ERO must include at least one URN.");

        requestedAzERO.add(0, srcPortName);
        requestedAzERO.add(dstPortName);
        requestedZaERO.add(0, dstPortName);
        requestedZaERO.add(srcPortName);

        Set<TopoVertex> azNodes = multiLayerTopoAzDirection.getVertices().stream()
                .filter(v -> requestedAzERO.contains(v.getUrn()))
                .collect(Collectors.toSet());

        Set<TopoVertex> zaNodes = multiLayerTopoZaDirection.getVertices().stream()
                .filter(v -> requestedZaERO.contains(v.getUrn()))
                .collect(Collectors.toSet());

        Set<TopoEdge> edgestoKeepAz = multiLayerTopoAzDirection.getEdges().stream()
                .filter(e -> (azNodes.contains(e.getA()) && azNodes.contains(e.getZ())) || (azNodes.contains(e.getZ()) && azNodes.contains(e.getA())))
                .collect(Collectors.toSet());

        Set<TopoEdge> edgestoKeepZa = multiLayerTopoZaDirection.getEdges().stream()
                .filter(e -> (zaNodes.contains(e.getA()) && zaNodes.contains(e.getZ())) || (zaNodes.contains(e.getZ()) && zaNodes.contains(e.getA())))
                .collect(Collectors.toSet());

        // Prune all URNs from topology not matching specified EROs
        multiLayerTopoAzDirection.getVertices().retainAll(azNodes);
        multiLayerTopoZaDirection.getVertices().retainAll(zaNodes);

        // Prune all Edges from topology not beginning/ending at specified ERO URNs
        multiLayerTopoAzDirection.getEdges().retainAll(edgestoKeepAz);
        multiLayerTopoZaDirection.getEdges().retainAll(edgestoKeepZa);

        for (TopoEdge badEdge : multiLayerTopoAzDirection.getEdges()) {
            if (badEdge.getA().getVertexType().equals(VertexType.SWITCH) || badEdge.getZ().getVertexType().equals(VertexType.SWITCH)) {
                if (!multiLayerTopoZaDirection.getVertices().contains(badEdge.getA()) && !multiLayerTopoZaDirection.getVertices().contains(badEdge.getZ())) {
                    throw new PCEException("All ETHERNET-layer devices and ports must be represented in both the forward-direction and reverse-direction EROs");
                }
            }
        }

        for (TopoEdge badEdge : multiLayerTopoZaDirection.getEdges()) {
            if (badEdge.getA().getVertexType().equals(VertexType.SWITCH) || badEdge.getZ().getVertexType().equals(VertexType.SWITCH)) {
                if (!multiLayerTopoAzDirection.getVertices().contains(badEdge.getA()) && !multiLayerTopoAzDirection.getVertices().contains(badEdge.getZ())) {
                    throw new PCEException("All ETHERNET-layer devices and ports must be represented in both the forward-direction and reverse-direction EROs");
                }
            }
        }

        // Check if the destination is reachable from the source
        // If not, then only a partial ERO has been passed in
        if (!checkForReachability(multiLayerTopoAzDirection, srcPort, dstPort) || !checkForReachability(multiLayerTopoAzDirection, srcPort, dstPort)) {
            return handlePartialERO(topoService.getMultilayerTopology(), requestPipe, requestSched, rsvBwList, rsvVlanList, requestedAzERO, requestedZaERO);
        }

        // Bandwidth and Vlan pruning
        Topology prunedTopoAZ = pruningService.pruneWithPipeAZ(multiLayerTopoAzDirection, requestPipe, requestSched, rsvBwList, rsvVlanList);
        Topology prunedTopoZA = pruningService.pruneWithPipeZA(multiLayerTopoZaDirection, requestPipe, requestSched, rsvBwList, rsvVlanList);


        if (!prunedTopoAZ.equals(multiLayerTopoAzDirection))
            throw new PCEException("Requested AZ ERO unavailable; failed to complete Path Computation");
        if (!prunedTopoZA.equals(multiLayerTopoZaDirection))
            throw new PCEException("Requested ZA ERO unavailable; failed to complete Path Computation");

        // Shortest path routing
        List<TopoEdge> azEroCalculated = dijkstraPCE.computeShortestPathEdges(prunedTopoAZ, srcPort, dstPort);
        List<TopoEdge> zaEroCalculated = dijkstraPCE.computeShortestPathEdges(prunedTopoZA, dstPort, srcPort);

        if (azEroCalculated.isEmpty() || zaEroCalculated.isEmpty()) {
            throw new PCEException("Empty path from Symmetric PCE");
        }

        List<TopoVertex> azEroVertices = dijkstraPCE.translatePathEdgesToVertices(azEroCalculated);
        List<TopoVertex> zaEroVertices = dijkstraPCE.translatePathEdgesToVertices(zaEroCalculated);
        List<String> azEroStrings = dijkstraPCE.translatePathVerticesToStrings(azEroVertices);
        List<String> zaEroStrings = dijkstraPCE.translatePathVerticesToStrings(zaEroVertices);


        if (!azEroStrings.equals(requestedAzERO) || !zaEroStrings.equals(requestedZaERO)) {
            throw new PCEException("Requested ERO unavailable; failed to complete Path Computation");
        }

        Map<String, List<TopoEdge>> theMap = new HashMap<>();
        theMap.put("az", azEroCalculated);
        theMap.put("za", zaEroCalculated);

        return theMap;
    }

    private Map<String, List<TopoEdge>> handlePartialERO(Topology topo, RequestedVlanPipeE reqPipe, ScheduleSpecificationE sched,
                                                         List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList,
                                                         List<String> azERO, List<String> zaERO) throws PCEException {

        // Find the shortest AZ path from the source to each intermediate "destination" before reaching the final
        // destination. Repeat this process for the ZA path
        List<TopoVertex> azNodes = azERO.stream().map(topo::getVertexByUrn).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
        List<TopoVertex> zaNodes = zaERO.stream().map(topo::getVertexByUrn).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());

        Set<TopoVertex> allNodes = new HashSet<>(azNodes);
        allNodes.addAll(zaNodes);
        if (allNodes.size() != azNodes.size() || allNodes.size() != zaNodes.size()) {
            log.info("AZ partial ERO: " + azERO);
            log.info("ZA partial ERO: " + zaERO);
            throw new PCEException("Routing failed as the AZ or ZA partial EROs are not palindromic.");
        }

        Topology prunedTopo = pruningService.pruneWithPipe(topo, reqPipe, sched, rsvBwList, rsvVlanList);

        List<TopoEdge> azPath = new ArrayList<>();
        for (Integer index = 0; index < azNodes.size() - 1; index++) {
            TopoVertex src = azNodes.get(index);
            TopoVertex dst = azNodes.get(index + 1);

            List<TopoEdge> path = dijkstraPCE.computeShortestPathEdges(prunedTopo, src, dst);
            if (path.isEmpty()) {
                throw new PCEException("AZ Path could not be found to connect Requested Nodes " + src.getUrn() + " and " + dst.getUrn());
            }
            azPath.addAll(path);
        }

        /**
         * Enforces using the reverse AZ path for the ZA path.
         * If not enforcing this, comment out this code block, and uncomment the code block below this one
         */
        List<TopoEdge> zaPath = azPath
                .stream()
                .map(e -> prunedTopo.getEdges()
                        .stream()
                        .filter(e2 -> e2.getA().equals(e.getZ()) && e2.getZ().equals(e.getA()) && e2.getLayer().equals(e.getLayer()))
                        .findFirst()
                        .orElse(null))
                .collect(Collectors.toList());
        Collections.reverse(zaPath);

        /*
        List<TopoEdge> zaPath = new ArrayList<>();
        for(Integer index = 0; index < zaNodes.size()-1; index++){
            TopoVertex src = zaNodes.get(index);
            TopoVertex dst = zaNodes.get(index+1);

            List<TopoEdge> path = dijkstraPCE.computeShortestPathEdges(prunedTopo, src, dst);
            if(path.isEmpty()){
                throw new PCEException("ZA Path could not be found to connect Requested Nodes " + src.getUrn() + " and " + dst.getUrn());
            }
            zaPath.addAll(path);
        }
        */

        if (!confirmValidPath(azPath, azNodes)) {
            throw new PCEException("The requested AZ path could not be found, " + azPath.toString() + " found instead");
        }
        if (!confirmValidPath(zaPath, zaNodes)) {
            throw new PCEException("The requested ZA path could not be found, " + zaPath.toString() + " found instead");
        }

        Map<String, List<TopoEdge>> theMap = new HashMap<>();
        theMap.put("az", azPath);
        theMap.put("za", zaPath);

        return theMap;
    }

    private boolean confirmValidPath(List<TopoEdge> path, List<TopoVertex> expectedNodes) {
        boolean allNodes = expectedNodes.stream().allMatch(v -> path.stream().anyMatch(edge -> edge.getA().equals(v) || edge.getZ().equals(v)));
        return allNodes && !path.isEmpty() && !path.contains(null);
    }

    private boolean checkForReachability(Topology topo, TopoVertex src, TopoVertex dst) {
        Map<TopoVertex, Boolean> discoveredMap = new HashMap<>();
        Map<TopoVertex, List<TopoEdge>> outgoingMap = buildOutgoingEdgeMap(topo);
        depthFirstSearch(topo, src, discoveredMap, outgoingMap);
        return discoveredMap.getOrDefault(dst, false);
    }

    private Map<TopoVertex, List<TopoEdge>> buildOutgoingEdgeMap(Topology topo) {
        return topo.getVertices()
                .stream()
                .collect(Collectors.toMap(v -> v, v -> topo.getEdges().stream().filter(e -> e.getA().equals(v)).collect(Collectors.toList())));
    }

    private void depthFirstSearch(Topology topo, TopoVertex vertex, Map<TopoVertex, Boolean> discoveredMap,
                                  Map<TopoVertex, List<TopoEdge>> outgoingEdgeMap) {
        discoveredMap.put(vertex, true);
        for (TopoEdge outgoingEdge : outgoingEdgeMap.getOrDefault(vertex, new ArrayList<>())) {
            if (!discoveredMap.getOrDefault(outgoingEdge.getZ(), false)) {
                depthFirstSearch(topo, outgoingEdge.getZ(), discoveredMap, outgoingEdgeMap);
            }
        }
    }
}
