package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.AsymmTopologyBuilder;
import net.es.oscars.topo.TopologyBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class TopPceTestSurvivablePartial
{

    @Autowired
    private TopPCE topPCE;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private AsymmTopologyBuilder asymmTopologyBuilder;

    @Test
    public void survivablePceTest1()
    {
        log.info("Initializing test: 'survivablePceTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeK";
        String srcPort = "portA";
        String dstPort = "portZ";
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivable = SurvivabilityType.SURVIVABILITY_PARTIAL;

        topologyBuilder.buildTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, dstDevice, srcPort, dstPort, azBW, zaBW, palindrome, survivable, vlan);

        log.info("Beginning test: 'survivablePceTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("Beginning test: 'survivablePceTest1'.");
    }

    @Test
    public void survivablePceTest2()
    {
        log.info("Initializing test: 'survivablePceTest2'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest2' passed.");
    }

    @Test
    public void survivablePceTest3()
    {
        log.info("Initializing test: 'survivablePceTest3'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest3'.");

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
        assert (allResEthPipes.size() == 1);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

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

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP"));

            assert (aFixes.size() == 1);
            assert (zFixes.size() == 0);
            ReservedVlanFixtureE theFix = aFixes.iterator().next();

            String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP";
            String expectedZaERO = "nodeP-nodeP:1-nodeK:1-nodeK";

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
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);
            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            String expectedAzPrimaryERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
            String expectedZaPrimaryERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";
            String expectedAzSecondaryERO = "nodeP-nodeP:3-nodeR:1-nodeR-nodeR:2-nodeQ:2-nodeQ";
            String expectedZaSecondaryERO = "nodeQ-nodeQ:2-nodeR:2-nodeR-nodeR:1-nodeP:3-nodeP";

            assert (theFix.getIfceUrn().getUrn().equals("portZ"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
            assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

            if(actualAzERO.equals(expectedAzPrimaryERO))
            {
                assert(actualZaERO.equals(expectedZaPrimaryERO));
                usedEro1 = true;
            }

            if(actualAzERO.equals(expectedAzSecondaryERO))
            {
                assert(actualZaERO.equals(expectedZaSecondaryERO));
                usedEro2 = true;
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest3' passed.");
    }

    @Test
    public void survivablePceTest4()
    {
        log.info("Initializing test: 'survivablePceTest4'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest4'.");

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
        assert (allResEthPipes.size() == 2);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

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
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeR"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 1);
            ReservedVlanFixtureE theFix = zFixes.iterator().next();

            assert (theFix.getIfceUrn().getUrn().equals("portZ"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                String expectedAzPrimaryERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                String expectedZaPrimaryERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";
                String expectedAzSecondaryERO = "nodeP-nodeP:3-nodeR:2-nodeR-nodeR:3-nodeQ:2-nodeQ";
                String expectedZaSecondaryERO = "nodeQ-nodeQ:2-nodeR:3-nodeR-nodeR:2-nodeP:3-nodeP";

                assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
                assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

                if(actualAzERO.equals(expectedAzPrimaryERO))
                {
                    assert(actualZaERO.equals(expectedZaPrimaryERO));
                    usedEro1 = true;
                }

                if(actualAzERO.equals(expectedAzSecondaryERO))
                {
                    assert(actualZaERO.equals(expectedZaSecondaryERO));
                    usedEro2 = true;
                }
            }
            else
            {
                String expectedAzPrimaryERO = "nodeR-nodeR:3-nodeQ:2-nodeQ";
                String expectedZaPrimaryERO = "nodeQ-nodeQ:2-nodeR:3-nodeR";
                String expectedAzSecondaryERO = "nodeR-nodeR:2-nodeP:3-nodeP-nodeP:2-nodeQ:1-nodeQ";
                String expectedZaSecondaryERO = "nodeQ-nodeQ:1-nodeP:2-nodeP-nodeP:3-nodeR:2-nodeR";

                assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
                assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

                if(actualAzERO.equals(expectedAzPrimaryERO))
                {
                    assert(actualZaERO.equals(expectedZaPrimaryERO));
                    usedEro1 = true;
                }

                if(actualAzERO.equals(expectedAzSecondaryERO))
                {
                    assert(actualZaERO.equals(expectedZaSecondaryERO));
                    usedEro2 = true;
                }
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest4' passed.");
    }

    @Test
    public void survivablePceTest4_2()
    {
        log.info("Initializing test: 'survivablePceTest4_2'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo4_2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest4_2'.");

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
        assert (allResEthPipes.size() == 0);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

        String expectedAzERO1 = "nodeK-nodeK:1-nodeL:1-nodeL-nodeL:3-nodeP:1-nodeP-nodeP:2-nodeQ:1-nodeQ";
        String expectedZaERO1 = "nodeQ-nodeQ:1-nodeP:2-nodeP-nodeP:1-nodeL:3-nodeL-nodeL:1-nodeK:1-nodeK";
        String expectedAzERO2 = "nodeK-nodeK:2-nodeM:1-nodeM-nodeM:3-nodeR:1-nodeR-nodeR:3-nodeQ:2-nodeQ";
        String expectedZaERO2 = "nodeQ-nodeQ:2-nodeR:3-nodeR-nodeR:1-nodeM:3-nodeM-nodeM:1-nodeK:2-nodeK";

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

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            assert (aFixes.size() == 1);
            assert (zFixes.size() == 1);
            ReservedVlanFixtureE aFix = aFixes.iterator().next();
            ReservedVlanFixtureE zFix = zFixes.iterator().next();

            assert (aFix.getIfceUrn().getUrn().equals("portA"));
            assert (zFix.getIfceUrn().getUrn().equals("portZ"));
            assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
            assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
            assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
            assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
            assert (actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));

            if(actualAzERO.equals(expectedAzERO1))
            {
                assert(actualZaERO.equals(expectedZaERO1));
                usedEro1 = true;
            }

            if(actualAzERO.equals(expectedAzERO2))
            {
                assert(actualZaERO.equals(expectedZaERO2));
                usedEro2 = true;
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest4_2' passed.");
    }

    @Test
    public void survivablePceTest5()
    {
        log.info("Initializing test: 'survivablePceTest5'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo5();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest5'.");

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
        assert (allResEthPipes.size() == 2);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

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
            {
                actualAzERO = actualAzERO + x + "-";
            }

            for(String x : zaERO)
            {
                actualZaERO = actualZaERO + x + "-";
            }

            actualAzERO = actualAzERO + zJunc.getDeviceUrn();
            actualZaERO = actualZaERO + aJunc.getDeviceUrn();


            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);

            String expectedAzPrimaryERO = "nodeP-nodeP:3-nodeQ:1-nodeQ";
            String expectedZaPrimaryERO = "nodeQ-nodeQ:1-nodeP:3-nodeP";
            String expectedAzSecondaryERO = "nodeP-nodeP:4-nodeR:2-nodeR-nodeR:3-nodeQ:2-nodeQ";
            String expectedZaSecondaryERO = "nodeQ-nodeQ:2-nodeR:3-nodeR-nodeR:2-nodeP:4-nodeP";

            assert (actualAzERO.equals(expectedAzPrimaryERO) || actualAzERO.equals(expectedAzSecondaryERO));
            assert (actualZaERO.equals(expectedZaPrimaryERO) || actualZaERO.equals(expectedZaSecondaryERO));

            if(actualAzERO.equals(expectedAzPrimaryERO))
            {
                assert(actualZaERO.equals(expectedZaPrimaryERO));
                usedEro1 = true;
            }

            if(actualAzERO.equals(expectedAzSecondaryERO))
            {
                assert(actualZaERO.equals(expectedZaSecondaryERO));
                usedEro2 = true;
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest5' passed.");
    }

    @Test
    public void survivablePceTest6()
    {
        log.info("Initializing test: 'survivablePceTest6'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        String dstDevice = "nodeP";
        String srcPort = "portA";
        String dstPort = "portZ";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivable = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo6();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, dstDevice, srcPort, dstPort, azBW, zaBW, palindrome, survivable, vlan);

        log.info("Beginning test: 'survivablePceTest6'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest6' passed.");
    }

    @Test
    public void survivablePceTest7()
    {
        log.info("Initializing test: 'survivablePceTest7'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo7();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest7'.");

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
        assert (allResEthPipes.size() == 1);
        assert (allResMplsPipes.size() == 0);

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

        log.info("test 'survivablePceTest7' passed.");
    }

    @Test
    public void survivablePceTest8()
    {
        log.info("Initializing test: 'survivablePceTest8'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo8();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest8'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest8' passed.");
    }

    @Test
    public void survivablePceTest9()
    {
        log.info("Initializing test: 'survivablePceTest9'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo9();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest9'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest9' passed.");
    }

    @Test
    public void survivablePceTest10()
    {
        log.info("Initializing test: 'survivablePceTest10'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo10();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest10'.");

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
        assert (allResEthPipes.size() == 2);
        assert (allResMplsPipes.size() == 0);

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

        log.info("test 'survivablePceTest10' passed.");
    }

    @Test
    public void survivablePceTest11()
    {
        log.info("Initializing test: 'survivablePceTest11'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo11();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest11'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest11' passed.");
    }

    @Test
    public void survivablePceTest12()
    {
        log.info("Initializing test: 'survivablePceTest12'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        topologyBuilder.buildTopo12();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest12'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (!reservedBlueprint.isPresent());

        log.info("test 'survivablePceTest12' passed.");
    }

    @Test
    public void survivablePceTest13()
    {
        log.info("Initializing test: 'survivablePceTest13'.");

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
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_PARTIAL;
        String vlan = "any";

        asymmTopologyBuilder.buildAsymmTopo13();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan);

        log.info("Beginning test: 'survivablePceTest13'.");

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
        assert (allResEthPipes.size() == 0);
        assert (allResMplsPipes.size() == 2);

        boolean usedEro1 = false;
        boolean usedEro2 = false;

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

            String expectedAzERO1 = "nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeR:1-nodeR";
            String expectedZaERO1 = "nodeR-nodeR:1-nodeQ:2-nodeQ-nodeQ:1-nodeP:1-nodeP";
            String expectedAzERO2 = "nodeP-nodeP:2-nodeS:1-nodeS-nodeS:2-nodeR:2-nodeR";
            String expectedZaERO2 = "nodeR-nodeR:2-nodeS:2-nodeS-nodeS:1-nodeP:2-nodeP";

            assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
            assert (actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));

            if(actualAzERO.equals(expectedAzERO1))
            {
                assert(actualZaERO.equals(expectedZaERO1));
                usedEro1 = true;
            }

            if(actualAzERO.equals(expectedAzERO2))
            {
                assert(actualZaERO.equals(expectedZaERO2));
                usedEro2 = true;
            }
        }

        assert(usedEro1 && usedEro2);

        log.info("test 'survivablePceTest13' passed.");
    }

}
