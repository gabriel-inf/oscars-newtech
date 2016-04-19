package net.es.oscars.spec;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.spec.ScheduleSpecification;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.spec.ent.VlanFlowE;
import net.es.oscars.spec.ent.VlanFixtureE;
import net.es.oscars.spec.ent.VlanJunctionE;
import net.es.oscars.spec.ent.VlanPipeE;
import net.es.oscars.spec.dao.SpecificationRepository;
import net.es.oscars.spec.ent.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(SpecUnitTestConfiguration.class)
public class SpecPopTest {

    @Autowired
    private SpecificationRepository specRepo;


    @Test
    public void testSave() throws PCEException, PSSException {

        if (specRepo.findAll().isEmpty()) {
            SpecificationE spec = getBasicSpec();
            addEndpoints(spec);

            specRepo.save(spec);

        } else {
            log.info("db not empty");
        }
    }



    public static SpecificationE addEndpoints(SpecificationE spec) {

        VlanFlowE flow = spec.getRequested().getVlanFlows().iterator().next();


        VlanJunctionE aj = VlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn("star-tb1")
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .build();

        VlanJunctionE zj = VlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn("nersc-tb1")
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .build();

        VlanFixtureE af = VlanFixtureE.builder()
                .portUrn("star-tb1:1/1/1")
                .vlanExpression("2-100")
                .inMbps(100)
                .egMbps(100)
                .fixtureType(EthFixtureType.REQUESTED)
                .build();

        aj.getFixtures().add(af);

        VlanFixtureE zf = VlanFixtureE.builder()
                .portUrn("nersc-tb1:1/1/1")
                .vlanExpression("2-100")
                .inMbps(100)
                .egMbps(100)
                .fixtureType(EthFixtureType.REQUESTED)
                .build();

        zj.getFixtures().add(zf);

        VlanPipeE az_p = VlanPipeE.builder()
                .azERO(new ArrayList<>())
                .zaERO(new ArrayList<>())
                .aJunction(aj)
                .zJunction(zj)
                .azMbps(1000)
                .zaMbps(1000)
                .pipeType(EthPipeType.REQUESTED)
                .build();

        flow.getPipes().add(az_p);
        return spec;
    }


    public static SpecificationE getBasicSpec() {
        Instant nowInstant = Instant.now();
        Date notBefore = new Date(nowInstant.plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date notAfter = new Date(nowInstant.plus(1L, ChronoUnit.DAYS).getEpochSecond());

        ScheduleSpecificationE sse = ScheduleSpecificationE.builder()
                .notBefore(notBefore)
                .notAfter(notAfter)
                .durationMinutes(30L)
                .build();

        SpecificationE spec = SpecificationE.builder()
                .scheduleSpec(sse)
                .version(1)
                .connectionId("UFAWE")
                .description("a description")
                .username("some user")
                .build();

        BlueprintE bp = BlueprintE.builder()
                .vlanFlows(new HashSet<>())
                .layer3Flows(new HashSet<>())
                .build();

        VlanFlowE flow = VlanFlowE.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        spec.setRequested(bp);
        bp.getVlanFlows().add(flow);
        return spec;
    }

}
