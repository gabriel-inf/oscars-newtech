package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.dto.spec.PalindromicType;
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
public class TopPceTestAsymmetric
{
    @Autowired
    private TopPCE topPCE;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;


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
        List<String> portNames = Stream.of("portA", "portZ").collect(Collectors.toList());
        Integer azBW = 50;
        Integer zaBW = 25;
        String vlan = "any";

        topologyBuilder.buildTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, portNames, azBW, zaBW, vlan);

        log.info("Beginning test: 'asymmPceTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 1);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 0);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().equals("nodeK"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();


            assert(fix1.getIfceUrn().equals("portA") || fix1.getIfceUrn().equals("portZ"));
            assert(fix2.getIfceUrn().equals("portA") || fix2.getIfceUrn().equals("portZ"));

            if(fix1.getIfceUrn().equals("portA")){
                assert(Objects.equals(fix1.getReservedBandwidth().getInBandwidth(), azBW));
                assert(Objects.equals(fix2.getReservedBandwidth().getInBandwidth(), azBW));

                assert(Objects.equals(fix1.getReservedBandwidth().getEgBandwidth(), zaBW));
                assert(Objects.equals(fix2.getReservedBandwidth().getEgBandwidth(), zaBW));
            }
            else{
                assert(Objects.equals(fix1.getReservedBandwidth().getInBandwidth(), azBW));
                assert(Objects.equals(fix2.getReservedBandwidth().getInBandwidth(), azBW));

                assert(Objects.equals(fix1.getReservedBandwidth().getEgBandwidth(), zaBW));
                assert(Objects.equals(fix2.getReservedBandwidth().getEgBandwidth(), zaBW));
            }


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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().equals("nodeP") || aJunc.getDeviceUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().equals("nodeL") || zJunc.getDeviceUrn().equals("nodeM"));

            if(aJunc.getDeviceUrn().equals("nodeP"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeP-nodeP:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeP:1-nodeP";

                assert (zJunc.getDeviceUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().equals("portA"));
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

                assert (zJunc.getDeviceUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().equals("portZ"));
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP";
            String expectedZaERO = "nodeP-nodeP:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().equals("nodeP"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 0);

            ReservedVlanFixtureE theFix = aFixes.iterator().next();

            assert (theFix.getIfceUrn().equals("portA"));
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";

            assert (aJunc.getDeviceUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().equals("nodeQ"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            assert (theFix.getIfceUrn().equals("portZ"));
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().equals("nodeK") || aJunc.getDeviceUrn().equals("nodeL") || aJunc.getDeviceUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().equals("nodeL") || zJunc.getDeviceUrn().equals("nodeM") || zJunc.getDeviceUrn().equals("nodeP") || zJunc.getDeviceUrn().equals("nodeR"));

            if(aJunc.getDeviceUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO1 = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedAzERO2 = "nodeK-nodeK:2-nodeM:1-nodeM";
                String expectedZaERO1 = "nodeL-nodeL:1-nodeK:1-nodeK";
                String expectedZaERO2 = "nodeM-nodeM:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().equals("nodeL") || zJunc.getDeviceUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
                assert (actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));
            }
            else if(aJunc.getDeviceUrn().equals("nodeL"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeL-nodeL:3-nodeP:1-nodeP";
                String expectedZaERO = "nodeP-nodeP:1-nodeL:3-nodeL";

                assert (zJunc.getDeviceUrn().equals("nodeP"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 0);

                String expectedAzERO = "nodeM-nodeM:3-nodeR:1-nodeR";
                String expectedZaERO = "nodeR-nodeR:1-nodeM:3-nodeM";

                assert (zJunc.getDeviceUrn().equals("nodeR"));
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().equals("nodeP") || aJunc.getDeviceUrn().equals("nodeR"));
            assert (zJunc.getDeviceUrn().equals("nodeQ"));

            if(aJunc.getDeviceUrn().equals("nodeP"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                String expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";

                assert (zJunc.getDeviceUrn().equals("nodeQ"));
                assert (theFix.getIfceUrn().equals("portZ"));
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

                assert (zJunc.getDeviceUrn().equals("nodeQ"));
                assert (theFix.getIfceUrn().equals("portZ"));
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().equals("nodeK") || aJunc.getDeviceUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().equals("nodeP") || zJunc.getDeviceUrn().equals("nodeS"));

            if(aJunc.getDeviceUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:2-nodeP:1-nodeP";
                String expectedZaERO = "nodeP-nodeP:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().equals("nodeP"));
                assert (theFix.getIfceUrn().equals("portA"));
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

                assert (zJunc.getDeviceUrn().equals("nodeS"));
                assert (theFix.getIfceUrn().equals("portZ"));
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:3-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeP:3-nodeP";

            assert (aJunc.getDeviceUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().equals("nodeQ"));
            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

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
        List<String> portNames = Stream.of("portA", "portZ").collect(Collectors.toList());
        Integer azBW = 50;
        Integer zaBW = 25;
        String vlan = "any";

        topologyBuilder.buildTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, portNames, azBW, zaBW, vlan);

        log.info("Beginning test: 'asymmPceTest6'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedVlanFlowE reservedFlow = reservedBlueprint.get().getVlanFlow();

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

        assert(allResJunctions.size() == 1);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 0);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().equals("nodeP"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();

            assert(fix1.getIfceUrn().equals("portA") || fix1.getIfceUrn().equals("portZ"));
            assert(fix2.getIfceUrn().equals("portA") || fix2.getIfceUrn().equals("portZ"));

            if(fix1.getIfceUrn().equals("portA")){
                assert(Objects.equals(fix1.getReservedBandwidth().getInBandwidth(), azBW));
                assert(Objects.equals(fix2.getReservedBandwidth().getInBandwidth(), azBW));

                assert(Objects.equals(fix1.getReservedBandwidth().getEgBandwidth(), zaBW));
                assert(Objects.equals(fix2.getReservedBandwidth().getEgBandwidth(), zaBW));
            }
            else{
                assert(Objects.equals(fix1.getReservedBandwidth().getInBandwidth(), azBW));
                assert(Objects.equals(fix2.getReservedBandwidth().getInBandwidth(), azBW));

                assert(Objects.equals(fix1.getReservedBandwidth().getEgBandwidth(), zaBW));
                assert(Objects.equals(fix2.getReservedBandwidth().getEgBandwidth(), zaBW));
            }
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
        Integer azBW = 50;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo7();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest7'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
            String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().equals("nodeL"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().equals("portZ"));
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo8();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest8'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ";
            String expectedZaERO = "nodeQ-nodeQ:1-nodeP:1-nodeP";

            assert (aJunc.getDeviceUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().equals("nodeQ"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().equals("portZ"));
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo9();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest9'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP";
            String expectedZaERO = "nodeP-nodeP:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().equals("nodeP"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().equals("portZ"));
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo10();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest10'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().equals("nodeK") || aJunc.getDeviceUrn().equals("nodeL"));
            assert (zJunc.getDeviceUrn().equals("nodeL") || zJunc.getDeviceUrn().equals("nodeM"));

            if(aJunc.getDeviceUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

                assert (zJunc.getDeviceUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().equals("portA"));
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

                assert (zJunc.getDeviceUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().equals("portZ"));
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo11();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest11'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeR:1-nodeR";
            String expectedZaERO = "nodeR-nodeR:1-nodeQ:2-nodeQ-nodeQ:1-nodeP:1-nodeP";

            assert (aJunc.getDeviceUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().equals("nodeR"));
            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);

            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().equals("portA"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getIfceUrn().equals("portZ"));
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopo12();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'asymmPceTest12'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().equals("nodeK") || aJunc.getDeviceUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().equals("nodeM") || zJunc.getDeviceUrn().equals("nodeQ"));

            if(aJunc.getDeviceUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeK-nodeK:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeK:2-nodeK";

                assert (zJunc.getDeviceUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().equals("portA"));
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

                assert (zJunc.getDeviceUrn().equals("nodeQ"));
                assert (theFix.getIfceUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'asymmPceTest12' passed.");
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
        requestedBlueprint = testBuilder.buildRequest(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome,
                survivability, vlan, 1, 1, 1);

        log.info("Beginning test: 'multiFixtureTest'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
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
            String actualAzERO = aJunc.getDeviceUrn() + "-";
            String actualZaERO = zJunc.getDeviceUrn() + "-";

            for(String x : azERO)
                actualAzERO = actualAzERO + x + "-";

            for(String x : zaERO)
                actualZaERO = actualZaERO + x + "-";

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();
            String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
            String expectedZaERO = "nodeL-nodeL:1-nodeK:1-nodeK";

            assert (aJunc.getDeviceUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().equals("nodeL"));
            assert (aFixes.size() == 3);
            assert (zFixes.size() == 3);


            assert(aFixes
                    .stream()
                    .filter(f -> f.getIfceUrn().equals("portA")
                            || f.getIfceUrn().equals("portB")
                            || f.getIfceUrn().equals("portC"))
                    .filter(f -> f.getReservedBandwidth().getInBandwidth().equals(azBW))
                    .filter(f -> f.getReservedBandwidth().getEgBandwidth().equals(zaBW))
                    .count() == 3);

            assert(zFixes
                    .stream()
                    .filter(f -> f.getIfceUrn().equals("portX")
                            || f.getIfceUrn().equals("portY")
                            || f.getIfceUrn().equals("portZ"))
                    .filter(f -> f.getReservedBandwidth().getInBandwidth().equals(zaBW))
                    .filter(f -> f.getReservedBandwidth().getEgBandwidth().equals(azBW))
                    .count() == 3);

            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        log.info("test 'multiFixtureTest' passed.");
    }
}
