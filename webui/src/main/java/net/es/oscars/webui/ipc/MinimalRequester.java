package net.es.oscars.webui.ipc;

import lombok.extern.slf4j.Slf4j;
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
import net.es.oscars.webui.dto.MinimalFixture;
import net.es.oscars.webui.dto.MinimalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Slf4j
@Component
public class MinimalRequester {
    @Autowired
    private RestTemplate restTemplate;


    public Connection submitMinimal(MinimalRequest minimalRequest) {
        log.info("submitting minimal " + minimalRequest.toString());

        Date notBefore = new Date(minimalRequest.getStartAt());
        Date notAfter = new Date(minimalRequest.getEndAt());
        String connectionId = UUID.randomUUID().toString();
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
                .durationMinutes(0L)
                .notAfter(notAfter)
                .notBefore(notBefore)
                .build();

        Layer3Flow layer3Flow = Layer3Flow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        RequestedVlanFlow reqvf = RequestedVlanFlow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        Map<String, RequestedVlanJunction> junctionMap = new HashMap<>();

        for (String nodeId : minimalRequest.getJunctions().keySet()) {
            RequestedVlanJunction rvj = RequestedVlanJunction.builder()
                    .deviceUrn(nodeId)
                    .fixtures(new HashSet<>())
                    .junctionType(EthJunctionType.REQUESTED)
                    .build();
            for (String port : minimalRequest.getJunctions().get(nodeId).getFixtures().keySet()) {
                MinimalFixture fix = minimalRequest.getJunctions().get(nodeId).getFixtures().get(port);
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
        for (String pipeId : minimalRequest.getPipes().keySet()) {
            RequestedVlanJunction aJ = junctionMap.get(minimalRequest.getPipes().get(pipeId).getA());
            RequestedVlanJunction zJ = junctionMap.get(minimalRequest.getPipes().get(pipeId).getZ());
            inPipes.add(aJ);
            inPipes.add(zJ);
            Integer bw = Integer.parseInt(minimalRequest.getPipes().get(pipeId).getBw());
            RequestedVlanPipe rvp = RequestedVlanPipe.builder()
                    .aJunction(aJ)
                    .zJunction(zJ)
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
                .description(minimalRequest.getDescription())
                .requested(reqbp)
                .username(username)
                .version(0)
                .build();

        Connection c = Connection.builder()
                .connectionId(connectionId)
                .specification(spec)
                .reserved(rbp)
                .schedule(sch)
                .states(states)
                .build();


        String submitUrl = "resv/connection/add";
        String restPath = "https://localhost:8000/" + submitUrl;
        log.info("sending connection " + c.toString());
        Connection resultC = restTemplate.postForObject(restPath, c, Connection.class);
        log.info("got connection " + resultC.toString());
        return c;
    }


}
