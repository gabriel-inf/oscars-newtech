package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.pce.helpers.RepoEntityBuilder;
import net.es.oscars.pce.helpers.TopologyBuilder;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.RequestedBlueprintE;
import net.es.oscars.resv.ent.ReservedBlueprintE;
import net.es.oscars.resv.ent.ScheduleSpecificationE;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=CoreUnitTestConfiguration.class)
@Transactional
public class HeavyLoadTest {

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private TopoService topoService;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ReservedBandwidthRepository reservedBandwidthRepo;

    @Autowired
    private RepoEntityBuilder repoEntityBuilder;

    @Autowired
    private TopPCE topPCE;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Before
    public void loadTopology() throws IOException {
        repoEntityBuilder.importEsnet();
    }

    @Test
    public void manyBandwidthReservationsTest(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoEsnet();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));

        Instant now = Instant.parse("1995-10-23T00:00:00Z");
        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);
        Integer numBandwidthReservations = 1000;

        String srcPort = "chic-cr5:3/2/1";
        String srcDevice = "chic-cr5";
        String dstPort = "lond-cr5:10/1/4";
        String dstDevice = "lond-cr5";


        reserveBandwidth(urnMap, now, numBandwidthReservations);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(Date.from(requestStartTime), Date.from(requestEndTime));
        RequestedBlueprintE requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice,
                50, 50, PalindromicType.PALINDROME, SurvivabilityType.SURVIVABILITY_NONE, "any",
                1, 1, 1, "test");

        log.info("Beginning test: 'Many Bandwidth Reservations Test'.");

        Optional<ReservedBlueprintE> reservedBlueprint = null;

        try
        {
           reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(Exception pceE)
        {
            log.error("", pceE);
        }

        // Assert that it was able to get through PCE without crashing
        assert(reservedBlueprint != null);

    }

    private void reserveBandwidth(Map<String, UrnE> urnMap, Instant now, Integer numBandwidthReservations) {

        List<String> reservedPortNames = new ArrayList<>();
        List<Instant> reservedStartTimes = new ArrayList<>();
        List<Instant> reservedEndTimes = new ArrayList<>();
        List<Integer> inBandwidths = new ArrayList<>();
        List<Integer> egBandwidths = new ArrayList<>();
        List<String> portUrns = urnMap.keySet().stream().filter(key -> urnMap.get(key).getUrnType().equals(UrnType.IFCE)).collect(Collectors.toList());
        Integer numPorts = portUrns.size();
        Integer maxBandwidth = 10;
        Integer maxTime = 9;
        Random rng = new Random(222L);
        for(Integer num = 0; num < numBandwidthReservations; num++){
            String randomUrn = portUrns.get(rng.nextInt(numPorts));

            Integer maxTimeInt = rng.nextInt(maxTime);
            Integer startTimeInt = Math.floorDiv(maxTimeInt, 3);
            Long start = (long) startTimeInt;
            Long end = (long) maxTimeInt;

            Integer bandwidth = rng.nextInt(maxBandwidth);

            reservedPortNames.add(randomUrn);
            reservedStartTimes.add(now.plus(start, ChronoUnit.HOURS));
            reservedEndTimes.add(now.plus(end, ChronoUnit.HOURS));
            inBandwidths.add(bandwidth);
            egBandwidths.add(bandwidth);
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
    }
}
