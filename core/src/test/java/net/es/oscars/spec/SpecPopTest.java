package net.es.oscars.spec;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.EthPCE;
import net.es.oscars.pce.PCEException;
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
                    .valves(new HashSet<>())
                    .build();

            EValve sValve = EValve.builder().valveId("1G-star").deviceUrn("star-tb1").mbps(1000).build();
            EValve nValve = EValve.builder().valveId("1G-nersc").deviceUrn("nersc-tb1").mbps(1000).build();
            flow.getValves().add(sValve);
            flow.getValves().add(nValve);


            EJunction aj = EJunction.builder()
                    .deviceUrn("star-tb1")
                    .junctionId("star-junction")
                    .fixtures(new HashSet<>())
                    .build();

            EJunction zj = EJunction.builder()
                    .deviceUrn("nersc-tb1")
                    .junctionId("nersc-junction")
                    .fixtures(new HashSet<>())
                    .build();

            EFixture af = EFixture.builder()
                    .portUrn("star-tb1:1/1/1")
                    .inValveId("1G-star")
                    .outValveId("1G-star")
                    .vlanId(100)
                    .build();

            aj.getFixtures().add(af);

            EFixture zf = EFixture.builder()
                    .portUrn("nersc-tb1:1/1/1")
                    .inValveId("1G-nersc")
                    .outValveId("1G-nersc")
                    .vlanId(1001)
                    .build();

            zj.getFixtures().add(zf);

            EPipe az_p = EPipe.builder()
                    .azPath(new ArrayList<>())
                    .aJunctionId("star-junction")
                    .zJunctionId("nersc-junction")
                    .azValveId("1G-star")
                    .build();

            EPipe za_p = EPipe.builder()
                    .azPath(new ArrayList<>())
                    .aJunctionId("nersc-junction")
                    .zJunctionId("star-junction")
                    .azValveId("1G-nersc")
                    .build();

            flow.getJunctions().add(aj);
            flow.getJunctions().add(zj);
            flow.getPipes().add(az_p);
            flow.getPipes().add(za_p);

            bp.getFlows().add(flow);

            EthPCE pce = new EthPCE();
            pce.makeSchematic(bp);
            log.info("got schematic");



            specRepo.save(spec);

        } else {
            log.info("db not empty");
        }

    }
}
