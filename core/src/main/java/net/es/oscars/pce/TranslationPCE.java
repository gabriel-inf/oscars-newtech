package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.dao.ReservedPssResourceRepository;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TranslationPCE {
    @Autowired
    private PCEAssistant pceAssistant;

    @Autowired
    private TopoService topoService;

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private ReservedVlanRepository vlanRepo;

    @Autowired
    private ReservedPssResourceRepository pssResourceRepo;

    @Autowired
    private UrnRepository urnRepository;

    @Autowired
    private PruningService pruningService;

    public ReservedVlanJunctionE reserveSimpleJunction(RequestedVlanJunctionE req_j, ScheduleSpecificationE sched,
                                                       Set<ReservedVlanJunctionE> simpleJunctions)
            throws PCEException, PSSException {
        String deviceUrn = req_j.getDeviceUrn().getUrn();
        Optional<UrnE> optUrn = urnRepository.findByUrn(deviceUrn);
        UrnE urn;
        if(optUrn.isPresent()){
            urn = optUrn.get();
        }
        else{
            log.error("URN " + deviceUrn + " not found in URN Repository");
            return null;
        }

        ReservedVlanJunctionE rsv_j = pceAssistant.createReservedJunction(urn, new HashSet<>(), new HashSet<>(),
                pceAssistant.decideJunctionType(urn.getDeviceModel()));

        // Select a VLAN ID for this junction
        Integer vlanId = selectVLANForJunction(req_j, sched, simpleJunctions);
        if(vlanId == -1){
            return null;
        }

        // Confirm that there is sufficient available bandwidth
        boolean sufficientBandwidth = confirmSufficientBandwidth(req_j, sched, simpleJunctions);
        if(!sufficientBandwidth){
            return null;
        }

        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();
        for(RequestedVlanFixtureE reqFix : reqFixtures){
            ReservedBandwidthE rsvBw = pceAssistant.createReservedBandwidth(reqFix.getPortUrn(), reqFix.getInMbps(),
                    reqFix.getEgMbps(), sched);

            ReservedVlanE rsvVlan = pceAssistant.createReservedVlan(reqFix.getPortUrn(), vlanId, sched);

            ReservedVlanFixtureE rsvFix = pceAssistant.createReservedFixture(reqFix.getPortUrn(), new HashSet<>(),
                    rsvVlan, rsvBw, pceAssistant.decideFixtureType(reqFix.getPortUrn().getDeviceModel()));

            rsv_j.getFixtures().add(rsvFix);
        }

        return rsv_j;
    }

    public void reserveRequestedPipe(RequestedVlanPipeE reqPipe, ScheduleSpecificationE sched, List<TopoEdge> azERO,
                                     List<TopoEdge> zaERO, Set<ReservedVlanJunctionE> simpleJunctions,
                                     Set<ReservedEthPipeE> reservedPipes, Set<ReservedVlanJunctionE> reservedEthJunctions)
    throws PCEException, PSSException{

        Map<String, DeviceModel> deviceModels = topoService.deviceModels();


        // now, decompose the path
        List<Map<Layer, List<TopoEdge>>> azSegments = PCEAssistant.decompose(azERO, deviceModels);
        List<Map<Layer, List<TopoEdge>>> zaSegments = PCEAssistant.decompose(zaERO, deviceModels);
        assert(azSegments.size() == zaSegments.size());

        Map<String , UrnE> urnMap = new HashMap<>();

        urnRepository.findAll().stream().forEach(u -> {
            urnMap.put(u.getUrn(), u);

        });


        List<Integer> vlanIdPerSegment = selectVlanIds(urnMap, reqPipe, sched, azSegments);

        // for each segment:
        // if it is an Ethernet segment, make junctions, one per device
        // if it is an MPLS segment, make a pipe
        // all the while, make sure to merge in the current first and last junctions as needed

        for (int i = 0; i < azSegments.size(); i++) {
            // Get az segment and za segment
            Map<Layer, List<TopoEdge>> azSegment = azSegments.get(i);
            Map<Layer, List<TopoEdge>> zaSegment = zaSegments.get(i);

            // Get Chosen VLAN ID for segment
            Integer vlanId = vlanIdPerSegment.get(0);

            // Create mergeA and MergeZ for AZ segment
            Optional<ReservedVlanJunctionE> mergeA = Optional.empty();
            Optional<ReservedVlanJunctionE> mergeZ = Optional.empty();
            if (i == 0) {
                UrnE urn = reqPipe.getAJunction().getDeviceUrn();

                mergeA = Optional.of(pceAssistant.createReservedJunction(urn, new HashSet<>(), new HashSet<>()
                        , pceAssistant.decideJunctionType(deviceModels.get(urn.getUrn()))));
            }
            if (i == azSegments.size() - 1) {
                UrnE urn = reqPipe.getZJunction().getDeviceUrn();
                mergeZ = Optional.of(pceAssistant.createReservedJunction(urn, new HashSet<>(), new HashSet<>()
                        , pceAssistant.decideJunctionType(deviceModels.get(urn.getUrn()))));
            }


            List<TopoEdge> azEdges;
            List<TopoEdge> zaEdges;

            if (azSegment.size() != 1) {
                throw new PCEException("invalid segmentation");
            }
            if (azSegment.containsKey(Layer.ETHERNET)) {
                if (azSegment.get(Layer.ETHERNET).size() != 3) {
                    throw new PCEException("invalid segmentation");
                }

                azEdges = azSegment.get(Layer.ETHERNET);

                // an ethernet segment: a list of junctions, one per device

                List<ReservedVlanJunctionE> azVjs = pceAssistant.makeEthernetJunctions(azEdges,
                        reqPipe.getAzMbps(), reqPipe.getZaMbps(), vlanId,
                        mergeA, mergeZ,
                        sched,
                        urnMap,
                        deviceModels);

                reservedEthJunctions.addAll(azVjs);

            } else if (azSegment.containsKey(Layer.MPLS)) {
                azEdges = azSegment.get(Layer.MPLS);
                zaEdges = zaSegment.get(Layer.MPLS);

                ReservedEthPipeE pipe = pceAssistant.makeVplsPipe(azEdges, zaEdges, reqPipe.getAzMbps(),
                        reqPipe.getZaMbps(), vlanId, mergeA, mergeZ, urnMap, deviceModels, sched);
                reservedPipes.add(pipe);
            } else {
                throw new PCEException("invalid segmentation");
            }
        }
    }

    private List<Integer> selectVlanIds(Map<String, UrnE> urnMap,
                                                   RequestedVlanPipeE reqPipe, ScheduleSpecificationE sched,
                                                   List<Map<Layer, List<TopoEdge>>> segments) {
        List<Integer> vlanIdPerSegment = new ArrayList<>();

        List<ReservedVlanE> rsvVlans = pruningService.getReservedVlans(sched.getNotBefore(), sched.getNotAfter());
        Map<UrnE, List<ReservedVlanE>> rsvVlanMap = pruningService.buildReservedVlanMap(rsvVlans);

        String vlanExpression = reqPipe.getAJunction().getFixtures().iterator().next().getVlanExpression();
        Set<Integer> requestedVlanIds = pruningService
                .getIntegersFromRanges(pruningService.getIntRangesFromString(vlanExpression));

        List<Set<Integer>> validIdsPerSegment = new ArrayList<>();
        for(Map<Layer, List<TopoEdge>> segment: segments){
            if(segment.containsKey(Layer.ETHERNET)){
                List<TopoEdge> segmentEdges = segment.get(Layer.ETHERNET);
                Map<Integer, Set<TopoEdge>> edgesPerVlanId = pruningService
                        .findEdgesPerVlanId(new HashSet<>(segmentEdges), urnMap, rsvVlanMap);
                validIdsPerSegment.add(new HashSet<>());
                for(Integer id : edgesPerVlanId.keySet()){
                    Set<TopoEdge> edgesForId = edgesPerVlanId.get(id);
                    if(edgesForId.size() == segmentEdges.size()){
                        if(requestedVlanIds.contains(id) || requestedVlanIds.isEmpty()){
                            validIdsPerSegment.get(validIdsPerSegment.size()-1).add(id);
                        }
                    }
                }
            }
        }

        //TODO: VLAN Translation
        //For now: Just use same VLAN ID on all segments
        Set<Integer> overlappingVlanIds = new HashSet<>();
        for(Set<Integer> validIds : validIdsPerSegment){
            overlappingVlanIds = pruningService.addToOverlap(overlappingVlanIds, validIds);
        }
        assert(!overlappingVlanIds.isEmpty());
        for(Integer i = 0; i < segments.size(); i++){
            vlanIdPerSegment.add(overlappingVlanIds.iterator().next());
        }
        return vlanIdPerSegment;
    }


    public boolean confirmSufficientBandwidth(RequestedVlanJunctionE req_j, ScheduleSpecificationE sched,
                                              Set<ReservedVlanJunctionE> rsvJunctions){

        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();

        // Get reserved bandwidth from repository
        List<ReservedBandwidthE> reservedBandwidthList = pruningService.getReservedBandwidth(sched.getNotBefore(), sched.getNotAfter());
        // Combine list with bandwidth retrieved from the passed in junctions
        reservedBandwidthList.addAll(retrieveReservedBandwidths(rsvJunctions));

        Map<UrnE, List<ReservedBandwidthE>> reservedBwMap = pruningService.buildReservedBandwidthMap(reservedBandwidthList);


        for(RequestedVlanFixtureE reqFix: reqFixtures){
            ReservableBandwidthE reservableBw = reqFix.getPortUrn().getReservableBandwidth();

            Map<String, Integer> availBwMap = pruningService.getBwAvailabilityForUrn(reqFix.getPortUrn(), reservableBw,
                    reservedBwMap);

            if(availBwMap.get("Ingress") < reqFix.getInMbps() || availBwMap.get("Egress") < reqFix.getEgMbps()){
                log.error("Insufficient Bandwidth at " + reqFix.getPortUrn().toString() + ". Requested: " +
                        reqFix.getInMbps() + " In and " + reqFix.getEgMbps() + " Out. Available: " + availBwMap.get("Ingress") +
                        " In and " + availBwMap.get("Egress") + " Out.");
                return false;
            }
        }
        return true;
    }

    public Integer selectVLANForJunction(RequestedVlanJunctionE req_j, ScheduleSpecificationE sched,
                                         Set<ReservedVlanJunctionE> rsvJunctions){
        // Get Reserved VLANs from Repository
        List<ReservedVlanE> rsvVlans = pruningService.getReservedVlans(sched.getNotBefore(), sched.getNotAfter());
        // Add already reserved VLANs from passed in junctions
        rsvVlans.addAll(retrieveReservedVlans(rsvJunctions));

        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();

        Set<Integer> overlap = null;
        for(RequestedVlanFixtureE reqFix : reqFixtures){
            Set<Integer> availableVlans = getAvailableVlanIds(reqFix, rsvVlans);
            String vlanExpression = reqFix.getVlanExpression();
            Set<Integer> reqVlanIds = pruningService.getIntegersFromRanges(pruningService.getIntRangesFromString(vlanExpression));
            Set<Integer> validVlans = pruningService.addToOverlap(availableVlans, reqVlanIds);
            if(overlap == null){
                overlap = validVlans;
            }
            overlap = pruningService.addToOverlap(overlap, validVlans);
            if(overlap.isEmpty()){
                log.error("Requested VLAN IDs " + reqVlanIds + " not available at " + reqFix.getPortUrn().toString());
                return -1;
            }
        }

        // The chosen VLAN ID
        if(overlap == null || overlap.isEmpty())
            return -1;
        return overlap.iterator().next();
    }

    public Set<Integer> getAvailableVlanIds(RequestedVlanFixtureE reqFix, List<ReservedVlanE> rsvVlans){

        // Build map from URNs to Reserved VLAN lists
        Map<UrnE, List<ReservedVlanE>> rsvVlanMap = pruningService.buildReservedVlanMap(rsvVlans);

        List<ReservedVlanE> rsvVlansAtFixture = rsvVlanMap.get(reqFix.getPortUrn());
        Set<Integer> reservedVlanIds = rsvVlansAtFixture.stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());

        Set<Integer> reservableVlanIds = pruningService.getIntegersFromRanges(
                reqFix.getPortUrn()
                        .getReservableVlans()
                        .getVlanRanges()
                        .stream()
                        .map(IntRangeE::toDtoIntRange)
                        .collect(Collectors.toList()));

        return reservableVlanIds
                .stream()
                .filter(id -> !reservedVlanIds.contains(id))
                .collect(Collectors.toSet());
    }

    public List<ReservedBandwidthE> retrieveReservedBandwidths(Set<ReservedVlanJunctionE> junctions){
        return junctions
                .stream()
                .map(ReservedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(ReservedVlanFixtureE::getReservedBandwidth)
                .collect(Collectors.toList());
    }

    public List<ReservedVlanE> retrieveReservedVlans(Set<ReservedVlanJunctionE> junctions){
        return junctions
                .stream()
                .map(ReservedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(ReservedVlanFixtureE::getReservedVlan)
                .collect(Collectors.toList());
    }

    public List<ReservedBandwidthE> retrieveReservedBandwidthsFromPipes(Set<ReservedEthPipeE> reservedPipes) {
        Set<ReservedVlanJunctionE> junctions = new HashSet<>();
        for(ReservedEthPipeE pipe : reservedPipes){
            junctions.add(pipe.getAJunction());
            junctions.add(pipe.getZJunction());
        }
        return retrieveReservedBandwidths(junctions);
    }

    public List<ReservedVlanE> retrieveReservedVlansFromPipes(Set<ReservedEthPipeE> reservedPipes) {
        Set<ReservedVlanJunctionE> junctions = new HashSet<>();
        for(ReservedEthPipeE pipe : reservedPipes){
            junctions.add(pipe.getAJunction());
            junctions.add(pipe.getZJunction());
        }
        return retrieveReservedVlans(junctions);
    }
}
