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
    private NonPalindromicalPCE nonPalindromicPCE;

    /**
     * Given a requested Blueprint (made up of a VLAN or Layer3 Flow) and a Schedule Specification, attempt
     * to reserve available resources to meet the demand. If it is not possible, return an empty Optional<ReservedBlueprintE>
     * @param requested - Requested blueprint
     * @param schedSpec - Requested schedule
     * @return ReservedBlueprint containing the reserved resources, or an empty Optional if the reservation is not possible.
     * @throws PCEException
     * @throws PSSException
     */
    public Optional<ReservedBlueprintE> makeReserved(RequestedBlueprintE requested, ScheduleSpecificationE schedSpec) throws PCEException, PSSException {

        // Verify that the input is valid
        verifyRequested(requested);

        // Initialize an empty Optional<>
        Optional<ReservedBlueprintE> reserved = Optional.empty();

        log.info("Handling Request");
        // Retrieve the VLAN flow
        RequestedVlanFlowE req_f = requested.getVlanFlow();

        // Attempt to reserve simple junctions
        log.info("Handling Simple Junctions");
        Set<ReservedVlanJunctionE> simpleJunctions = new HashSet<>();
        for(RequestedVlanJunctionE reqJunction : req_f.getJunctions())
        {
            ReservedVlanJunctionE junction = transPCE.reserveSimpleJunction(reqJunction, schedSpec, simpleJunctions);

            if(junction != null){
                simpleJunctions.add(junction);
            }
        }
        log.info("All simple junctions handled");
        // If not all junctions were able to be reserved, return the empty Optional<Blueprint>
        if(simpleJunctions.size() != req_f.getJunctions().size()){
            return reserved;
        }

        List<RequestedVlanPipeE> pipes = new ArrayList<>();
        pipes.addAll(req_f.getPipes());

        // Create temporary storage for reserved pipes and junctions
        Set<ReservedEthPipeE> reservedPipes = new HashSet<>();
        Set<ReservedVlanJunctionE> reservedEthJunctions = new HashSet<>();

        // Keep track of the number of successfully reserved pipes (numReserved)
        // Attempt to reserve all requested pipes
        log.info("Starting to handle pipes");
        Integer numReserved = handleRequestedPipes(pipes, schedSpec, simpleJunctions, reservedPipes, reservedEthJunctions);

        // If pipes were not able to be reserved in the original order, try reversing the order pipes are attempted
        if((numReserved != pipes.size()) && (pipes.size() > 1)){
            Collections.reverse(pipes);
            reservedPipes = new HashSet<>();
            reservedEthJunctions = new HashSet<>();
            numReserved = handleRequestedPipes(pipes, schedSpec, simpleJunctions, reservedPipes, reservedEthJunctions);
        }

        // If the pipes still cannot be reserved, return the blank Reserved Vlan Flow
        if(numReserved != pipes.size()){
            return reserved;
        }
        // All pipes were successfully found, store the reserved resources
        Set<ReservedVlanJunctionE> reservedJunctions = new HashSet<>(simpleJunctions);
        reservedJunctions.addAll(reservedEthJunctions);

        // Make the reserved flow
        ReservedVlanFlowE res_f = ReservedVlanFlowE.builder()
                .junctions(reservedJunctions)
                .pipes(reservedPipes)
                .build();

        // Build the reserved Blueprint
        reserved = Optional.of(ReservedBlueprintE.builder().vlanFlow(res_f).build());
        return reserved;

    }

    /**
     * Given a list of requested pipes and a schedule, attempt to reserve junctions & pipes to implement the request.
     * Sets of currently reserved junctions and pipes are passed in to track how many resources have already been reserved.
     * @param pipes - The requested pipes
     * @param schedSpec - The requested schedule
     * @param simpleJunctions - The currently reserved independent junctions
     * @param reservedPipes - The currently reserved pipes
     * @param reservedEthJunctions - The currently reserved junctions (from an ethernet segment)
     * @return The number of requested pipes which were able to be reserved
     */
    private Integer handleRequestedPipes(List<RequestedVlanPipeE> pipes, ScheduleSpecificationE schedSpec,
                                      Set<ReservedVlanJunctionE> simpleJunctions, Set<ReservedEthPipeE> reservedPipes,
                                      Set<ReservedVlanJunctionE> reservedEthJunctions) {

        // The number of requested pipes successfully reserved
        Integer numReserved = 0;
        // Loop through all requested pipes
        for(RequestedVlanPipeE pipe: pipes){
            // Find the shortest path for the pipe, build a map for the AZ and ZA path
            Map<String, List<TopoEdge>> eroMapForPipe = findShortestConstrainedPath(pipe, schedSpec, simpleJunctions,
                    reservedPipes, reservedEthJunctions);
            // If there paths are valid, attempt to reserve the resources
            if(verifyEros(eroMapForPipe)){
                // Increment the number reserved
                numReserved++;
                // Get the AZ and ZA paths
                List<TopoEdge> azEros = eroMapForPipe.get("az");
                List<TopoEdge> zaEros = eroMapForPipe.get("za");

                // Try to get the reserved resources
                try {
                    transPCE.reserveRequestedPipe(pipe, schedSpec, azEros, zaEros, simpleJunctions, reservedPipes,
                            reservedEthJunctions);
                }
                // If it failed, decrement the number reserved
                catch(Exception e){
                    log.info(e.toString());
                    numReserved--;
                }
            }
        }
        return numReserved;
    }

    /**
     * Given a requested pipe and schedule, find the shortest path that meets the demand given what has been requested
     * so far.
     * @param pipe - The requested pipe.
     * @param schedSpec - The requested schedule
     * @param simpleJunctions - A set of all discrete junctions reserved so far
     * @param reservedPipes - A set of all pipes reserved so far
     * @param reservedEthJunctions - A set of all junctions (one per device per ethernet segment)
     * @return A map containing the AZ and ZA shortest paths
     */
    private Map<String,List<TopoEdge>> findShortestConstrainedPath(RequestedVlanPipeE pipe,
                                                                   ScheduleSpecificationE schedSpec,
                                                                   Set<ReservedVlanJunctionE> simpleJunctions,
                                                                   Set<ReservedEthPipeE> reservedPipes,
                                                                   Set<ReservedVlanJunctionE> reservedEthJunctions) {
        log.info("Computing Shortest Constrained Path");
        Map<String, List<TopoEdge>> eroMap = null;
        Set<ReservedVlanJunctionE> reservedJunctions = new HashSet<>(simpleJunctions);
        reservedJunctions.addAll(reservedEthJunctions);

        List<ReservedBandwidthE> rsvBandwidths = transPCE.retrieveReservedBandwidths(reservedJunctions);
        rsvBandwidths.addAll(transPCE.retrieveReservedBandwidthsFromPipes(reservedPipes));

        List<ReservedVlanE> rsvVlans = transPCE.retrieveReservedVlans(reservedJunctions);
        rsvVlans.addAll(transPCE.retrieveReservedVlansFromPipes(reservedPipes));

        if(pipe.getEroPalindromic()){
            try{
                log.info("Entering Palindromical PCE");
                eroMap = palindromicalPCE.computePalindromicERO(pipe, schedSpec, rsvBandwidths, rsvVlans);       // A->Z ERO is palindrome of Z->A ERO
                log.info("Exitting Palindromical PCE");
            }
            catch(PCEException e){
                log.error("PCE Unsuccessful", e);
            }
        }
        else{
            try{
                log.info("Entering NON-Palindromical PCE");
                eroMap = nonPalindromicPCE.computeNonPalindromicERO(pipe, schedSpec, rsvBandwidths, rsvVlans);       // A->Z ERO is NOT palindrome of Z->A ERO
                log.info("Exiting NON-Palindromical PCE");
            }
            catch(PCEException e){
                log.error("PCE Unsuccessful", e);
            }
        }
        return eroMap;
    }


    /**
     * Verify that there both the AZ and ZA paths were found given a map of shortest paths.
     * @param eroMap The map containing the AZ and ZA shortest paths (keys: "az" and "za")
     * @return True if both paths found, False otherwise.
     */
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

    /**
     * Confirm that the requested blueprint is valid.
     * @param requested The requested blueprint.
     * @throws PCEException
     */
    public void verifyRequested(RequestedBlueprintE requested) throws PCEException {
        log.info("starting verification");
        if (requested == null) {
            throw new PCEException("Null blueprint!");
        }
        if (requested.getVlanFlow() == null) {
            throw new PCEException("No VLAN flows");
        }

        RequestedVlanFlowE flow = requested.getVlanFlow();

        log.info("verifying junctions & pipes");
        if (flow.getJunctions().isEmpty() && flow.getPipes().isEmpty()) {
            throw new PCEException("Junctions and pipes both empty.");
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
