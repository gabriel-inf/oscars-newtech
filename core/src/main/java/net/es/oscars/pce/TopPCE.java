package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TopPCE {

    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private Layer3PCE layer3PCE;

    @Autowired
    private TranslationPCE transPCE;

    @Autowired
    private PalindromicalPCE palindromicalPCE;

    @Autowired
    private LacimordnilapPCE nonPalindromicPCE;

    public ReservedBlueprintE makeReserved(RequestedBlueprintE requested, ScheduleSpecificationE schedSpec) throws PCEException, PSSException {

        verifyRequested(requested);

        ReservedBlueprintE reserved = ReservedBlueprintE.builder()
                .vlanFlows(new HashSet<>())
                .build();

        /*
        for (Layer3FlowE req_f : requested.getLayer3Flows()) {
            Layer3FlowE res_f = layer3PCE.makeReserved(req_f, schedSpec);
            reserved.getLayer3Flows().add(res_f);
        }
        */

        for (RequestedVlanFlowE req_f : requested.getVlanFlows()) {
            ReservedVlanFlowE res_f = new ReservedVlanFlowE();

            // Attempt to reserve simple junctions
            Set<ReservedVlanJunctionE> simpleJunctions = new HashSet<>();
            for(RequestedVlanJunctionE reqJunction : req_f.getJunctions()){
                ReservedVlanJunctionE junction = transPCE.reserveSimpleJunction(reqJunction, schedSpec, simpleJunctions);
                if(junction != null){
                    simpleJunctions.add(junction);
                }
            }

            // If not all junctions were able to be reserved, return the blank Reserved Vlan FLow
            if(simpleJunctions.size() != req_f.getJunctions().size()){
                reserved.getVlanFlows().add(res_f);
            }

            List<RequestedVlanPipeE> pipes = new ArrayList<>();
            pipes.addAll(req_f.getPipes());

            // Create temporary storage for reserved pipes and junctions
            Set<ReservedEthPipeE> reservedPipes = new HashSet<>();
            Set<ReservedVlanJunctionE> reservedEthJunctions = new HashSet<>();

            // Keep track of the number of successfully reserved pipes
            Integer numReserved = 0;

            // Attempt to reserve all requested pipes
            handleRequestedPipes(pipes, schedSpec, simpleJunctions, reservedPipes, reservedEthJunctions, numReserved);

            // If pipes were not able to be reserved in the original order, try reversing the order pipes are attempted
            if(numReserved != pipes.size()){
                Collections.reverse(pipes);
                numReserved = 0;
                reservedPipes = new HashSet<>();
                reservedEthJunctions = new HashSet<>();
                handleRequestedPipes(pipes, schedSpec, simpleJunctions, reservedPipes, reservedEthJunctions, numReserved);
            }

            // If the pipes still cannot be reserved, return the blank Reserved Vlan Flow
            if(numReserved != pipes.size()){
                reserved.getVlanFlows().add(res_f);
            }
            // All pipes were successfully found, translate store the reserved resources
            else{
                Set<ReservedVlanJunctionE> rsvJunctions = new HashSet<>(simpleJunctions);
                rsvJunctions.addAll(reservedEthJunctions);
                res_f.setJunctions(rsvJunctions);
                res_f.setPipes(reservedPipes);
                reserved.getVlanFlows().add(res_f);
            }
        }
        return reserved;

    }

    private void handleRequestedPipes(List<RequestedVlanPipeE> pipes, ScheduleSpecificationE schedSpec,
                                      Set<ReservedVlanJunctionE> simpleJunctions, Set<ReservedEthPipeE> reservedPipes,
                                      Set<ReservedVlanJunctionE> reservedEthJunctions, Integer numReserved) {

        for(RequestedVlanPipeE pipe: pipes){
            Map<String, List<TopoEdge>> eroMapForPipe = findShortestConstrainedPath(pipe, schedSpec, simpleJunctions,
                    reservedPipes, reservedEthJunctions);
            if(verifyEros(eroMapForPipe)){
                numReserved++;
                List<TopoEdge> azEros = eroMapForPipe.get("az");
                List<TopoEdge> zaEros = eroMapForPipe.get("za");
                transPCE.reserveRequestedPipe(pipe, schedSpec, azEros, zaEros, simpleJunctions, reservedPipes,
                        reservedEthJunctions);
            }
        }
    }

    private Map<String,List<TopoEdge>> findShortestConstrainedPath(RequestedVlanPipeE pipe,
                                                                   ScheduleSpecificationE schedSpec,
                                                                   Set<ReservedVlanJunctionE> simpleJunctions,
                                                                   Set<ReservedEthPipeE> reservedPipes,
                                                                   Set<ReservedVlanJunctionE> reservedEthJunctions) {
        Map<String, List<TopoEdge>> eroMap = null;
        Set<ReservedVlanJunctionE> reservedJunctions = new HashSet<>(simpleJunctions);
        reservedJunctions.addAll(reservedEthJunctions);

        List<ReservedBandwidthE> rsvBandwidths = transPCE.retrieveReservedBandwidths(reservedJunctions);
        rsvBandwidths.addAll(transPCE.retrieveReservedBandwidthsFromPipes(reservedPipes));

        List<ReservedVlanE> rsvVlans = transPCE.retrieveReservedVlans(reservedJunctions);
        rsvVlans.addAll(transPCE.retrieveReservedVlansFromPipes(reservedPipes));

        if(pipe.getEroPalindromic()){
            try{
                eroMap = palindromicalPCE.computePalindromicERO(pipe, schedSpec, rsvBandwidths, rsvVlans);       // A->Z ERO is palindrome of Z->A ERO
            }
            catch(PCEException e){
                log.error("PCE Unsuccessful", e);
            }
        }
        else{
            try{
                eroMap = nonPalindromicPCE.computeCimordnilapERO(pipe, schedSpec, rsvBandwidths, rsvVlans);       // A->Z ERO is NOT palindrome of Z->A ERO
            }
            catch(PCEException e){
                log.error("PCE Unsuccessful", e);
            }
        }
        return eroMap;
    }


    private boolean verifyEros(Map<String, List<TopoEdge>> eroMap)
    {
        if(eroMap != null)
        {
            if (eroMap.size() == 2)
            {
                return eroMap.values().stream().allMatch(l -> l.size() > 0);
            }
        }

        return false;
    }

    public void verifyRequested(RequestedBlueprintE requested) throws PCEException {
        log.info("starting verification");
        if (requested == null) {
            throw new PCEException("Null blueprint!");
        } else if (requested.getVlanFlows() == null || requested.getVlanFlows().isEmpty()) {
            throw new PCEException("No VLAN flows");
        } else if (requested.getVlanFlows().size() != 1) {
            throw new PCEException("Exactly one flow supported right now");
        }

        RequestedVlanFlowE flow = requested.getVlanFlows().iterator().next();

        log.info("verifying junctions & pipes");
        if (flow.getJunctions().isEmpty() && flow.getPipes().isEmpty()) {
            throw new PCEException("Junctions or pipes both empty.");
        }

        Set<RequestedVlanJunctionE> allJunctions = new HashSet<>();
        allJunctions.addAll(flow.getJunctions());
        flow.getPipes().stream().forEach(t -> {
            allJunctions.add(t.getAJunction());
            allJunctions.add(t.getZJunction());
        });

        for (RequestedVlanJunctionE junction: allJunctions) {
            // throws exception if device not found in topology
            try {
                topoService.device(junction.getDeviceUrn().getUrn());
            } catch (NoSuchElementException ex) {
                throw new PCEException("device not found in topology");
            }
        }

        Set<String> junctionsWithNoFixtures = allJunctions.stream().
                filter(t -> t.getFixtures().isEmpty()).
                map(t -> t.getDeviceUrn().getUrn()).collect(Collectors.toSet());

        if (!junctionsWithNoFixtures.isEmpty()) {
            throw new PCEException("Junctions with no fixtures found: " + String.join(" ", junctionsWithNoFixtures));
        }
        log.info("all junctions & pipes are ok");

    }
}
