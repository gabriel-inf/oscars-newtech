package net.es.oscars.bwavail;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.bwavail.enums.BandwidthAvailabilityRequest;
import net.es.oscars.bwavail.enums.BandwidthAvailabilityResponse;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class BandwidthAvailabilityServiceTest {

    @Autowired
    private BandwidthAvailabilityService bwAvailService;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private TopoService topoService;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ReservedBandwidthRepository reservedBandwidthRepo;

    @Test
    public void noReservationsTest(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = new ArrayList<>();
        List<Instant> reservedStartTimes = new ArrayList<>();
        List<Instant> reservedEndTimes = new ArrayList<>();
        List<Integer> inBandwidths = new ArrayList<>();
        List<Integer> egBandwidths = new ArrayList<>();

        Integer expectedMinAzBw = 1000;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 1000;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsOffPathTest(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeM:1", "nodeL:1");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 1000;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 1000;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsStartBeforeEndAfterTest(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "portA");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(-1L, ChronoUnit.HOURS),
                now.plus(-1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(8L, ChronoUnit.HOURS),
                now.plus(10L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 800;
        Integer expectedMaxAzBw = 800;
        Integer expectedMinZaBw = 800;
        Integer expectedMaxZaBw = 800;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 800);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 800);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 800);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 800);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);

    }

    @Test
    public void reservationsStartBeforeEndDuring(){
        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.minus(100L, ChronoUnit.DAYS),
                now.minus(100L, ChronoUnit.DAYS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 900;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMinAzBw);
        azGoalMap.put(reservedEndTimes.get(0), expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMinZaBw);
        zaGoalMap.put(reservedEndTimes.get(0), expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);

    }

    @Test
    public void reservationsStartDuringEndDuring(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 900;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(reservedStartTimes.get(0), expectedMinAzBw);
        azGoalMap.put(reservedEndTimes.get(0), expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(reservedStartTimes.get(0), expectedMinZaBw);
        zaGoalMap.put(reservedEndTimes.get(0), expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsStartDuringEndAfter(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(7L, ChronoUnit.HOURS),
                now.plus(7L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 900;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(reservedStartTimes.get(0), expectedMinAzBw);
        azGoalMap.put(requestEndTime, expectedMinAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(reservedStartTimes.get(0), expectedMinZaBw);
        zaGoalMap.put(requestEndTime, expectedMinZaBw);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsStartAfterEndAfter(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(6L, ChronoUnit.HOURS),
                now.plus(6L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(8L, ChronoUnit.HOURS),
                now.plus(8L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 1000;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 1000;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsStartBeforeEndAtStart(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(0L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 1000;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 1000;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsStartBeforeEndBefore(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(-1L, ChronoUnit.HOURS),
                now.plus(-1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(0L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 1000;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 1000;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsMix(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "portA", "portA", "portA", "portA");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(-1L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(9L, ChronoUnit.HOURS),
                now.plus(-1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(4L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(8L, ChronoUnit.HOURS),
                now.plus(10L, ChronoUnit.HOURS),
                now.plus(0L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100, 100, 100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100, 100, 100, 100);

        Integer expectedMinAzBw = 700;
        Integer expectedMaxAzBw = 900;
        Integer expectedMinZaBw = 700;
        Integer expectedMaxZaBw = 900;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 700);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 800);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 700);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 800);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsThreeWayOverlapDifferentNodes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeK:3", "nodeQ:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(6L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100, 100);

        Integer expectedMinAzBw = 900;
        Integer expectedMaxAzBw = 900;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 900;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsThreeWayOverlapSameNode(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(6L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100, 100);

        Integer expectedMinAzBw = 800;
        Integer expectedMaxAzBw = 900;
        Integer expectedMinZaBw = 800;
        Integer expectedMaxZaBw = 900;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 800);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 800);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 800);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 800);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsDifferentBandwidthValuesSameNode(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 500);
        List<Integer> egBandwidths = Arrays.asList(100, 500);

        Integer expectedMinAzBw = 400;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 400;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 400);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 400);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsDifferentBandwidthValuesDifferentNodes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 500);
        List<Integer> egBandwidths = Arrays.asList(100, 500);

        Integer expectedMinAzBw = 500;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 500;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsDifferentBandwidthValuesSameNodeDifferentTimes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 500);
        List<Integer> egBandwidths = Arrays.asList(100, 500);

        Integer expectedMinAzBw = 500;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 500;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsDifferentInEgBandwidthValuesSameNodeDifferentTimes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 25);
        List<Integer> egBandwidths = Arrays.asList(50, 3);

        Integer expectedMinAzBw = 950;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 997);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 975);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsDifferentInEgBandwidthValuesDifferentNodesDifferentTimes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeQ:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 25);
        List<Integer> egBandwidths = Arrays.asList(50, 3);

        Integer expectedMinAzBw = 950;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 975);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 997);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsDifferentInEgBandwidthValuesDifferentNodesSameTime(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeQ:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 25);
        List<Integer> egBandwidths = Arrays.asList(50, 3);

        Integer expectedMinAzBw = 950;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 975);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 997);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void reservationsDifferentInEgBandwidthValuesSameNodeSameTime(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 25);
        List<Integer> egBandwidths = Arrays.asList(50, 3);

        Integer expectedMinAzBw = 947;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 875;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 947);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 997);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 875);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 975);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void noPathStartNodeFullyReserved(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Collections.singletonList("portA");
        List<Instant> reservedStartTimes = Collections.singletonList(
                now.plus(1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Collections.singletonList(
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Collections.singletonList(1000);
        List<Integer> egBandwidths = Collections.singletonList(1000);

        Integer expectedMinAzBw = 0;
        Integer expectedMaxAzBw = 0;
        Integer expectedMinZaBw = 0;
        Integer expectedMaxZaBw = 0;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 0);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 0);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 0);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 0);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void noPathIntermediateNodesFullyReserved(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeW:1", "nodeX:1", "nodeM:3", "nodeL:3");

        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(1000, 1000, 1000, 1000);
        List<Integer> egBandwidths = Arrays.asList(1000, 1000, 1000, 1000);

        Integer expectedMinAzBw = 0;
        Integer expectedMaxAzBw = 0;
        Integer expectedMinZaBw = 0;
        Integer expectedMaxZaBw = 0;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 0);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 0);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 0);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 0);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }

    @Test
    public void onePathAvailable(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeW:1", "nodeX:1", "nodeL:3", "nodeM:3");

        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(1000, 1000, 1000, 500);
        List<Integer> egBandwidths = Arrays.asList(1000, 1000, 1000, 500);

        Integer expectedMinAzBw = 500;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 500;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths, urnMap);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, expectedMinAzBw, expectedMaxAzBw, expectedMinZaBw, expectedMaxZaBw, azGoalMap, zaGoalMap);
    }


    private void testResponse(BandwidthAvailabilityResponse response, Integer expectedMinAzBw,
                              Integer expectedMaxAzBw, Integer expectedMinZaBw, Integer expectedMaxZaBw,
                              Map<Instant, Integer> azGoalMap, Map<Instant, Integer> zaGoalMap) {

        Map<Instant, Integer> azBwMap = response.getAzBwAvailMap();
        Map<Instant, Integer> zaBwMap = response.getZaBwAvailMap();

        log.info("Expected AZ Map: " + azGoalMap);
        log.info("Actual AZ Map: " + azBwMap);
        log.info("Expected ZA Map: " + zaGoalMap);
        log.info("Actual ZA Map: " + zaBwMap);

        Integer minAzBw = response.getMinAvailableAzBandwidth();
        Integer maxAzBw = response.getMaxAvailableAzBandwidth();
        Integer minZaBw = response.getMinAvailableZaBandwidth();
        Integer maxZaBw = response.getMaxAvailableZaBandwidth();
        assert(minAzBw.equals(expectedMinAzBw));
        assert(maxAzBw.equals(expectedMaxAzBw));
        assert(minZaBw.equals(expectedMinZaBw));
        assert(maxZaBw.equals(expectedMaxZaBw));

        assert(azBwMap.equals(azGoalMap));
        assert(zaBwMap.equals(zaGoalMap));
    }

    private BandwidthAvailabilityResponse placeRequest(Instant requestStartTime, Instant requestEndTime) {
        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .requestID(1L)
                .srcPort("portA")
                .srcDevice("nodeK")
                .dstPort("portZ")
                .dstDevice("nodeQ")
                .startDate(new Date(requestStartTime.toEpochMilli()))
                .endDate(new Date(requestEndTime.toEpochMilli()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .palindromicType(PalindromicType.PALINDROME)
                .survivabilityType(SurvivabilityType.SURVIVABILITY_NONE)
                .numDisjoint(1)
                .build();
        return bwAvailService.getBandwidthAvailabilityMap(request);
    }

    private void reserveBandwidth(List<String> reservedPortNames, List<Instant> reservedStartTimes,
                                  List<Instant> reservedEndTimes, List<Integer> inBandwidths, List<Integer> egBandwidths,
                                  Map<String, UrnE> urnMap) {
        List<ReservedBandwidthE> reservedBandwidths = new ArrayList<>();
        for(Integer index = 0; index < reservedPortNames.size(); index++){
            reservedBandwidths.add(ReservedBandwidthE.builder()
                    .urn(urnMap.get(reservedPortNames.get(index)))
                    .beginning(reservedStartTimes.get(index))
                    .ending(reservedEndTimes.get(index))
                    .inBandwidth(inBandwidths.get(index))
                    .egBandwidth(egBandwidths.get(index))
                    .build());
        }
        reservedBandwidthRepo.save(reservedBandwidths);
    }
}
