package net.es.oscars.pce;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.VlanFlow;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.enums.PalindromicType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

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
    private TestEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;


    @Test
    public void basicPceTest1()
    {
        log.info("Initializing test: 'basicPceTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        List<String> portNames = Stream.of("portA", "portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";

        topologyBuilder.buildTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, portNames, azBW, zaBW, vlan);

        log.info("Beginning test: 'basicPceTest1'.");

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

        assert(allResJunctions.size() == 1);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 0);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeK"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();

            assert(Objects.equals(fix1.getReservedBandwidth().getInBandwidth(), azBW));
            assert(Objects.equals(fix2.getReservedBandwidth().getInBandwidth(), azBW));

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
        PalindromicType palindrome = PalindromicType.PALINDROME;
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
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
    public void basicPceTest6()
    {
        log.info("Initializing test: 'basicPceTest6'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        List<String> portNames = Stream.of("portA", "portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";

        topologyBuilder.buildTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, portNames, azBW, zaBW, vlan);

        log.info("Beginning test: 'basicPceTest6'.");

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

        assert(allResJunctions.size() == 1);
        assert(allResEthPipes.size() == 0);
        assert(allResMplsPipes.size() == 0);

        // Junctions
        for(ReservedVlanJunctionE oneJunc : allResJunctions)
        {
            assert(oneJunc.getDeviceUrn().getUrn().equals("nodeP"));

            Iterator<ReservedVlanFixtureE> iterF = oneJunc.getFixtures().iterator();
            ReservedVlanFixtureE fix1 = iterF.next();
            ReservedVlanFixtureE fix2 = iterF.next();

            assert(fix1.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert(fix2.getReservedBandwidth().getInBandwidth().equals(azBW));

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
        PalindromicType palindrome = PalindromicType.PALINDROME;
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
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
        PalindromicType palindrome = PalindromicType.PALINDROME;
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

        // Build Topology
        topologyBuilder.buildTopo7MultiFix();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeK";
        String dst = "nodeQ";
        List<String> aPorts = Arrays.asList("portA");
        List<String> zPorts = Arrays.asList("portZ");

        // Set Expected Parameters - Per Expected Pipe
        List<String> potentialAzEROsPipe1 = Collections.singletonList("nodeK-nodeK:2-nodeM:1-nodeM");
        List<String> potentialZaEROsPipe1 = Collections.singletonList("nodeM-nodeM:1-nodeK:2-nodeK");
        List<String> potentialAzEROsPipe2 = Collections.singletonList("nodeM-nodeM:2-nodeQ:2-nodeQ");
        List<String> potentialZaEROsPipe2 = Collections.singletonList("nodeQ-nodeQ:2-nodeM:2-nodeM");
        List<Integer> expectedAZInBandwidthsPipe1 = Arrays.asList(azBw, azBw);
        List<Integer> expectedAZEgBandwidthsPipe1 = Arrays.asList(azBw, azBw);
        List<Integer> expectedZAInBandwidthsPipe1 = Arrays.asList(zaBw, zaBw);
        List<Integer> expectedZAEgBandwidthsPipe1 = Arrays.asList(zaBw, zaBw);

        // Containers for requested junctions
        List<Junction> reqJunctions = new ArrayList<>();
        List<Pipe> reqPipes = new ArrayList<>();

        // Containers for expected junctions/pipes
        List<Junction> expectedJunctions = new ArrayList<>();
        List<Pipe> expectedEthPipes = new ArrayList<>();
        List<Pipe> expectedMplsPipes = new ArrayList<>();

        // Set up Requested Junctions

        // Set up Requested Pipes
        Pipe pipeKL = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipeKL);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes
        Pipe expectedEthPipe1 = testBuilder.makeExpectedPipe(src, "nodeM", aPorts, new ArrayList<>(), azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        Pipe expectedEthPipe2 = testBuilder.makeExpectedPipe("nodeM", dst, new ArrayList<>(), zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe2, potentialZaEROsPipe2,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedEthPipes.add(expectedEthPipe1);
        expectedEthPipes.add(expectedEthPipe2);

        // Set up Expected MPLS Pipes

        // Run the test
        log.info("Beginning test: 'basicPceTest12'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'basicPceTest12' passed.");


        log.info("test 'basicPceTest12' passed.");
    }

    @Test
    public void multiFixtureTest()
    {
        log.info("Initializing test: 'multiFixtureTest'.");

        // Build Topology
        topologyBuilder.buildTopo7MultiFix();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeK";
        String dst = "nodeL";
        List<String> aPorts = Arrays.asList("portA", "portB", "portC");
        List<String> zPorts = Arrays.asList("portX", "portY", "portZ");

        // Set Expected Parameters - Per Expected Pipe
        List<String> potentialAzEROsPipe1 = Collections.singletonList("nodeK-nodeK:1-nodeL:1-nodeL");
        List<String> potentialZaEROsPipe1 = Collections.singletonList("nodeL-nodeL:1-nodeK:1-nodeK");
        List<Integer> expectedAZInBandwidthsPipe1 = Arrays.asList(azBw, azBw);
        List<Integer> expectedAZEgBandwidthsPipe1 = Arrays.asList(azBw, azBw);
        List<Integer> expectedZAInBandwidthsPipe1 = Arrays.asList(zaBw, zaBw);
        List<Integer> expectedZAEgBandwidthsPipe1 = Arrays.asList(zaBw, zaBw);

        // Containers for requested junctions
        List<Junction> reqJunctions = new ArrayList<>();
        List<Pipe> reqPipes = new ArrayList<>();

        // Containers for expected junctions/pipes
        List<Junction> expectedJunctions = new ArrayList<>();
        List<Pipe> expectedEthPipes = new ArrayList<>();
        List<Pipe> expectedMplsPipes = new ArrayList<>();

        // Set up Requested Junctions

        // Set up Requested Pipes
        Pipe pipeKL = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipeKL);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes
        Pipe expectedEthPipe1 = testBuilder.makeExpectedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedEthPipes.add(expectedEthPipe1);

        // Set up Expected MPLS Pipes

        // Run the test
        log.info("Beginning test: 'multiFixtureTest'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'multiFixtureTest' passed.");
    }


    public void pceTest(List<Junction> junctions, List<Pipe> pipes, List<Junction> expectedJunctions,
                        List<Pipe> expectedEthPipes, List<Pipe> expectedMplsPipes){

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        Set<RequestedVlanJunctionE> reqJunctions = junctions
                .stream()
                .map(j -> testBuilder.convertToRequestedJunction(j))
                .collect(Collectors.toSet());

        Set<RequestedVlanPipeE> reqPipes = pipes
                .stream()
                .map(p -> testBuilder.convertToRequestedPipe(p))
                .collect(Collectors.toSet());


        RequestedVlanFlowE flow = RequestedVlanFlowE.builder()
                .junctions(reqJunctions)
                .pipes(reqPipes)
                .build();

        requestedBlueprint = RequestedBlueprintE.builder()
                .layer3Flow(new Layer3FlowE())
                .vlanFlow(flow)
                .build();

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

        assert(allResJunctions.size() == expectedJunctions.size());
        assert(allResEthPipes.size() == expectedEthPipes.size());
        assert(allResMplsPipes.size() == expectedMplsPipes.size());

        // Junctions
        expectedJunctions.forEach(expectedJunction -> {
                    boolean match = false;
                    for (ReservedVlanJunctionE resJunction : allResJunctions) {
                        if (resJunction.getDeviceUrn().getUrn().equals(expectedJunction.getUrn())) {
                            matchJunction(resJunction, expectedJunction);
                            match = true;
                        }
                    }
                    assert (match);
                }
        );

        // Ethernet Pipes
        expectedEthPipes.forEach(expectPipe -> matchEthPipe(allResEthPipes, expectPipe));

        // MPLS Pipes
        expectedMplsPipes.forEach(expectPipe -> matchMplsPipe(allResMplsPipes, expectPipe));

    }

    public void matchEthPipe(Set<ReservedEthPipeE> resPipes, Pipe expectedPipe) {
        for(ReservedEthPipeE p : resPipes){
            ReservedVlanJunctionE aJunction = p.getAJunction();
            ReservedVlanJunctionE zJunction = p.getZJunction();

            List<String> azERO = p.getAzERO();
            List<String> zaERO = p.getZaERO();

            Set<ReservedBandwidthE> reservedBandwidths = p.getReservedBandwidths();

            if(aJunction.getDeviceUrn().getUrn().equals(expectedPipe.aJunction.getUrn())
                    && zJunction.getDeviceUrn().getUrn().equals(expectedPipe.zJunction.getUrn())){

                matchPipeFields(aJunction, zJunction, azERO, zaERO, reservedBandwidths, expectedPipe);
            }
        }
    }

    public void matchMplsPipe(Set<ReservedMplsPipeE> resPipes, Pipe expectedPipe) {
        for(ReservedMplsPipeE p : resPipes){
            ReservedVlanJunctionE aJunction = p.getAJunction();
            ReservedVlanJunctionE zJunction = p.getZJunction();

            List<String> azERO = p.getAzERO();
            List<String> zaERO = p.getZaERO();

            Set<ReservedBandwidthE> reservedBandwidths = p.getReservedBandwidths();

            if(aJunction.getDeviceUrn().getUrn().equals(expectedPipe.aJunction.getUrn())
                    && zJunction.getDeviceUrn().getUrn().equals(expectedPipe.zJunction.getUrn())){

                matchPipeFields(aJunction, zJunction, azERO, zaERO, reservedBandwidths, expectedPipe);
            }
        }
    }

    private void matchPipeFields(ReservedVlanJunctionE aJunc, ReservedVlanJunctionE zJunc, List<String> azERO,
                                      List<String> zaERO, Set<ReservedBandwidthE> reservedBandwidths, Pipe expectedPipe){
        Junction expectedAJunction = expectedPipe.getAJunction();
        Junction expectedZJunction = expectedPipe.getZJunction();

        String actualAzERO = aJunc.getDeviceUrn().getUrn() + "-";
        String actualZaERO = zJunc.getDeviceUrn().getUrn() + "-";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        actualAzERO = actualAzERO + zJunc.getDeviceUrn();
        actualZaERO = actualZaERO + aJunc.getDeviceUrn();


        matchJunction(aJunc, expectedAJunction);
        matchJunction(zJunc, expectedZJunction);
        expectedPipe.getPotentialAZEROs().contains(actualAzERO);
        expectedPipe.getPotentialZAEROs().contains(actualZaERO);
        matchBandwidths(reservedBandwidths, expectedPipe);
    }

    public void matchJunction(ReservedVlanJunctionE resJunction, Junction expectedJunction){
        assert(resJunction.getDeviceUrn().getUrn().equals(expectedJunction.getUrn()));
        matchFixtures(resJunction.getFixtures(), expectedJunction);
    }

    private void matchBandwidths(Set<ReservedBandwidthE> rsvBws, Pipe expectedPipe){
        log.info(rsvBws.toString());
        log.info(expectedPipe.toString());
        assert(rsvBws
                .stream()
                .filter(bw -> expectedPipe.getPotentialAZEROs().stream().anyMatch(s -> s.contains(bw.getUrn().getUrn())))
                .filter(bw -> expectedPipe.getExpectedAZInBandwidths().contains(bw.getInBandwidth()))
                .filter(bw -> expectedPipe.getExpectedAZEgBandwidths().contains(bw.getEgBandwidth()))
                .count() == expectedPipe.getExpectedAZInBandwidths().size());

        assert(rsvBws
                .stream()
                .filter(bw -> expectedPipe.getPotentialZAEROs().stream().anyMatch(s -> s.contains(bw.getUrn().getUrn())))
                .filter(bw -> expectedPipe.getExpectedZAInBandwidths().contains(bw.getInBandwidth()))
                .filter(bw -> expectedPipe.getExpectedZAEgBandwidths().contains(bw.getEgBandwidth()))
                .count() == expectedPipe.getExpectedZAInBandwidths().size());
    }

    private void matchFixtures(Set<ReservedVlanFixtureE> fixtures, Junction j){
        assert(fixtures
                .stream()
                .filter(fix -> j.getIngressBWs().contains(fix.getReservedBandwidth().getInBandwidth()))
                .filter(fix -> j.getEgressBWs().contains(fix.getReservedBandwidth().getEgBandwidth()))
                .map(fix -> fix.getIfceUrn().getUrn())
                .filter(urn -> j.getFixtures().contains(urn))
                .count() == j.getFixtures().size());
    }
}
