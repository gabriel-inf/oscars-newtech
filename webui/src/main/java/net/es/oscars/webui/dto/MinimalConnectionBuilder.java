package net.es.oscars.webui.dto;

import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.Schedule;
import net.es.oscars.dto.resv.States;
import net.es.oscars.dto.spec.*;
import net.es.oscars.st.oper.OperState;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;

import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by jeremy on 11/1/16.
 */
public class MinimalConnectionBuilder
{
    public Connection buildMinimalConnectionFromRequest(MinimalRequest minRequest)
    {
        Connection minConnection;


        // Multiply start and end times by 1000
        // Unix time was returned by resv_gui.js as the number of SECONDS since the epoch
        // Date expects the number of MILLISECONDS
        Date notBefore = new Date(minRequest.getStartAt() * 1000L);
        Date notAfter = new Date(minRequest.getEndAt() * 1000L);
        List<Date> startDates = Collections.singletonList(notBefore);
        List<Date> endDates = Collections.singletonList(notAfter);
        List<Long> durations = new ArrayList<>();
        for(Integer index = 0; index < startDates.size(); index++){
            ChronoUnit.MINUTES.between(startDates.get(0).toInstant(), endDates.get(0).toInstant());
        }

        //TODO: Pull this minimum duration from the user
        Long minimumDuration = 0L;


        String connectionId = minRequest.getConnectionId();
        String username = "some user";

        ReservedVlanFlow resvf = ReservedVlanFlow.builder()
                .ethPipes(new HashSet<>())
                .junctions(new HashSet<>())
                .mplsPipes(new HashSet<>())
                .build();

        ReservedBlueprint rbp = ReservedBlueprint.builder()
                .vlanFlow(resvf).build();
        Schedule sch = Schedule.builder()
                .setup(new Date())
                .teardown(new Date())
                .submitted(new Date())
                .build();
        States states = States.builder()
                .oper(OperState.ADMIN_DOWN_OPER_DOWN)
                .prov(ProvState.INITIAL)
                .resv(ResvState.SUBMITTED)
                .build();

        ScheduleSpecification ss = ScheduleSpecification.builder()
                .durationMinutes(durations)
                .startDates(startDates)
                .endDates(endDates)
                .minimumDuration(minimumDuration)
                .build();

        Layer3Flow layer3Flow = Layer3Flow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        RequestedVlanFlow reqvf = RequestedVlanFlow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .maxPipes(1)
                .minPipes(1)
                .build();

        Map<String, RequestedVlanJunction> junctionMap = new HashMap<>();

        for (String nodeId : minRequest.getJunctions().keySet()) {
            RequestedVlanJunction rvj = RequestedVlanJunction.builder()
                    .deviceUrn(nodeId)
                    .fixtures(new HashSet<>())
                    .junctionType(EthJunctionType.REQUESTED)
                    .build();
            for (String port : minRequest.getJunctions().get(nodeId).getFixtures().keySet()) {
                MinimalFixture fix = minRequest.getJunctions().get(nodeId).getFixtures().get(port);
                Integer bw = Integer.parseInt(fix.getBw());
                String vlan = fix.getVlan();

                RequestedVlanFixture rvfix = RequestedVlanFixture.builder()
                        .fixtureType(EthFixtureType.REQUESTED)
                        .portUrn(port)
                        .egMbps(bw)
                        .inMbps(bw)
                        .vlanExpression(vlan)
                        .build();
                rvj.getFixtures().add(rvfix);
            }

            junctionMap.put(nodeId, rvj);
        }
        Set<RequestedVlanJunction> inPipes = new HashSet<>();
        for (String pipeId : minRequest.getPipes().keySet()) {
            RequestedVlanJunction aJ = junctionMap.get(minRequest.getPipes().get(pipeId).getA());
            RequestedVlanJunction zJ = junctionMap.get(minRequest.getPipes().get(pipeId).getZ());
            inPipes.add(aJ);
            inPipes.add(zJ);
            Integer bw = Integer.parseInt(minRequest.getPipes().get(pipeId).getBw());
            RequestedVlanPipe rvp = RequestedVlanPipe.builder()
                    .aJunction(aJ)
                    .zJunction(zJ)
                    .numDisjoint(1)
                    .azMbps(bw)
                    .zaMbps(bw)
                    .azERO(new ArrayList<>())
                    .zaERO(new ArrayList<>())
                    .urnBlacklist(new HashSet<>())
                    .eroPalindromic(PalindromicType.PALINDROME)
                    .eroSurvivability(SurvivabilityType.SURVIVABILITY_NONE)
                    .pipeType(EthPipeType.REQUESTED)
                    .build();
            reqvf.getPipes().add(rvp);
        }
        for (RequestedVlanJunction rvj : junctionMap.values()) {
            if (!inPipes.contains(rvj)) {
                reqvf.getJunctions().add(rvj);
            }
        }


        RequestedBlueprint reqbp = RequestedBlueprint.builder()
                .layer3Flow(layer3Flow)
                .vlanFlow(reqvf)
                .build();

        Specification spec = Specification.builder()
                .scheduleSpec(ss)
                .connectionId(connectionId)
                .description(minRequest.getDescription())
                .requested(reqbp)
                .username(username)
                .version(0)
                .build();

        minConnection = Connection.builder()
                .connectionId(connectionId)
                .specification(spec)
                .reserved(rbp)
                .schedule(sch)
                .states(states)
                .reservedSchedule(new ArrayList<>())
                .build();

        return minConnection;
    }
}
