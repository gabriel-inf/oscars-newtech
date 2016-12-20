package net.es.oscars.helpers;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
public class RequestedEntityBuilder {

    @Autowired
    UrnRepository urnRepo;
    public RequestedBlueprintE buildRequest(String deviceName, List<String> fixtureNames,
                                            Integer azMbps, Integer zaMbps, String vlanExp, String connectionId){
        Set<RequestedVlanJunctionE> junctions = new HashSet<>();
        RequestedVlanJunctionE junction = buildRequestedJunction(deviceName, fixtureNames, azMbps, zaMbps, vlanExp, true);
        junctions.add(junction);

        return buildRequestedBlueprint(buildRequestedFlow(junctions, new HashSet<>(), 0, 0, connectionId), Layer3FlowE.builder().build(), connectionId);
    }

    public RequestedBlueprintE buildRequest(String aPort, String aDevice, String zPort, String zDevice,
                                            Integer azMbps, Integer zaMbps, PalindromicType palindromic,
                                            SurvivabilityType survivable, String vlanExp, Integer numDisjoint,
                                            Integer minPipes, Integer maxPipes, String connectionId){

        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedVlanPipeE pipe = buildRequestedPipe(aPort, aDevice, zPort, zDevice, azMbps, zaMbps, palindromic,
                survivable, vlanExp, numDisjoint);
        pipes.add(pipe);

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes, minPipes, maxPipes, connectionId), Layer3FlowE.builder().build(), connectionId);

    }

    public RequestedBlueprintE buildRequest(String aPort, String aDevice, String zPort, String zDevice,
                                            Integer azMbps, Integer zaMbps, PalindromicType palindromic,
                                            SurvivabilityType survivable, String vlanExp, Set<String> blacklist,
                                            Integer numDisjoint, Integer minPipes, Integer maxPipes, String connectionId){

        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedVlanPipeE pipe = buildRequestedPipe(aPort, aDevice, zPort, zDevice, azMbps, zaMbps, palindromic,
                survivable, vlanExp, blacklist, numDisjoint);
        pipes.add(pipe);

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes, minPipes, maxPipes, connectionId), Layer3FlowE.builder().build(), connectionId);

    }

    public RequestedBlueprintE buildRequest(String aPort, String aDevice, String zPort, String zDevice,
                                            Integer azMbps, Integer zaMbps, PalindromicType palindromic,
                                            SurvivabilityType survivable, String aVlanExp, String zVlanExp,
                                            Integer numDisjoint, Integer minPipes, Integer maxPipes, String connectionId){

        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedVlanPipeE pipe = buildRequestedPipe(aPort, aDevice, zPort, zDevice, azMbps, zaMbps, palindromic,
                survivable, aVlanExp, zVlanExp, numDisjoint);
        pipes.add(pipe);

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes, minPipes, maxPipes, connectionId), Layer3FlowE.builder().build(), connectionId);

    }

    public RequestedBlueprintE buildRequest(List<String> aPorts, String aDevice, List<String> zPorts, String zDevice,
                                            Integer azMbps, Integer zaMbps, PalindromicType palindromic,
                                            SurvivabilityType survivable, String vlanExp, Integer numDisjoint,
                                            Integer minPipes, Integer maxPipes, String connectionId){

        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedVlanPipeE pipe = buildRequestedPipe(aPorts, aDevice, zPorts, zDevice, azMbps, zaMbps, palindromic,
                survivable, vlanExp, numDisjoint);
        pipes.add(pipe);

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes, minPipes, maxPipes, connectionId), Layer3FlowE.builder().build(), connectionId);

    }

    public RequestedBlueprintE buildRequest(List<String> aPorts, List<String> aDevices, List<String> zPorts,
                                            List<String> zDevices, List<Integer> azMbpsList, List<Integer> zaMbpsList,
                                            List<PalindromicType> palindromicList,
                                            List<SurvivabilityType> survivableList, List<String> vlanExps,
                                            List<Integer> numDisjoints, Integer minPipes, Integer maxPipes, String connectionId){
        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        for(int i = 0; i < aPorts.size(); i++){
            RequestedVlanPipeE pipe = buildRequestedPipe(
                    aPorts.get(i),
                    aDevices.get(i),
                    zPorts.get(i),
                    zDevices.get(i),
                    azMbpsList.get(i),
                    zaMbpsList.get(i),
                    palindromicList.get(i),
                    survivableList.get(i),
                    vlanExps.get(i),
                    numDisjoints.get(i));
            pipes.add(pipe);
        }

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes, minPipes, maxPipes, connectionId), Layer3FlowE.builder().build(), connectionId);
    }

    // Added for multi-pipe request
    public RequestedBlueprintE buildRequest(Set<RequestedVlanPipeE> requestedPipes, Integer minPipes, Integer maxPipes, String connectionId)
    {
        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), requestedPipes, minPipes, maxPipes, connectionId), Layer3FlowE.builder().build(), connectionId);
    }

    public RequestedBlueprintE buildRequest(List<String> deviceNames, List<List<String>> portNames,
                                            List<Integer> azMbpsList, List<Integer> zaMbpsList, List<String> vlanExps, String connectionId){
        Set<RequestedVlanJunctionE> junctions = new HashSet<>();
        for(int i = 0; i < deviceNames.size(); i++)
        {
            boolean aJunction;

            if(i == 0)
                aJunction = true;
            else
                aJunction = false;

            RequestedVlanJunctionE junction = buildRequestedJunction(
                    deviceNames.get(i),
                    portNames.get(i),
                    azMbpsList.get(i),
                    zaMbpsList.get(i),
                    vlanExps.get(i),
                    aJunction);
            junctions.add(junction);
        }

        return buildRequestedBlueprint(buildRequestedFlow(junctions, new HashSet<>(), 0, 0, connectionId), Layer3FlowE.builder().build(), connectionId);
    }

    public RequestedBlueprintE buildRequest(List<String> azERO, List<String> zaERO, Integer azBandwidth, Integer zaBandwidth, String connectionId) {
        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedVlanPipeE pipe = buildRequestedPipe(azERO, zaERO, azBandwidth, zaBandwidth);
        pipes.add(pipe);

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes, 1, 1, connectionId), Layer3FlowE.builder().build(), connectionId);
    }


    private RequestedBlueprintE buildRequestedBlueprint(RequestedVlanFlowE vlanFlow, Layer3FlowE l3Flow, String connectionId){
        return RequestedBlueprintE.builder()
                .vlanFlow(vlanFlow)
                .layer3Flow(l3Flow)
                .containerConnectionId(connectionId)
                .build();
    }

    private RequestedVlanFlowE buildRequestedFlow(Set<RequestedVlanJunctionE> junctions, Set<RequestedVlanPipeE> pipes,
                                                  Integer minPipes, Integer maxPipes, String connectionId){
        return RequestedVlanFlowE.builder()
                .junctions(junctions)
                .pipes(pipes)
                .minPipes(minPipes)
                .maxPipes(minPipes)
                .containerConnectionId(connectionId)
                .build();
    }

    public ScheduleSpecificationE buildSchedule(Date start, Date end){

        Long duration = ChronoUnit.MINUTES.between(start.toInstant(), end.toInstant());
        List<Date> startDates = new ArrayList<>();
        startDates.add(start);
        List<Date> endDates = new ArrayList<>();
        endDates.add(end);

        return ScheduleSpecificationE.builder()
                .startDates(startDates)
                .endDates(endDates)
                .minimumDuration(duration)
                .build();
    }


    private RequestedVlanPipeE buildRequestedPipe(String aPort, String aDevice, String zPort, String zDevice,
                                                 Integer azMbps, Integer zaMbps, PalindromicType palindromic,
                                                 SurvivabilityType survivable, String vlanExp, Integer numDisjoint){

        List<String> aFixNames = new ArrayList<>();
        aFixNames.add(aPort);

        List<String> zFixNames = new ArrayList<>();
        zFixNames.add(zPort);

        return RequestedVlanPipeE.builder()
                .aJunction(buildRequestedJunction(aDevice, aFixNames, azMbps, zaMbps, vlanExp, true))
                .zJunction(buildRequestedJunction(zDevice, zFixNames, azMbps, zaMbps, vlanExp, false))
                .pipeType(EthPipeType.REQUESTED)
                .azERO(new ArrayList<>())
                .zaERO(new ArrayList<>())
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .eroPalindromic(palindromic)
                .eroSurvivability(survivable)
                .numDisjoint(numDisjoint)
                .build();
    }

    public RequestedVlanPipeE buildRequestedPipe(String aPort, String aDevice, String zPort, String zDevice,
                                                 Integer azMbps, Integer zaMbps, PalindromicType palindromic,
                                                 SurvivabilityType survivable, String aVlanExp, String zVlanExp,
                                                 Integer numDisjoint){

        List<String> aFixNames = new ArrayList<>();
        aFixNames.add(aPort);

        List<String> zFixNames = new ArrayList<>();
        zFixNames.add(zPort);

        return RequestedVlanPipeE.builder()
                .aJunction(buildRequestedJunction(aDevice, aFixNames, azMbps, zaMbps, aVlanExp, true))
                .zJunction(buildRequestedJunction(zDevice, zFixNames, azMbps, zaMbps, zVlanExp, false))
                .pipeType(EthPipeType.REQUESTED)
                .azERO(new ArrayList<>())
                .zaERO(new ArrayList<>())
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .eroPalindromic(palindromic)
                .eroSurvivability(survivable)
                .numDisjoint(numDisjoint)
                .build();
    }

    public RequestedVlanPipeE buildRequestedPipe(String aPort, String aDevice, String zPort, String zDevice,
                                                 Integer azMbps, Integer zaMbps, PalindromicType palindromic,
                                                 SurvivabilityType survivable, String vlanExp, Set<String> blacklist,
                                                 Integer numDisjoint){

        List<String> aFixNames = new ArrayList<>();
        aFixNames.add(aPort);

        List<String> zFixNames = new ArrayList<>();
        zFixNames.add(zPort);

        return RequestedVlanPipeE.builder()
                .aJunction(buildRequestedJunction(aDevice, aFixNames, azMbps, zaMbps, vlanExp, true))
                .zJunction(buildRequestedJunction(zDevice, zFixNames, azMbps, zaMbps, vlanExp, false))
                .pipeType(EthPipeType.REQUESTED)
                .azERO(new ArrayList<>())
                .zaERO(new ArrayList<>())
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .eroPalindromic(palindromic)
                .eroSurvivability(survivable)
                .urnBlacklist(blacklist)
                .numDisjoint(numDisjoint)
                .build();
    }

    public RequestedVlanPipeE buildRequestedPipe(List<String> aPorts, String aDevice, List<String> zPorts, String zDevice,
                                                 Integer azMbps, Integer zaMbps, PalindromicType palindromic,
                                                 SurvivabilityType survivable, String vlanExp, Integer numDisjoint){


        return RequestedVlanPipeE.builder()
                .aJunction(buildRequestedJunction(aDevice, aPorts, azMbps, zaMbps, vlanExp, true))
                .zJunction(buildRequestedJunction(zDevice, zPorts, azMbps, zaMbps, vlanExp, false))
                .pipeType(EthPipeType.REQUESTED)
                .azERO(new ArrayList<>())
                .zaERO(new ArrayList<>())
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .eroPalindromic(palindromic)
                .eroSurvivability(survivable)
                .numDisjoint(numDisjoint)
                .build();
    }

    private RequestedVlanPipeE buildRequestedPipe(List<String> azERO, List<String> zaERO, Integer azMbps, Integer zaMbps) {
        List<String> aPorts = new ArrayList<>();
        List<String> zPorts = new ArrayList<>();
        String aDevice = "";
        String zDevice = "";
        Integer aDeviceIndex = -1;
        Integer zDeviceIndex = -1;
        // Get the source/dest ports, and the first/last device
        for(Integer index = 0; index < azERO.size(); index++){
            String pathElement = azERO.get(index);
            Optional<UrnE> elementUrnOpt = urnRepo.findByUrn(pathElement);
            if(elementUrnOpt.isPresent()){
                UrnE elementUrn = elementUrnOpt.get();
                if(aDeviceIndex == -1){
                    if(elementUrn.getUrnType().equals(UrnType.IFCE)){
                        aPorts.add(pathElement);
                    }
                    else{
                        aDevice = pathElement;
                        aDeviceIndex = index;
                    }
                } else{
                    break;
                }
            }
        }

        for(Integer index = azERO.size()-1; index > 0; index--){
            String pathElement = azERO.get(index);
            Optional<UrnE> elementUrnOpt = urnRepo.findByUrn(pathElement);
            if(elementUrnOpt.isPresent()){
                UrnE elementUrn = elementUrnOpt.get();
                if(zDeviceIndex == -1){
                    if(elementUrn.getUrnType().equals(UrnType.IFCE)){
                        zPorts.add(pathElement);
                    }
                    else{
                        zDevice = pathElement;
                        zDeviceIndex = index;
                    }
                } else{
                    break;
                }
            }
        }

        return RequestedVlanPipeE.builder()
                .aJunction(buildRequestedJunction(aDevice, aPorts, azMbps, zaMbps, "any", true))
                .zJunction(buildRequestedJunction(zDevice, zPorts, azMbps, zaMbps, "any", false))
                .pipeType(EthPipeType.REQUESTED)
                .azERO(azERO.subList(aDeviceIndex, zDeviceIndex+1))
                .zaERO(zaERO.subList(aDeviceIndex, zDeviceIndex+1))
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .eroPalindromic(PalindromicType.NON_PALINDROME)
                .eroSurvivability(SurvivabilityType.SURVIVABILITY_NONE)
                .numDisjoint(1)
                .build();
    }

    public RequestedVlanJunctionE buildRequestedJunction(String deviceName, List<String> fixtureNames,
                                                         Integer azMbps, Integer zaMbps, String vlanExp, boolean startJunc){

        Set<RequestedVlanFixtureE> fixtures = new HashSet<>();

        // assert(fixtureNames.size() >= 1);

        for(String fixName : fixtureNames){
            RequestedVlanFixtureE fix;
            if(startJunc)
                fix = buildRequestedFixture(fixName, azMbps, zaMbps, vlanExp);
            else
                fix = buildRequestedFixture(fixName, zaMbps, azMbps, vlanExp);
            fixtures.add(fix);
        }

        return RequestedVlanJunctionE.builder()
                .deviceUrn(deviceName)
                .fixtures(fixtures)
                .junctionType(EthJunctionType.REQUESTED)
                .build();
    }

    public RequestedVlanFixtureE buildRequestedFixture(String fixName, Integer azMbps, Integer zaMbps,
                                                       String vlanExp){
        return RequestedVlanFixtureE.builder()
                .portUrn(fixName)
                .fixtureType(EthFixtureType.REQUESTED)
                .inMbps(azMbps)
                .egMbps(zaMbps)
                .vlanExpression(vlanExp)
                .build();
    }


    public ConnectionE buildConnection(RequestedBlueprintE blueprint, ScheduleSpecificationE schedule, String connectionID, String description)
    {
        SpecificationE requestSpec = SpecificationE.builder()
                .containerConnectionId(connectionID)
                .description(description)
                .version(1)
                .username("TestUser")
                .requested(blueprint)
                .scheduleSpec(schedule)
                .build();

        return ConnectionE.builder()
                .connectionId(connectionID)
                .specification(requestSpec)
                .states(new StatesE())
                .reservedSchedule(new ArrayList<>())
                .build();

    }

}
