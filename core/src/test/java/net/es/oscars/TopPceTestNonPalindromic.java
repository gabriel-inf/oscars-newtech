package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pce.TestEntityBuilder;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.AsymmTopologyBuilder;
import net.es.oscars.topo.AsymmTopologyBuilder2;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.enums.Layer;
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
 * Tests End-to-End correctness of the Non-Palindromical PCE modules
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class TopPceTestNonPalindromic
{
    @Autowired
    private TopPCE topPCE;

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private TestEntityBuilder testBuilder;

    @Autowired
    private AsymmTopologyBuilder asymmTopologyBuilder;

    @Autowired
    private AsymmTopologyBuilder2 asymmTopologyBuilder2;

    @Autowired
    private TopologyBuilder topologyBuilder;

    private ScheduleSpecificationE requestedSched;

    @Test
    public void nonPalPceSymmTest1()
    {
        log.info("Initializing test: 'nonPalPceSymmTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 30;
        Integer zaBW = 30;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceSymmTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("test 'nonPalPceSymmTest1' passed.");
    }

    @Test
    public void nonPalPceSymmTest2()
    {
        log.info("Initializing test: 'nonPalPceSymmTest2.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeR";
        Integer azBW = 30;
        Integer zaBW = 30;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceSymmTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("test 'nonPalPceSymmTest2' passed.");
    }

    @Test
    public void nonPalPceSymmTest3()
    {
        log.info("Initializing test: 'nonPalPceSymmTest3.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 30;
        Integer zaBW = 30;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder2.buildAsymmTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceSymmTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("test 'nonPalPceSymmTest3' passed.");
    }

    @Test
    public void nonPalPceSymmTest4()
    {
        log.info("Initializing test: 'nonPalPceSymmTest4.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 30;
        Integer zaBW = 30;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder2.buildAsymmTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceSymmTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("test 'nonPalPceSymmTest4' passed.");
    }

    @Test
    public void nonPalPceSymmTest5()
    {
        log.info("Initializing test: 'nonPalPceSymmTest5.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 30;
        Integer zaBW = 30;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder2.buildAsymmTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceSymmTest5'.");

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

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeS"));
        assert(fixA.getIfceUrn().getUrn().equals("portA"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        assert(fixA.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getEgBandwidth().equals(azBW));
        assert(fixA.getReservedBandwidth().getEgBandwidth().equals(zaBW));
        assert(fixZ.getReservedBandwidth().getInBandwidth().equals(zaBW));

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeS:1-nodeS-";
        String expectedZaERO = "nodeS-nodeS:2-nodeR:2-nodeR-nodeR:1-nodeQ:3-nodeQ-nodeQ:1-nodeP:1-nodeP-";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'nonPalPceSymmTest5' passed.");
    }

    @Test
    public void nonPalPceSymmTest6()
    {
        log.info("Initializing test: 'nonPalPceSymmTest6.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 30;
        Integer zaBW = 30;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder2.buildAsymmTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceSymmTest6'.");

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

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();

            assert(fix1.getReservedBandwidth().getInBandwidth().equals(azBW) || fix1.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert(fix1.getReservedBandwidth().getEgBandwidth().equals(zaBW) || fix1.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert(fix2.getReservedBandwidth().getInBandwidth().equals(zaBW) || fix2.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert(fix2.getReservedBandwidth().getEgBandwidth().equals(azBW) || fix2.getReservedBandwidth().getInBandwidth().equals(azBW));

            assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("nodeK:1"));
            assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("nodeK:1"));
        }

        // Pipes
        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        ReservedVlanFixtureE fixA = juncA.getFixtures().iterator().next();
        ReservedVlanFixtureE fixZ = juncZ.getFixtures().iterator().next();

        assert(juncA.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeS"));
        assert(fixA.getIfceUrn().getUrn().equals("nodeQ:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        log.info("FixA In-BW: " + fixA.getReservedBandwidth().getInBandwidth());
        log.info("FixA Eg-BW: " + fixA.getReservedBandwidth().getEgBandwidth());
        log.info("FixZ In-BW: " + fixZ.getReservedBandwidth().getInBandwidth());
        log.info("FixZ Eg-BW: " + fixZ.getReservedBandwidth().getEgBandwidth());

        assert(fixA.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getEgBandwidth().equals(azBW));
        assert(fixA.getReservedBandwidth().getEgBandwidth().equals(zaBW));
        assert(fixZ.getReservedBandwidth().getInBandwidth().equals(zaBW));

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO = "nodeQ-nodeQ:2-nodeS:1-nodeS-";
        String expectedZaERO = "nodeS-nodeS:2-nodeR:2-nodeR-nodeR:1-nodeQ:3-nodeQ-";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'nonPalPceSymmTest6' passed.");
    }


    @Test
    public void nonPalPceAsymmTest1()
    {
        log.info("Initializing test: 'nonPalPceAsymmTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 50;
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceAsymmTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("test 'nonPalPceAsymmTest1' passed.");
    }

    @Test
    public void nonPalPceAsymmTest2()
    {
        log.info("Initializing test: 'nonPalPceAsymmTest2.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeR";
        Integer azBW = 50;
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceAsymmTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("test 'nonPalPceAsymmTest2' passed.");
    }

    @Test
    public void nonPalPceAsymmTest3()
    {
        log.info("Initializing test: 'nonPalPceAsymmTest3.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 50;
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceAsymmTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("test 'nonPalPceAsymmTest3' passed.");
    }

    @Test
    public void nonPalPceAsymmTest4()
    {
        log.info("Initializing test: 'nonPalPceAsymmTest4.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 50;
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceAsymmTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("test 'nonPalPceAsymmTest4' passed.");
    }

    @Test
    public void nonPalPceAsymmTest5()
    {
        log.info("Initializing test: 'nonPalPceAsymmTest5.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 50;
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceAsymmTest5'.");

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

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeS"));
        assert(fixA.getIfceUrn().getUrn().equals("portA"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        assert(fixA.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getEgBandwidth().equals(azBW));
        assert(fixA.getReservedBandwidth().getEgBandwidth().equals(zaBW));
        assert(fixZ.getReservedBandwidth().getInBandwidth().equals(zaBW));

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeS:1-nodeS";
        String expectedZaERO = "nodeS-nodeS:2-nodeR:2-nodeR-nodeR:1-nodeQ:3-nodeQ-nodeQ:1-nodeP:1-nodeP";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'nonPalPceAsymmTest5' passed.");
    }

    @Test
    public void nonPalPceAsymmTest6()
    {
        log.info("Initializing test: 'nonPalPceAsymmTest6.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeS";
        Integer azBW = 50;
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'nonPalPceAsymmTest6'.");

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

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();

            assert(fix1.getReservedBandwidth().getInBandwidth().equals(azBW) || fix1.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert(fix1.getReservedBandwidth().getEgBandwidth().equals(zaBW) || fix1.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert(fix2.getReservedBandwidth().getInBandwidth().equals(zaBW) || fix2.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert(fix2.getReservedBandwidth().getEgBandwidth().equals(azBW) || fix2.getReservedBandwidth().getInBandwidth().equals(azBW));

            assert(fix1.getIfceUrn().getUrn().equals("portA") || fix1.getIfceUrn().getUrn().equals("nodeK:1"));
            assert(fix2.getIfceUrn().getUrn().equals("portA") || fix2.getIfceUrn().getUrn().equals("nodeK:1"));
        }

        // Pipes
        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        ReservedVlanFixtureE fixA = juncA.getFixtures().iterator().next();
        ReservedVlanFixtureE fixZ = juncZ.getFixtures().iterator().next();

        assert(juncA.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeS"));
        assert(fixA.getIfceUrn().getUrn().equals("nodeQ:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        log.info("FixA In-BW: " + fixA.getReservedBandwidth().getInBandwidth());
        log.info("FixA Eg-BW: " + fixA.getReservedBandwidth().getEgBandwidth());
        log.info("FixZ In-BW: " + fixZ.getReservedBandwidth().getInBandwidth());
        log.info("FixZ Eg-BW: " + fixZ.getReservedBandwidth().getEgBandwidth());

        assert(fixA.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getEgBandwidth().equals(azBW));
        assert(fixA.getReservedBandwidth().getEgBandwidth().equals(zaBW));
        assert(fixZ.getReservedBandwidth().getInBandwidth().equals(zaBW));

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO = "nodeQ-nodeQ:2-nodeS:1-nodeS";
        String expectedZaERO = "nodeS-nodeS:2-nodeR:2-nodeR-nodeR:1-nodeQ:3-nodeQ-nodeQ:1-nodeK:1-nodeK";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        log.info("test 'nonPalPceAsymmTest6' passed.");
    }
    
    /* All of the following tests are copied from AsymmTopPceTest, but are passed through the non-palindromical PCE instead of the palindromical one. */

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
        Boolean palindrome = false;
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
        Boolean palindrome = false;
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
        Boolean palindrome = false;
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
        Boolean palindrome = false;
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
        Boolean palindrome = false;
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
        Boolean palindrome = false;
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
        Boolean palindrome = false;
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
        Boolean palindrome = false;
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
        Boolean palindrome = false;
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
        Boolean palindrome = false;
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

    /* All of the following tests are copied from TopPceTest, but are passed through the non-palindromical PCE instead of the palindromical one. */
    @Test
    public void basicPceTest2()
    {
        log.info("Initializing test: 'basicPceTest2'.");

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
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();
        List<ReservedBandwidthE> allResBWs = new ArrayList<>();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 3);

        allResJunctions.stream()
                .forEach(j -> {
                    assert(j.getDeviceUrn().getUrn().equals("nodeL") || j.getDeviceUrn().getUrn().equals("nodeM") || j.getDeviceUrn().getUrn().equals("nodeP"));

                    assert(j.getFixtures().size() == 2);

                    Iterator<ReservedVlanFixtureE> jIter = j.getFixtures().iterator();
                    ReservedVlanFixtureE fixA = jIter.next();
                    ReservedVlanFixtureE fixZ = jIter.next();

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

                    allResBWs.add(j.getFixtures().iterator().next().getReservedBandwidth());
                    allResBWs.add(j.getFixtures().iterator().next().getReservedBandwidth());
                });

        allResBWs.stream()
                .forEach(bw -> {
                    assert(bw.getInBandwidth().equals(bw.getEgBandwidth()));
                    assert(bw.getInBandwidth().equals(azBW));
                });

        log.info("test 'basicPceTest2' passed.");
    }

    @Test
    public void basicPceTest3()
    {
        log.info("Initializing test: 'basicPceTest3'.");

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
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest3'.");

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

        assert(onlyJunc.getDeviceUrn().getUrn().equals("nodeK"));

        assert(fixJ1.getIfceUrn().getUrn().equals("portA") || fixJ1.getIfceUrn().getUrn().equals("nodeK:1"));
        assert(fixJ2.getIfceUrn().getUrn().equals("portA") || fixJ2.getIfceUrn().getUrn().equals("nodeK:1"));

        assert(fixJ1.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixJ2.getReservedBandwidth().getInBandwidth().equals(azBW));

        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        ReservedVlanFixtureE fixA = juncA.getFixtures().iterator().next();
        ReservedVlanFixtureE fixZ = juncZ.getFixtures().iterator().next();

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("nodeP:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));


        assert(fixA.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixA.getReservedBandwidth().getEgBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getEgBandwidth().equals(azBW));

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

        log.info("test 'basicPceTest3' passed.");
    }

    @Test
    public void basicPceTest4()
    {
        // Two possible shortest routes here!
        log.info("Initializing test: 'basicPceTest4'.");

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
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest4'.");

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

            assert(fix1.getReservedBandwidth().getInBandwidth() == azBW);
            assert(fix2.getReservedBandwidth().getInBandwidth() == azBW);

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
        assert(fixA.getIfceUrn().getUrn().equals("nodeP:1") || fixA.getIfceUrn().getUrn().equals("nodeR:1"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        assert(fixA.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixA.getReservedBandwidth().getEgBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getEgBandwidth().equals(azBW));

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

        log.info("test 'basicPceTest4' passed.");
    }

    @Test
    public void basicPceTest5()
    {
        log.info("Initializing test: 'basicPceTest5'.");

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
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest5'.");

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

            assert(fix1.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert(fix2.getReservedBandwidth().getInBandwidth().equals(azBW));

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

        assert(fixA.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixA.getReservedBandwidth().getEgBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getEgBandwidth().equals(azBW));

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

        log.info("test 'basicPceTest5' passed.");
    }

    

    @Test
    public void basicPceTest7()
    {
        log.info("Initializing test: 'basicPceTest7'.");

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
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo7();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest7'.");

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

            assert(fix1.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert(fix2.getReservedBandwidth().getInBandwidth().equals(azBW));

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
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo8();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest8'.");

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

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA.getIfceUrn().getUrn().equals("portA"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        assert(fixA.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixA.getReservedBandwidth().getEgBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getEgBandwidth().equals(azBW));

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

        log.info("test 'basicPceTest8' passed.");
    }

    @Test
    public void basicPceTest9()
    {
        log.info("Initializing test: 'basicPceTest9'.");

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
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo9();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest9'.");

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

            assert(fix1.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert(fix2.getReservedBandwidth().getInBandwidth().equals(azBW));

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
        Optional<ReservedBlueprintE > reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeM";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo10();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest10'.");

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

            assert(fix1.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert(fix2.getReservedBandwidth().getInBandwidth().equals(azBW));

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
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeP";
        String dstPort = "portZ";
        String dstDevice = "nodeR";
        Integer azBW = 25;
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo11();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest11'.");

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

        assert(juncA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeR"));
        assert(fixA.getIfceUrn().getUrn().equals("portA"));
        assert(fixZ.getIfceUrn().getUrn().equals("portZ"));

        assert(fixA.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getInBandwidth().equals(azBW));
        assert(fixA.getReservedBandwidth().getEgBandwidth().equals(azBW));
        assert(fixZ.getReservedBandwidth().getEgBandwidth().equals(azBW));

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

        log.info("test 'basicPceTest11' passed.");
    }

    @Test
    public void basicPceTest12()
    {
        log.info("Initializing test: 'basicPceTest12'.");

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
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo12();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        log.info("Beginning test: 'basicPceTest12'.");

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

            assert(fix1.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert(fix2.getReservedBandwidth().getInBandwidth().equals(azBW));

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


    /* All of the following tests are copied from TopPceTest, but modify link metrics to evaluate NonPalindromicalPCE pathfinding in networks with sufficient B/W at all ingress/egress ports */
    @Test
    public void nonPalPceHighLinkCostTest1()
    {
        log.info("Initializing test: 'nonPalPceHighLinkCostTest1'.");

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
        Integer zaBW = 25;
        Boolean palindrome = false;
        String vlan = "any";

        topologyBuilder.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, vlan);

        // Modify link weights to *potentially* force NonPalindromical ERO computations //
        String linkSrc1 = "nodeM:1";
        String linkDst1 = "nodeL:2";

        adjcyRepo.findAll().stream()
                .filter(adj -> adj.getA().getUrn().equals(linkSrc1) && adj.getZ().getUrn().equals(linkDst1))
                .forEach(adj -> {
                    Map<Layer, Long> newMetric = new HashMap<>();
                    Long ethMetric = adj.getMetrics().get(Layer.ETHERNET);
                    Long mplsMetric = adj.getMetrics().get(Layer.MPLS);
                    if(ethMetric != null)
                        if(ethMetric > 0)
                            newMetric.put(Layer.ETHERNET, 400L);
                    if(mplsMetric != null)
                        if(mplsMetric > 0)
                        newMetric.put(Layer.MPLS, 400L);
                    adj.setMetrics(newMetric);
                });


        log.info("Beginning test: 'nonPalPceHighLinkCostTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResPipes = reservedFlow.getPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();
        List<ReservedBandwidthE> allResBWs = new ArrayList<>();

        assert(allResPipes.size() == 0);
        assert(allResJunctions.size() == 3);

        allResJunctions.stream()
                .forEach(j -> {
                    assert(j.getDeviceUrn().getUrn().equals("nodeL") || j.getDeviceUrn().getUrn().equals("nodeM") || j.getDeviceUrn().getUrn().equals("nodeP"));

                    assert(j.getFixtures().size() == 2);

                    Iterator<ReservedVlanFixtureE> jIter = j.getFixtures().iterator();
                    ReservedVlanFixtureE fixA = jIter.next();
                    ReservedVlanFixtureE fixZ = jIter.next();

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

                    allResBWs.add(j.getFixtures().iterator().next().getReservedBandwidth());
                    allResBWs.add(j.getFixtures().iterator().next().getReservedBandwidth());
                });

        allResBWs.stream()
                .forEach(bw -> {
                    assert(bw.getInBandwidth().equals(bw.getEgBandwidth()));
                    assert(bw.getInBandwidth().equals(azBW));
                });

        log.info("test 'nonPalPceHighLinkCostTest1' passed.");
    }

}
