package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.enums.Layer;
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
    private EthPCE ethPCE;

    @Autowired
    private Layer3PCE layer3PCE;

    @Autowired
    private TranslationPCE transPCE;

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

            List<RequestedVlanPipeE> pipes = new ArrayList<>();
            pipes.addAll(req_f.getPipes());

            // Attempt to find the az / za paths for the flow
            Map<RequestedVlanPipeE, Map<String, List<TopoEdge>>> eroMapsForFlow = handleRequestedPipes(pipes, schedSpec);

            // If the EROs are not valid for every pipe, try reversing the order pipes are attempted
            if(eroMapsForFlow.size() != pipes.size()){
                Collections.reverse(pipes);
                eroMapsForFlow = handleRequestedPipes(pipes, schedSpec);
            }
            // If the EROs are still not valid for every pipe, return the blank Reserved Vlan Flow
            if(eroMapsForFlow.size() != pipes.size()){
                reserved.getVlanFlows().add(res_f);
            }
            // All pipes were successfully found, translate the EROs into a ReservedVlanFlow
            else{
                res_f = transPCE.makeReservedFlow(req_f, schedSpec, eroMapsForFlow);
                reserved.getVlanFlows().add(res_f);
            }
        }
        return reserved;

    }

    private Map<RequestedVlanPipeE, Map<String, List<TopoEdge>>> handleRequestedPipes(List<RequestedVlanPipeE> pipes,
                                                                   ScheduleSpecificationE sched){
        Map<RequestedVlanPipeE,Map<String, List<TopoEdge>>> eroMapsPerFlow= new HashMap<>();
        for(RequestedVlanPipeE pipe : pipes){
            // Find the shortest path
            Map<String, List<TopoEdge>> eroMap = new HashMap<>();
            if(verifyEros(eroMap)){
                eroMapsPerFlow.put(pipe, eroMap);
            }
        }
        return eroMapsPerFlow;
    }

    private boolean verifyEros(Map<String, List<TopoEdge>> eroMap){
        return eroMap.values().stream().allMatch(l -> l.size() > 0);
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
