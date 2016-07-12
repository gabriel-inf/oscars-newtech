package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.enums.*;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.dao.SpecificationRepository;
import net.es.oscars.topo.ent.UrnE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class SpecPopTest {

    @Autowired
    private SpecificationRepository specRepo;

    @Autowired
    private UrnRepository urnRepo;

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

        RequestedVlanFlowE flow = spec.getRequested().getVlanFlow();


        UrnE startb1 = UrnE.builder()
                .deviceModel(DeviceModel.JUNIPER_EX)
                .capabilities(new HashSet<>())
                .deviceType(DeviceType.SWITCH)
                .urnType(UrnType.DEVICE)
                .urn("star-tb1")
                .valid(true)
                .build();
        startb1.getCapabilities().add(Layer.ETHERNET);

        urnRepo.save(startb1);

        UrnE nersctb1 = UrnE.builder()
                .deviceModel(DeviceModel.JUNIPER_EX)
                .capabilities(new HashSet<>())
                .deviceType(DeviceType.SWITCH)
                .urnType(UrnType.DEVICE)
                .urn("nersc-tb1")
                .valid(true)
                .build();
        nersctb1.getCapabilities().add(Layer.ETHERNET);

        urnRepo.save(nersctb1);


        UrnE nersctb1_3_1_1 = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("nersc-tb1:3/1/1")
                .valid(true)
                .build();
        nersctb1_3_1_1.getCapabilities().add(Layer.ETHERNET);

        urnRepo.save(nersctb1_3_1_1);


        UrnE startb1_1_1_1 = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("star-tb1:1/1/1")
                .valid(true)
                .build();
        startb1_1_1_1.getCapabilities().add(Layer.ETHERNET);

        urnRepo.save(startb1_1_1_1);


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
                .pipeType(EthPipeType.REQUESTED)
                .build();

        flow.getPipes().add(az_p);
        return spec;
    }


    public SpecificationE getBasicSpec() {
        Instant nowInstant = Instant.now();
        Date notBefore = new Date(nowInstant.plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date notAfter = new Date(nowInstant.plus(1L, ChronoUnit.DAYS).getEpochSecond());

        ScheduleSpecificationE sse = ScheduleSpecificationE.builder()
                .notBefore(notBefore)
                .notAfter(notAfter)
                .durationMinutes(30L)
                .build();

        RequestedVlanFlowE flow = RequestedVlanFlowE.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        RequestedBlueprintE bp = RequestedBlueprintE.builder()
                .vlanFlow(flow)
                .layer3Flow(Layer3FlowE.builder().build())
                .build();

        SpecificationE spec = SpecificationE.builder()
                .scheduleSpec(sse)
                .version(1)
                .requested(bp)
                .connectionId("UFAWE")
                .description("a description")
                .username("some user")
                .build();



        spec.setRequested(bp);
        return spec;
    }

}
