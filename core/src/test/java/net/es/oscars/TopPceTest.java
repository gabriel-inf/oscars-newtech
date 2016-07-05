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
    public void simpleTest()
    {
        log.info("Initializing test: 'simpleTest'.");

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

        this.buildSimpleTopo();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcDevice, portNames, azBW, zaBW, vlan);

        log.info("Beginning test: 'simpleTest'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException pceE){ log.error("", pceE); }
        catch(PSSException pssE){ log.error("", pssE); }

        assert(reservedBlueprint != null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();
        ReservedVlanJunctionE reservedJunction = reservedFlow.getJunctions().iterator().next();
        ReservedVlanFixtureE reservedFixtureA = reservedJunction.getFixtures().iterator().next();
        ReservedVlanFixtureE reservedFixtureZ = reservedJunction.getFixtures().iterator().next();

        assert(reservedFlow.getPipes().isEmpty());
        assert(reservedJunction.getDeviceUrn().getUrn().equals("nodeK"));
        assert(reservedFixtureA.getIfceUrn().getUrn().equals("portA") || reservedFixtureA.getIfceUrn().getUrn().equals("portZ"));
        assert(reservedFixtureZ.getIfceUrn().getUrn().equals("portZ") || reservedFixtureZ.getIfceUrn().getUrn().equals("portA"));

        log.info("test 'simpleTest' passed.");
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
        catch(PCEException pceE){ log.error("", pceE); }
        catch(PSSException pssE){ log.error("", pssE); }

        assert(reservedBlueprint != null);


        Set<ReservedEthPipeE> allResPipes;
        Set<ReservedBandwidthE> allresBW;

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlows().iterator().next();
        allResPipes = reservedFlow.getPipes();
        ReservedEthPipeE reservedPipe = allResPipes.iterator().next();

        allresBW = reservedPipe.getReservedBandwidths();

        ReservedVlanJunctionE reservedJunctionA = reservedPipe.getAJunction();
        ReservedVlanJunctionE reservedJunctionZ = reservedPipe.getZJunction();
        ReservedVlanFixtureE reservedFixA = reservedJunctionA.getFixtures().iterator().next();
        ReservedVlanFixtureE reservedFixZ = reservedJunctionZ.getFixtures().iterator().next();

        List<String> azERO = reservedPipe.getAzERO();
        List<String> zaERO = reservedPipe.getZaERO();

        String expectedAzERO = "portA-nodeP-portP:1-portL:1-nodeL-portL:2-portM:1-nodeM-portZ-";
        String expectedZaERO = "portZ-nodeM-portM:1-portL:2-nodeL-portL:1-portP:1-nodeP-portA-";

        String actualAzERO = "";
        String actualZaERO = "";

        for(String oneHop : azERO)
        {
            actualAzERO = actualAzERO + oneHop + "-";
        }

        for(String oneHop : zaERO)
        {
            actualZaERO = actualZaERO + oneHop + "-";
        }

        // Verify EROs //
        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));

        // Verify Pipes/Junctions/Fixtures //
        assert(allResPipes.size() == 1);

        for(ReservedBandwidthE oneResBW : allresBW)
        {
            assert(oneResBW.getInBandwidth() == azBW);
            assert(oneResBW.getEgBandwidth() == zaBW);
        }

        assert(reservedJunctionA.getDeviceUrn().getUrn().equals("nodeP"));
        assert(reservedJunctionZ.getDeviceUrn().getUrn().equals("nodeM"));
        assert(reservedJunctionA.getFixtures().size() == 1);
        assert(reservedJunctionZ.getFixtures().size() == 1);
        assert(reservedFixA.getIfceUrn().getUrn().equals("portA"));
        assert(reservedFixZ.getIfceUrn().getUrn().equals("portZ"));

        log.info("test 'basicPceTest2' passed.");
    }

    private void buildSimpleTopo()
    {
        log.info("Building Test Topology");

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
}
