package net.es.oscars.helpers;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.PalindromicType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class RequestedEntityBuilder {

    @Autowired
    UrnRepository urnRepo;
    public RequestedBlueprintE buildRequest(String deviceName, List<String> fixtureNames,
                                            Integer azMbps, Integer zaMbps, String vlanExp){
        log.info("Building RequestedBlueprintE");
        log.info("Device: " + deviceName);
        fixtureNames.stream()
                .forEach(f -> log.info("Fixture: " + f));
        log.info("A-Z Mbps: " + azMbps);
        log.info("Z-A Mbps: " + zaMbps);
        log.info("VLAN Expression: " + vlanExp);

        Set<RequestedVlanJunctionE> junctions = new HashSet<>();
        RequestedVlanJunctionE junction = buildRequestedJunction(deviceName, fixtureNames, azMbps, zaMbps, vlanExp, true);
        junctions.add(junction);



        return buildRequestedBlueprint(buildRequestedFlow(junctions, new HashSet<>()), Layer3FlowE.builder().build());
    }

    public RequestedBlueprintE buildRequest(String aPort, String aDevice, String zPort, String zDevice,
                                            Integer azMbps, Integer zaMbps, PalindromicType palindromic, String vlanExp){
        log.info("Building RequestedBlueprintE");


        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedVlanPipeE pipe = buildRequestedPipe(aPort, aDevice, zPort, zDevice, azMbps, zaMbps, palindromic, vlanExp);
        pipes.add(pipe);

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes), Layer3FlowE.builder().build());

    }

    public RequestedBlueprintE buildRequest(String aPort, String aDevice, String zPort, String zDevice,
                                            Integer azMbps, Integer zaMbps, PalindromicType palindromic, String aVlanExp, String zVlanExp){
        log.info("Building RequestedBlueprintE");


        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedVlanPipeE pipe = buildRequestedPipe(aPort, aDevice, zPort, zDevice, azMbps, zaMbps, palindromic, aVlanExp, zVlanExp);
        pipes.add(pipe);

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes), Layer3FlowE.builder().build());

    }

    public RequestedBlueprintE buildRequest(List<String> aPorts, String aDevice, List<String> zPorts, String zDevice,
                                            Integer azMbps, Integer zaMbps, PalindromicType palindromic, String vlanExp){
        log.info("Building RequestedBlueprintE");


        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedVlanPipeE pipe = buildRequestedPipe(aPorts, aDevice, zPorts, zDevice, azMbps, zaMbps, palindromic, vlanExp);
        pipes.add(pipe);

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes), Layer3FlowE.builder().build());

    }

    public RequestedBlueprintE buildRequest(List<String> aPorts, List<String> aDevices, List<String> zPorts,
                                            List<String> zDevices, List<Integer> azMbpsList, List<Integer> zaMbpsList,
                                            List<PalindromicType> palindromicList, List<String> vlanExps){
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
                    vlanExps.get(i));
            pipes.add(pipe);
        }

        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), pipes), Layer3FlowE.builder().build());
    }

    // Added for multi-pipe request
    public RequestedBlueprintE buildRequest(Set<RequestedVlanPipeE> requestedPipes)
    {
        return buildRequestedBlueprint(buildRequestedFlow(new HashSet<>(), requestedPipes), Layer3FlowE.builder().build());
    }

    public RequestedBlueprintE buildRequest(List<String> deviceNames, List<List<String>> portNames,
                                            List<Integer> azMbpsList, List<Integer> zaMbpsList, List<String> vlanExps){
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

        return buildRequestedBlueprint(buildRequestedFlow(junctions, new HashSet<>()), Layer3FlowE.builder().build());
    }

    public RequestedBlueprintE buildRequestedBlueprint(RequestedVlanFlowE vlanFlow, Layer3FlowE l3Flow){
        return RequestedBlueprintE.builder()
                .vlanFlow(vlanFlow)
                .layer3Flow(l3Flow)
                .build();
    }

    public RequestedVlanFlowE buildRequestedFlow(Set<RequestedVlanJunctionE> junctions, Set<RequestedVlanPipeE> pipes){
        return RequestedVlanFlowE.builder()
                .junctions(junctions)
                .pipes(pipes)
                .build();
    }

    public ScheduleSpecificationE buildSchedule(Date start, Date end){
        log.info("Populating request schedule");

        return ScheduleSpecificationE.builder()
                .notBefore(start)
                .notAfter(end)
                .durationMinutes(Duration.between(start.toInstant(), end.toInstant()).toMinutes())
                .build();
    }


    public RequestedVlanPipeE buildRequestedPipe(String aPort, String aDevice, String zPort, String zDevice,
                                                 Integer azMbps, Integer zaMbps, PalindromicType palindromic, String vlanExp){

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
                .build();
    }

    public RequestedVlanPipeE buildRequestedPipe(String aPort, String aDevice, String zPort, String zDevice,
                                                 Integer azMbps, Integer zaMbps, PalindromicType palindromic, String aVlanExp,
                                                 String zVlanExp){

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
                .build();
    }

    public RequestedVlanPipeE buildRequestedPipe(List<String> aPorts, String aDevice, List<String> zPorts, String zDevice,
                                                 Integer azMbps, Integer zaMbps, PalindromicType palindromic, String vlanExp){


        return RequestedVlanPipeE.builder()
                .aJunction(buildRequestedJunction(aDevice, aPorts, azMbps, zaMbps, vlanExp, true))
                .zJunction(buildRequestedJunction(zDevice, zPorts, azMbps, zaMbps, vlanExp, false))
                .pipeType(EthPipeType.REQUESTED)
                .azERO(new ArrayList<>())
                .zaERO(new ArrayList<>())
                .azMbps(azMbps)
                .zaMbps(zaMbps)
                .eroPalindromic(palindromic)
                .build();
    }

    public RequestedVlanJunctionE buildRequestedJunction(String deviceName, List<String> fixtureNames,
                                                         Integer azMbps, Integer zaMbps, String vlanExp, boolean startJunc){
        log.info("Building requested junction");

        Optional<UrnE> optUrn = urnRepo.findByUrn(deviceName);

        Set<RequestedVlanFixtureE> fixtures = new HashSet<>();

        assert(fixtureNames.size() >= 1);

        for(String fixName : fixtureNames){
            RequestedVlanFixtureE fix;
            if(startJunc)
                fix = buildRequestedFixture(fixName, azMbps, zaMbps, vlanExp);
            else
                fix = buildRequestedFixture(fixName, zaMbps, azMbps, vlanExp);
            fixtures.add(fix);
        }

        return RequestedVlanJunctionE.builder()
                .deviceUrn(optUrn.isPresent() ? optUrn.get() : null)
                .fixtures(fixtures)
                .junctionType(EthJunctionType.REQUESTED)
                .build();
    }

    public RequestedVlanFixtureE buildRequestedFixture(String fixName, Integer azMbps, Integer zaMbps,
                                                       String vlanExp){
        log.info("Building requested fixture");

        Optional<UrnE> optUrn = urnRepo.findByUrn(fixName);

        return RequestedVlanFixtureE.builder()
                .portUrn(optUrn.isPresent() ? optUrn.get() : null)
                .fixtureType(EthFixtureType.REQUESTED)
                .inMbps(azMbps)
                .egMbps(zaMbps)
                .vlanExpression(vlanExp)
                .build();
    }


    public ConnectionE buildConnection(RequestedBlueprintE blueprint, ScheduleSpecificationE schedule, String connectionID, String description)
    {
        SpecificationE requestSpec = SpecificationE.builder()
                .connectionId(connectionID)
                .description(description)
                .version(1)
                .username("TestUser")
                .requested(blueprint)
                .scheduleSpec(schedule)
                .build();

        ConnectionE theConnection = ConnectionE.builder()
                .connectionId(connectionID)
                .specification(requestSpec)
                .states(new StatesE())
                .build();

        return theConnection;
    }
}
