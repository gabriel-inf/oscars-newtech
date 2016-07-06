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
        List<ReservedBandwidthE> allResBWs = new ArrayList<>();

        assert(allResPipes.size() == 1);
        assert(allResJunctions.size() == 0);

        ReservedEthPipeE onlyPipe = allResPipes.iterator().next();

        ReservedVlanJunctionE juncA = onlyPipe.getAJunction();
        ReservedVlanJunctionE juncZ = onlyPipe.getZJunction();
        Iterator<ReservedVlanFixtureE> iterA = juncA.getFixtures().iterator();
        Iterator<ReservedVlanFixtureE> iterZ = juncZ.getFixtures().iterator();

        ReservedVlanFixtureE fixA1 = iterA.next();
        ReservedVlanFixtureE fixA2 = iterA.next();
        ReservedVlanFixtureE fixZ1 = iterZ.next();
        ReservedVlanFixtureE fixZ2 = iterZ.next();

        assert(juncA.getDeviceUrn().getUrn().equals("nodeK"));
        assert(juncZ.getDeviceUrn().getUrn().equals("nodeQ"));
        assert(fixA1.getIfceUrn().getUrn().equals("portA") || fixA1.getIfceUrn().getUrn().equals("nodeK:1"));
        assert(fixA2.getIfceUrn().getUrn().equals("portA") || fixA2.getIfceUrn().getUrn().equals("nodeK:1"));
        assert(fixZ1.getIfceUrn().getUrn().equals("portA") || fixZ1.getIfceUrn().getUrn().equals("nodeK:1"));
        assert(fixZ2.getIfceUrn().getUrn().equals("portA") || fixZ2.getIfceUrn().getUrn().equals("nodeK:1"));

        assert(fixA1.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixA2.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixZ1.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixZ2.getReservedBandwidth().getInBandwidth() == azBW);
        assert(fixA1.getReservedBandwidth().getEgBandwidth() == azBW);
        assert(fixA2.getReservedBandwidth().getEgBandwidth() == azBW);
        assert(fixZ1.getReservedBandwidth().getEgBandwidth() == azBW);
        assert(fixZ2.getReservedBandwidth().getEgBandwidth() == azBW);

        List<String> azERO = onlyPipe.getAzERO();
        List<String> zaERO = onlyPipe.getZaERO();

        String actualAzERO = "";
        String actualZaERO = "";

        for(String x : azERO)
            actualAzERO = actualAzERO + x + "-";

        for(String x : zaERO)
            actualZaERO = actualZaERO + x + "-";

        String expectedAzERO = "nodeK-nodeK:1-nodeP:1-nodeP-nodeP:2-nodeQ:1-nodeQ-";
        String expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP-nodeP:1-nodeK:1-nodeK-";

        assert(actualAzERO.equals(expectedAzERO));
        assert(actualZaERO.equals(expectedZaERO));
        
        log.info("test 'basicPceTest3' passed.");
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
}
