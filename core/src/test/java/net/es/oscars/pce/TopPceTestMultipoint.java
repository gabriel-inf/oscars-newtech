package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.RequestedEntityBuilder;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
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
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private MultipointTopologyBuilder mpTopoBuilder;

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Test
    public void multipointPceTest1()
    {
        mpTopoBuilder.buildMultipointTopo1();

        // Symmetric - All pipes
        Integer azBW = 25;
        Integer zaBW = 25;
        Integer bzBW = 25;
        Integer zbBW = 25;
        String testName = "\'multipointPceTest1 - Symmetric All Pipes\'";
        test1(azBW, zaBW, bzBW, zbBW, testName);

        // Symmetric - Different pipes
        azBW = 25;
        zaBW = 25;
        bzBW = 50;
        zbBW = 50;
        testName = "\'multipointPceTest1 - Symmetric Different Pipes\'";
        test1(azBW, zaBW, bzBW, zbBW, testName);

        // Asymmetric - One pipe
        azBW = 50;
        zaBW = 25;
        bzBW = 25;
        zbBW = 25;
        testName = "\'multipointPceTest1 - Asymmetric One Pipe\'";
        test1(azBW, zaBW, bzBW, zbBW, testName);

        // Asymmetric - Other pipe
        azBW = 25;
        zaBW = 25;
        bzBW = 25;
        zbBW = 50;
        testName = "\'multipointPceTest1 - Asymmetric Other Pipe\'";
        test1(azBW, zaBW, bzBW, zbBW, testName);

        // Asymmetric - All pipes
        azBW = 35;
        zaBW = 25;
        bzBW = 20;
        zbBW = 50;
        testName = "\'multipointPceTest1 - Asymmetric Both Pipes\'";
        test1(azBW, zaBW, bzBW, zbBW, testName);
    }

    @Test
    public void multipointPceTest2()
    {
        mpTopoBuilder.buildMultipointTopo2();

        // Symmetric - All pipes
        Integer azBW = 25;
        Integer zaBW = 25;
        Integer bzBW = 25;
        Integer zbBW = 25;
        String testName = "\'multipointPceTest2 - Symmetric All Pipes\'";
        test2(azBW, zaBW, bzBW, zbBW, testName);

        // Symmetric - Different pipes
        azBW = 25;
        zaBW = 25;
        bzBW = 50;
        zbBW = 50;
        testName = "\'multipointPceTest2 - Symmetric Different Pipes\'";
        test2(azBW, zaBW, bzBW, zbBW, testName);

        // Asymmetric - One pipe
        azBW = 50;
        zaBW = 25;
        bzBW = 25;
        zbBW = 25;
        testName = "\'multipointPceTest2 - Asymmetric One Pipe\'";
        test2(azBW, zaBW, bzBW, zbBW, testName);

        // Asymmetric - Other pipe
        azBW = 25;
        zaBW = 25;
        bzBW = 25;
        zbBW = 50;
        testName = "\'multipointPceTest2 - Asymmetric Other Pipe\'";
        test2(azBW, zaBW, bzBW, zbBW, testName);

        // Asymmetric - All pipes
        azBW = 35;
        zaBW = 25;
        bzBW = 20;
        zbBW = 50;
        testName = "\'multipointPceTest2 - Asymmetric Both Pipes\'";
        test2(azBW, zaBW, bzBW, zbBW, testName);
    }

    @Test
    public void multipointPceTest3()
    {
        mpTopoBuilder.buildMultipointTopo3();

        // Symmetric - All pipes
        Integer azBW = 25;
        Integer zaBW = 25;
        Integer bzBW = 25;
        Integer zbBW = 25;
        String testName = "\'multipointPceTest3 - Symmetric All Pipes\'";
        test3(azBW, zaBW, bzBW, zbBW, testName);

        // Symmetric - Different pipes
        azBW = 25;
        zaBW = 25;
        bzBW = 50;
        zbBW = 50;
        testName = "\'multipointPceTest3 - Symmetric Different Pipes\'";
        test3(azBW, zaBW, bzBW, zbBW, testName);

        // Asymmetric - One pipe
        azBW = 50;
        zaBW = 25;
        bzBW = 25;
        zbBW = 25;
        testName = "\'multipointPceTest3 - Asymmetric One Pipe\'";
        test3(azBW, zaBW, bzBW, zbBW, testName);

        // Asymmetric - Other pipe
        azBW = 25;
        zaBW = 25;
        bzBW = 25;
        zbBW = 50;
        testName = "\'multipointPceTest3 - Asymmetric Other Pipe\'";
        test3(azBW, zaBW, bzBW, zbBW, testName);

        // Asymmetric - All pipes
        azBW = 35;
        zaBW = 25;
        bzBW = 20;
        zbBW = 50;
        testName = "\'multipointPceTest3 - Asymmetric Both Pipes\'";
        test3(azBW, zaBW, bzBW, zbBW, testName);
    }

    @Test
    public void multipointPceTest4()
    {
        mpTopoBuilder.buildMultipointTopo1();

        // Symmetric - All pipes
        Integer azBW = 25;
        Integer zaBW = 25;
        Integer abBW = 25;
        Integer baBW = 25;
        Integer bzBW = 25;
        Integer zbBW = 25;
        String testName = "\'multipointPceTest4 - Symmetric All Pipes\'";
        test4(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);

        // Symmetric - Different pipes
        azBW = 25;
        zaBW = 25;
        abBW = 65;
        baBW = 65;
        bzBW = 50;
        zbBW = 50;
        testName = "\'multipointPceTest4 - Symmetric Different Pipes\'";
        test4(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);

        // Asymmetric - One pipe
        azBW = 50;
        zaBW = 25;
        abBW = 25;
        baBW = 25;
        bzBW = 25;
        zbBW = 25;
        testName = "\'multipointPceTest4 - Asymmetric One Pipe\'";
        test4(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);

        // Asymmetric - Two pipes
        azBW = 50;
        zaBW = 25;
        abBW = 25;
        baBW = 25;
        bzBW = 25;
        zbBW = 50;
        testName = "\'multipointPceTest4 - Asymmetric Other Pipe\'";
        test4(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);

        // Asymmetric - All pipes
        azBW = 15;
        zaBW = 25;
        abBW = 20;
        baBW = 35;
        bzBW = 40;
        zbBW = 10;
        testName = "\'multipointPceTest4 - Asymmetric Both Pipes\'";
        test4(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);
    }

    @Test
    public void multipointPceTest5()
    {
        mpTopoBuilder.buildMultipointTopo4();

        // Symmetric - All pipes
        Integer azBW = 25;
        Integer zaBW = 25;
        Integer abBW = 25;
        Integer baBW = 25;
        Integer bzBW = 25;
        Integer zbBW = 25;
        String testName = "\'multipointPceTest5 - Symmetric All Pipes\'";
        test5(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);

        // Symmetric - Different pipes
        azBW = 25;
        zaBW = 25;
        abBW = 65;
        baBW = 65;
        bzBW = 50;
        zbBW = 50;
        testName = "\'multipointPceTest5 - Symmetric Different Pipes\'";
        test5(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);

        // Asymmetric - One pipe
        azBW = 50;
        zaBW = 25;
        abBW = 25;
        baBW = 25;
        bzBW = 25;
        zbBW = 25;
        testName = "\'multipointPceTest5 - Asymmetric One Pipe\'";
        test5(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);

        // Asymmetric - Two pipes
        azBW = 50;
        zaBW = 25;
        abBW = 25;
        baBW = 25;
        bzBW = 25;
        zbBW = 50;
        testName = "\'multipointPceTest5 - Asymmetric Other Pipe\'";
        test5(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);

        // Asymmetric - All pipes
        azBW = 15;
        zaBW = 25;
        abBW = 20;
        baBW = 35;
        bzBW = 40;
        zbBW = 10;
        testName = "\'multipointPceTest5 - Asymmetric Both Pipes\'";
        test5(azBW, zaBW, abBW, baBW, bzBW, zbBW, testName);
    }


    private void test1(Integer azBW, Integer zaBW, Integer bzBW, Integer zbBW, String testName)
    {
        log.info("Initializing test: " + testName + ".");

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
        String vlan = "any";

        PalindromicType palindrome = PalindromicType.PALINDROME;

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Set up Requested Pipes
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(aPorts, srcAZ, zPorts, dstAZ, azBW, zaBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW, zbBW, palindrome, vlan);

        reqPipes.add(pipeAZ);
        reqPipes.add(pipeBZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);

        log.info("Beginning test: " + testName + ".");

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

        int totalBwIn_A = 0;
        int totalBwEg_A = 0;
        int totalBwIn_B = 0;
        int totalBwEg_B = 0;
        int totalBwIn_Z = 0;
        int totalBwEg_Z = 0;

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

            // Check that all reserved vlans in the pipe use the same VLAN ID
            Set<ReservedVlanE> rsvVlans = ethPipe.getReservedVlans();
            assert(rsvVlans.stream().map(ReservedVlanE::getVlan).distinct().count()==1);

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
                totalBwIn_A += theFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_A += theFix.getReservedBandwidth().getEgBandwidth();
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
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                totalBwIn_Z += theFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_Z += theFix.getReservedBandwidth().getEgBandwidth();
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

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (aFix.getIfceUrn().getUrn().equals("portB"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(bzBW));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zbBW));
                totalBwIn_B += aFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_B += aFix.getReservedBandwidth().getEgBandwidth();
                assert (zFix.getIfceUrn().getUrn().equals("portZ"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(zbBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(bzBW));
                totalBwIn_Z += zFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_Z += zFix.getReservedBandwidth().getEgBandwidth();
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Check bandwidth consumption at fixture-ports
        assert(totalBwIn_A == azBW);
        assert(totalBwEg_A == zaBW);
        assert(totalBwIn_B == bzBW);
        assert(totalBwEg_B == zbBW);
        assert(totalBwIn_Z == zaBW + zbBW);
        assert(totalBwEg_Z == azBW + bzBW);

        log.info("test " + testName + " passed.");
    }


    private void test2(Integer azBW, Integer zaBW, Integer bzBW, Integer zbBW, String testName)
    {
        log.info("Initializing test: " + testName + ".");

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
        String vlan = "any";

        PalindromicType palindrome = PalindromicType.PALINDROME;
        
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Set up Requested Pipes
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(aPorts, srcAZ, zPorts, dstAZ, azBW, zaBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW, zbBW, palindrome, vlan);

        reqPipes.add(pipeAZ);
        reqPipes.add(pipeBZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);

        log.info("Beginning test: " + testName + ".");

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

        int totalBwIn_A = 0;
        int totalBwEg_A = 0;
        int totalBwIn_B = 0;
        int totalBwEg_B = 0;
        int totalBwIn_Z = 0;
        int totalBwEg_Z = 0;

        Integer vlanChosenPipe_MR1 = null;
        Integer vlanChosenPipe_MR2;

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

            // Check that all reserved vlans in the pipe use the same VLAN ID
            Set<ReservedVlanE> rsvVlans = ethPipe.getReservedVlans();
            assert(rsvVlans.stream().map(ReservedVlanE::getVlan).distinct().count()==1);

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
                totalBwIn_A += theFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_A += theFix.getReservedBandwidth().getEgBandwidth();
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
                assert (aFixes.size() == 0 || aFixes.size() == 1); // 0 if pipe is part of A->Z circuit, 1 if B->Z
                assert (zFixes.size() == 0);

                if(aFixes.size() == 1)
                {
                    ReservedVlanFixtureE theFix = aFixes.iterator().next();
                    assert (theFix.getIfceUrn().getUrn().equals("portB"));
                    assert (theFix.getReservedBandwidth().getInBandwidth().equals(bzBW));
                    assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zbBW));
                    totalBwIn_B += theFix.getReservedBandwidth().getInBandwidth();
                    totalBwEg_B += theFix.getReservedBandwidth().getEgBandwidth();
                }

                String expectedAzERO = "nodeM-nodeM:3-nodeR:1-nodeR";
                String expectedZaERO = "nodeR-nodeR:1-nodeM:3-nodeM";

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeR"));
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));

                // Confirm that if there two overlapping pipes, they do not share a VLAN ID
                if(vlanChosenPipe_MR1 == null)
                {
                    vlanChosenPipe_MR1 = rsvVlans.iterator().next().getVlan();
                }
                else    // Only triggered if this is the second time through this pipe
                {
                    vlanChosenPipe_MR2 = rsvVlans.iterator().next().getVlan();

                    log.info("VLAN ID For Pipe M-R 1: " + vlanChosenPipe_MR1);
                    log.info("VLAN ID For Pipe M-R 2: " + vlanChosenPipe_MR2);
                    assert(!vlanChosenPipe_MR1.equals(vlanChosenPipe_MR2));
                }
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
                totalBwIn_Z += theFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_Z += theFix.getReservedBandwidth().getEgBandwidth();
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
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW) || theFix.getReservedBandwidth().getInBandwidth().equals(zbBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW) || theFix.getReservedBandwidth().getEgBandwidth().equals(bzBW));
                totalBwIn_Z += theFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_Z += theFix.getReservedBandwidth().getEgBandwidth();
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Check bandwidth consumption at fixture-ports
        assert(totalBwIn_A == azBW);
        assert(totalBwEg_A == zaBW);
        assert(totalBwIn_B == bzBW);
        assert(totalBwEg_B == zbBW);
        assert(totalBwIn_Z == zaBW + zbBW);
        assert(totalBwEg_Z == azBW + bzBW);

        log.info("test " + testName + " passed.");
    }


    private void test3(Integer azBW, Integer zaBW, Integer bzBW, Integer zbBW, String testName)
    {
        log.info("Initializing test: " + testName + ".");

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
        String vlan = "any";

        PalindromicType palindrome = PalindromicType.PALINDROME;

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Set up Requested Pipes
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(aPorts, srcAZ, zPorts, dstAZ, azBW, zaBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW, zbBW, palindrome, vlan);

        reqPipes.add(pipeAZ);
        reqPipes.add(pipeBZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);

        log.info("Beginning test: " + testName + ".");

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
        assert(allResMplsPipes.size() == 2);

        int totalBwIn_A = 0;
        int totalBwEg_A = 0;
        int totalBwIn_B = 0;
        int totalBwEg_B = 0;
        int totalBwIn_Z = 0;
        int totalBwEg_Z = 0;

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

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeK") || aJunc.getDeviceUrn().getUrn().equals("nodeM"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeQ"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE aFix = aFixes.iterator().next();
                ReservedVlanFixtureE zFix = zFixes.iterator().next();

                String expectedAzERO1 = "nodeK-nodeK:1-nodeL:1-nodeL-nodeL:3-nodeP:1-nodeP-nodeP:2-nodeQ:1-nodeQ";
                String expectedAzERO2 = "nodeK-nodeK:2-nodeM:1-nodeM-nodeM:3-nodeR:1-nodeR-nodeR:3-nodeQ:2-nodeQ";
                String expectedZaERO1 = "nodeQ-nodeQ:1-nodeP:2-nodeP-nodeP:1-nodeL:3-nodeL-nodeL:1-nodeK:1-nodeK";
                String expectedZaERO2 = "nodeQ-nodeQ:2-nodeR:3-nodeR-nodeR:1-nodeM:3-nodeM-nodeM:1-nodeK:2-nodeK";

                assert (aFix.getIfceUrn().getUrn().equals("portA"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));
                totalBwIn_A += aFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_A += aFix.getReservedBandwidth().getEgBandwidth();
                assert (zFix.getIfceUrn().getUrn().equals("portZ"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                totalBwIn_Z += zFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_Z += zFix.getReservedBandwidth().getEgBandwidth();
                assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
                assert (actualZaERO.equals(expectedZaERO1) || actualZaERO.equals(expectedZaERO2));
            }
            else
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE aFix = aFixes.iterator().next();
                ReservedVlanFixtureE zFix = zFixes.iterator().next();

                String expectedAzERO = "nodeM-nodeM:3-nodeR:1-nodeR-nodeR:3-nodeQ:2-nodeQ";
                String expectedZaERO = "nodeQ-nodeQ:2-nodeR:3-nodeR-nodeR:1-nodeM:3-nodeM";

                assert (aFix.getIfceUrn().getUrn().equals("portB"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(bzBW));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zbBW));
                totalBwIn_B += aFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_B += aFix.getReservedBandwidth().getEgBandwidth();
                assert (zFix.getIfceUrn().getUrn().equals("portZ"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(zbBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(bzBW));
                totalBwIn_Z += zFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_Z += zFix.getReservedBandwidth().getEgBandwidth();
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Check bandwidth consumption at fixture-ports
        assert(totalBwIn_A == azBW);
        assert(totalBwEg_A == zaBW);
        assert(totalBwIn_B == bzBW);
        assert(totalBwEg_B == zbBW);
        assert(totalBwIn_Z == zaBW + zbBW);
        assert(totalBwEg_Z == azBW + bzBW);

        log.info("test " + testName + " passed.");
    }


    private void test4(Integer azBW, Integer zaBW, Integer abBW, Integer baBW, Integer bzBW, Integer zbBW, String testName)
    {
        log.info("Initializing test: " + testName + ".");

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
        String vlan = "any";

        PalindromicType palindrome = PalindromicType.PALINDROME;
        
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

        log.info("Beginning test: " + testName + ".");

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

        Integer vlanChosenPipePL1 = null;
        Integer vlanChosenPipePL2 = null;

        int totalBwIn_A = 0;
        int totalBwEg_A = 0;
        int totalBwIn_B = 0;
        int totalBwEg_B = 0;
        int totalBwIn_Z = 0;
        int totalBwEg_Z = 0;

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


            // Check that all reserved vlans in the pipe use the same VLAN ID
            Set<ReservedVlanE> rsvVlans = ethPipe.getReservedVlans();
            assert(rsvVlans.stream().map(ReservedVlanE::getVlan).distinct().count()==1);

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

                // Confirm that there two overlapping pipes do not share a VLAN ID
                if(vlanChosenPipePL1 == null){
                    vlanChosenPipePL1 = rsvVlans.iterator().next().getVlan();
                }
                else if(vlanChosenPipePL2 == null){
                    vlanChosenPipePL2 = rsvVlans.iterator().next().getVlan();
                    assert(!vlanChosenPipePL1.equals(vlanChosenPipePL2));
                    log.info("VLAN ID For Pipe PL 1: " + vlanChosenPipePL1);
                    log.info("VLAN ID For Pipe PL 2: " + vlanChosenPipePL2);
                }

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeL"));
                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(abBW) || theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW) || theFix.getReservedBandwidth().getEgBandwidth().equals(baBW));
                totalBwIn_A += theFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_A += theFix.getReservedBandwidth().getEgBandwidth();
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
                    assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                    assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                    totalBwIn_Z += theFix.getReservedBandwidth().getInBandwidth();
                    totalBwEg_Z += theFix.getReservedBandwidth().getEgBandwidth();
                    assert (actualAzERO.equals(expectedAzERO));
                    assert (actualZaERO.equals(expectedZaERO));
                }
                else
                {
                    expectedAzERO = "nodeL-nodeL:3-nodeN:1-nodeN";
                    expectedZaERO = "nodeN-nodeN:1-nodeL:3-nodeL";
                    assert (theFix.getIfceUrn().getUrn().equals("portB"));
                    assert (theFix.getReservedBandwidth().getInBandwidth().equals(baBW));
                    assert (theFix.getReservedBandwidth().getEgBandwidth().equals(abBW));
                    totalBwIn_B += theFix.getReservedBandwidth().getInBandwidth();
                    totalBwEg_B += theFix.getReservedBandwidth().getEgBandwidth();
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

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (aFix.getIfceUrn().getUrn().equals("portB"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(bzBW));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zbBW));
                totalBwIn_B += aFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_B += aFix.getReservedBandwidth().getEgBandwidth();
                assert (zFix.getIfceUrn().getUrn().equals("portZ"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(zbBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(bzBW));
                totalBwIn_Z += zFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_Z += zFix.getReservedBandwidth().getEgBandwidth();
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Check bandwidth consumption at fixture-ports
        assert(totalBwIn_A == abBW + azBW);
        assert(totalBwEg_A == baBW + zaBW);
        assert(totalBwIn_B == baBW + bzBW);
        assert(totalBwEg_B == abBW + zbBW);
        assert(totalBwIn_Z == zaBW + zbBW);
        assert(totalBwEg_Z == azBW + bzBW);

        log.info("test " + testName + " passed.");
    }


    private void test5(Integer azBW, Integer zaBW, Integer abBW, Integer baBW, Integer bzBW, Integer zbBW, String testName)
    {
        log.info("Initializing test: " + testName + ".");

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
        String vlan = "any";

        PalindromicType palindrome = PalindromicType.PALINDROME;

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

        log.info("Beginning test: " + testName + ".");

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

        int totalBwIn_A = 0;
        int totalBwEg_A = 0;
        int totalBwIn_B = 0;
        int totalBwEg_B = 0;
        int totalBwIn_Z = 0;
        int totalBwEg_Z = 0;

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

            // Check that all reserved vlans in the pipe use the same VLAN ID
            Set<ReservedVlanE> rsvVlans = ethPipe.getReservedVlans();
            assert(rsvVlans.stream().map(ReservedVlanE::getVlan).distinct().count()==1);

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeN"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeN"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
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
                    assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                    assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));
                    totalBwIn_Z += theFix.getReservedBandwidth().getInBandwidth();
                    totalBwEg_Z += theFix.getReservedBandwidth().getEgBandwidth();
                    assert (actualAzERO.equals(expectedAzERO));
                    assert (actualZaERO.equals(expectedZaERO));
                }
                else
                {
                    expectedAzERO = "nodeL-nodeL:3-nodeN:1-nodeN";
                    expectedZaERO = "nodeN-nodeN:1-nodeL:3-nodeL";
                    assert (theFix.getIfceUrn().getUrn().equals("portB"));
                    assert (theFix.getReservedBandwidth().getInBandwidth().equals(baBW));
                    assert (theFix.getReservedBandwidth().getEgBandwidth().equals(abBW));
                    totalBwIn_B += theFix.getReservedBandwidth().getInBandwidth();
                    totalBwEg_B += theFix.getReservedBandwidth().getEgBandwidth();
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

                assert (zJunc.getDeviceUrn().getUrn().equals("nodeM"));
                assert (aFix.getIfceUrn().getUrn().equals("portB"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(bzBW));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zbBW));
                totalBwIn_B += aFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_B += aFix.getReservedBandwidth().getEgBandwidth();
                assert (zFix.getIfceUrn().getUrn().equals("portZ"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(zbBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(bzBW));
                totalBwIn_Z += zFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_Z += zFix.getReservedBandwidth().getEgBandwidth();
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
            assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW) || theFix.getReservedBandwidth().getInBandwidth().equals(abBW));
            assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW) || theFix.getReservedBandwidth().getEgBandwidth().equals(baBW));
            totalBwIn_A += theFix.getReservedBandwidth().getInBandwidth();
            totalBwEg_A += theFix.getReservedBandwidth().getEgBandwidth();
            assert (actualAzERO.equals(expectedAzERO));
            assert (actualZaERO.equals(expectedZaERO));
        }

        // Check bandwidth consumption at fixture-ports
        assert(totalBwIn_A == abBW + azBW);
        assert(totalBwEg_A == baBW + zaBW);
        assert(totalBwIn_B == baBW + bzBW);
        assert(totalBwEg_B == abBW + zbBW);
        assert(totalBwIn_Z == zaBW + zbBW);
        assert(totalBwEg_Z == azBW + bzBW);

        log.info("test " + testName + " passed.");
    }

    @Test
    public void multipointPceTestComplexPalindrome()
    {
        String testName = "\'multipointPceTestComplex - Palindrome\'";
        log.info("Initializing test: " + testName + ".");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        // Set requested parameters
        String srcAB = "nodeP";
        String dstAB = "nodeN";
        String srcBZ = "nodeN";
        String dstBZ = "nodeM";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> bPorts = Collections.singletonList("portB");
        List<String> zPorts = Collections.singletonList("portZ");
        String vlan = "any";
        Integer abBW = 100;
        Integer baBW = 100;
        Integer bzBW1 = 100;
        Integer bzBW2 = 200;
        Integer zbBW = 100;

        PalindromicType palindrome = PalindromicType.PALINDROME;
        mpTopoBuilder.buildComplexMultipointTopo();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Set up Requested Pipes
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        RequestedVlanPipeE pipeAB1 = testBuilder.buildRequestedPipe(aPorts, srcAB, bPorts, dstAB, abBW, baBW, palindrome, vlan);
        RequestedVlanPipeE pipeAB2 = testBuilder.buildRequestedPipe(aPorts, srcAB, bPorts, dstAB, abBW, baBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ1 = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW1, zbBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ2 = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW2, zbBW, palindrome, vlan);

        reqPipes.add(pipeAB1);
        reqPipes.add(pipeAB2);
        reqPipes.add(pipeBZ1);
        reqPipes.add(pipeBZ2);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);

        log.info("Beginning test: " + testName + ".");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("test " + testName + " passed.");
    }


    @Test
    public void multipointPceTestComplexNonPalindrome()
    {
        String testName = "\'multipointPceTestComplex - Non-Palindrome\'";
        log.info("Initializing test: " + testName + ".");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        // Set requested parameters
        String srcAB = "nodeP";
        String dstAB = "nodeN";
        String srcBZ = "nodeN";
        String dstBZ = "nodeM";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> bPorts = Collections.singletonList("portB");
        List<String> zPorts = Collections.singletonList("portZ");
        String vlan = "any";
        Integer abBW = 100;
        Integer baBW = 100;
        Integer bzBW1 = 100;
        Integer bzBW2 = 200;
        Integer zbBW = 100;

        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        mpTopoBuilder.buildComplexMultipointTopo();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Set up Requested Pipes
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        RequestedVlanPipeE pipeAB1 = testBuilder.buildRequestedPipe(aPorts, srcAB, bPorts, dstAB, abBW, baBW, palindrome, vlan);
        RequestedVlanPipeE pipeAB2 = testBuilder.buildRequestedPipe(aPorts, srcAB, bPorts, dstAB, abBW, baBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ1 = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW1, zbBW, palindrome, vlan);
        RequestedVlanPipeE pipeBZ2 = testBuilder.buildRequestedPipe(bPorts, srcBZ, zPorts, dstBZ, bzBW2, zbBW, palindrome, vlan);

        reqPipes.add(pipeAB1);
        reqPipes.add(pipeAB2);
        reqPipes.add(pipeBZ1);
        reqPipes.add(pipeBZ2);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);

        log.info("Beginning test: " + testName + ".");

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
        assert(allResMplsPipes.size() == 4);

        int totalBwIn_A = 0;
        int totalBwEg_A = 0;
        int totalBwIn_B = 0;
        int totalBwEg_B = 0;
        int totalBwIn_Z = 0;
        int totalBwEg_Z = 0;

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

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeN"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeM") || zJunc.getDeviceUrn().getUrn().equals("nodeN"));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE aFix = aFixes.iterator().next();
                ReservedVlanFixtureE zFix = zFixes.iterator().next();

                String expectedAzERO1 = "nodeP-nodeP:1-nodeL:1-nodeL-nodeL:3-nodeN:1-nodeN";
                String expectedAzERO2 = "nodeP-nodeP:2-nodeN:3-nodeN";
                String expectedZaERO = "nodeN-nodeN:1-nodeL:3-nodeL-nodeL:1-nodeP:1-nodeP";

                assert (aFix.getIfceUrn().getUrn().equals("portA"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(abBW));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(baBW));
                totalBwIn_A += aFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_A += aFix.getReservedBandwidth().getEgBandwidth();
                assert (zFix.getIfceUrn().getUrn().equals("portB"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(baBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(abBW));
                totalBwIn_B += zFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_B += zFix.getReservedBandwidth().getEgBandwidth();
                assert (actualAzERO.equals(expectedAzERO1) || actualAzERO.equals(expectedAzERO2));
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

                assert (aFix.getIfceUrn().getUrn().equals("portB"));
                assert (aFix.getReservedBandwidth().getInBandwidth().equals(bzBW1) || aFix.getReservedBandwidth().getInBandwidth().equals(bzBW2));
                assert (aFix.getReservedBandwidth().getEgBandwidth().equals(zbBW));
                totalBwIn_B += aFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_B += aFix.getReservedBandwidth().getEgBandwidth();
                assert (zFix.getIfceUrn().getUrn().equals("portZ"));
                assert (zFix.getReservedBandwidth().getInBandwidth().equals(zbBW));
                assert (zFix.getReservedBandwidth().getEgBandwidth().equals(bzBW1) || zFix.getReservedBandwidth().getEgBandwidth().equals(bzBW2));
                totalBwIn_Z += zFix.getReservedBandwidth().getInBandwidth();
                totalBwEg_Z += zFix.getReservedBandwidth().getEgBandwidth();
                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        // Check bandwidth consumption at fixture-ports
        assert(totalBwIn_A == abBW + abBW);
        assert(totalBwEg_A == baBW + baBW);
        assert(totalBwIn_B == baBW + baBW + bzBW1 + bzBW2);
        assert(totalBwEg_B == abBW + abBW + zbBW + zbBW);
        assert(totalBwIn_Z == zbBW + zbBW);
        assert(totalBwEg_Z == bzBW1 + bzBW2);

        log.info("test " + testName + " passed.");
    }
}
