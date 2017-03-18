package net.es.oscars.resv;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.SpecificationRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.pop.TopoFileImporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

@Slf4j
@Transactional
public class SpecPopTest extends AbstractCoreTest {

    @Autowired
    private TopoFileImporter topoFileImporter;

    @Autowired
    private SpecificationRepository specRepo;


    @Before
    public void startup() throws IOException {
        topoFileImporter.importFromFile(true, "config/topo/basic-devices.json", "config/topo/basic-adjcies.json");
    }

    @Test
    public void testSave() throws PCEException, PSSException {

        if (specRepo.findAll().isEmpty()) {
            SpecificationE spec = getBasicSpec();
            this.addEndpoints(spec);

            specRepo.save(spec);

        } else {
            log.info("db not empty");
        }
    }

    public SpecificationE addEndpoints(SpecificationE spec) {

        String startb1 = "star-tb1";
        String nersctb1 = "nersc-tb1";
        String nersctb1_3_1_1 = "nersc-tb1:3/1/1";
        String startb1_1_1_1 = "star-tb1:1/1/1";


        RequestedVlanJunctionE aj = RequestedVlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn(startb1)
                .fixtures(new HashSet<>())
                .build();

        RequestedVlanJunctionE zj = RequestedVlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn(nersctb1)
                .fixtures(new HashSet<>())
                .build();

        RequestedVlanFixtureE af = RequestedVlanFixtureE.builder()
                .portUrn(startb1_1_1_1)
                .vlanExpression("2:100")
                .inMbps(100)
                .egMbps(100)
                .fixtureType(EthFixtureType.REQUESTED)
                .build();

        aj.getFixtures().add(af);

        RequestedVlanFixtureE zf = RequestedVlanFixtureE.builder()
                .portUrn(nersctb1_3_1_1)
                .vlanExpression("2:100")
                .inMbps(100)
                .egMbps(100)
                .fixtureType(EthFixtureType.REQUESTED)
                .build();

        zj.getFixtures().add(zf);

        RequestedVlanPipeE az_p = RequestedVlanPipeE.builder()
                .azERO(new ArrayList<>())
                .zaERO(new ArrayList<>())
                .aJunction(aj)
                .zJunction(zj)
                .azMbps(1000)
                .zaMbps(1000)
                .eroPalindromic(PalindromicType.PALINDROME)
                .eroSurvivability(SurvivabilityType.SURVIVABILITY_NONE)
                .pipeType(EthPipeType.REQUESTED)
                .numPaths(1)
                .priority(Integer.MAX_VALUE)
                .build();

        RequestedVlanFlowE flow = spec.getRequested().getVlanFlow();

        flow.getPipes().add(az_p);
        return spec;
    }


    public SpecificationE getBasicSpec() {
        Instant nowInstant = Instant.now();
        Date notBefore = new Date(nowInstant.plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date notAfter = new Date(nowInstant.plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String connID = "UFAWE";

        ScheduleSpecificationE sse = ScheduleSpecificationE.builder()
                .startDates(Collections.singletonList(notBefore))
                .endDates(Collections.singletonList(notAfter))
                .minimumDuration(30L)
                .build();

        RequestedVlanFlowE flow = RequestedVlanFlowE.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .minPipes(1)
                .maxPipes(1)
                .containerConnectionId(connID)
                .build();

        RequestedBlueprintE bp = RequestedBlueprintE.builder()
                .vlanFlow(flow)
                .layer3Flow(Layer3FlowE.builder().build())
                .containerConnectionId(connID)
                .build();

        SpecificationE spec = SpecificationE.builder()
                .scheduleSpec(sse)
                .version(1)
                .requested(bp)
                .containerConnectionId(connID)
                .description("a description")
                .username("some user")
                .build();


        spec.setRequested(bp);
        return spec;
    }

}
