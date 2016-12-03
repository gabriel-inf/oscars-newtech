package net.es.oscars.resv.svc;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.*;
import net.es.oscars.dto.spec.*;
import net.es.oscars.helpers.DateService;
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
                .connectionId(spec.getConnectionId())
                .reserved(ReservedBlueprint.builder().vlanFlow(generateReservedVlanFlow()).build())
                .schedule(generateSchedule())
                .specification(spec)
                .states(generateStates())
                .reservedSchedule(new ArrayList<>())
                .build();
    }

    public Specification generateSpecification(BasicCircuitSpecification bcs){
        Set<CircuitFlow> flows = new HashSet<>();
        CircuitFlow flow = generateCircuitFlow(bcs.getSourceDevice(), bcs.getSourcePort(), "any",
                bcs.getDestDevice(), bcs.getDestPort(), "any", bcs.getAzMbps(),
                bcs.getZaMbps(), new ArrayList<>(), new ArrayList<>(),
                new HashSet<>(), "palindrome", "none", 1);
        flows.add(flow);

        RequestedBlueprint reqBlueprint = generateRequestedBlueprint(flows, 1, 1);
        return  Specification.builder()
                .scheduleSpec(generateScheduleSpecification(bcs.getStart(), bcs.getEnd()))
                .connectionId(bcs.getConnectionId())
                .description("What-if Connection")
                .requested(reqBlueprint)
                .username("What-If")
                .version(0)
                .build();
    }

    public Specification generateSpecification(CircuitSpecification cs){

        RequestedBlueprint reqBlueprint = generateRequestedBlueprint(cs.getFlows(), cs.getMaxNumFlows(), cs.getMinNumFlows());

        return  Specification.builder()
                .scheduleSpec(generateScheduleSpecification(cs.getStart(), cs.getEnd()))
                .connectionId(cs.getConnectionId())
                .description(cs.getDescription())
                .requested(reqBlueprint)
                .username(cs.getUsername())
                .version(0)
                .build();
    }

    public RequestedBlueprint generateRequestedBlueprint(Set<CircuitFlow> flows, Integer maxNumFlows, Integer minNumFlows){

        Set<RequestedVlanJunction> junctions = new HashSet<>();
        Set<RequestedVlanPipe> pipes = new HashSet<>();

        for(CircuitFlow flow : flows){
            Integer azMbps = flow.getAzMbps();
            Integer zaMbps = flow.getZaMbps();
            List<String> azRoute = flow.getAzRoute();
            List<String> zaRoute = flow.getZaRoute();
            Set<String> blacklist = flow.getBlacklist();
            String destDevice = flow.getDestDevice();
            String destPort = flow.getDestPort();
            String destVlan = flow.getDestVlan();
            String sourceDevice = flow.getSourceDevice();
            String sourcePort = flow.getSourcePort();
            String sourceVlan = flow.getSourceVlan();
            String palindromic = flow.getPalindromic();
            String survivability = flow.getSurvivability();
            Integer numDisjointPaths = flow.getNumDisjointPaths();

            if(sourceDevice.equals(destDevice)){
                junctions.add(generateRequestedJunction(sourceDevice, sourcePort, sourceVlan, azMbps, zaMbps));
            }
            else{
                pipes.add(generateRequestedPipe(sourceDevice, sourcePort, sourceVlan, destDevice, destPort, destVlan,
                        azMbps, zaMbps, azRoute, zaRoute, blacklist, palindromic, survivability, numDisjointPaths));
            }
        }

        RequestedVlanFlow reqvf = RequestedVlanFlow.builder()
                .junctions(junctions)
                .pipes(pipes)
                .maxPipes(maxNumFlows)
                .minPipes(minNumFlows)
                .build();

        Layer3Flow layer3Flow = Layer3Flow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        return RequestedBlueprint.builder()
                .vlanFlow(reqvf)
                .layer3Flow(layer3Flow)
                .build();
    }

    private RequestedVlanPipe generateRequestedPipe(String sourceDevice, String sourcePort, String sourceVlan,
                                                    String destDevice, String destPort, String destVlan,
                                                    Integer azMbps, Integer zaMbps, List<String> azRoute,
                                                    List<String> zaRoute, Set<String> blacklist, String palindromic,
                                                    String survivability, Integer numDisjointPaths) {

        return RequestedVlanPipe.builder()
                .aJunction(generateRequestedJunction(sourceDevice, sourcePort, sourceVlan, azMbps, zaMbps))
                .zJunction(generateRequestedJunction(destDevice, destPort, destVlan, azMbps, zaMbps))
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .azERO(azRoute)
                .zaERO(zaRoute)
                .urnBlacklist(blacklist)
                .eroPalindromic(parsePalindrome(palindromic))
                .eroSurvivability(parseSurvivability(survivability))
                .numDisjoint(numDisjointPaths)
                .pipeType(EthPipeType.REQUESTED)
                .build();
    }

    public RequestedVlanJunction generateRequestedJunction(String name, String port, String vlan, Integer inMbps,
                                                           Integer egMbps){

        List<String> portNames = new ArrayList<>();
        portNames.add(port);
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

    public ReservedVlanFlow generateReservedVlanFlow(){
        return ReservedVlanFlow.builder()
                .ethPipes(new HashSet<>())
                .junctions(new HashSet<>())
                .mplsPipes(new HashSet<>())
                .build();
    }

    public Schedule generateSchedule(){
        return Schedule.builder()
                .setup(new Date())
                .teardown(new Date())
                .submitted(new Date())
                .build();
    }

    public CircuitFlow generateCircuitFlow(String sourceDevice, String sourcePort, String sourceVlan,
                                           String destDevice, String destPort, String destVlan, Integer azMbps,
                                           Integer zaMbps, List<String> azRoute, List<String> zaRoute,
                                           Set<String> blacklist, String palindromic, String survivability,
                                           Integer numDisjointPaths){

        return CircuitFlow.builder()
                .sourceDevice(sourceDevice)
                .sourcePort(sourcePort)
                .sourceVlan(sourceVlan)
                .destDevice(destDevice)
                .destPort(destPort)
                .destVlan(destVlan)
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .azRoute(azRoute)
                .zaRoute(zaRoute)
                .blacklist(blacklist)
                .palindromic(palindromic)
                .survivability(survivability)
                .numDisjointPaths(numDisjointPaths)
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
