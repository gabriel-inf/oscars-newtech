package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.spec.PalindromicType;
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

    @Autowired
    private EroPCE eroPCE;

    @Autowired
    private SurvivabilityPCE survivabilityPCE;

    @Autowired
    private VlanService vlanService;

    @Autowired
    private BandwidthService bwService;

    /**
     * Given a requested Blueprint (made up of a VLAN or Layer3 Flow) and a Schedule Specification, attempt
     * to reserve available resources to meet the demand. If it is not possible, return an empty Optional<ReservedBlueprintE>
     *
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

        // Create temporary storage for reserved pipes and junctions
        Set<ReservedMplsPipeE> reservedMplsPipes = new HashSet<>();
        Set<ReservedEthPipeE> reservedEthPipes = new HashSet<>();
        Set<ReservedVlanJunctionE> reservedEthJunctions = new HashSet<>();

        // Store the min/max number of pipes needed
        // TODO: Actually use these values to reserve a number of pipes
        Integer minPipes = req_f.getMinPipes();
        Integer maxPipes = req_f.getMaxPipes();

        // Get map of parent device vertex -> set of port vertices
        Map<String, Set<String>> deviceToPortMap = topoService.buildDeviceToPortMap();
        Map<String, String> portToDeviceMap = topoService.buildPortToDeviceMap(deviceToPortMap);

        // Attempt to reserve simple junctions
        log.info("Handling Simple Junctions");
        Set<ReservedVlanJunctionE> simpleJunctions = new HashSet<>();
        for (RequestedVlanJunctionE reqJunction : req_f.getJunctions()) {

            log.info("handling junction "+reqJunction.getDeviceUrn());

            // Update list of reserved bandwidths
            List<ReservedBandwidthE> rsvBandwidths = bwService.createReservedBandwidthList(simpleJunctions, reservedMplsPipes,
                    reservedEthPipes, schedSpec);

            // Update list of reserved VLAN IDs
            List<ReservedVlanE> rsvVlans = vlanService.createReservedVlanList(simpleJunctions, reservedEthPipes, schedSpec);

            ReservedVlanJunctionE junction = transPCE.reserveSimpleJunction(reqJunction, schedSpec, simpleJunctions,
                    rsvBandwidths, rsvVlans, deviceToPortMap, portToDeviceMap);

            if (junction != null) {
                simpleJunctions.add(junction);
            } else {
                log.error("failed at junction "+reqJunction.getDeviceUrn());
            }
        }
        log.info("All simple junctions handled");
        // If not all junctions were able to be reserved, return the empty Optional<Blueprint>
        if (simpleJunctions.size() != req_f.getJunctions().size()) {
            return reserved;
        }

        List<RequestedVlanPipeE> pipes = new ArrayList<>();
        pipes.addAll(req_f.getPipes());

        // Keep track of the number of successfully reserved pipes (numReserved)
        // Attempt to reserve all requested pipes
        log.info("Starting to handle pipes");
        Integer numReserved = handleRequestedPipes(pipes, schedSpec, simpleJunctions, reservedMplsPipes,
                reservedEthPipes, deviceToPortMap, portToDeviceMap);

        // If pipes were not able to be reserved in the original order, try reversing the order pipes are attempted
        if ((numReserved != pipes.size()) && (pipes.size() > 1)) {
            Collections.reverse(pipes);
            reservedEthPipes = new HashSet<>();
            reservedMplsPipes = new HashSet<>();
            reservedEthJunctions = new HashSet<>();
            numReserved = handleRequestedPipes(pipes, schedSpec, simpleJunctions, reservedMplsPipes,
                    reservedEthPipes, deviceToPortMap, portToDeviceMap);
        }

        // If the pipes still cannot be reserved, return the blank Reserved Vlan Flow
        if (numReserved != pipes.size()) {
            return reserved;
        }
        // All pipes were successfully found, store the reserved resources
        Set<ReservedVlanJunctionE> reservedJunctions = new HashSet<>(simpleJunctions);
        reservedJunctions.addAll(reservedEthJunctions);

        // Make the reserved flow
        ReservedVlanFlowE res_f = ReservedVlanFlowE.builder()
                .junctions(reservedJunctions)
                .ethPipes(reservedEthPipes)
                .mplsPipes(reservedMplsPipes)
                .build();

        // Build the reserved Blueprint
        reserved = Optional.of(ReservedBlueprintE.builder().vlanFlow(res_f).build());
        return reserved;

    }

    /**
     * Given a list of requested pipes and a schedule, attempt to reserve junctions & pipes to implement the request.
     * Sets of currently reserved junctions and pipes are passed in to track how many resources have already been reserved.
     *
     * @param pipes             - The requested pipes
     * @param schedSpec         - The requested schedule
     * @param simpleJunctions   - The currently reserved independent junctions
     * @param reservedMplsPipes - The currently reserved MPLS pipes
     * @param reservedEthPipes  - The currently reserved Ethernet pipes
     * @param deviceToPortMap
     * @param portToDeviceMap   @return The number of requested pipes which were able to be reserved
     */
    private Integer handleRequestedPipes(List<RequestedVlanPipeE> pipes, ScheduleSpecificationE schedSpec,
                                         Set<ReservedVlanJunctionE> simpleJunctions, Set<ReservedMplsPipeE> reservedMplsPipes,
                                         Set<ReservedEthPipeE> reservedEthPipes, Map<String, Set<String>> deviceToPortMap, Map<String, String> portToDeviceMap) {
        // The number of requested pipes successfully reserved
        Integer numReserved = 0;

        // Loop through all requested pipes
        for (RequestedVlanPipeE pipe : pipes) {

            // Update list of reserved bandwidths
            List<ReservedBandwidthE> rsvBandwidths = bwService.createReservedBandwidthList(simpleJunctions, reservedMplsPipes,
                    reservedEthPipes, schedSpec);

            // Update list of reserved VLAN IDs
            List<ReservedVlanE> rsvVlans = vlanService.createReservedVlanList(simpleJunctions, reservedEthPipes, schedSpec);

            // Find the shortest path for the pipe, build a map for the AZ and ZA path
            Map<String, List<TopoEdge>> eroMapForPipe = findShortestConstrainedPath(pipe, schedSpec, rsvBandwidths, rsvVlans);

            // If the paths are valid, attempt to reserve the resources
            if (verifyEros(eroMapForPipe)) {
                // Increment the number reserved
                numReserved++;
                // Get the AZ and ZA paths
                List<TopoEdge> azEros = eroMapForPipe.get("az");
                List<TopoEdge> zaEros = eroMapForPipe.get("za");

                // Try to get the reserved resources
                try {
                    transPCE.reserveRequestedPipe(pipe, schedSpec, azEros, zaEros, rsvBandwidths, rsvVlans,
                            reservedMplsPipes, reservedEthPipes, deviceToPortMap, portToDeviceMap);
                }
                // If it failed, decrement the number reserved
                catch (Exception e) {
                    log.info(e.toString());
                    e.printStackTrace();
                    numReserved--;
                }
            }
            // If the survivable paths are valid, attempt to reserve the resources
            else if (verifySurvEros(eroMapForPipe)) {
                // Increment the number reserved
                numReserved++;
                // Get the AZ and ZA paths

                List<List<TopoEdge>> azEROs = new ArrayList<>();
                List<List<TopoEdge>> zaEROs = new ArrayList<>();

                for (Integer i = 1; i < eroMapForPipe.size() / 2 + 1; i++) {

                    azEROs.add(eroMapForPipe.get("az" + i));
                    zaEROs.add(eroMapForPipe.get("za" + i));
                }

                // Try to get the reserved resources
                try {
                    transPCE.reserveRequestedPipeWithPairs(pipe, schedSpec, azEROs, zaEROs, rsvBandwidths,
                            rsvVlans, reservedMplsPipes, reservedEthPipes, deviceToPortMap, portToDeviceMap);
                }
                // If it failed, decrement the number reserved
                catch (Exception e) {
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
     *
     * @param pipe          - The requested pipe.
     * @param schedSpec     - The requested schedule
     * @param rsvBandwidths - A list of all reserved bandwidths (so far)
     * @param rsvVlans      - A list of all reserved VLANs (so far)
     * @return A map containing the AZ and ZA shortest paths
     */
    private Map<String, List<TopoEdge>> findShortestConstrainedPath(RequestedVlanPipeE pipe,
                                                                    ScheduleSpecificationE schedSpec,
                                                                    List<ReservedBandwidthE> rsvBandwidths,
                                                                    List<ReservedVlanE> rsvVlans) {
        log.info("Computing Shortest Constrained Path");
        Map<String, List<TopoEdge>> eroMap = null;

        try {
            if (!pipe.getEroSurvivability().equals(SurvivabilityType.SURVIVABILITY_NONE) && pipe.getNumDisjoint() > 1) {
                log.info("Entering Survivability PCE");
                eroMap = survivabilityPCE.computeSurvivableERO(pipe, schedSpec, rsvBandwidths, rsvVlans);
                log.info("Exiting Survivability PCE");
            } else if (!pipe.getAzERO().isEmpty() && !pipe.getZaERO().isEmpty()) {
                log.info("Attempting to reserve specified Explicit Route Object");
                eroMap = eroPCE.computeSpecifiedERO(pipe, schedSpec, rsvBandwidths, rsvVlans);
            } else if (pipe.getEroPalindromic().equals(PalindromicType.PALINDROME)) {
                log.info("Entering Palindromical PCE");
                eroMap = palindromicalPCE.computePalindromicERO(pipe, schedSpec, rsvBandwidths, rsvVlans);       // A->Z ERO is palindrome of Z->A ERO
                log.info("Exiting Palindromical PCE");
            } else {
                log.info("Entering NON-Palindromical PCE");
                eroMap = nonPalindromicPCE.computeNonPalindromicERO(pipe, schedSpec, rsvBandwidths, rsvVlans);       // A->Z ERO is NOT palindrome of Z->A ERO
                log.info("Exiting NON-Palindromical PCE");
            }
        } catch (PCEException e) {
            log.error("PCE Unsuccessful", e);
        }

        return eroMap;
    }


    /**
     * Verify that there both the AZ and ZA paths were found given a map of shortest paths.
     *
     * @param eroMap The map containing the AZ and ZA shortest paths (keys: "az" and "za")
     * @return True if both paths found, False otherwise.
     */
    private boolean verifyEros(Map<String, List<TopoEdge>> eroMap) {
        if (eroMap != null) {
            if (eroMap.size() == 2) {
                return eroMap.values().stream().allMatch(l -> l.size() > 0);
            }
        }

        return false;
    }

    /**
     * Verify that primary and secondary AZ and ZA paths were found given a map of shortest paths.
     *
     * @param eroMap The map containing the AZ and ZA shortest paths (keys: "azPrimary", "zaPrimary", "azSecondary", "zaSecondary")
     * @return True if both paths found, False otherwise.
     */
    private boolean verifySurvEros(Map<String, List<TopoEdge>> eroMap) {
        if (eroMap != null) {
            if (eroMap.size() == 4) {
                return eroMap.values().stream().allMatch(l -> l.size() > 0);
            }
        }

        return false;
    }

    /**
     * Confirm that the requested blueprint is valid.
     *
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

        for (RequestedVlanJunctionE junction : allJunctions) {
            // throws exception if device not found in topology
            try {
                topoService.device(junction.getDeviceUrn());
            } catch (NoSuchElementException ex) {
                throw new PCEException("device not found in topology");
            }
        }

        Set<String> junctionsWithNoFixtures = allJunctions.stream().
                filter(t -> t.getFixtures().isEmpty()).
                map(t -> t.getDeviceUrn()).collect(Collectors.toSet());

        if (!junctionsWithNoFixtures.isEmpty()) {
            throw new PCEException("Junctions with no fixtures found: " + String.join(" ", junctionsWithNoFixtures));
        }
        log.info("all junctions & pipes are ok");

    }
}
