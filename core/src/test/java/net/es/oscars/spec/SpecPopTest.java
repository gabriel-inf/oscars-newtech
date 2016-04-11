package net.es.oscars.spec;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.EthPCE;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pss.enums.EthJunctionType;
import net.es.oscars.pss.enums.EthPipeType;
import net.es.oscars.spec.ent.EFlow;
import net.es.oscars.spec.ent.EVlanFixture;
import net.es.oscars.spec.ent.EVlanJunction;
import net.es.oscars.spec.ent.EVlanPipe;
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
    public void testSave() throws PCEException {

        if (specRepo.findAll().isEmpty()) {
            ESpecification spec = this.getBasicSpec();

            EFlow flow = spec.getBlueprint().getFlows().iterator().next();


            EVlanJunction aj = EVlanJunction.builder()
                    .junctionType(EthJunctionType.REQUESTED)
                    .deviceUrn("star-tb1")
                    .fixtures(new HashSet<>())
                    .resourceIds(new HashSet<>())
                    .build();

            EVlanJunction zj = EVlanJunction.builder()
                    .junctionType(EthJunctionType.REQUESTED)
                    .deviceUrn("nersc-tb1")
                    .fixtures(new HashSet<>())
                    .resourceIds(new HashSet<>())
                    .build();

            EVlanFixture af = EVlanFixture.builder()
                    .portUrn("star-tb1:1/1/1")
                    .vlanExpression("2-100")
                    .inMbps(100)
                    .egMbps(100)
                    .build();

            aj.getFixtures().add(af);

            EVlanFixture zf = EVlanFixture.builder()
                    .portUrn("nersc-tb1:1/1/1")
                    .vlanExpression("2-100")
                    .inMbps(100)
                    .egMbps(100)
                    .build();

            zj.getFixtures().add(zf);

            EVlanPipe az_p = EVlanPipe.builder()
                    .azERO(new ArrayList<>())
                    .aJunction(aj)
                    .zJunction(zj)
                    .azMbps(1000)
                    .pipeType(EthPipeType.REQUESTED)
                    .build();

            EVlanPipe za_p = EVlanPipe.builder()
                    .azERO(new ArrayList<>())
                    .aJunction(zj)
                    .zJunction(aj)
                    .azMbps(1000)
                    .pipeType(EthPipeType.REQUESTED)
                    .build();

            flow.getPipes().add(az_p);
            flow.getPipes().add(za_p);


            EthPCE pce = new EthPCE();
            pce.makeSchematic(spec.getBlueprint());
            log.info("got schematic");


            specRepo.save(spec);

        } else {
            log.info("db not empty");
        }
    }

    @Test(expected = PCEException.class)
    public void testNoFixtures() throws PCEException {
        ESpecification spec = this.getBasicSpec();

        EFlow flow = spec.getBlueprint().getFlows().iterator().next();

        EVlanJunction somejunction = EVlanJunction.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn("star-tb1")
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .build();

        flow.getJunctions().add(somejunction);
        EthPCE pce = new EthPCE();
        pce.verifyBlueprint(spec.getBlueprint());

    }

    private ESpecification getBasicSpec() {
        Date now = new Date();
        Instant nowInstant = Instant.now();
        Date notBefore = new Date(nowInstant.plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date notAfter = new Date(nowInstant.plus(1L, ChronoUnit.DAYS).getEpochSecond());

        ESpecification spec = ESpecification.builder()
                .submitted(now)
                .notBefore(notBefore)
                .notAfter(notAfter)
                .durationMinutes(30L)
                .version(1)
                .username("some user")
                .specificationId("UANS8A")
                .build();

        EBlueprint bp = EBlueprint.builder()
                .flows(new HashSet<>())
                .build();

        EFlow flow = EFlow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        spec.setBlueprint(bp);
        bp.getFlows().add(flow);
        return spec;
    }

}
