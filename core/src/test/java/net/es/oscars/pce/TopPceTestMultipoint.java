package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.MultipointTopologyBuilder;
import net.es.oscars.topo.enums.PalindromicType;
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
 * Created by jeremy on 6/30/16.
 *
 * Tests End-to-End correctness of the PCE modules
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class TopPceTestMultipoint
{
    @Autowired
    private TopPCE topPCE;

    @Autowired
    private TestEntityBuilder testBuilder;

    @Autowired
    private MultipointTopologyBuilder mpTopoBuilder;

    @Test
    public void multipointPceTest1()
    {
        log.info("Initializing test: 'multipointPceTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        // Set requested parameters
        String srcAZ = "nodeP";
        String dstAZ = "nodeM";
        String srcBZ = "nodeN";
        String dstBZ = "nodeM";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> bPorts = Collections.singletonList("portB");
        List<String> zPorts = Collections.singletonList("portZ");
        Integer azBW = 25;
        Integer zaBW = 25;
        Integer bzBW = 25;
        Integer zbBW = 25;
        String vlan = "any";

        PalindromicType palindrome = PalindromicType.PALINDROME;

        mpTopoBuilder.buildMultipointTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Set up Requested Pipes
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(aPorts, srcAZ, zPorts, dstAZ, azBW, zaBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW, zbBW, palindrome, vlan);

        reqPipes.add(pipeAZ);
        reqPipes.add(pipeBZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);

        log.info("Beginning test: 'multipointPceTest1'.");

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

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeN"));
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
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                String expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW + bzBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW + zbBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE aFix = aFixes.iterator().next();
                ReservedVlanFixtureE zFix = zFixes.iterator().next();

                String expectedAzERO = "nodeN-nodeN:2-nodeM:2-nodeM";
                String expectedZaERO = "nodeM-nodeM:2-nodeN:2-nodeN";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeN"));
                assert (aFix.getIfceUrn().getUrn().equals("portB"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(bzBW));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zbBW));
                assert (zFix.getIfceUrn().getUrn().equals("portZ"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(zbBW + zaBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(bzBW + azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'multipointPceTest1' passed.");
    }

    @Test
    public void multipointPceTest2()
    {
        log.info("Initializing test: 'multipointPceTest2'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        // Set requested parameters
        String srcAZ = "nodeK";
        String dstAZ = "nodeQ";
        String srcBZ = "nodeM";
        String dstBZ = "nodeQ";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> bPorts = Collections.singletonList("portB");
        List<String> zPorts = Collections.singletonList("portZ");
        Integer azBW = 25;
        Integer zaBW = 25;
        Integer bzBW = 25;
        Integer zbBW = 25;
        String vlan = "any";

        PalindromicType palindrome = PalindromicType.PALINDROME;

        mpTopoBuilder.buildMultipointTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Set up Requested Pipes
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(aPorts, srcAZ, zPorts, dstAZ, azBW, zaBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW, zbBW, palindrome, vlan);

        reqPipes.add(pipeAZ);
        reqPipes.add(pipeBZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);

        log.info("Beginning test: 'multipointPceTest2'.");

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
        assert(allResMplsPipes.size() == 2);

        assert false;
        //TODO: Have to account for optional paths - can take more than one SP.

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

        log.info("test 'multipointPceTest2' passed.");
    }

    @Test
    public void multipointPceTest4()
    {
        log.info("Initializing test: 'multipointPceTest4'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        // Set requested parameters
        String srcAZ = "nodeP";
        String dstAZ = "nodeM";
        String srcAB = "nodeP";
        String dstAB = "nodeN";
        String srcBZ = "nodeN";
        String dstBZ = "nodeM";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> bPorts = Collections.singletonList("portB");
        List<String> zPorts = Collections.singletonList("portZ");
        Integer azBW = 25;
        Integer zaBW = 25;
        Integer abBW = 25;
        Integer baBW = 25;
        Integer bzBW = 25;
        Integer zbBW = 25;
        String vlan = "any";

        PalindromicType palindrome = PalindromicType.PALINDROME;

        mpTopoBuilder.buildMultipointTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Set up Requested Pipes
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(aPorts, srcAZ, zPorts, dstAZ, azBW, zaBW, palindrome, vlan);
        RequestedVlanPipeE pipeAB = testBuilder.buildRequestedPipe(aPorts, srcAB, bPorts, dstAB, abBW, baBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW, zbBW, palindrome, vlan);

        reqPipes.add(pipeAZ);
        reqPipes.add(pipeAB);
        reqPipes.add(pipeBZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);

        log.info("Beginning test: 'multipointPceTest4'.");

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
        assert(allResEthPipes.size() == 5);
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

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeN"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL") || zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeN"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO = "nodeP-nodeP:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:1-nodeP:1-nodeP";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW + bzBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW + zbBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                String expectedAzERO;
                String expectedZaERO;

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeN"));

                if(zJunc.getDeviceUrn().getUrn().equals("nodeM"))
                {
                    expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                    expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";
                    assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                    assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW + zbBW));
                    assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW + bzBW));
                    assert (actualAzERO.equals(expectedAzERO));
                    assert (actualZaERO.equals(expectedZaERO));
                }
                else
                {
                    expectedAzERO = "nodeL-nodeL:3-nodeN:1-nodeN";
                    expectedZaERO = "nodeN-nodeN:1-nodeL:3-nodeL";
                    assert (theFix.getIfceUrn().getUrn().equals("portB"));
                    assert (theFix.getReservedBandwidth().getInBandwidth().equals(baBW + bzBW));
                    assert (theFix.getReservedBandwidth().getEgBandwidth().equals(abBW + zbBW));
                    assert (actualAzERO.equals(expectedAzERO));
                    assert (actualZaERO.equals(expectedZaERO));
                }
            }
            else
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE aFix = aFixes.iterator().next();
                ReservedVlanFixtureE zFix = zFixes.iterator().next();

                String expectedAzERO = "nodeN-nodeN:2-nodeM:2-nodeM";
                String expectedZaERO = "nodeM-nodeM:2-nodeN:2-nodeN";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeN"));
                assert (aFix.getIfceUrn().getUrn().equals("portB"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(bzBW + baBW));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zbBW + abBW));
                assert (zFix.getIfceUrn().getUrn().equals("portZ"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW + zbBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW + bzBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'multipointPceTest4' passed.");
    }

    @Test
    public void multipointPceTest5()
    {
        log.info("Initializing test: 'multipointPceTest5'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        // Set requested parameters
        String srcAZ = "nodeP";
        String dstAZ = "nodeM";
        String srcAB = "nodeP";
        String dstAB = "nodeN";
        String srcBZ = "nodeN";
        String dstBZ = "nodeM";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> bPorts = Collections.singletonList("portB");
        List<String> zPorts = Collections.singletonList("portZ");
        Integer azBW = 25;
        Integer zaBW = 25;
        Integer abBW = 25;
        Integer baBW = 25;
        Integer bzBW = 25;
        Integer zbBW = 25;
        String vlan = "any";

        PalindromicType palindrome = PalindromicType.PALINDROME;

        mpTopoBuilder.buildMultipointTopo4();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Set up Requested Pipes
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(aPorts, srcAZ, zPorts, dstAZ, azBW, zaBW, palindrome, vlan);
        RequestedVlanPipeE pipeAB = testBuilder.buildRequestedPipe(aPorts, srcAB, bPorts, dstAB, abBW, baBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW, zbBW, palindrome, vlan);

        reqPipes.add(pipeAZ);
        reqPipes.add(pipeAB);
        reqPipes.add(pipeBZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);

        log.info("Beginning test: 'multipointPceTest5'.");

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
        assert(allResMplsPipes.size() == 2);

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

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeN"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeN"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                String expectedAzERO;
                String expectedZaERO;

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeN"));

                if(zJunc.getDeviceUrn().getUrn().equals("nodeM"))
                {
                    expectedAzERO = "nodeL-nodeL:2-nodeM:1-nodeM";
                    expectedZaERO = "nodeM-nodeM:1-nodeL:2-nodeL";
                    assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                    assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW + zbBW));
                    assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW + bzBW));
                    assert (actualAzERO.equals(expectedAzERO));
                    assert (actualZaERO.equals(expectedZaERO));
                }
                else
                {
                    expectedAzERO = "nodeL-nodeL:3-nodeN:1-nodeN";
                    expectedZaERO = "nodeN-nodeN:1-nodeL:3-nodeL";
                    assert (theFix.getIfceUrn().getUrn().equals("portB"));
                    assert (theFix.getReservedBandwidth().getInBandwidth().equals(baBW + bzBW));
                    assert (theFix.getReservedBandwidth().getEgBandwidth().equals(abBW + zbBW));
                    assert (actualAzERO.equals(expectedAzERO));
                    assert (actualZaERO.equals(expectedZaERO));
                }
            }
            else
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE aFix = aFixes.iterator().next();
                ReservedVlanFixtureE zFix = zFixes.iterator().next();

                String expectedAzERO = "nodeN-nodeN:2-nodeM:2-nodeM";
                String expectedZaERO = "nodeM-nodeM:2-nodeN:2-nodeN";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeN"));
                assert (aFix.getIfceUrn().getUrn().equals("portB"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(bzBW + baBW));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zbBW + abBW));
                assert (zFix.getIfceUrn().getUrn().equals("portZ"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(zbBW + zaBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(bzBW + azBW));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // MPLS Pipes
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

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));

            assert (aFixes.size() == 1);
            assert (zFixes.size() == 0);
            ReservedVlanFixtureE theFix = aFixes.iterator().next();

            String expectedAzERO = "nodeP-nodeP:1-nodeL:1-nodeL";
            String expectedZaERO = "nodeL-nodeL:1-nodeP:1-nodeP";

            assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
            assert (theFix.getIfceUrn().getUrn().equals("portA"));
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW + bzBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW + zbBW));
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        log.info("test 'multipointPceTest5' passed.");
    }
}
