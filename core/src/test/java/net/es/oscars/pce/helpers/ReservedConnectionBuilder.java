package net.es.oscars.pce.helpers;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.resv.ent.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;


@Slf4j
@Component
public class ReservedConnectionBuilder {
    public ConnectionE singleAluJunction(String connectionId, String deviceUrn, Map<String, Integer> fixtures) {

        ReservedVlanJunctionE rvj = ReservedVlanJunctionE.builder()
                .deviceUrn(deviceUrn)
                .fixtures(new HashSet<>())
                .junctionType(EthJunctionType.ALU_VPLS)
                .id(100L)
                .reservedPssResources(new HashSet<>())
                .reservedVlans(new HashSet<>())
                .build();

        for (String port : fixtures.keySet()) {
            ReservedBandwidthE rbe = ReservedBandwidthE.builder()
                    .containerConnectionId(connectionId)
                    .inBandwidth(1000)
                    .egBandwidth(1000)
                    .urn(port)
                    .build();

            ReservedVlanFixtureE fix = ReservedVlanFixtureE.builder()
                    .fixtureType(EthFixtureType.ALU_SAP)
                    .ifceUrn(port)
                    .reservedBandwidth(rbe)
                    .reservedVlans(new HashSet<>())
                    .reservedPssResources(new HashSet<>())
                    .build();

            ReservedVlanE rv = ReservedVlanE.builder()
                    .vlan(fixtures.get(port))
                    .urn(port)
                    .build();
            fix.getReservedVlans().add(rv);
            rvj.getFixtures().add(fix);
        }

        ReservedVlanFlowE rvf = ReservedVlanFlowE.builder()
                .junctions(new HashSet<>())
                .allPaths(new HashSet<>())
                .ethPipes(new HashSet<>())
                .mplsPipes(new HashSet<>())
                .containerConnectionId(connectionId)
                .id(100L)
                .build();

        rvf.getJunctions().add(rvj);

        ReservedBlueprintE rbp = ReservedBlueprintE.builder()
                .vlanFlow(rvf)
                .containerConnectionId(connectionId)
                .id(100L)
                .build();


        RequestedBlueprintE rbe = RequestedBlueprintE.builder()
                .containerConnectionId(connectionId)
                .build();
        ScheduleSpecificationE sse = ScheduleSpecificationE.builder()
                .minimumDuration(100L)
                .startDates(new ArrayList<>())
                .endDates(new ArrayList<>())
                .build();



        SpecificationE spE = SpecificationE.builder()
                .containerConnectionId(connectionId)
                .username("someuser")
                .version(1)
                .description("description")
                .requested(rbe)
                .scheduleSpec(sse)
                .build();

        ScheduleE sch = ScheduleE.builder()
                .setup(new Date())
                .submitted(new Date())
                .teardown(new Date())
                .build();

        ArrayList<Date> reservedSchedule = new ArrayList<>();
        reservedSchedule.add(new Date());
        reservedSchedule.add(new Date());

        return ConnectionE.builder()
                .connectionId(connectionId)
                .reserved(rbp)
                .reservedSchedule(reservedSchedule)
                .specification(spE)
                .schedule(sch)
                .id(100L)
                .build();

    }


}
