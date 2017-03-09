package net.es.oscars.simpleresv.svc;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.*;
import net.es.oscars.dto.spec.*;
import net.es.oscars.st.oper.OperState;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class ConnectionGenerationService {

    @Autowired
    private DateService dateService;


    public Connection generateConnection(BasicCircuitSpecification bcs){
        Specification spec = generateSpecification(bcs);
        return buildConnection(spec);
    }


    public Connection generateConnection(CircuitSpecification cs){
        Specification spec = generateSpecification(cs);
        return buildConnection(spec);
    }

    public Connection buildConnection(Specification spec){
        return Connection.builder()
                .connectionId(spec.getContainerConnectionId())
                .reserved(ReservedBlueprint.builder().vlanFlow(generateReservedVlanFlow(spec.getContainerConnectionId())).containerConnectionId(spec.getContainerConnectionId()).build())
                .schedule(generateSchedule())
                .specification(spec)
                .states(generateStates())
                .reservedSchedule(new ArrayList<>())
                .build();
    }

    public Specification generateSpecification(BasicCircuitSpecification bcs){
        Set<CircuitFlow> flows = new HashSet<>();
        CircuitFlow flow = generateCircuitFlow(bcs.getSourceDevice(), Collections.singleton(bcs.getSourcePort()), "any",
                bcs.getDestDevice(), Collections.singleton(bcs.getDestPort()), "any", bcs.getAzMbps(),
                bcs.getZaMbps(), new ArrayList<>(), new ArrayList<>(),
                new HashSet<>(), "palindrome", "none", 1, Integer.MAX_VALUE);
        flows.add(flow);

        RequestedBlueprint reqBlueprint = generateRequestedBlueprint(flows, 1, 1, bcs.getConnectionId());
        return  Specification.builder()
                .scheduleSpec(generateScheduleSpecification(bcs.getStart(), bcs.getEnd()))
                .containerConnectionId(bcs.getConnectionId())
                .description(bcs.getDescription())
                .requested(reqBlueprint)
                .username("What-If")
                .version(0)
                .build();
    }

    public Specification generateSpecification(CircuitSpecification cs){

        RequestedBlueprint reqBlueprint = generateRequestedBlueprint(cs.getFlows(), cs.getMaxNumFlows(),
                cs.getMinNumFlows(), cs.getConnectionId());

        return  Specification.builder()
                .scheduleSpec(generateScheduleSpecification(cs.getStart(), cs.getEnd()))
                .containerConnectionId(cs.getConnectionId())
                .description(cs.getDescription())
                .requested(reqBlueprint)
                .username(cs.getUsername())
                .version(0)
                .build();
    }

    public RequestedBlueprint generateRequestedBlueprint(Set<CircuitFlow> flows, Integer maxNumFlows, Integer minNumFlows,
                                                         String connectionId){

        Set<RequestedVlanJunction> junctions = new HashSet<>();
        Set<RequestedVlanPipe> pipes = new HashSet<>();

        for(CircuitFlow flow : flows){
            String sourceDevice = flow.getSourceDevice();
            Set<String> sourcePorts = flow.getSourcePorts() != null ? flow.getSourcePorts() : new HashSet<>();
            String sourceVlan = flow.getSourceVlan() != null ? flow.getSourceVlan() : "any";
            String destDevice = flow.getDestDevice();
            Set<String> destPorts = flow.getDestPorts() != null ? flow.getDestPorts() : new HashSet<>();
            String destVlan = flow.getDestVlan() != null ? flow.getDestVlan() : "any";
            Integer azMbps = flow.getAzMbps() != null ? flow.getAzMbps() : 0;
            Integer zaMbps = flow.getZaMbps() != null ? flow.getZaMbps() : 0;
            List<String> azRoute = flow.getAzRoute() != null ? flow.getAzRoute() : new ArrayList<>();
            List<String> zaRoute = flow.getZaRoute() != null ? flow.getZaRoute() : new ArrayList<>();
            Set<String> blacklist = flow.getBlacklist() != null ? flow.getBlacklist() : new HashSet<>();
            String palindromic = flow.getPalindromic() != null ? flow.getPalindromic() : "PALINDROME";
            String survivability = flow.getSurvivability() != null ? flow.getSurvivability() : "NONE";
            Integer numPaths = flow.getNumPaths() != null ? flow.getNumPaths() : 1;
            Integer priority = flow.getPriority() != null ? flow.getPriority() : Integer.MAX_VALUE;

            if(sourceDevice.equals(destDevice)){
                junctions.add(generateRequestedJunction(sourceDevice, sourcePorts, sourceVlan, azMbps, zaMbps));
            }
            else{
                pipes.add(generateRequestedPipe(sourceDevice, sourcePorts, sourceVlan, destDevice, destPorts, destVlan,
                        azMbps, zaMbps, azRoute, zaRoute, blacklist, palindromic, survivability, numPaths,
                        priority));
            }
        }

        RequestedVlanFlow reqvf = RequestedVlanFlow.builder()
                .junctions(junctions)
                .pipes(pipes)
                .maxPipes(maxNumFlows)
                .minPipes(minNumFlows)
                .containerConnectionId(connectionId)
                .build();

        Layer3Flow layer3Flow = Layer3Flow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        return RequestedBlueprint.builder()
                .vlanFlow(reqvf)
                .layer3Flow(layer3Flow)
                .containerConnectionId(connectionId)
                .build();
    }

    private RequestedVlanPipe generateRequestedPipe(String sourceDevice, Set<String> sourcePorts, String sourceVlan,
                                                    String destDevice, Set<String> destPorts, String destVlan,
                                                    Integer azMbps, Integer zaMbps, List<String> azRoute,
                                                    List<String> zaRoute, Set<String> blacklist, String palindromic,
                                                    String survivability, Integer numPaths, Integer priority) {

        return RequestedVlanPipe.builder()
                .aJunction(generateRequestedJunction(sourceDevice, sourcePorts, sourceVlan, azMbps, zaMbps))
                .zJunction(generateRequestedJunction(destDevice, destPorts, destVlan, azMbps, zaMbps))
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .azERO(azRoute)
                .zaERO(zaRoute)
                .urnBlacklist(blacklist)
                .eroPalindromic(parsePalindrome(palindromic))
                .eroSurvivability(parseSurvivability(survivability))
                .numPaths(numPaths)
                .priority(priority)
                .pipeType(EthPipeType.REQUESTED)
                .build();
    }

    public RequestedVlanJunction generateRequestedJunction(String name, Set<String> ports, String vlan, Integer inMbps,
                                                           Integer egMbps){

        List<String> portNames = new ArrayList<>();
        portNames.addAll(ports);
        List<String> vlans = new ArrayList<>();
        vlans.add(vlan);
        List<Integer> inBws = new ArrayList<>();
        inBws.add(inMbps);
        List<Integer> egBws = new ArrayList<>();
        egBws.add(egMbps);

        return RequestedVlanJunction.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn(name)
                .fixtures(generateRequestedFixtures(portNames, vlans, inBws, egBws))
                .build();
    }

    public Set<RequestedVlanFixture> generateRequestedFixtures(List<String> urns, List<String> vlans,
                                                               List<Integer> inBws, List<Integer> egBws){

        Set<RequestedVlanFixture> fixtures = new HashSet<>();

        for(Integer index = 0; index < urns.size(); index++){
            fixtures.add(RequestedVlanFixture.builder()
                    .portUrn(urns.get(index))
                    .vlanExpression(vlans.get(index))
                    .fixtureType(EthFixtureType.REQUESTED)
                    .inMbps(inBws.get(index))
                    .egMbps(egBws.get(index))
                    .build());
        }
        return fixtures;
    }

    public ScheduleSpecification generateScheduleSpecification(String start, String end){

        Date startDate = dateService.parseDate(start);
        Date endDate = dateService.parseDate(end);

        return ScheduleSpecification.builder()
                .startDates(Collections.singletonList(startDate))
                .endDates(Collections.singletonList(endDate))
                .minimumDuration(ChronoUnit.MINUTES.between(startDate.toInstant(), endDate.toInstant()))
                .build();
    }


    public States generateStates(){
        return States.builder()
                .oper(OperState.ADMIN_DOWN_OPER_DOWN)
                .prov(ProvState.INITIAL)
                .resv(ResvState.SUBMITTED)
                .build();
    }

    public ReservedVlanFlow generateReservedVlanFlow(String connectionId){
        return ReservedVlanFlow.builder()
                .ethPipes(new HashSet<>())
                .junctions(new HashSet<>())
                .mplsPipes(new HashSet<>())
                .containerConnectionId(connectionId)
                .build();
    }

    public Schedule generateSchedule(){
        return Schedule.builder()
                .setup(new Date())
                .teardown(new Date())
                .submitted(new Date())
                .build();
    }

    public CircuitFlow generateCircuitFlow(String sourceDevice, Set<String> sourcePorts, String sourceVlan,
                                           String destDevice, Set<String> destPorts, String destVlan, Integer azMbps,
                                           Integer zaMbps, List<String> azRoute, List<String> zaRoute,
                                           Set<String> blacklist, String palindromic, String survivability,
                                           Integer numPaths, Integer priority){

        return CircuitFlow.builder()
                .sourceDevice(sourceDevice)
                .sourcePorts(sourcePorts)
                .sourceVlan(sourceVlan)
                .destDevice(destDevice)
                .destPorts(destPorts)
                .destVlan(destVlan)
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .azRoute(azRoute)
                .zaRoute(zaRoute)
                .blacklist(blacklist)
                .palindromic(palindromic)
                .survivability(survivability)
                .numPaths(numPaths)
                .priority(priority)
                .build();
    }


    public PalindromicType parsePalindrome(String pString){
        switch(pString.toUpperCase()){
            case "NON_PALINDROME": return PalindromicType.NON_PALINDROME;
            default: return PalindromicType.PALINDROME;
        }
    }

    public SurvivabilityType parseSurvivability(String sString){
        switch(sString.toUpperCase()){
            case "TOTAL": return SurvivabilityType.SURVIVABILITY_TOTAL;
            case "PARTIAL": return SurvivabilityType.SURVIVABILITY_PARTIAL;
            default: return SurvivabilityType.SURVIVABILITY_NONE;
        }
    }
}
