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

        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .requestID(1L)
                .srcPort("portA")
                .srcDevice("nodeK")
                .dstPort("portZ")
                .dstDevice("nodeQ")
                .startDate(new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).toEpochMilli()))
                .endDate(new Date(Instant.now().plus(1L, ChronoUnit.DAYS).toEpochMilli()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .palindromicType(PalindromicType.PALINDROME)
                .survivabilityType(SurvivabilityType.SURVIVABILITY_NONE)
                .build();
        BandwidthAvailabilityResponse response = bwAvailService.getBandwidthAvailabilityMap(request);

        List<UrnE> urns = urnRepo.findAll();
        List<ReservableBandwidthE> reservableBandwidths = urns
                .stream()
                .filter(u -> u.getReservableBandwidth() != null)
                .map(UrnE::getReservableBandwidth)
                .collect(Collectors.toList());
        List<Integer> inReservableBws = reservableBandwidths.stream().map(ReservableBandwidthE::getIngressBw).collect(Collectors.toList());
        List<Integer> egReservableBws = reservableBandwidths.stream().map(ReservableBandwidthE::getEgressBw).collect(Collectors.toList());

        Map<Instant, Integer> azBwMap = response.getAzBwAvailMap();
        Map<Instant, Integer> zaBwMap = response.getZaBwAvailMap();
        Integer minAzBw = response.getMinAvailableAzBandwidth();
        Integer maxAzBw = response.getMaxAvailableAzBandwidth();
        Integer minZaBw = response.getMinAvailableZaBandwidth();
        Integer maxZaBw = response.getMaxAvailableZaBandwidth();
        assert(minAzBw.equals(maxAzBw));
        assert(maxAzBw.equals(minZaBw));
        assert(minZaBw.equals(maxZaBw));
        assert(azBwMap.values().stream().allMatch(inReservableBws::contains));
        assert(zaBwMap.values().stream().allMatch(inReservableBws::contains));
        assert(azBwMap.values().stream().allMatch(egReservableBws::contains));
        assert(zaBwMap.values().stream().allMatch(egReservableBws::contains));
    }

    @Test
    public void reservationsOffPathTest(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        Topology topo = topoService.getMultilayerTopology();
        List<UrnE> urns = urnRepo.findAll();

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        List<ReservedBandwidthE> reservedBandwidths = new ArrayList<>();
        ReservedBandwidthE resM = ReservedBandwidthE.builder()
                .urn(urns.stream().filter(u -> u.getUrn().equals("nodeM:1")).collect(Collectors.toList()).get(0))
                .beginning(Instant.now().minus(100L, ChronoUnit.DAYS))
                .ending(Instant.now().plus(100L, ChronoUnit.DAYS))
                .inBandwidth(100)
                .egBandwidth(100)
                .build();
        ReservedBandwidthE resP = ReservedBandwidthE.builder()
                .urn(urns.stream().filter(u -> u.getUrn().equals("nodeP:1")).collect(Collectors.toList()).get(0))
                .beginning(Instant.now().minus(100L, ChronoUnit.DAYS))
                .ending(Instant.now().plus(100L, ChronoUnit.DAYS))
                .inBandwidth(100)
                .egBandwidth(100)
                .build();
        reservedBandwidths.add(resM);
        reservedBandwidths.add(resP);

        reservedBandwidthRepo.save(reservedBandwidths);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .requestID(1L)
                .srcPort("portA")
                .srcDevice("nodeK")
                .dstPort("portZ")
                .dstDevice("nodeQ")
                .startDate(new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).toEpochMilli()))
                .endDate(new Date(Instant.now().plus(1L, ChronoUnit.DAYS).toEpochMilli()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .palindromicType(PalindromicType.PALINDROME)
                .survivabilityType(SurvivabilityType.SURVIVABILITY_NONE)
                .build();
        BandwidthAvailabilityResponse response = bwAvailService.getBandwidthAvailabilityMap(request);

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");
        List<UrnE> portPath = urns.stream().filter(u -> chosenPortNames.contains(u.getUrn())).collect(Collectors.toList());

        Map<UrnE, Integer> inReservableBws = portPath
                .stream()
                .filter(urn -> urn.getReservableBandwidth() != null)
                .collect(Collectors.toMap(urn -> urn, urn -> urn.getReservableBandwidth().getIngressBw()));
        Map<UrnE, Integer> egReservableBws = portPath
                .stream()
                .filter(urn -> urn.getReservableBandwidth() != null)
                .collect(Collectors.toMap(urn -> urn, urn -> urn.getReservableBandwidth().getEgressBw()));

        Map<Instant, Integer> azBwMap = response.getAzBwAvailMap();
        Map<Instant, Integer> zaBwMap = response.getZaBwAvailMap();
        Integer minAzBw = response.getMinAvailableAzBandwidth();
        Integer maxAzBw = response.getMaxAvailableAzBandwidth();
        Integer minZaBw = response.getMinAvailableZaBandwidth();
        Integer maxZaBw = response.getMaxAvailableZaBandwidth();
        assert(minAzBw.equals(maxAzBw));
        assert(maxAzBw.equals(minZaBw));
        assert(minZaBw.equals(maxZaBw));
        for(Integer azBw : azBwMap.values()){
            assert(inReservableBws.values().stream().allMatch(bw -> bw.equals(azBw)));
            assert(egReservableBws.values().stream().allMatch(bw -> bw.equals(azBw)));
        }
        for(Integer zaBw : zaBwMap.values()){
            assert(inReservableBws.values().stream().allMatch(bw -> bw.equals(zaBw)));
            assert(egReservableBws.values().stream().allMatch(bw -> bw.equals(zaBw)));
        }
    }

    @Test
    public void reservationsOnPathStartBeforeEndAfterTest(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        Topology topo = topoService.getMultilayerTopology();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<ReservedBandwidthE> reservedBandwidths = new ArrayList<>();
        ReservedBandwidthE resM = ReservedBandwidthE.builder()
                .urn(urnMap.get("portA"))
                .beginning(Instant.now().minus(100L, ChronoUnit.DAYS))
                .ending(Instant.now().plus(100L, ChronoUnit.DAYS))
                .inBandwidth(100)
                .egBandwidth(100)
                .build();
        ReservedBandwidthE resP = ReservedBandwidthE.builder()
                .urn(urnMap.get("nodeW:2"))
                .beginning(Instant.now().minus(100L, ChronoUnit.DAYS))
                .ending(Instant.now().plus(100L, ChronoUnit.DAYS))
                .inBandwidth(100)
                .egBandwidth(100)
                .build();
        reservedBandwidths.add(resM);
        reservedBandwidths.add(resP);

        reservedBandwidthRepo.save(reservedBandwidths);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .requestID(1L)
                .srcPort("portA")
                .srcDevice("nodeK")
                .dstPort("portZ")
                .dstDevice("nodeQ")
                .startDate(new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).toEpochMilli()))
                .endDate(new Date(Instant.now().plus(1L, ChronoUnit.DAYS).toEpochMilli()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .palindromicType(PalindromicType.PALINDROME)
                .survivabilityType(SurvivabilityType.SURVIVABILITY_NONE)
                .build();
        BandwidthAvailabilityResponse response = bwAvailService.getBandwidthAvailabilityMap(request);

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");
        List<UrnE> portPath = urns.stream().filter(u -> chosenPortNames.contains(u.getUrn())).collect(Collectors.toList());

        Map<UrnE, Integer> inReservableBws = portPath
                .stream()
                .filter(urn -> urn.getReservableBandwidth() != null)
                .collect(Collectors.toMap(urn -> urn, urn -> urn.getReservableBandwidth().getIngressBw()));
        Map<UrnE, Integer> egReservableBws = portPath
                .stream()
                .filter(urn -> urn.getReservableBandwidth() != null)
                .collect(Collectors.toMap(urn -> urn, urn -> urn.getReservableBandwidth().getEgressBw()));

        Map<Instant, Integer> azBwMap = response.getAzBwAvailMap();
        Map<Instant, Integer> zaBwMap = response.getZaBwAvailMap();
        Integer minAzBw = response.getMinAvailableAzBandwidth();
        Integer maxAzBw = response.getMaxAvailableAzBandwidth();
        Integer minZaBw = response.getMinAvailableZaBandwidth();
        Integer maxZaBw = response.getMaxAvailableZaBandwidth();
        assert(minAzBw.equals(maxAzBw));
        assert(maxAzBw.equals(minZaBw));
        assert(minZaBw.equals(maxZaBw));
        for(Integer azBw : azBwMap.values()){
            for(String portName : chosenPortNames){
                if(reservedPortNames.contains(portName)){
                    Integer inReservableBw = inReservableBws.get(urnMap.get(portName));
                    Integer egReservableBw = inReservableBws.get(urnMap.get(portName));

                    Integer inReservedBw = reservedBandwidths
                            .stream()
                            .filter(u -> u.getUrn().getUrn().equals(portName))
                            .mapToInt(ReservedBandwidthE::getInBandwidth)
                            .sum();

                    Integer egReservedBw = reservedBandwidths
                            .stream()
                            .filter(u -> u.getUrn().getUrn().equals(portName))
                            .mapToInt(ReservedBandwidthE::getEgBandwidth)
                            .sum();

                    assert(azBw.equals(inReservableBw - inReservedBw));
                    assert(azBw.equals(egReservableBw - egReservedBw));
                }
                else{
                    assert(azBw < inReservableBws.get(urnMap.get(portName)));
                    assert(azBw < egReservableBws.get(urnMap.get(portName)));
                }
            }
        }
        for(Integer zaBw : zaBwMap.values()){
            for(String portName : chosenPortNames){
                if(reservedPortNames.contains(portName)){
                    Integer inReservableBw = inReservableBws.get(urnMap.get(portName));
                    Integer egReservableBw = inReservableBws.get(urnMap.get(portName));

                    Integer inReservedBw = reservedBandwidths
                            .stream()
                            .filter(u -> u.getUrn().getUrn().equals(portName))
                            .mapToInt(ReservedBandwidthE::getInBandwidth)
                            .sum();

                    Integer egReservedBw = reservedBandwidths
                            .stream()
                            .filter(u -> u.getUrn().getUrn().equals(portName))
                            .mapToInt(ReservedBandwidthE::getEgBandwidth)
                            .sum();

                    assert(zaBw.equals(inReservableBw - inReservedBw));
                    assert(zaBw.equals(egReservableBw - egReservedBw));
                }
                else{
                    assert(zaBw < inReservableBws.get(urnMap.get(portName)));
                    assert(zaBw < egReservableBws.get(urnMap.get(portName)));
                }
            }
        }
    }

    @Test
    public void reservationsOnPathStartBeforeEndDuring(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.now();

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.minus(100L, ChronoUnit.DAYS),
                now.minus(100L, ChronoUnit.DAYS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 900;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> chosenPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");

        Instant requestStartTime = now.plus(15L, ChronoUnit.MINUTES);
        Instant requestEndTime = now.plus(1L, ChronoUnit.DAYS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMinAzBw);
        azGoalMap.put(reservedEndTimes.get(0), expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMinZaBw);
        zaGoalMap.put(reservedEndTimes.get(0), expectedMaxZaBw);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
        Map<String, ReservedBandwidthE> resBWMap = reservedBandwidths.stream()
                .collect(Collectors.toMap(rsv -> rsv.getUrn().getUrn(), rsv -> rsv));
        reservedBandwidthRepo.save(reservedBandwidths);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
                .build();
        BandwidthAvailabilityResponse response = bwAvailService.getBandwidthAvailabilityMap(request);

        List<UrnE> portPath = urns.stream().filter(u -> chosenPortNames.contains(u.getUrn())).collect(Collectors.toList());

        Integer minAzBw = response.getMinAvailableAzBandwidth();
        Integer maxAzBw = response.getMaxAvailableAzBandwidth();
        Integer minZaBw = response.getMinAvailableZaBandwidth();
        Integer maxZaBw = response.getMaxAvailableZaBandwidth();
        assert(minAzBw.equals(expectedMinAzBw));
        assert(maxAzBw.equals(expectedMaxAzBw));
        assert(minZaBw.equals(expectedMinZaBw));
        assert(maxZaBw.equals(expectedMaxZaBw));

        Map<Instant, Integer> azBwMap = response.getAzBwAvailMap();
        Map<Instant, Integer> zaBwMap = response.getZaBwAvailMap();

        log.info(azBwMap.toString());
        log.info(azGoalMap.toString());
        assert(azBwMap.equals(azGoalMap));
        assert(zaBwMap.equals(zaGoalMap));
    }
}
