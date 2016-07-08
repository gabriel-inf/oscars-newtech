package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pce.TestEntityBuilder;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
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
import java.util.stream.Stream;

/**
 * Created by jeremy on 7/8/16.
 *
 * Tests End-to-End correctness of the PCE modules with Asymmetric bandwidth requirements
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class AsymmTopPceTest
{
    @Autowired
    private TopPCE topPCE;

    @Autowired
    private TestEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;

    private ScheduleSpecificationE requestedSched;

    List<UrnE> urnList;
    List<UrnAdjcyE> adjcyList;

    @Test
    public void asymmPceTest1()
    {
        log.info("Initializing test: 'asymmPceTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        Set<String> portNames = Stream.of("portA", "portZ").collect(Collectors.toSet());
        Integer azBW = 25;
        Integer zaBW = 50;
        String vlan = "any";

        topologyBuilder.buildTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, portNames, azBW, zaBW, vlan);

        log.info("Beginning test: 'asymmPceTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 1);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();

            int fix1InBW = fix1.getReservedBandwidth().getInBandwidth().intValue();
            int fix1EgBW = fix1.getReservedBandwidth().getEgBandwidth().intValue();
            int fix2InBW = fix2.getReservedBandwidth().getInBandwidth().intValue();
            int fix2EgBW = fix2.getReservedBandwidth().getEgBandwidth().intValue();

            log.info("fix1 URN: " + fix1.getIfceUrn().getUrn());
            log.info("fix2 URN: " + fix2.getIfceUrn().getUrn());
            log.info("fix1 In-B/W: " + fix1InBW);
            log.info("fix1 Eg-B/W: " + fix1EgBW);
            log.info("fix2 In-B/W: " + fix2InBW);
            log.info("fix2 Eg-B/W: " + fix2EgBW);

            assert((fix1InBW == azBW && fix1EgBW == zaBW) || (fix1EgBW == azBW && fix1InBW == zaBW));
            assert((fix2InBW == azBW && fix2EgBW == zaBW) || (fix2EgBW == azBW && fix2InBW == zaBW));

            assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("portZ"));
            assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("portZ"));
        }


        log.info("test 'asymmPceTest1' passed.");
    }

    @Test
    public void asymmPceTest2()
    {
        log.info("Initializing test: 'asymmPceTest2'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 3);

        allResJunctions.stream()
                .forEach(j -> {
                    assert(j.getDeviceUrn().getUrn().equals("nodeL") || j.getDeviceUrn().getUrn().equals("nodeM") || j.getDeviceUrn().getUrn().equals("nodeP"));

                    assert(j.getFixtures().size() == 2);

                    Iterator<ReservedVlanFixtureE> jIter = j.getFixtures().iterator();
                    ReservedVlanFixtureE fixA = jIter.next();
                    ReservedVlanFixtureE fixZ = jIter.next();
                    int fixAInBW = fixA.getReservedBandwidth().getInBandwidth().intValue();
                    int fixAEgBW = fixA.getReservedBandwidth().getEgBandwidth().intValue();
                    int fixZInBW = fixZ.getReservedBandwidth().getInBandwidth().intValue();
                    int fixZEgBW = fixZ.getReservedBandwidth().getEgBandwidth().intValue();

                    assert((fixAInBW == azBW && fixAEgBW == zaBW) || (fixAEgBW == azBW && fixAInBW == zaBW));
                    assert((fixZInBW == azBW && fixZEgBW == zaBW) || (fixZEgBW == azBW && fixZInBW == zaBW));

                    if(j.getDeviceUrn().getUrn().equals("nodeL"))
                    {
                        assert(fixA.getIfceUrn().getUrn().equals("nodeL:1") && fixZ.getIfceUrn().getUrn().equals("nodeL:2") || fixZ.getIfceUrn().getUrn().equals("nodeL:1") && fixA.getIfceUrn().getUrn().equals("nodeL:2"));
                    }
                    else if(j.getDeviceUrn().getUrn().equals("nodeM"))
                    {
                        assert(fixA.getIfceUrn().getUrn().equals("nodeM:1") && fixZ.getIfceUrn().getUrn().equals("portZ") || fixZ.getIfceUrn().getUrn().equals("nodeM:1") && fixA.getIfceUrn().getUrn().equals("portZ"));
                    }
                    else if(j.getDeviceUrn().getUrn().equals("nodeP"))
                    {
                        assert(fixA.getIfceUrn().getUrn().equals("portA") && fixZ.getIfceUrn().getUrn().equals("nodeP:1") || fixZ.getIfceUrn().getUrn().equals("portA") && fixA.getIfceUrn().getUrn().equals("nodeP:1"));
                    }
                });

        log.info("test 'asymmPceTest2' passed.");
    }

    @Test
    public void asymmPceTest3()
    {
        log.info("Initializing test: 'asymmPceTest3'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 1);
        assert(allResJunctions.size() == 1);

        ReservedVlanJunctionE onlyJunc = allResJunctions.iterator().next();
        Iterator<ReservedVlanFixtureE> iterF = onlyJunc.getFixtures().iterator();
        ReservedVlanFixtureE fixJ1 = iterF.next();
        ReservedVlanFixtureE fixJ2 = iterF.next();
        int fixJ1InBW = fixJ1.getReservedBandwidth().getInBandwidth().intValue();
        int fixJ1EgBW = fixJ1.getReservedBandwidth().getEgBandwidth().intValue();
        int fixJ2InBW = fixJ2.getReservedBandwidth().getInBandwidth().intValue();
        int fixJ2EgBW = fixJ2.getReservedBandwidth().getEgBandwidth().intValue();

        assert((fixJ1InBW == azBW && fixJ1EgBW == zaBW) || (fixJ1EgBW == azBW && fixJ1InBW == zaBW));
        assert((fixJ2InBW == azBW && fixJ2EgBW == zaBW) || (fixJ2EgBW == azBW && fixJ2InBW == zaBW));

        assert(onlyJunc.getDeviceUrn().getUrn().equals("nodeK"));

        assert(fixJ1.getIfceUrn().getUrn().equals("portA") || fixJ1.getIfceUrn().getUrn().equals("nodeK:1"));
        assert(fixJ2.getIfceUrn().getUrn().equals("portA") || fixJ2.getIfceUrn().getUrn().equals("nodeK:1"));

        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        ReservedVlanFixtureE fixA = juncA.getFixtures().iterator().next();
        ReservedVlanFixtureE fixZ = juncZ.getFixtures().iterator().next();
        int fixAInBW = fixA.getReservedBandwidth().getInBandwidth().intValue();
        int fixAEgBW = fixA.getReservedBandwidth().getEgBandwidth().intValue();
        int fixZInBW = fixZ.getReservedBandwidth().getInBandwidth().intValue();
        int fixZEgBW = fixZ.getReservedBandwidth().getEgBandwidth().intValue();

        assert((fixAInBW == azBW && fixAEgBW == zaBW) || (fixAEgBW == azBW && fixAInBW == zaBW));
        assert((fixZInBW == azBW && fixZEgBW == zaBW) || (fixZEgBW == azBW && fixZInBW == zaBW));

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("nodeP:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ-";
        String expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP-";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'asymmPceTest3' passed.");
    }

    @Test
    public void asymmPceTest4()
    {
        // Two possible shortest routes here!
        log.info("Initializing test: 'asymmPceTest4'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 1);
        assert(allResJunctions.size() == 2);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK") || oneJunc.getDeviceUrn().getUrn().equals("nodeL") || oneJunc.getDeviceUrn().getUrn().equals("nodeM"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();
            int fix1InBW = fix1.getReservedBandwidth().getInBandwidth().intValue();
            int fix1EgBW = fix1.getReservedBandwidth().getEgBandwidth().intValue();
            int fix2InBW = fix2.getReservedBandwidth().getInBandwidth().intValue();
            int fix2EgBW = fix2.getReservedBandwidth().getEgBandwidth().intValue();

            assert((fix1InBW == azBW && fix1EgBW == zaBW) || (fix1EgBW == azBW && fix1InBW == zaBW));
            assert((fix2InBW == azBW && fix2EgBW == zaBW) || (fix2EgBW == azBW && fix2InBW == zaBW));

            if(oneJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("nodeK:1") || fix1.getIfceUrn().getUrn().equals("nodeK:2"));
                assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("nodeK:1") || fix2.getIfceUrn().getUrn().equals("nodeK:2"));

                if(fix1.getIfceUrn().getUrn().equals("nodeK:1") || fix1.getIfceUrn().getUrn().equals("nodeK:2"))
                    assert(fix2.getIfceUrn().getUrn().equals("portA"));

                if(fix2.getIfceUrn().getUrn().equals("nodeK:1") || fix2.getIfceUrn().getUrn().equals("nodeK:2"))
                    assert(fix1.getIfceUrn().getUrn().equals("portA"));
            }
            else if(oneJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert(fix1.getIfceUrn().getUrn().equals("nodeL:1") || fix1.getIfceUrn().getUrn().equals("nodeL:3"));
                assert(fix2.getIfceUrn().getUrn().equals("nodeL:1") || fix2.getIfceUrn().getUrn().equals("nodeL:3"));
            }
            else
            {
                assert(fix1.getIfceUrn().getUrn().equals("nodeM:1") || fix1.getIfceUrn().getUrn().equals("nodeM:3"));
                assert(fix2.getIfceUrn().getUrn().equals("nodeM:1") || fix2.getIfceUrn().getUrn().equals("nodeM:3"));
            }
        }

        // Pipes
        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        ReservedVlanFixtureE fixA = juncA.getFixtures().iterator().next();
        ReservedVlanFixtureE fixZ = juncZ.getFixtures().iterator().next();
        int fixAInBW = fixA.getReservedBandwidth().getInBandwidth().intValue();
        int fixAEgBW = fixA.getReservedBandwidth().getEgBandwidth().intValue();
        int fixZInBW = fixZ.getReservedBandwidth().getInBandwidth().intValue();
        int fixZEgBW = fixZ.getReservedBandwidth().getEgBandwidth().intValue();

        assert((fixAInBW == azBW && fixAEgBW == zaBW) || (fixAEgBW == azBW && fixAInBW == zaBW));
        assert((fixZInBW == azBW && fixZEgBW == zaBW) || (fixZEgBW == azBW && fixZInBW == zaBW));

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP") || juncA.getDeviceUrn().getUrn().equals("nodeR"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("nodeP:1") || fixA.getIfceUrn().getUrn().equals("nodeR:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO1 = "nodeP-nodeP:2-nodeQ:1-nodeQ-";
        String expectedZaERO1 = "nodeQ-nodeQ:1-nodeP:2-nodeP-";
        String expectedAzERO2 = "nodeR-nodeR:3-nodeQ:2-nodeQ-";
        String expectedZaERO2 = "nodeQ-nodeQ:2-nodeR:3-nodeR-";

        assert(actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
        assert(actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));

        log.info("test 'asymmPceTest4' passed.");
    }

    @Test
    public void asymmPceTest5()
    {
        log.info("Initializing test: 'asymmPceTest5'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 1);
        assert(allResJunctions.size() == 2);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK") || oneJunc.getDeviceUrn().getUrn().equals("nodeS"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();
            int fix1InBW = fix1.getReservedBandwidth().getInBandwidth().intValue();
            int fix1EgBW = fix1.getReservedBandwidth().getEgBandwidth().intValue();
            int fix2InBW = fix2.getReservedBandwidth().getInBandwidth().intValue();
            int fix2EgBW = fix2.getReservedBandwidth().getEgBandwidth().intValue();

            assert((fix1InBW == azBW && fix1EgBW == zaBW) || (fix1EgBW == azBW && fix1InBW == zaBW));
            assert((fix2InBW == azBW && fix2EgBW == zaBW) || (fix2EgBW == azBW && fix2InBW == zaBW));

            if(oneJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("nodeK:2"));
                assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("nodeK:2"));
            }
            else
            {
                assert(fix1.getIfceUrn().getUrn().equals("nodeS:1") || fix1.getIfceUrn().getUrn().equals("portZ"));
                assert(fix2.getIfceUrn().getUrn().equals("nodeS:1") || fix2.getIfceUrn().getUrn().equals("portZ"));
            }
        }

        // Pipes
        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        ReservedVlanFixtureE fixA = juncA.getFixtures().iterator().next();
        ReservedVlanFixtureE fixZ = juncZ.getFixtures().iterator().next();
        int fixAInBW = fixA.getReservedBandwidth().getInBandwidth().intValue();
        int fixAEgBW = fixA.getReservedBandwidth().getEgBandwidth().intValue();
        int fixZInBW = fixZ.getReservedBandwidth().getInBandwidth().intValue();
        int fixZEgBW = fixZ.getReservedBandwidth().getEgBandwidth().intValue();

        assert((fixAInBW == azBW && fixAEgBW == zaBW) || (fixAEgBW == azBW && fixAInBW == zaBW));
        assert((fixZInBW == azBW && fixZEgBW == zaBW) || (fixZEgBW == azBW && fixZInBW == zaBW));

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("nodeP:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("nodeQ:3"));

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO = "nodeP-nodeP:3-nodeQ:1-nodeQ-";
        String expectedZaERO = "nodeQ-nodeQ:1-nodeP:3-nodeP-";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'asymmPceTest5' passed.");
    }

    @Test
    public void asymmPceTest6()
    {
        log.info("Initializing test: 'asymmPceTest6'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        Set<String> portNames = Stream.of("portA", "portZ").collect(Collectors.toSet());
        Integer azBW = 25;
        Integer zaBW = 50;
        String vlan = "any";

        topologyBuilder.buildTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, portNames, azBW, zaBW, vlan);

        log.info("Beginning test: 'asymmPceTest6'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 1);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeP"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();
            int fix1InBW = fix1.getReservedBandwidth().getInBandwidth().intValue();
            int fix1EgBW = fix1.getReservedBandwidth().getEgBandwidth().intValue();
            int fix2InBW = fix2.getReservedBandwidth().getInBandwidth().intValue();
            int fix2EgBW = fix2.getReservedBandwidth().getEgBandwidth().intValue();

            assert((fix1InBW == azBW && fix1EgBW == zaBW) || (fix1EgBW == azBW && fix1InBW == zaBW));
            assert((fix2InBW == azBW && fix2EgBW == zaBW) || (fix2EgBW == azBW && fix2InBW == zaBW));

            assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("portZ"));
            assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("portZ"));
        }

        log.info("test 'asymmPceTest6' passed.");
    }

    @Test
    public void asymmPceTest7()
    {
        log.info("Initializing test: 'asymmPceTest7'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeL";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo7();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest7'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 2);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK") || oneJunc.getDeviceUrn().getUrn().equals("nodeL"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();
            int fix1InBW = fix1.getReservedBandwidth().getInBandwidth().intValue();
            int fix1EgBW = fix1.getReservedBandwidth().getEgBandwidth().intValue();
            int fix2InBW = fix2.getReservedBandwidth().getInBandwidth().intValue();
            int fix2EgBW = fix2.getReservedBandwidth().getEgBandwidth().intValue();

            assert((fix1InBW == azBW && fix1EgBW == zaBW) || (fix1EgBW == azBW && fix1InBW == zaBW));
            assert((fix2InBW == azBW && fix2EgBW == zaBW) || (fix2EgBW == azBW && fix2InBW == zaBW));

            if(oneJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("nodeK:1"));
                assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("nodeK:1"));
            }
            else
            {
                assert(fix1.getIfceUrn().getUrn().equals("nodeL:1") || fix1.getIfceUrn().getUrn().equals("portZ"));
                assert(fix2.getIfceUrn().getUrn().equals("nodeL:1") || fix2.getIfceUrn().getUrn().equals("portZ"));
            }
        }

        log.info("test 'asymmPceTest7' passed.");
    }

    @Test
    public void asymmPceTest8()
    {
        log.info("Initializing test: 'asymmPceTest8'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo8();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest8'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 1);
        assert(allResJunctions.size() == 0);

        // Pipes
        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        ReservedVlanFixtureE fixA = juncA.getFixtures().iterator().next();
        ReservedVlanFixtureE fixZ = juncZ.getFixtures().iterator().next();
        int fixAInBW = fixA.getReservedBandwidth().getInBandwidth().intValue();
        int fixAEgBW = fixA.getReservedBandwidth().getEgBandwidth().intValue();
        int fixZInBW = fixZ.getReservedBandwidth().getInBandwidth().intValue();
        int fixZEgBW = fixZ.getReservedBandwidth().getEgBandwidth().intValue();

        assert((fixAInBW == azBW && fixAEgBW == zaBW) || (fixAEgBW == azBW && fixAInBW == zaBW));
        assert((fixZInBW == azBW && fixZEgBW == zaBW) || (fixZEgBW == azBW && fixZInBW == zaBW));

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("portA"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));
        
        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-";
        String expectedZaERO = "nodeQ-nodeQ:1-nodeP:1-nodeP-";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'asymmPceTest8' passed.");
    }

    @Test
    public void asymmPceTest9()
    {
        log.info("Initializing test: 'asymmPceTest9'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeP";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo9();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest9'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 2);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK") || oneJunc.getDeviceUrn().getUrn().equals("nodeP"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();
            int fix1InBW = fix1.getReservedBandwidth().getInBandwidth().intValue();
            int fix1EgBW = fix1.getReservedBandwidth().getEgBandwidth().intValue();
            int fix2InBW = fix2.getReservedBandwidth().getInBandwidth().intValue();
            int fix2EgBW = fix2.getReservedBandwidth().getEgBandwidth().intValue();

            assert((fix1InBW == azBW && fix1EgBW == zaBW) || (fix1EgBW == azBW && fix1InBW == zaBW));
            assert((fix2InBW == azBW && fix2EgBW == zaBW) || (fix2EgBW == azBW && fix2InBW == zaBW));

            if(oneJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("nodeK:1"));
                assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("nodeK:1"));
            }
            else
            {
                assert(fix1.getIfceUrn().getUrn().equals("nodeP:1") || fix1.getIfceUrn().getUrn().equals("portZ"));
                assert(fix2.getIfceUrn().getUrn().equals("nodeP:1") || fix2.getIfceUrn().getUrn().equals("portZ"));
            }
        }

        log.info("test 'asymmPceTest9' passed.");
    }

    @Test
    public void asymmPceTest10()
    {
        log.info("Initializing test: 'asymmPceTest10'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE > reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo10();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest10'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 3);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK") || oneJunc.getDeviceUrn().getUrn().equals("nodeL") || oneJunc.getDeviceUrn().getUrn().equals("nodeM"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();
            int fix1InBW = fix1.getReservedBandwidth().getInBandwidth().intValue();
            int fix1EgBW = fix1.getReservedBandwidth().getEgBandwidth().intValue();
            int fix2InBW = fix2.getReservedBandwidth().getInBandwidth().intValue();
            int fix2EgBW = fix2.getReservedBandwidth().getEgBandwidth().intValue();

            assert((fix1InBW == azBW && fix1EgBW == zaBW) || (fix1EgBW == azBW && fix1InBW == zaBW));
            assert((fix2InBW == azBW && fix2EgBW == zaBW) || (fix2EgBW == azBW && fix2InBW == zaBW));

            if(oneJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("nodeK:1"));
                assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("nodeK:1"));
            }
            else if(oneJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert(fix1.getIfceUrn().getUrn().equals("nodeL:1") || fix1.getIfceUrn().getUrn().equals("nodeL:2"));
                assert(fix2.getIfceUrn().getUrn().equals("nodeL:1") || fix2.getIfceUrn().getUrn().equals("nodeL:2"));
            }
            else
            {
                assert(fix1.getIfceUrn().getUrn().equals("nodeM:1") || fix1.getIfceUrn().getUrn().equals("portZ"));
                assert(fix2.getIfceUrn().getUrn().equals("nodeM:1") || fix2.getIfceUrn().getUrn().equals("portZ"));
            }
        }

        log.info("test 'asymmPceTest10' passed.");
    }

    @Test
    public void asymmPceTest11()
    {
        log.info("Initializing test: 'asymmPceTest11'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeR";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo11();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest11'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 1);
        assert(allResJunctions.size() == 0);

        // Pipes
        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        ReservedVlanFixtureE fixA = juncA.getFixtures().iterator().next();
        ReservedVlanFixtureE fixZ = juncZ.getFixtures().iterator().next();
        int fixAInBW = fixA.getReservedBandwidth().getInBandwidth().intValue();
        int fixAEgBW = fixA.getReservedBandwidth().getEgBandwidth().intValue();
        int fixZInBW = fixZ.getReservedBandwidth().getInBandwidth().intValue();
        int fixZEgBW = fixZ.getReservedBandwidth().getEgBandwidth().intValue();

        assert((fixAInBW == azBW && fixAEgBW == zaBW) || (fixAEgBW == azBW && fixAInBW == zaBW));
        assert((fixZInBW == azBW && fixZEgBW == zaBW) || (fixZEgBW == azBW && fixZInBW == zaBW));

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeR"));
        assert(fixA.getIfceUrn().getUrn().equals("portA"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeR:1-nodeR-";
        String expectedZaERO = "nodeR-nodeR:1-nodeQ:2-nodeQ-nodeQ:1-nodeP:1-nodeP-";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'asymmPceTest11' passed.");
    }

    @Test
    public void asymmPceTest12()
    {
        log.info("Initializing test: 'asymmPceTest12'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 50;
        Boolean palindrome = true;
        String vlan = "any";

        topologyBuilder.buildTopo12();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'asymmPceTest12'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 3);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK") || oneJunc.getDeviceUrn().getUrn().equals("nodeM") || oneJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();
            int fix1InBW = fix1.getReservedBandwidth().getInBandwidth().intValue();
            int fix1EgBW = fix1.getReservedBandwidth().getEgBandwidth().intValue();
            int fix2InBW = fix2.getReservedBandwidth().getInBandwidth().intValue();
            int fix2EgBW = fix2.getReservedBandwidth().getEgBandwidth().intValue();

            assert((fix1InBW == azBW && fix1EgBW == zaBW) || (fix1EgBW == azBW && fix1InBW == zaBW));
            assert((fix2InBW == azBW && fix2EgBW == zaBW) || (fix2EgBW == azBW && fix2InBW == zaBW));

            if(oneJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("nodeK:2"));
                assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("nodeK:2"));
            }
            else if(oneJunc.getDeviceUrn().getUrn().equals("nodeM"))
            {
                assert(fix1.getIfceUrn().getUrn().equals("nodeM:1") || fix1.getIfceUrn().getUrn().equals("nodeM:2"));
                assert(fix2.getIfceUrn().getUrn().equals("nodeM:1") || fix2.getIfceUrn().getUrn().equals("nodeM:2"));
            }
            else
            {
                assert(fix1.getIfceUrn().getUrn().equals("nodeQ:2") || fix1.getIfceUrn().getUrn().equals("portZ"));
                assert(fix2.getIfceUrn().getUrn().equals("nodeQ:2") || fix2.getIfceUrn().getUrn().equals("portZ"));
            }
        }

        log.info("test 'asymmPceTest12' passed.");
    }
}
