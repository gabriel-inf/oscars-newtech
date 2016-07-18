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


        // Build Topology
        topologyBuilder.buildTopo1();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        String vlan = "any";
        String src = "nodeK";
        List<String> ports = Arrays.asList("portA", "portZ");

        // Set Expected Parameters - Per Expected Pipe

        // Containers for requested junctions
        List<Junction> reqJunctions = new ArrayList<>();
        List<Pipe> reqPipes = new ArrayList<>();

        // Containers for expected junctions/pipes
        List<Junction> expectedJunctions = new ArrayList<>();
        List<Pipe> expectedEthPipes = new ArrayList<>();
        List<Pipe> expectedMplsPipes = new ArrayList<>();

        // Set up Requested Junctions
        Junction reqJunction = testBuilder.makeJunction(src, ports, azBw, zaBw, vlan, true);
        reqJunctions.add(reqJunction);

        // Set up Requested Pipes

        // Set up Expected Single Junctions
        expectedJunctions.add(reqJunction);

        // Set up Expected Ethernet Pipes

        // Set up Expected MPLS Pipes

        // Run the test
        log.info("Beginning test: 'basicPceTest1'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'basicPceTest1' passed.");
    }

    @Test
    public void basicPceTest2()
    {

        log.info("Initializing test: 'basicPceTest2'.");

        // Build Topology
        topologyBuilder.buildTopo2();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeP";
        String dst = "nodeM";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

        // Set Expected Parameters - Per Expected Pipe

        List<String> potentialAzEROsPipe1 = Collections.singletonList("nodeP-nodeP:1-nodeL:1-nodeL");
        List<String> potentialZaEROsPipe1 = Collections.singletonList("nodeL-nodeL:1-nodeP:1-nodeP");

        List<String> potentialAzEROsPipe2 = Collections.singletonList("nodeL-nodeL:2-nodeM:1-nodeM");
        List<String> potentialZaEROsPipe2 = Collections.singletonList("nodeM-nodeM:1-nodeL:2-nodeL");

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
        Pipe pipePR = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipePR);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes
        Pipe expectedEthPipe1 = testBuilder.makeExpectedPipe(src, "nodeL", aPorts, new ArrayList<>(), azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedEthPipes.add(expectedEthPipe1);

        Pipe expectedEthPipe2 = testBuilder.makeExpectedPipe("nodeL", dst, new ArrayList<>(), zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe2, potentialZaEROsPipe2,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedEthPipes.add(expectedEthPipe2);

        // Set up Expected MPLS Pipes

        // Run the test
        log.info("Beginning test: 'basicPceTest2'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'basicPceTest2' passed.");
    }

    @Test
    public void basicPceTest3()
    {
        log.info("Initializing test: 'basicPceTest3'.");

        // Build Topology
        topologyBuilder.buildTopo3();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeK";
        String dst = "nodeQ";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

        // Set Expected Parameters - Per Expected Pipe

        List<String> potentialAzEROsPipe1 = Collections.singletonList("nodeK-nodeK:1-nodeP:1-nodeP");
        List<String> potentialZaEROsPipe1 = Collections.singletonList("nodeP-nodeP:1-nodeK:1-nodeK");

        List<String> potentialAzEROsPipe2 = Collections.singletonList("nodeP-nodeP:2-nodeQ:1-nodeQ");
        List<String> potentialZaEROsPipe2 = Collections.singletonList("nodeQ-nodeQ:1-nodeP:2-nodeP");

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
        Pipe pipePR = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipePR);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes
        Pipe expectedEthPipe1 = testBuilder.makeExpectedPipe(src, "nodeP", aPorts, new ArrayList<>(), azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedEthPipes.add(expectedEthPipe1);

        // Set up Expected MPLS Pipes
        Pipe expectedMplsPipe1 = testBuilder.makeExpectedPipe("nodeP", dst, new ArrayList<>(), zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe2, potentialZaEROsPipe2,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedMplsPipes.add(expectedMplsPipe1);

        // Run the test
        log.info("Beginning test: 'basicPceTest3'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'basicPceTest3' passed.");
    }

    @Test
    public void basicPceTest4()
    {
        // Two possible shortest routes here!
        log.info("Initializing test: 'basicPceTest4'.");


        // Build Topology
        topologyBuilder.buildTopo4();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeK";
        String dst = "nodeQ";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

        // Set Expected Parameters - Per Expected Pipe

        List<String> potentialAzEROsPipe1 = Arrays.asList("nodeK-nodeK:1-nodeL:1-nodeL", "nodeK-nodeK:2-nodeM:1-nodeM");
        List<String> potentialZaEROsPipe1 = Arrays.asList("nodeL-nodeL:1-nodeK:1-nodeK", "nodeM-nodeM:1-nodeK:2-nodeK");

        List<String> potentialAzEROsPipe2 = Arrays.asList("nodeL-nodeL:3-nodeP:1-nodeP", "nodeM-nodeM:3-nodeR:1-nodeR");
        List<String> potentialZaEROsPipe2 = Arrays.asList("nodeP-nodeP:1-nodeL:3-nodeL", "nodeR-nodeR:1-nodeM:3-nodeM");

        List<String> potentialAzEROsPipe3 = Arrays.asList("nodeP-nodeP:2-nodeQ:1-nodeQ", "nodeR-nodeR:3-nodeQ:2-nodeQ");
        List<String> potentialZaEROsPipe3 = Arrays.asList("nodeQ-nodeQ:1-nodeP:2-nodeP", "nodeQ-nodeQ:2-nodeR:3-nodeR");

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
        Pipe pipePR = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipePR);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes
        Pipe expectedEthPipe1 = testBuilder.makeExpectedPipe(src, "nodeM", aPorts, new ArrayList<>(), azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        Pipe expectedEthPipe2 = testBuilder.makeExpectedPipe("nodeL", "nodeP", new ArrayList<>(), new ArrayList<>(), azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe2, potentialZaEROsPipe2,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedEthPipes.add(expectedEthPipe1);
        expectedEthPipes.add(expectedEthPipe2);

        // Set up Expected MPLS Pipes
        Pipe expectedMplsPipe1 = testBuilder.makeExpectedPipe("nodeP", dst, new ArrayList<>(), zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe3, potentialZaEROsPipe3,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedMplsPipes.add(expectedMplsPipe1);

        // Run the test
        log.info("Beginning test: 'basicPceTest4'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);


        log.info("test 'basicPceTest4' passed.");

    }

    @Test
    public void basicPceTest5()
    {

        log.info("Initializing test: 'basicPceTest5'.");


        // Build Topology
        topologyBuilder.buildTopo5();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeK";
        String dst = "nodeS";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

        // Set Expected Parameters - Per Expected Pipe

        List<String> potentialAzEROsPipe1 = Collections.singletonList("nodeK-nodeK:2-nodeP:1-nodeP");
        List<String> potentialZaEROsPipe1 = Collections.singletonList("nodeP-nodeP:1-nodeK:2-nodeK");

        List<String> potentialAzEROsPipe2 = Collections.singletonList("nodeQ-nodeQ:3-nodeS:1-nodeS");
        List<String> potentialZaEROsPipe2 = Collections.singletonList("nodeS-nodeS:1-nodeQ:3-nodeQ");

        List<String> potentialAzEROsPipe3 = Collections.singletonList("nodeP-nodeP:3-nodeQ:1-nodeQ");
        List<String> potentialZaEROsPipe3 = Collections.singletonList("nodeQ-nodeQ:1-nodeP:3-nodeP");

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
        Pipe pipePR = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipePR);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes
        Pipe expectedEthPipe1 = testBuilder.makeExpectedPipe(src, "nodeP", aPorts, new ArrayList<>(), azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        Pipe expectedEthPipe2 = testBuilder.makeExpectedPipe("nodeP", "nodeQ", new ArrayList<>(), new ArrayList<>(), azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe2, potentialZaEROsPipe2,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedEthPipes.add(expectedEthPipe1);
        expectedEthPipes.add(expectedEthPipe2);

        // Set up Expected MPLS Pipes
        Pipe expectedMplsPipe1 = testBuilder.makeExpectedPipe("nodeQ", dst, new ArrayList<>(), zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe3, potentialZaEROsPipe3,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedMplsPipes.add(expectedMplsPipe1);

        // Run the test
        log.info("Beginning test: 'basicPceTest5'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);


        log.info("test 'basicPceTest5' passed.");
    }

    @Test
    public void basicPceTest6()
    {
        log.info("Initializing test: 'basicPceTest6'.");


        // Build Topology
        topologyBuilder.buildTopo6();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        String vlan = "any";
        String src = "nodeP";
        List<String> ports = Arrays.asList("portA", "portZ");

        // Set Expected Parameters - Per Expected Pipe

        // Containers for requested junctions
        List<Junction> reqJunctions = new ArrayList<>();
        List<Pipe> reqPipes = new ArrayList<>();

        // Containers for expected junctions/pipes
        List<Junction> expectedJunctions = new ArrayList<>();
        List<Pipe> expectedEthPipes = new ArrayList<>();
        List<Pipe> expectedMplsPipes = new ArrayList<>();

        // Set up Requested Junctions
        Junction reqJunction = testBuilder.makeJunction(src, ports, azBw, zaBw, vlan, true);
        reqJunctions.add(reqJunction);

        // Set up Requested Pipes

        // Set up Expected Single Junctions
        expectedJunctions.add(reqJunction);

        // Set up Expected Ethernet Pipes

        // Set up Expected MPLS Pipes

        // Run the test
        log.info("Beginning test: 'basicPceTest6'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);


        log.info("test 'basicPceTest6' passed.");
    }

    @Test
    public void basicPceTest7()
    {
        log.info("Initializing test: 'basicPceTest7'.");

        // Build Topology
        topologyBuilder.buildTopo7();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeK";
        String dst = "nodeL";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

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
        Pipe pipePR = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipePR);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes
        Pipe expectedEthPipe1 = testBuilder.makeExpectedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedEthPipes.add(expectedEthPipe1);

        // Set up Expected MPLS Pipes

        // Run the test
        log.info("Beginning test: 'basicPceTest7'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'basicPceTest7' passed.");
    }

    @Test
    public void basicPceTest8()
    {
        log.info("Initializing test: 'basicPceTest8'.");

        // Build Topology
        topologyBuilder.buildTopo8();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeP";
        String dst = "nodeQ";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

        // Set Expected Parameters - Per Expected Pipe

        List<String> potentialAzEROsPipe1 = Collections.singletonList("nodeP-nodeP:1-nodeQ:1-nodeQ");
        List<String> potentialZaEROsPipe1 = Collections.singletonList("nodeQ-nodeQ:1-nodeP:1-nodeP");
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
        Pipe pipePR = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipePR);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes

        // Set up Expected MPLS Pipes
        Pipe expectedMplsPipe1 = testBuilder.makeExpectedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedMplsPipes.add(expectedMplsPipe1);

        // Run the test
        log.info("Beginning test: 'basicPceTest8'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'basicPceTest8' passed.");
    }

    @Test
    public void basicPceTest9()
    {

        log.info("Initializing test: 'basicPceTest9'.");

        // Build Topology
        topologyBuilder.buildTopo9();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeK";
        String dst = "nodeP";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

        // Set Expected Parameters - Per Expected Pipe

        List<String> potentialAzEROsPipe1 = Collections.singletonList("nodeK-nodeK:1-nodeP:1-nodeP");
        List<String> potentialZaEROsPipe1 = Collections.singletonList("nodeP-nodeP:1-nodeK:1-nodeK");
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
        Pipe pipePR = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipePR);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes
        Pipe expectedEthPipe1 = testBuilder.makeExpectedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedEthPipes.add(expectedEthPipe1);

        // Set up Expected MPLS Pipes

        // Run the test
        log.info("Beginning test: 'basicPceTest9'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'basicPceTest9' passed.");
    }

    @Test
    public void basicPceTest10()
    {
        log.info("Initializing test: 'basicPceTest10'.");

        // Build Topology
        topologyBuilder.buildTopo10();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeK";
        String dst = "nodeM";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

        // Set Expected Parameters - Per Expected Pipe

        List<String> potentialAzEROsPipe1 = Collections.singletonList("nodeK-nodeK:1-nodeL:1-nodeL");
        List<String> potentialZaEROsPipe1 = Collections.singletonList("nodeL-nodeL:1-nodeK:1-nodeK");
        List<String> potentialAzEROsPipe2 = Collections.singletonList("nodeL-nodeL:2-nodeM:1-nodeM");
        List<String> potentialZaEROsPipe2 = Collections.singletonList("nodeM-nodeM:1-nodeL:2-nodeL");
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
        Pipe pipePR = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipePR);

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
        log.info("Beginning test: 'basicPceTest10'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'basicPceTest10' passed.");
    }

    @Test
    public void basicPceTest11()
    {
        log.info("Initializing test: 'basicPceTest11'.");

        // Build Topology
        topologyBuilder.buildTopo11();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeP";
        String dst = "nodeR";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

        // Set Expected Parameters - Per Expected Pipe
        List<String> potentialAzEROsPipe1 = Collections.singletonList("nodeP-nodeP:1-nodeQ:1-nodeQ-nodeQ:2-nodeR:1-nodeR");
        List<String> potentialZaEROsPipe1 = Collections.singletonList("nodeR-nodeR:1-nodeQ:2-nodeQ-nodeQ:1-nodeP:1-nodeP");
        List<Integer> expectedAZInBandwidthsPipe1 = Arrays.asList(azBw, azBw, azBw, azBw);
        List<Integer> expectedAZEgBandwidthsPipe1 = Arrays.asList(azBw, azBw, azBw, azBw);
        List<Integer> expectedZAInBandwidthsPipe1 = Arrays.asList(zaBw, zaBw, zaBw, zaBw);
        List<Integer> expectedZAEgBandwidthsPipe1 = Arrays.asList(zaBw, zaBw, zaBw, zaBw);

        // Containers for requested junctions
        List<Junction> reqJunctions = new ArrayList<>();
        List<Pipe> reqPipes = new ArrayList<>();

        // Containers for expected junctions/pipes
        List<Junction> expectedJunctions = new ArrayList<>();
        List<Pipe> expectedEthPipes = new ArrayList<>();
        List<Pipe> expectedMplsPipes = new ArrayList<>();

        // Set up Requested Junctions

        // Set up Requested Pipes
        Pipe pipePR = testBuilder.makeRequestedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome);
        reqPipes.add(pipePR);

        // Set up Expected Single Junctions

        // Set up Expected Ethernet Pipes

        // Set up Expected MPLS Pipes
        Pipe expectedMplsPipe1 = testBuilder.makeExpectedPipe(src, dst, aPorts, zPorts, azBw, zaBw, vlan, palindrome,
                potentialAzEROsPipe1, potentialZaEROsPipe1,
                expectedAZInBandwidthsPipe1, expectedAZEgBandwidthsPipe1,
                expectedZAInBandwidthsPipe1, expectedZAEgBandwidthsPipe1);
        expectedMplsPipes.add(expectedMplsPipe1);

        // Run the test
        log.info("Beginning test: 'basicPceTest11'.");

        pceTest(reqJunctions, reqPipes, expectedJunctions, expectedEthPipes, expectedMplsPipes);

        log.info("test 'basicPceTest11' passed.");
    }

    @Test
    public void basicPceTest12()
    {
        log.info("Initializing test: 'basicPceTest12'.");

        // Build Topology
        topologyBuilder.buildTopo12();

        // Set requested parameters
        Integer azBw = 25;
        Integer zaBw = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String vlan = "any";
        String src = "nodeK";
        String dst = "nodeQ";
        List<String> aPorts = Collections.singletonList("portA");
        List<String> zPorts = Collections.singletonList("portZ");

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

            String aURN = aJunction.getDeviceUrn().getUrn();
            String zURN = zJunction.getDeviceUrn().getUrn();

            boolean eroMatch = expectedPipe.getPotentialAZEROs().stream().anyMatch(ero -> ero.contains(aURN) && ero.contains(zURN)
                    && !aURN.equals(expectedPipe.aJunction.getUrn()) && !zURN.equals(expectedPipe.zJunction.getUrn()));

            if(aURN.equals(expectedPipe.aJunction.getUrn()) && zURN.equals(expectedPipe.zJunction.getUrn()) || eroMatch){

                matchPipeFields(aJunction, zJunction, azERO, zaERO, reservedBandwidths, expectedPipe, eroMatch);
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

            String aURN = aJunction.getDeviceUrn().getUrn();
            String zURN = zJunction.getDeviceUrn().getUrn();

            boolean eroMatch = expectedPipe.getPotentialAZEROs().stream().anyMatch(ero -> ero.contains(aURN) && ero.contains(zURN)
            && !aURN.equals(expectedPipe.aJunction.getUrn()) && !zURN.equals(expectedPipe.zJunction.getUrn()));

            if(aURN.equals(expectedPipe.aJunction.getUrn()) && zURN.equals(expectedPipe.zJunction.getUrn()) || eroMatch){

                matchPipeFields(aJunction, zJunction, azERO, zaERO, reservedBandwidths, expectedPipe, eroMatch);
            }
        }
    }

    private void matchPipeFields(ReservedVlanJunctionE aJunc, ReservedVlanJunctionE zJunc, List<String> azERO,
                                      List<String> zaERO, Set<ReservedBandwidthE> reservedBandwidths, Pipe expectedPipe,
                                 boolean alternateEROMatch){
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


        if(!alternateEROMatch){
            matchJunction(aJunc, expectedAJunction);
            matchJunction(zJunc, expectedZJunction);
        }
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
