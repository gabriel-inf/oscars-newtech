package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.helpers.TestEntityBuilder;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.VertexType;
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
 * Created by jeremy on 6/30/16.
 *
 * Tests End-to-End correctness of the PCE modules
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class TopPceTest
{
    @Autowired
    private TopPCE topPCE;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private TestEntityBuilder testBuilder;

    private ScheduleSpecificationE requestedSched;

    List<UrnE> urnList;
    List<UrnAdjcyE> adjcyList;

    @Test
    public void basicPceTest1()
    {
        log.info("Initializing test: 'basicPceTest1'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        Set<String> portNames = Stream.of("portA", "portZ").collect(Collectors.toSet());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";

        this.buildTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, portNames, azBW, zaBW, vlan);

        log.info("Beginning test: 'basicPceTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException pceE){ log.error("", pceE); }
        catch(PSSException pssE){ log.error("", pssE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

            assert(fix1.getReservedBandwidth().getInBandwidth() == azBW);
            assert(fix2.getReservedBandwidth().getInBandwidth() == azBW);

            assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("portZ"));
            assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("portZ"));
        }


        log.info("test 'basicPceTest1' passed.");
    }

    @Test
    public void basicPceTest2()
    {
        log.info("Initializing test: 'basicPceTest2'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();
        List<ReservedBandwidthE> allResBWs = new ArrayList<>();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 3);

        allResJunctions.stream()
                .forEach(j -> {
                    if(j.getDeviceUrn().equals("nodeL") || j.getDeviceUrn().equals("nodeM") || j.getDeviceUrn().equals("nodeP"))
                        assert(true);

                    assert(j.getFixtures().size() == 2);

                    Iterator<ReservedVlanFixtureE> jIter = j.getFixtures().iterator();
                    ReservedVlanFixtureE fixA = jIter.next();
                    ReservedVlanFixtureE fixZ = jIter.next();

                    if(j.getDeviceUrn().equals("nodeL"))
                    {
                        assert(fixA.getIfceUrn().equals("nodeL:1") && fixZ.getIfceUrn().equals("nodeL:2") || fixZ.getIfceUrn().equals("nodeL:1") && fixA.getIfceUrn().equals("nodeL:2"));
                    }
                    else if(j.getDeviceUrn().equals("nodeM"))
                    {
                        assert(fixA.getIfceUrn().equals("nodeM:1") && fixZ.getIfceUrn().equals("portZ") || fixZ.getIfceUrn().equals("nodeM:1") && fixA.getIfceUrn().equals("portZ"));
                    }
                    else if(j.getDeviceUrn().equals("nodeP"))
                    {
                        assert(fixA.getIfceUrn().equals("portA") && fixZ.getIfceUrn().equals("nodeP:1") || fixZ.getIfceUrn().equals("portA") && fixA.getIfceUrn().equals("nodeP:1"));
                    }

                    allResBWs.add(j.getFixtures().iterator().next().getReservedBandwidth());
                    allResBWs.add(j.getFixtures().iterator().next().getReservedBandwidth());
                });

        allResBWs.stream()
                .forEach(bw -> {
                    assert(bw.getInBandwidth() == bw.getEgBandwidth());
                    assert(bw.getInBandwidth() == azBW);
                });

        log.info("test 'basicPceTest2' passed.");
    }

    @Test
    public void basicPceTest3()
    {
        log.info("Initializing test: 'basicPceTest3'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResPipes.size() == 1);
        assert(allResJunctions.size() == 1);

        ReservedVlanJunctionE onlyJunc = allResJunctions.iterator().next();
        Iterator<ReservedVlanFixtureE> iterF = onlyJunc.getFixtures().iterator();
        ReservedVlanFixtureE fixJ1 = iterF.next();
        ReservedVlanFixtureE fixJ2 = iterF.next();

        assert(onlyJunc.getDeviceUrn().getUrn().equals("nodeK"));

        assert(fixJ1.getIfceUrn().getUrn().equals("portA") || fixJ1.getIfceUrn().getUrn().equals("nodeK:1"));
        assert(fixJ2.getIfceUrn().getUrn().equals("portA") || fixJ2.getIfceUrn().getUrn().equals("nodeK:1"));

        assert(fixJ1.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixJ2.getReservedBandwidth().getInBandwidth() == azBW);

        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        ReservedVlanFixtureE fixA = juncA.getFixtures().iterator().next();
        ReservedVlanFixtureE fixZ = juncZ.getFixtures().iterator().next();

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("nodeP:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));


        assert(fixA.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixA.getReservedBandwidth().getEgBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getEgBandwidth() == azBW);

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = juncA.getDeviceUrn() + "-";
        String actualZaERO = juncZ.getDeviceUrn() + "-";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        actualAzERO = actualAzERO + juncZ.getDeviceUrn();
        actualZaERO = actualZaERO + juncA.getDeviceUrn();

        String expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
        String expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'basicPceTest3' passed.");
    }

    @Test
    public void basicPceTest4()
    {
        // Two possible shortest routes here!
        log.info("Initializing test: 'basicPceTest4'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

            assert(fix1.getReservedBandwidth().getInBandwidth() == azBW);
            assert(fix2.getReservedBandwidth().getInBandwidth() == azBW);

            log.info("Junction URN: " + oneJunc.getDeviceUrn().getUrn());
            log.info("FixtureA URN: " + fix1.getIfceUrn().getUrn());
            log.info("FixtureZ URN: " + fix2.getIfceUrn().getUrn());

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

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP") || juncA.getDeviceUrn().getUrn().equals("nodeR"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("nodeP:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        assert(fixA.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixA.getReservedBandwidth().getEgBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getEgBandwidth() == azBW);

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = juncA.getDeviceUrn() + "-";
        String actualZaERO = juncZ.getDeviceUrn() + "-";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        actualAzERO = actualAzERO + juncZ.getDeviceUrn();
        actualZaERO = actualZaERO + juncA.getDeviceUrn();

        String expectedAzERO1 = "nodeP-nodeP:2-nodeQ:1-nodeQ";
        String expectedZaERO1 = "nodeQ-nodeQ:1-nodeP:2-nodeP";
        String expectedAzERO2 = "nodeR-nodeR:1-nodeQ:1-nodeQ";
        String expectedZaERO2 = "nodeQ-nodeQ:1-nodeR:1-nodeR";

        assert(actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
        assert(actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedAzERO2));

        log.info("test 'basicPceTest4' passed.");
    }

    @Test
    public void basicPceTest5()
    {
        log.info("Initializing test: 'basicPceTest5'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

            assert(fix1.getReservedBandwidth().getInBandwidth() == azBW);
            assert(fix2.getReservedBandwidth().getInBandwidth() == azBW);

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

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("nodeP:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("nodeQ:3"));

        assert(fixA.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixA.getReservedBandwidth().getEgBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getEgBandwidth() == azBW);

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = juncA.getDeviceUrn() + "-";
        String actualZaERO = juncZ.getDeviceUrn() + "-";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        actualAzERO = actualAzERO + juncZ.getDeviceUrn();
        actualZaERO = actualZaERO + juncA.getDeviceUrn();

        String expectedAzERO = "nodeP-nodeP:3-nodeQ:1-nodeQ";
        String expectedZaERO = "nodeQ-nodeQ:1-nodeP:3-nodeP";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'basicPceTest5' passed.");
    }

    @Test
    public void basicPceTest6()
    {
        log.info("Initializing test: 'basicPceTest6'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        Set<String> portNames = Stream.of("portA", "portZ").collect(Collectors.toSet());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";

        this.buildTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, portNames, azBW, zaBW, vlan);

        log.info("Beginning test: 'basicPceTest6'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

            assert(fix1.getReservedBandwidth().getInBandwidth() == azBW);
            assert(fix2.getReservedBandwidth().getInBandwidth() == azBW);

            assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("portZ"));
            assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("portZ"));
        }

        log.info("test 'basicPceTest6' passed.");
    }

    @Test
    public void basicPceTest7()
    {
        log.info("Initializing test: 'basicPceTest7'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeL";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo7();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest7'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

            assert(fix1.getReservedBandwidth().getInBandwidth() == azBW);
            assert(fix2.getReservedBandwidth().getInBandwidth() == azBW);

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

        log.info("test 'basicPceTest7' passed.");
    }

    @Test
    public void basicPceTest8()
    {
        log.info("Initializing test: 'basicPceTest8'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo8();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest8'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("portA"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        assert(fixA.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixA.getReservedBandwidth().getEgBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getEgBandwidth() == azBW);

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = juncA.getDeviceUrn() + "-";
        String actualZaERO = juncZ.getDeviceUrn() + "-";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        actualAzERO = actualAzERO + juncZ.getDeviceUrn();
        actualZaERO = actualZaERO + juncA.getDeviceUrn();

        String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ";
        String expectedZaERO = "nodeQ-nodeQ:1-nodeP:1-nodeP";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'basicPceTest8' passed.");
    }

    @Test
    public void basicPceTest9()
    {
        log.info("Initializing test: 'basicPceTest9'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeP";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo9();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest9'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

            assert(fix1.getReservedBandwidth().getInBandwidth() == azBW);
            assert(fix2.getReservedBandwidth().getInBandwidth() == azBW);

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

        log.info("test 'basicPceTest9' passed.");
    }

    @Test
    public void basicPceTest10()
    {
        log.info("Initializing test: 'basicPceTest10'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo10();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest10'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

            assert(fix1.getReservedBandwidth().getInBandwidth() == azBW);
            assert(fix2.getReservedBandwidth().getInBandwidth() == azBW);

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

        log.info("test 'basicPceTest10' passed.");
    }

    @Test
    public void basicPceTest11()
    {
        log.info("Initializing test: 'basicPceTest11'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeR";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo11();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest11'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeR"));
        assert(fixA.getIfceUrn().getUrn().equals("portA"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        assert(fixA.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixA.getReservedBandwidth().getEgBandwidth() == azBW);
        assert(fixZ.getReservedBandwidth().getEgBandwidth() == azBW);

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = juncA.getDeviceUrn() + "-";
        String actualZaERO = juncZ.getDeviceUrn() + "-";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        actualAzERO = actualAzERO + juncZ.getDeviceUrn();
        actualZaERO = actualZaERO + juncA.getDeviceUrn();

        String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeR:1-nodeR";
        String expectedZaERO = "nodeR-nodeR:1-nodeQ:2-nodeQ-nodeQ:1-nodeP:1-nodeP";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'basicPceTest11' passed.");
    }

    @Test
    public void basicPceTest12()
    {
        log.info("Initializing test: 'basicPceTest12'.");

        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint = null;
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = true;
        String vlan = "any";

        this.buildTopo12();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest12'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();

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

            assert(fix1.getReservedBandwidth().getInBandwidth() == azBW);
            assert(fix2.getReservedBandwidth().getInBandwidth() == azBW);

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

        log.info("test 'basicPceTest12' passed.");
    }


    private void buildTopo1()
    {
        log.info("Building Test Topology 1");

        urnRepo.deleteAll();
        adjcyRepo.deleteAll();

        urnList = new ArrayList<>();
        adjcyList = new ArrayList<>();

        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        portDeviceMap.put(portA, nodeK);
        portDeviceMap.put(portZ, nodeK);

        //Internal Links
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_K = new TopoEdge(portZ, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_Z = new TopoEdge(nodeK, portZ, 0L, Layer.INTERNAL);

        List<TopoVertex> topoNodes = new ArrayList<>();
        topoNodes.add(nodeK);
        topoNodes.add(portA);
        topoNodes.add(portZ);

        List<TopoEdge> topoLinks = new ArrayList<>();
        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_K);
        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_K_Z);


        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo2()
    {
        log.info("Building Test Topology 2");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeN = new TopoVertex("nodeN", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portN1 = new TopoVertex("nodeN:1", VertexType.PORT);
        TopoVertex portN2 = new TopoVertex("nodeN:2", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_M = new TopoEdge(portZ, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_Z = new TopoEdge(nodeM, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N1_N = new TopoEdge(portN1, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N2_N = new TopoEdge(portN2, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N1 = new TopoEdge(nodeN, portN1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N2 = new TopoEdge(nodeN, portN2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_L1_P1 = new TopoEdge(portL1, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_M1 = new TopoEdge(portL2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_N1 = new TopoEdge(portL3, portN1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_L2 = new TopoEdge(portM1, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_N2 = new TopoEdge(portM2, portN2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N1_L3 = new TopoEdge(portN1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N2_M2 = new TopoEdge(portN2, portM2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_L1 = new TopoEdge(portP1, portL1, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeN);
        topoNodes.add(nodeP);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portN1);
        topoNodes.add(portN2);
        topoNodes.add(portP1);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_M);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_N1_N);
        topoLinks.add(edgeInt_N2_N);
        topoLinks.add(edgeInt_P1_P);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_M_Z);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_N_N1);
        topoLinks.add(edgeInt_N_N2);
        topoLinks.add(edgeInt_P_P1);

        topoLinks.add(edgeEth_L1_P1);
        topoLinks.add(edgeEth_L2_M1);
        topoLinks.add(edgeEth_L3_N1);
        topoLinks.add(edgeEth_M1_L2);
        topoLinks.add(edgeEth_M2_N2);
        topoLinks.add(edgeEth_N1_L3);
        topoLinks.add(edgeEth_N2_M2);
        topoLinks.add(edgeEth_P1_L1);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo3()
    {
        log.info("Building Test Topology 3");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_P1 = new TopoEdge(portK1, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_K1 = new TopoEdge(portP1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P3_R1 = new TopoEdge(portP3, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R2 = new TopoEdge(portQ2, portR2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_P3 = new TopoEdge(portR1, portP3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_Q2 = new TopoEdge(portR2, portQ2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portR1);
        topoNodes.add(portR2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);

        topoLinks.add(edgeEth_K1_P1);
        topoLinks.add(edgeEth_P1_K1);
        topoLinks.add(edgeMpls_P2_Q1);
        topoLinks.add(edgeMpls_P3_R1);
        topoLinks.add(edgeMpls_Q1_P2);
        topoLinks.add(edgeMpls_Q2_R2);
        topoLinks.add(edgeMpls_R1_P3);
        topoLinks.add(edgeMpls_R2_Q2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo4()
    {
        log.info("Building Test Topology 4");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portM3 = new TopoVertex("nodeM:3", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portR3 = new TopoVertex("nodeR:3", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M3_M = new TopoEdge(portM3, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R3_R = new TopoEdge(portR3, nodeR, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M3 = new TopoEdge(nodeM, portM3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R3 = new TopoEdge(nodeR, portR3, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_K2_M1 = new TopoEdge(portK2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_M2 = new TopoEdge(portL2, portM2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_P1 = new TopoEdge(portL3, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_K2 = new TopoEdge(portM1, portK2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_L2 = new TopoEdge(portM2, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M3_R1 = new TopoEdge(portM3, portR1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_L3 = new TopoEdge(portP1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P3_R2 = new TopoEdge(portP3, portR2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R3 = new TopoEdge(portQ2, portR3, 100L, Layer.MPLS);
        TopoEdge edgeEth_R1_M3 = new TopoEdge(portR1, portM3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_R2_P3 = new TopoEdge(portR2, portP3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R3_Q2 = new TopoEdge(portR3, portQ2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portM3);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portR3);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_M3_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_R3_R);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_M_M3);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_R_R3);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_K2_M1);
        topoLinks.add(edgeEth_L1_K1);
        topoLinks.add(edgeEth_L2_M2);
        topoLinks.add(edgeEth_L3_P1);
        topoLinks.add(edgeEth_M1_K2);
        topoLinks.add(edgeEth_M2_L2);
        topoLinks.add(edgeEth_M3_R1);
        topoLinks.add(edgeEth_P1_L3);
        topoLinks.add(edgeMpls_P2_Q1);
        topoLinks.add(edgeMpls_P3_R2);
        topoLinks.add(edgeMpls_Q1_P2);
        topoLinks.add(edgeMpls_Q2_R3);
        topoLinks.add(edgeEth_R1_M3);
        topoLinks.add(edgeMpls_R2_P3);
        topoLinks.add(edgeMpls_R3_Q2);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo5()
    {
        log.info("Building Test Topology 5");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portM3 = new TopoVertex("nodeM:3", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portP3 = new TopoVertex("nodeP:3", VertexType.PORT);
        TopoVertex portP4 = new TopoVertex("nodeP:4", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portR3 = new TopoVertex("nodeR:3", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_S = new TopoEdge(portZ, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_Z = new TopoEdge(nodeS, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M3_M = new TopoEdge(portM3, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P3_P = new TopoEdge(portP3, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P4_P = new TopoEdge(portP4, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R3_R = new TopoEdge(portR3, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M3 = new TopoEdge(nodeM, portM3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P3 = new TopoEdge(nodeP, portP3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P4 = new TopoEdge(nodeP, portP4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R3 = new TopoEdge(nodeR, portR3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_M1 = new TopoEdge(portK1, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_K2_P1 = new TopoEdge(portK2, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_K1 = new TopoEdge(portM1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_P2 = new TopoEdge(portM2, portP2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M3_R1 = new TopoEdge(portM3, portR1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_K2 = new TopoEdge(portP1, portK2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P2_M2 = new TopoEdge(portP2, portM2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P3_Q1 = new TopoEdge(portP3, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_P4_R2 = new TopoEdge(portP4, portR2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P3 = new TopoEdge(portQ1, portP3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R3 = new TopoEdge(portQ2, portR3, 100L, Layer.MPLS);
        TopoEdge edgeEth_Q3_S1 = new TopoEdge(portQ3, portS1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_R1_M3 = new TopoEdge(portR1, portM3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_R2_P4 = new TopoEdge(portR2, portP4, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R3_Q2 = new TopoEdge(portR3, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeEth_S1_Q3 = new TopoEdge(portS1, portQ3, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portM3);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portP3);
        topoNodes.add(portP4);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portR3);
        topoNodes.add(portS1);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_S);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_M3_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_P3_P);
        topoLinks.add(edgeInt_P4_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_R3_R);
        topoLinks.add(edgeInt_S1_S);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_S_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_M_M3);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_P_P3);
        topoLinks.add(edgeInt_P_P4);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_R_R3);
        topoLinks.add(edgeInt_S_S1);

        topoLinks.add(edgeEth_K1_M1);
        topoLinks.add(edgeEth_K2_P1);
        topoLinks.add(edgeEth_M1_K1);
        topoLinks.add(edgeEth_M2_P2);
        topoLinks.add(edgeEth_M3_R1);
        topoLinks.add(edgeEth_P1_K2);
        topoLinks.add(edgeEth_P2_M2);
        topoLinks.add(edgeMpls_P3_Q1);
        topoLinks.add(edgeMpls_P4_R2);
        topoLinks.add(edgeMpls_Q1_P3);
        topoLinks.add(edgeMpls_Q2_R3);
        topoLinks.add(edgeEth_Q3_S1);
        topoLinks.add(edgeEth_R1_M3);
        topoLinks.add(edgeMpls_R2_P4);
        topoLinks.add(edgeMpls_R3_Q2);
        topoLinks.add(edgeEth_S1_Q3);

        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }


    private void buildTopo6()
    {
        log.info("Building Test Topology 6");

        urnRepo.deleteAll();
        adjcyRepo.deleteAll();

        urnList = new ArrayList<>();
        adjcyList = new ArrayList<>();

        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();
        portDeviceMap.put(portA, nodeP);
        portDeviceMap.put(portZ, nodeP);

        //Internal Links
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_P = new TopoEdge(portZ, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_Z = new TopoEdge(nodeP, portZ, 0L, Layer.INTERNAL);

        List<TopoVertex> topoNodes = new ArrayList<>();
        topoNodes.add(nodeP);
        topoNodes.add(portA);
        topoNodes.add(portZ);

        List<TopoEdge> topoLinks = new ArrayList<>();
        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_P);
        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_P_Z);

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo7()
    {
        log.info("Building Test Topology 7");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_L = new TopoEdge(portZ, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_Z = new TopoEdge(nodeL, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portL1);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_L);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_L1_L);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_L_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_L_L1);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_L1_K1);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo8()
    {
        log.info("Building Test Topology 8");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_P1_Q1 = new TopoEdge(portP1, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P1 = new TopoEdge(portQ1, portP1, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portP1);
        topoNodes.add(portQ1);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_Q1_Q);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_Q_Q1);

        topoLinks.add(edgeMpls_P1_Q1);
        topoLinks.add(edgeMpls_Q1_P1);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo9()
    {
        log.info("Building Test Topology 9");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_P = new TopoEdge(portZ, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_Z = new TopoEdge(nodeP, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_P1 = new TopoEdge(portK1, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_K1 = new TopoEdge(portP1, portK1, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeP);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portP1);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_P);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_P1_P);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_P_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_P_P1);

        topoLinks.add(edgeEth_K1_P1);
        topoLinks.add(edgeEth_P1_K1);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo10()
    {
        log.info("Building Test Topology 10");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeN = new TopoVertex("nodeN", VertexType.SWITCH);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portL3 = new TopoVertex("nodeL:3", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portN1 = new TopoVertex("nodeN:1", VertexType.PORT);
        TopoVertex portN2 = new TopoVertex("nodeN:2", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_M = new TopoEdge(portZ, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_Z = new TopoEdge(nodeM, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L3_L = new TopoEdge(portL3, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N1_N = new TopoEdge(portN1, nodeN, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N2_N = new TopoEdge(portN2, nodeN, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L3 = new TopoEdge(nodeL, portL3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N1 = new TopoEdge(nodeN, portN1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_N_N2 = new TopoEdge(nodeN, portN2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_M1 = new TopoEdge(portL2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L3_N1 = new TopoEdge(portL3, portN1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_L2 = new TopoEdge(portM1, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_N2 = new TopoEdge(portM2, portN2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N1_L3 = new TopoEdge(portN1, portL3, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_N2_M2 = new TopoEdge(portN2, portM2, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeN);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portL3);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portN1);
        topoNodes.add(portN2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_M);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_L3_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_N1_N);
        topoLinks.add(edgeInt_N2_N);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_M_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_L_L3);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_N_N1);
        topoLinks.add(edgeInt_N_N2);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_L1_K1);
        topoLinks.add(edgeEth_L2_M1);
        topoLinks.add(edgeEth_L3_N1);
        topoLinks.add(edgeEth_M1_L2);
        topoLinks.add(edgeEth_M2_N2);
        topoLinks.add(edgeEth_N1_L3);
        topoLinks.add(edgeEth_N2_M2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo11()
    {
        log.info("Building Test Topology 11");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);
        TopoVertex nodeR = new TopoVertex("nodeR", VertexType.ROUTER);
        TopoVertex nodeS = new TopoVertex("nodeS", VertexType.ROUTER);

        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);
        TopoVertex portQ3 = new TopoVertex("nodeQ:3", VertexType.PORT);
        TopoVertex portR1 = new TopoVertex("nodeR:1", VertexType.PORT);
        TopoVertex portR2 = new TopoVertex("nodeR:2", VertexType.PORT);
        TopoVertex portS1 = new TopoVertex("nodeS:1", VertexType.PORT);
        TopoVertex portS2 = new TopoVertex("nodeS:2", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_P = new TopoEdge(portA, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_R = new TopoEdge(portZ, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_A = new TopoEdge(nodeP, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_Z = new TopoEdge(nodeR, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q3_Q = new TopoEdge(portQ3, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R1_R = new TopoEdge(portR1, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R2_R = new TopoEdge(portR2, nodeR, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S1_S = new TopoEdge(portS1, nodeS, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S2_S = new TopoEdge(portS2, nodeS, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q3 = new TopoEdge(nodeQ, portQ3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R1 = new TopoEdge(nodeR, portR1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_R_R2 = new TopoEdge(nodeR, portR2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S1 = new TopoEdge(nodeS, portS1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_S_S2 = new TopoEdge(nodeS, portS2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeMpls_P1_Q1 = new TopoEdge(portP1, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P1 = new TopoEdge(portQ1, portP1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q2_R1 = new TopoEdge(portQ2, portR1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q3_S1 = new TopoEdge(portQ3, portS1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R1_Q2 = new TopoEdge(portR1, portQ2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_R2_S2 = new TopoEdge(portR2, portS2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S1_Q3 = new TopoEdge(portS1, portQ3, 100L, Layer.MPLS);
        TopoEdge edgeMpls_S2_R2 = new TopoEdge(portS2, portR2, 100L, Layer.MPLS);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);
        topoNodes.add(nodeR);
        topoNodes.add(nodeS);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portP1);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);
        topoNodes.add(portQ3);
        topoNodes.add(portR1);
        topoNodes.add(portR2);
        topoNodes.add(portS1);
        topoNodes.add(portS2);

        topoLinks.add(edgeInt_A_P);
        topoLinks.add(edgeInt_Z_R);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);
        topoLinks.add(edgeInt_Q3_Q);
        topoLinks.add(edgeInt_R1_R);
        topoLinks.add(edgeInt_R2_R);
        topoLinks.add(edgeInt_S1_S);
        topoLinks.add(edgeInt_S2_S);

        topoLinks.add(edgeInt_P_A);
        topoLinks.add(edgeInt_R_Z);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);
        topoLinks.add(edgeInt_Q_Q3);
        topoLinks.add(edgeInt_R_R1);
        topoLinks.add(edgeInt_R_R2);
        topoLinks.add(edgeInt_S_S1);
        topoLinks.add(edgeInt_S_S2);

        topoLinks.add(edgeMpls_P1_Q1);
        topoLinks.add(edgeMpls_Q1_P1);
        topoLinks.add(edgeMpls_Q2_R1);
        topoLinks.add(edgeMpls_Q3_S1);
        topoLinks.add(edgeMpls_R1_Q2);
        topoLinks.add(edgeMpls_R2_S2);
        topoLinks.add(edgeMpls_S1_Q3);
        topoLinks.add(edgeMpls_S2_R2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }

    private void buildTopo12()
    {
        log.info("Building Test Topology 12");

        Map<TopoVertex, TopoVertex> portDeviceMap = new HashMap<>();

        // Devices //
        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeL = new TopoVertex("nodeL", VertexType.SWITCH);
        TopoVertex nodeM = new TopoVertex("nodeM", VertexType.SWITCH);
        TopoVertex nodeP = new TopoVertex("nodeP", VertexType.ROUTER);
        TopoVertex nodeQ = new TopoVertex("nodeQ", VertexType.ROUTER);


        // Ports //
        TopoVertex portA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex portZ = new TopoVertex("portZ", VertexType.PORT);
        TopoVertex portK1 = new TopoVertex("nodeK:1", VertexType.PORT);
        TopoVertex portK2 = new TopoVertex("nodeK:2", VertexType.PORT);
        TopoVertex portL1 = new TopoVertex("nodeL:1", VertexType.PORT);
        TopoVertex portL2 = new TopoVertex("nodeL:2", VertexType.PORT);
        TopoVertex portM1 = new TopoVertex("nodeM:1", VertexType.PORT);
        TopoVertex portM2 = new TopoVertex("nodeM:2", VertexType.PORT);
        TopoVertex portP1 = new TopoVertex("nodeP:1", VertexType.PORT);
        TopoVertex portP2 = new TopoVertex("nodeP:2", VertexType.PORT);
        TopoVertex portQ1 = new TopoVertex("nodeQ:1", VertexType.PORT);
        TopoVertex portQ2 = new TopoVertex("nodeQ:2", VertexType.PORT);

        // End-Port Links //
        TopoEdge edgeInt_A_K = new TopoEdge(portA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_Q = new TopoEdge(portZ, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, portA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Z = new TopoEdge(nodeQ, portZ, 0L, Layer.INTERNAL);

        // Internal Links //
        TopoEdge edgeInt_K1_K = new TopoEdge(portK1, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K2_K = new TopoEdge(portK2, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L1_L = new TopoEdge(portL1, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L2_L = new TopoEdge(portL2, nodeL, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M1_M = new TopoEdge(portM1, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M2_M = new TopoEdge(portM2, nodeM, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P1_P = new TopoEdge(portP1, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P2_P = new TopoEdge(portP2, nodeP, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q1_Q = new TopoEdge(portQ1, nodeQ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q2_Q = new TopoEdge(portQ2, nodeQ, 0L, Layer.INTERNAL);

        // Internal-Reverse Links //
        TopoEdge edgeInt_K_K1 = new TopoEdge(nodeK, portK1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_K2 = new TopoEdge(nodeK, portK2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L1 = new TopoEdge(nodeL, portL1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_L_L2 = new TopoEdge(nodeL, portL2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M1 = new TopoEdge(nodeM, portM1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_M_M2 = new TopoEdge(nodeM, portM2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P1 = new TopoEdge(nodeP, portP1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_P_P2 = new TopoEdge(nodeP, portP2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q1 = new TopoEdge(nodeQ, portQ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Q_Q2 = new TopoEdge(nodeQ, portQ2, 0L, Layer.INTERNAL);

        // Network Links //
        TopoEdge edgeEth_K1_L1 = new TopoEdge(portK1, portL1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_K2_M1 = new TopoEdge(portK2, portM1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L1_K1 = new TopoEdge(portL1, portK1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_L2_P1 = new TopoEdge(portL2, portP1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M1_K2 = new TopoEdge(portM1, portK2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_M2_Q2 = new TopoEdge(portM2, portQ2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_P1_L2 = new TopoEdge(portP1, portL2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_P2_Q1 = new TopoEdge(portP2, portQ1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_Q1_P2 = new TopoEdge(portQ1, portP2, 100L, Layer.MPLS);
        TopoEdge edgeEth_Q2_M2 = new TopoEdge(portQ2, portM2, 100L, Layer.ETHERNET);


        List<TopoVertex> topoNodes = new ArrayList<>();
        List<TopoEdge> topoLinks = new ArrayList<>();

        topoNodes.add(nodeK);
        topoNodes.add(nodeL);
        topoNodes.add(nodeM);
        topoNodes.add(nodeP);
        topoNodes.add(nodeQ);

        topoNodes.add(portA);
        topoNodes.add(portZ);
        topoNodes.add(portK1);
        topoNodes.add(portK2);
        topoNodes.add(portL1);
        topoNodes.add(portL2);
        topoNodes.add(portM1);
        topoNodes.add(portM2);
        topoNodes.add(portP1);
        topoNodes.add(portP2);
        topoNodes.add(portQ1);
        topoNodes.add(portQ2);

        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_Q);
        topoLinks.add(edgeInt_K1_K);
        topoLinks.add(edgeInt_K2_K);
        topoLinks.add(edgeInt_L1_L);
        topoLinks.add(edgeInt_L2_L);
        topoLinks.add(edgeInt_M1_M);
        topoLinks.add(edgeInt_M2_M);
        topoLinks.add(edgeInt_P1_P);
        topoLinks.add(edgeInt_P2_P);
        topoLinks.add(edgeInt_Q1_Q);
        topoLinks.add(edgeInt_Q2_Q);

        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_Q_Z);
        topoLinks.add(edgeInt_K_K1);
        topoLinks.add(edgeInt_K_K2);
        topoLinks.add(edgeInt_L_L1);
        topoLinks.add(edgeInt_L_L2);
        topoLinks.add(edgeInt_M_M1);
        topoLinks.add(edgeInt_M_M2);
        topoLinks.add(edgeInt_P_P1);
        topoLinks.add(edgeInt_P_P2);
        topoLinks.add(edgeInt_Q_Q1);
        topoLinks.add(edgeInt_Q_Q2);

        topoLinks.add(edgeEth_K1_L1);
        topoLinks.add(edgeEth_K2_M1);
        topoLinks.add(edgeEth_L1_K1);
        topoLinks.add(edgeEth_L2_P1);
        topoLinks.add(edgeEth_M1_K2);
        topoLinks.add(edgeEth_M2_Q2);
        topoLinks.add(edgeEth_P1_L2);
        topoLinks.add(edgeMpls_P2_Q1);
        topoLinks.add(edgeMpls_Q1_P2);
        topoLinks.add(edgeEth_Q2_M2);


        // Map Ports to Devices for simplicity in utility class //
        for(TopoEdge oneEdge : topoLinks)
        {
            if(oneEdge.getLayer().equals(Layer.INTERNAL))
            {
                if(oneEdge.getA().getVertexType().equals(VertexType.PORT))
                {
                    portDeviceMap.put(oneEdge.getA(), oneEdge.getZ());
                }
            }
        }

        testBuilder.populateRepos(topoNodes, topoLinks, portDeviceMap);
    }
}
