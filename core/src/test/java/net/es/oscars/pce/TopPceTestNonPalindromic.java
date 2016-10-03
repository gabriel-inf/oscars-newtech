package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.AsymmTopologyBuilder;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.topo.enums.UrnType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


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
    private UrnRepository urnRepo;


    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private AsymmTopologyBuilder asymmTopologyBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;


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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        // Update URN in/eg bandwidths for this test
        urnRepo.findAll().stream()
                .filter(n -> n.getUrnType().equals(UrnType.IFCE))
                .forEach(n -> {
                    if(n.getReservableBandwidth().getIngressBw().equals(40))
                        n.getReservableBandwidth().setIngressBw(20);
                    else if(n.getReservableBandwidth().getEgressBw().equals(40))
                        n.getReservableBandwidth().setEgressBw(20);
                });

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        // Update URN in/eg bandwidths for this test
        urnRepo.findAll().stream()
                .filter(n -> n.getUrnType().equals(UrnType.IFCE))
                .forEach(n -> {
                    if(n.getReservableBandwidth().getIngressBw().equals(40))
                        n.getReservableBandwidth().setIngressBw(20);
                    else if(n.getReservableBandwidth().getEgressBw().equals(40))
                        n.getReservableBandwidth().setEgressBw(20);
                });

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        // Update URN in/eg bandwidths for this test
        urnRepo.findAll().stream()
                .filter(n -> n.getUrnType().equals(UrnType.IFCE))
                .forEach(n -> {
                    if(n.getReservableBandwidth().getIngressBw().equals(40))
                        n.getReservableBandwidth().setIngressBw(20);
                    else if(n.getReservableBandwidth().getEgressBw().equals(40))
                        n.getReservableBandwidth().setEgressBw(20);
                });

        log.info("Beginning test: 'nonPalPceSymmTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 1);

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeS:1-nodeS";
            String expectedZaERO = "nodeS-nodeS:2-nodeR:2-nodeR-nodeR:1-nodeQ:3-nodeQ-nodeQ:1-nodeP:1-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeS"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        // Update URN in/eg bandwidths for this test
        urnRepo.findAll().stream()
                .filter(n -> n.getUrnType().equals(UrnType.IFCE))
                .forEach(n -> {
                    if(n.getReservableBandwidth().getIngressBw().equals(40))
                        n.getReservableBandwidth().setIngressBw(20);
                    else if(n.getReservableBandwidth().getEgressBw().equals(40))
                        n.getReservableBandwidth().setEgressBw(20);
                });

        log.info("Beginning test: 'nonPalPceSymmTest6'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 0);

            ReservedVlanFixtureE theFix = aFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portA"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeQ-nodeQ:2-nodeS:1-nodeS";
            String expectedZaERO = "nodeS-nodeS:2-nodeR:2-nodeR-nodeR:1-nodeQ:3-nodeQ";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeS"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portZ"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'nonPalPceAsymmTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 1);

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeS:1-nodeS";
            String expectedZaERO = "nodeS-nodeS:2-nodeR:2-nodeR-nodeR:1-nodeQ:3-nodeQ-nodeQ:1-nodeP:1-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeS"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'nonPalPceAsymmTest6'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 0);

            ReservedVlanFixtureE theFix = aFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portA"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeQ-nodeQ:2-nodeS:1-nodeS";
            String expectedZaERO = "nodeS-nodeS:2-nodeR:2-nodeR-nodeR:1-nodeQ:3-nodeQ";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeS"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portZ"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        log.info("test 'nonPalPceAsymmTest6' passed.");
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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeP-nodeP:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeP:1-nodeP";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP";
            String expectedZaERO = "nodeP-nodeP:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 0);

            ReservedVlanFixtureE theFix = aFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portA"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portZ"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeP") || zJunc.getDeviceUrn().getUrn().equals("nodeR"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO1 = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedAzERO2 = "nodeK-nodeK:2-nodeM:1-nodeM";
                String expectedZaERO1 = "nodeL-nodeL:1-nodeK:1-nodeK";
                String expectedZaERO2 = "nodeM-nodeM:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
                assert (actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));
            }
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeL-nodeL:3-nodeP:1-nodeP";
                String expectedZaERO = "nodeP-nodeP:1-nodeL:3-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeM-nodeM:3-nodeR:1-nodeR";
                String expectedZaERO = "nodeR-nodeR:1-nodeM:3-nodeM";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeR"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeR"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                String expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeR-nodeR:3-nodeQ:2-nodeQ";
                String expectedZaERO = "nodeQ-nodeQ:2-nodeR:3-nodeR";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP") || zJunc.getDeviceUrn().getUrn().equals("nodeS"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:2-nodeP:1-nodeP";
                String expectedZaERO = "nodeP-nodeP:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeQ-nodeQ:3-nodeS:1-nodeS";
                String expectedZaERO = "nodeS-nodeS:1-nodeQ:3-nodeQ";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeS"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:3-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeP:3-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo7();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest7'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
            String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo8();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest8'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 1);

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeP:1-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo9();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest9'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP";
            String expectedZaERO = "nodeP-nodeP:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo10();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest10'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            log.info(aJunc.getDeviceUrn().getUrn());
            log.info(zJunc.getDeviceUrn().getUrn());
            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo11();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest11'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 1);

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeR:1-nodeR";
            String expectedZaERO = "nodeR-nodeR:1-nodeQ:2-nodeQ-nodeQ:1-nodeP:1-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeR"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo12();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'basicPceTest12'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        log.info(allResEthPipes.toString());
        log.info(allResMplsPipes.toString());
        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeM-nodeM:2-nodeQ:2-nodeQ";
                String expectedZaERO = "nodeQ-nodeQ:2-nodeM:2-nodeM";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'basicPceTest12' passed.");
    }

    /* All of the following tests are copied from TopPceTestAsymmetric, but are passed through the non-palindromical PCE instead of the palindromical one. */
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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeP-nodeP:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeP:1-nodeP";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP";
            String expectedZaERO = "nodeP-nodeP:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 0);

            ReservedVlanFixtureE theFix = aFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portA"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portZ"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeP") || zJunc.getDeviceUrn().getUrn().equals("nodeR"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO1 = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedAzERO2 = "nodeK-nodeK:2-nodeM:1-nodeM";
                String expectedZaERO1 = "nodeL-nodeL:1-nodeK:1-nodeK";
                String expectedZaERO2 = "nodeM-nodeM:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
                assert (actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));
            }
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeL-nodeL:3-nodeP:1-nodeP";
                String expectedZaERO = "nodeP-nodeP:1-nodeL:3-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeM-nodeM:3-nodeR:1-nodeR";
                String expectedZaERO = "nodeR-nodeR:1-nodeM:3-nodeM";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeR"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeR"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                String expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeR-nodeR:3-nodeQ:2-nodeQ";
                String expectedZaERO = "nodeQ-nodeQ:2-nodeR:3-nodeR";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP") || zJunc.getDeviceUrn().getUrn().equals("nodeS"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:2-nodeP:1-nodeP";
                String expectedZaERO = "nodeP-nodeP:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeQ-nodeQ:3-nodeS:1-nodeS";
                String expectedZaERO = "nodeS-nodeS:1-nodeQ:3-nodeQ";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeS"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:3-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeP:3-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo7();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest7'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
            String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo8();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest8'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 1);

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeP:1-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo9();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest9'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP";
            String expectedZaERO = "nodeP-nodeP:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo10();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest10'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo11();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest11'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 1);

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeR:1-nodeR";
            String expectedZaERO = "nodeR-nodeR:1-nodeQ:2-nodeQ-nodeQ:1-nodeP:1-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeR"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo12();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest12'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeM-nodeM:2-nodeQ:2-nodeQ";
                String expectedZaERO = "nodeQ-nodeQ:2-nodeM:2-nodeM";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'asymmPceTest12' passed.");
    }


    /* All of the following tests are copied from TopPceTest, but modify link metrics to evaluate NonPalindromicalPCE pathfinding in networks with sufficient B/W at all ingress/egress ports */
    @Test
    public void nonPalPceHighLinkCostTest2()
    {
        log.info("Initializing test: 'nonPalPceHighLinkCostTest2'.");

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

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


        log.info("Beginning test: 'nonPalPceHighLinkCostTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeP-nodeP:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeP:1-nodeP";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'nonPalPceHighLinkCostTest2' passed.");
    }

    @Test
    public void nonPalPceHighLinkCostTest3()
    {
        log.info("Initializing test: 'nonPalPceHighLinkCostTest3'.");

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        // Modify link weights to *potentially* force NonPalindromical ERO computations //
        String linkSrc1 = "nodeQ:1";
        String linkDst1 = "nodeP:2";

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


        log.info("Beginning test: 'nonPalPceHighLinkCostTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP";
            String expectedZaERO = "nodeP-nodeP:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 0);

            ReservedVlanFixtureE theFix = aFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portA"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:2-nodeR:2-nodeR-nodeR:1-nodeP:3-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portZ"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        log.info("test 'nonPalPceHighLinkCostTest3' passed.");
    }

    @Test
    public void nonPalPceHighLinkCostTest5()
    {
        log.info("Initializing test: 'nonPalPceHighLinkCostTest5'.");

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        // Modify link weights to *potentially* force NonPalindromical ERO computations //
        String linkSrc1 = "nodeQ:1";
        String linkDst1 = "nodeP:3";

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

        log.info("Beginning test: 'nonPalPceHighLinkCostTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP") || zJunc.getDeviceUrn().getUrn().equals("nodeS"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:2-nodeP:1-nodeP";
                String expectedZaERO = "nodeP-nodeP:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeQ-nodeQ:3-nodeS:1-nodeS";
                String expectedZaERO = "nodeS-nodeS:1-nodeQ:3-nodeQ";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeS"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:3-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:2-nodeR:3-nodeR-nodeR:2-nodeP:4-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        log.info("test 'nonPalPceHighLinkCostTest5' passed.");
    }

    @Test
    public void nonPalHighLinkCostTest10()
    {
        log.info("Initializing test: 'nonPalHighLinkCostTest10'.");

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo10();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

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

        log.info("Beginning test: 'nonPalHighLinkCostTest10'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'nonPalHighLinkCostTest10' passed.");
    }

    @Test
    public void nonPalHighLinkCostTest11()
    {
        log.info("Initializing test: 'nonPalHighLinkCostTest11'.");

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
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo11();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        // Modify link weights to *potentially* force NonPalindromical ERO computations //
        String linkSrc1 = "nodeR:1";
        String linkDst1 = "nodeQ:2";

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

        log.info("Beginning test: 'nonPalHighLinkCostTest11'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 1);

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeR:1-nodeR";
            String expectedZaERO = "nodeR-nodeR:2-nodeS:2-nodeS-nodeS:1-nodeQ:3-nodeQ-nodeQ:1-nodeP:1-nodeP";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeR"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        log.info("test 'nonPalHighLinkCostTest11' passed.");
    }


    @Test
    public void sharedLinkPceTest1()
    {
        log.info("Initializing test: 'sharedLinkPceTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeN";
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildSharedLinkTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'sharedLinkPceTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 3);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeN"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeL-nodeL:3-nodeM:2-nodeM";
                String expectedZaERO = "nodeM-nodeM:2-nodeL:3-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));

                UrnE portL3 = urnRepo.findByUrn("nodeL:3").get();
                UrnE portM2 = urnRepo.findByUrn("nodeM:2").get();

                ethPipe.getReservedBandwidths().stream()
                        .filter(bw -> bw.getUrn().equals(portL3) || bw.getUrn().equals(portM2))
                        .forEach(bw -> {
                            if(bw.getUrn().equals(portL3))
                            {
                                assert(bw.getInBandwidth().equals(zaBW));
                                assert(bw.getEgBandwidth().equals(azBW));
                            }
                            else
                            {
                                assert(bw.getInBandwidth().equals(azBW));
                                assert(bw.getEgBandwidth().equals(zaBW));
                            }
                        });
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeM-nodeM:3-nodeN:2-nodeN";
                String expectedZaERO = "nodeN-nodeN:2-nodeM:3-nodeM";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeN"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'sharedLinkPceTest2' passed.");
    }

    @Test
    public void sharedLinkPceTest2()
    {
        log.info("Initializing test: 'sharedLinkPceTest2'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeN";
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildSharedLinkTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'sharedLinkPceTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 1);

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL-nodeL:3-nodeM:2-nodeM-nodeM:3-nodeN:2-nodeN";
            String expectedZaERO = "nodeN-nodeN:1-nodeL:2-nodeL-nodeL:3-nodeM:2-nodeM-nodeM:1-nodeK:2-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeN"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));

            UrnE portL3 = urnRepo.findByUrn("nodeL:3").get();
            UrnE portM2 = urnRepo.findByUrn("nodeM:2").get();

            mplsPipe.getReservedBandwidths().stream()
                    .filter(bw -> bw.getUrn().equals(portL3) || bw.getUrn().equals(portM2))
                    .forEach(bw -> {
                        if(bw.getUrn().equals(portL3))
                        {
                            assert(bw.getInBandwidth().equals(0));
                            assert(bw.getEgBandwidth().equals(azBW + zaBW));
                        }
                        else
                        {
                            assert(bw.getInBandwidth().equals(azBW + zaBW));
                            assert(bw.getEgBandwidth().equals(0));
                        }
                    });
        }

        log.info("test 'sharedLinkPceTest2' passed.");
    }

    @Test
    public void sharedLinkPceTest3()
    {
        log.info("Initializing test: 'sharedLinkPceTest3'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeN";
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildSharedLinkTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'sharedLinkPceTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 2);
        assert(allResMplsPipes.size() == 1);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeN"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeM-nodeM:3-nodeN:2-nodeN";
                String expectedZaERO = "nodeN-nodeN:2-nodeM:3-nodeM";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeN"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeL-nodeL:3-nodeM:2-nodeM";
            String expectedZaERO = "nodeM-nodeM:2-nodeL:3-nodeL";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));

            UrnE portL3 = urnRepo.findByUrn("nodeL:3").get();
            UrnE portM2 = urnRepo.findByUrn("nodeM:2").get();

            mplsPipe.getReservedBandwidths().stream()
                    .filter(bw -> bw.getUrn().equals(portL3) || bw.getUrn().equals(portM2))
                    .forEach(bw -> {
                        if(bw.getUrn().equals(portL3))
                        {
                            assert(bw.getInBandwidth().equals(zaBW));
                            assert(bw.getEgBandwidth().equals(azBW));
                        }
                        else
                        {
                            assert(bw.getInBandwidth().equals(azBW));
                            assert(bw.getEgBandwidth().equals(zaBW));
                        }
                    });
        }

        log.info("test 'sharedLinkPceTest3' passed.");
    }

    @Test
    public void sharedLinkPceTest4()
    {
        log.info("Initializing test: 'sharedLinkPceTest4'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeN";
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildSharedLinkTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'sharedLinkPceTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 3);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeN"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeL-nodeL:3-nodeM:2-nodeM";
                String expectedZaERO = "nodeM-nodeM:2-nodeL:3-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));

                UrnE portL3 = urnRepo.findByUrn("nodeL:3").get();
                UrnE portM2 = urnRepo.findByUrn("nodeM:2").get();

                ethPipe.getReservedBandwidths().stream()
                        .filter(bw -> bw.getUrn().equals(portL3) || bw.getUrn().equals(portM2))
                        .forEach(bw -> {
                            if(bw.getUrn().equals(portL3))
                            {
                                assert(bw.getInBandwidth().equals(zaBW));
                                assert(bw.getEgBandwidth().equals(azBW));
                            }
                            else
                            {
                                assert(bw.getInBandwidth().equals(azBW));
                                assert(bw.getEgBandwidth().equals(zaBW));
                            }
                        });
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeM-nodeM:3-nodeN:2-nodeN";
                String expectedZaERO = "nodeN-nodeN:2-nodeM:3-nodeM";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeN"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'sharedLinkPceTest4' passed.");
    }

    @Test
    public void multiFixtureTest()
    {
        log.info("Initializing test: 'multiFixtureTest'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        List<String> srcPorts = Arrays.asList("portA", "portB", "portC");
        List<String> dstPorts = Arrays.asList("portX", "portY", "portZ");
        String srcDevice = "nodeK";
        String dstDevice = "nodeL";
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo7MultiFix();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'multiFixtureTest'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 0);
        assert(allResEthPipes.size() == 1);
        assert(allResMplsPipes.size() == 0);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
            String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (aFixes.size() == 3);
            assert (zFixes.size() == 3);


            assert(aFixes
                    .stream()
                    .filter(f -> f.getIfceUrn().getUrn().equals("portA")
                            || f.getIfceUrn().getUrn().equals("portB")
                            || f.getIfceUrn().getUrn().equals("portC"))
                    .filter(f -> f.getReservedBandwidth().getInBandwidth().equals(azBW))
                    .filter(f -> f.getReservedBandwidth().getEgBandwidth().equals(zaBW))
                    .count() == 3);

            assert(zFixes
                    .stream()
                    .filter(f -> f.getIfceUrn().getUrn().equals("portX")
                            || f.getIfceUrn().getUrn().equals("portY")
                            || f.getIfceUrn().getUrn().equals("portZ"))
                    .filter(f -> f.getReservedBandwidth().getInBandwidth().equals(zaBW))
                    .filter(f -> f.getReservedBandwidth().getEgBandwidth().equals(azBW))
                    .count() == 3);

            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        log.info("test 'multiFixtureTest' passed.");
    }

    @Test
    public void multiMplsPipeTestPal()
    {
        log.info("Initializing test: 'multiMplsPipeTestPal'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        List<String> srcPorts = Arrays.asList("portA");
        List<String> dstPorts = Arrays.asList("portZ");
        String srcDevice = "nodeK";
        String dstDevice = "nodeT";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildMultiMplsTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'multiMplsPipeTestPal'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 3);
        assert (allResMplsPipes.size() == 2);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            log.info("Eth Pipe: " + ethPipe.getAJunction().getDeviceUrn().getUrn() + "----" + ethPipe.getZJunction().getDeviceUrn().getUrn());

            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP") || zJunc.getDeviceUrn().getUrn().equals("nodeQ") || zJunc.getDeviceUrn().getUrn().equals("nodeS"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);

            String expectedAzERO;
            String expectedZaERO;

            if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeP"));
                expectedAzERO = "nodeL-nodeL:3-nodeP:1-nodeP";
                expectedZaERO = "nodeP-nodeP:1-nodeL:3-nodeL";
            }
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";
            }
            else
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeS"));
                expectedAzERO = "nodeQ-nodeQ:3-nodeS:1-nodeS";
                expectedZaERO = "nodeS-nodeS:1-nodeQ:3-nodeQ";
            }

            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert ((aJunc.getDeviceUrn().getUrn().equals("nodeK") && zJunc.getDeviceUrn().getUrn().equals("nodeL"))
                    || (aJunc.getDeviceUrn().getUrn().equals("nodeS") && zJunc.getDeviceUrn().getUrn().equals("nodeT")));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));

                String expectedAzERO = "nodeS-nodeS:2-nodeT:1-nodeT";
                String expectedZaERO = "nodeT-nodeT:1-nodeS:2-nodeS";

                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'multiMplsPipeTestPal' passed.");
    }

    @Test
    public void multiMplsPipeTestPalHighBW()
    {
        log.info("Initializing test: 'multiMplsPipeTestPalHighBW'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        List<String> srcPorts = Arrays.asList("portA");
        List<String> dstPorts = Arrays.asList("portZ");
        String srcDevice = "nodeK";
        String dstDevice = "nodeT";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildMultiMplsTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'multiMplsPipeTestPalHighBW'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 3);
        assert (allResMplsPipes.size() == 2);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP") || zJunc.getDeviceUrn().getUrn().equals("nodeQ") || zJunc.getDeviceUrn().getUrn().equals("nodeS"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);

            String expectedAzERO;
            String expectedZaERO;

            if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeP"));
                expectedAzERO = "nodeL-nodeL:3-nodeP:1-nodeP";
                expectedZaERO = "nodeP-nodeP:1-nodeL:3-nodeL";
            }
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";
            }
            else
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeS"));
                expectedAzERO = "nodeQ-nodeQ:3-nodeS:1-nodeS";
                expectedZaERO = "nodeS-nodeS:1-nodeQ:3-nodeQ";
            }

            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert ((aJunc.getDeviceUrn().getUrn().equals("nodeK") && zJunc.getDeviceUrn().getUrn().equals("nodeL"))
                    || (aJunc.getDeviceUrn().getUrn().equals("nodeS") && zJunc.getDeviceUrn().getUrn().equals("nodeT")));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));

                String expectedAzERO = "nodeK-nodeK:2-nodeM:1-nodeM-nodeM:2-nodeL:2-nodeL";
                String expectedZaERO = "nodeL-nodeL:2-nodeM:2-nodeM-nodeM:1-nodeK:2-nodeK";

                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));

                String expectedAzERO = "nodeS-nodeS:3-nodeU:1-nodeU-nodeU:2-nodeT:2-nodeT";
                String expectedZaERO = "nodeT-nodeT:2-nodeU:2-nodeU-nodeU:1-nodeS:3-nodeS";

                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'multiMplsPipeTestPalHighBW' passed.");
    }

    @Test
    public void multiMplsPipeTestNonPal()
    {
        log.info("Initializing test: 'multiMplsPipeTestNonPal'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        List<String> srcPorts = Arrays.asList("portA");
        List<String> dstPorts = Arrays.asList("portZ");
        String srcDevice = "nodeK";
        String dstDevice = "nodeT";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        asymmTopologyBuilder.buildMultiMplsTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'multiMplsPipeTestNonPal'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 3);
        assert (allResMplsPipes.size() == 2);

        // Ethernet Pipes
        for(ReservedEthPipeE ethPipe : allResEthPipes)
        {
            ReservedVlanJunctionE aJunc = ethPipe.getAJunction();
            ReservedVlanJunctionE zJunc = ethPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = ethPipe.getAzERO();
            List<String> zaERO = ethPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP") || zJunc.getDeviceUrn().getUrn().equals("nodeQ") || zJunc.getDeviceUrn().getUrn().equals("nodeS"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);

            String expectedAzERO;
            String expectedZaERO;

            if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeP"));
                expectedAzERO = "nodeL-nodeL:3-nodeP:1-nodeP";
                expectedZaERO = "nodeP-nodeP:1-nodeL:3-nodeL";
            }
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";
            }
            else
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeS"));
                expectedAzERO = "nodeQ-nodeQ:3-nodeS:1-nodeS";
                expectedZaERO = "nodeS-nodeS:1-nodeQ:3-nodeQ";
            }

            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Mpls Pipes
        for(ReservedMplsPipeE mplsPipe : allResMplsPipes)
        {
            ReservedVlanJunctionE aJunc = mplsPipe.getAJunction();
            ReservedVlanJunctionE zJunc = mplsPipe.getZJunction();
            Set<ReservedVlanFixtureE> aFixes = aJunc.getFixtures();
            Set<ReservedVlanFixtureE> zFixes = zJunc.getFixtures();
            List<String> azERO = mplsPipe.getAzERO();
            List<String> zaERO = mplsPipe.getZaERO();
            String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

            for(String x : azERO)
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert ((aJunc.getDeviceUrn().getUrn().equals("nodeK") && zJunc.getDeviceUrn().getUrn().equals("nodeL"))
                    || (aJunc.getDeviceUrn().getUrn().equals("nodeS") && zJunc.getDeviceUrn().getUrn().equals("nodeT")));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:2-nodeM:2-nodeM-nodeM:1-nodeK:2-nodeK";

                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));

                String expectedAzERO = "nodeS-nodeS:2-nodeT:1-nodeT";
                String expectedZaERO = "nodeT-nodeT:2-nodeU:2-nodeU-nodeU:1-nodeS:3-nodeS";

                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'multiMplsPipeTestNonPal' passed.");
    }
}
