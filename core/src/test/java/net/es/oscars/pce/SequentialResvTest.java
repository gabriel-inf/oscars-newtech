package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.AsymmTopologyBuilder;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.svc.TopoService;
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
 * Created by jeremy on 7/20/16.
 * <p>
 * Tests End-to-End correctness of the PCE modules and reservation persistence through processing of sequential connections.
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class SequentialResvTest {
    @Autowired
    private ResvService resvService;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private AsymmTopologyBuilder asymmTopologyBuilder;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private TopoService topoService;


    @Test
    public void sequentialResvTest1() {
        String testName = "sequentialResvTest1";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo8();
        bwRepo.deleteAll();

        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE connection1;
        ConnectionE connection2;
        ConnectionE connection3;
        List<ConnectionE> allConnections = new ArrayList<>();

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1);
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        connection1 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn1", "First Connection");
        connection2 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn2", "Next Connection");
        connection3 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn3", "Last Connection");

        allConnections.add(connection1);
        allConnections.add(connection2);
        allConnections.add(connection3);

        log.info("Beginning test: \'" + testName + "\'.");

        for (ConnectionE oneConnection : allConnections) {
            try {
                resvService.hold(oneConnection);
            } catch (PCEException | PSSException pceE) {
                log.error("", pceE);
            }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();
            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert (reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert (allResJunctions.size() == 0);
            assert (allResEthPipes.size() == 0);
            assert (allResMplsPipes.size() == 1);
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert (portBWs.size() == 6);

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for (ReservedBandwidthE oneBW : portBWs) {
            if (oneBW.getUrn().equals("portA")) {
                assert (oneBW.getInBandwidth().equals(azBW));
                assert (oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            } else {
                assert (oneBW.getInBandwidth().equals(zaBW));
                assert (oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert (totalBwAIn == azBW * 3);
        assert (totalBwAEg == zaBW * 3);
        assert (totalBwZIn == zaBW * 3);
        assert (totalBwZEg == azBW * 3);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void sequentialResvInsufficientBW() {
        String testName = "sequentialResvInsufficientBW";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo8();
        bwRepo.deleteAll();

        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE connection1;
        ConnectionE connection2;
        ConnectionE connection3;
        List<ConnectionE> allConnections = new ArrayList<>();

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 334;
        Integer zaBW = 334;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1);
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        connection1 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn1", "First Connection");
        connection2 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn2", "Next Connection");
        connection3 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn3", "Last Connection");

        allConnections.add(connection1);
        allConnections.add(connection2);
        allConnections.add(connection3);

        log.info("Beginning test: \'" + testName + "\'.");

        for (ConnectionE oneConnection : allConnections) {
            log.info("Connection ID: " + oneConnection.getConnectionId());

            try {
                resvService.hold(oneConnection);
            } catch (PCEException | PSSException pceE) {
                log.error("", pceE);
            }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();

            if (oneConnection.getConnectionId().equals("conn3")) {
                assert(oneConnection.getReserved().getVlanFlow().getEthPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getMplsPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getJunctions().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getAllPaths().isEmpty());
                continue;
            }

            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert (reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert (allResJunctions.size() == 0);
            assert (allResEthPipes.size() == 0);
            assert (allResMplsPipes.size() == 1);
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert (portBWs.size() == 4);

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for (ReservedBandwidthE oneBW : portBWs) {
            if (oneBW.getUrn().equals("portA")) {
                assert (oneBW.getInBandwidth().equals(azBW));
                assert (oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            } else {
                assert (oneBW.getInBandwidth().equals(zaBW));
                assert (oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert (totalBwAIn == azBW * 2);
        assert (totalBwAEg == zaBW * 2);
        assert (totalBwZIn == zaBW * 2);
        assert (totalBwZEg == azBW * 2);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void sequentialResvInsufficientVlan() {
        String testName = "sequentialResvInsufficientVlan";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo7();
        bwRepo.deleteAll();

        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE connection;
        List<ConnectionE> allConnections = new ArrayList<>();

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeL";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 1;
        Integer zaBW = 1;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1);
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        int numConnections = 6; // Enough to run out of VLANS

        for (int conn = 1; conn <= numConnections; conn++) {
            String connID = "conn" + conn;

            connection = testBuilder.buildConnection(requestedBlueprint, requestedSched, connID, "A Connection");

            allConnections.add(connection);
        }

        log.info("Beginning test: \'" + testName + "\'.");


        List<Integer> usedVlanIDsA = new ArrayList<>();
        List<Integer> usedVlanIDsZ = new ArrayList<>();

        for (ConnectionE oneConnection : allConnections) {
            log.info("Connection ID: " + oneConnection.getConnectionId());

            try {
                resvService.hold(oneConnection);
            } catch (PCEException | PSSException pceE) {
                log.error("", pceE);
            }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();

            if (oneConnection.getConnectionId().equals("conn6")) {
                assert(oneConnection.getReserved().getVlanFlow().getEthPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getMplsPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getJunctions().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getAllPaths().isEmpty());
                continue;
            }

            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert (reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert (allResJunctions.size() == 0);
            assert (allResEthPipes.size() == 1);
            assert (allResMplsPipes.size() == 0);

            ReservedVlanFixtureE portA = allResEthPipes.iterator().next().getAJunction().getFixtures().iterator().next();
            ReservedVlanFixtureE portZ = allResEthPipes.iterator().next().getZJunction().getFixtures().iterator().next();
            Set<Integer> vlansA = portA.getReservedVlans().stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());
            Set<Integer> vlansZ = portZ.getReservedVlans().stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());

            assert (!usedVlanIDsA.containsAll(vlansA));
            assert (!usedVlanIDsZ.containsAll(vlansZ));

            usedVlanIDsA.addAll(vlansA);
            usedVlanIDsZ.addAll(vlansZ);
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert (portBWs.size() == 2 * (numConnections - 1));

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for (ReservedBandwidthE oneBW : portBWs) {
            if (oneBW.getUrn().equals("portA")) {
                assert (oneBW.getInBandwidth().equals(azBW));
                assert (oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            } else {
                assert (oneBW.getInBandwidth().equals(zaBW));
                assert (oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert (totalBwAIn == azBW * (numConnections - 1));
        assert (totalBwAEg == zaBW * (numConnections - 1));
        assert (totalBwZIn == zaBW * (numConnections - 1));
        assert (totalBwZEg == azBW * (numConnections - 1));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void sequentialResvInsufficientVlan2() {
        String testName = "sequentialResvInsufficientVlan2";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo8();
        bwRepo.deleteAll();

        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE connection;
        List<ConnectionE> allConnections = new ArrayList<>();

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 1;
        Integer zaBW = 1;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1);
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        int numConnections = 6; // Enough to run out of VLANS

        for (int conn = 1; conn <= numConnections; conn++) {
            String connID = "conn" + conn;

            connection = testBuilder.buildConnection(requestedBlueprint, requestedSched, connID, "A Connection");

            allConnections.add(connection);
        }

        log.info("Beginning test: \'" + testName + "\'.");


        List<Integer> usedVlanIDsA = new ArrayList<>();
        List<Integer> usedVlanIDsZ = new ArrayList<>();

        for (ConnectionE oneConnection : allConnections) {
            log.info("Connection ID: " + oneConnection.getConnectionId());

            try {
                resvService.hold(oneConnection);
            } catch (PCEException | PSSException pceE) {
                log.error("", pceE);
            }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();

            if (oneConnection.getConnectionId().equals("conn6")) {
                assert(oneConnection.getReserved().getVlanFlow().getEthPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getMplsPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getJunctions().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getAllPaths().isEmpty());
                continue;
            }

            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert (reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert (allResJunctions.size() == 0);
            assert (allResEthPipes.size() == 0);
            assert (allResMplsPipes.size() == 1);

            ReservedVlanFixtureE portA = allResMplsPipes.iterator().next().getAJunction().getFixtures().iterator().next();
            ReservedVlanFixtureE portZ = allResMplsPipes.iterator().next().getZJunction().getFixtures().iterator().next();
            Set<Integer> vlansA = portA.getReservedVlans().stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());
            Set<Integer> vlansZ = portZ.getReservedVlans().stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());

            assert (!usedVlanIDsA.containsAll(vlansA));
            assert (!usedVlanIDsZ.containsAll(vlansZ));

            usedVlanIDsA.addAll(vlansA);
            usedVlanIDsZ.addAll(vlansZ);
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert (portBWs.size() == 2 * (numConnections - 1));

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for (ReservedBandwidthE oneBW : portBWs) {
            if (oneBW.getUrn().equals("portA")) {
                assert (oneBW.getInBandwidth().equals(azBW));
                assert (oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            } else {
                assert (oneBW.getInBandwidth().equals(zaBW));
                assert (oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert (totalBwAIn == azBW * (numConnections - 1));
        assert (totalBwAEg == zaBW * (numConnections - 1));
        assert (totalBwZIn == zaBW * (numConnections - 1));
        assert (totalBwZEg == azBW * (numConnections - 1));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void sequentialResvAlternatePaths() {
        String testName = "sequentialResvAlternatePaths";
        log.info("Initializing test: \'" + testName + "\'.");

        asymmTopologyBuilder.buildAsymmTopo13();
        bwRepo.deleteAll();

        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE connection1;
        ConnectionE connection2;
        ConnectionE connection3;
        ConnectionE connection4;
        ConnectionE connection5;
        List<ConnectionE> allConnections = new ArrayList<>();

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        String dstDevice = "nodeR";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 34;
        Integer zaBW = 34;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1);
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        connection1 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn1", "First Connection");
        connection2 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn2", "Second Connection");
        connection3 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn3", "Third Connection");
        connection4 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn4", "Fourth Connection");
        connection5 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn5", "Failed Connection");

        allConnections.add(connection1);
        allConnections.add(connection2);
        allConnections.add(connection3);
        allConnections.add(connection4);
        allConnections.add(connection5);

        log.info("Beginning test: \'" + testName + "\'.");

        for (ConnectionE oneConnection : allConnections) {
            log.info("Connection ID: " + oneConnection.getConnectionId());

            try {
                resvService.hold(oneConnection);
            } catch (PCEException | PSSException pceE) {
                log.error("", pceE);
            }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();

            if (oneConnection.getConnectionId().equals("conn5")) {
                assert(oneConnection.getReserved().getVlanFlow().getEthPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getMplsPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getJunctions().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getAllPaths().isEmpty());
                continue;
            }

            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert (reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert (allResJunctions.size() == 0);
            assert (allResEthPipes.size() == 0);
            assert (allResMplsPipes.size() == 1);
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert (portBWs.size() == 8);

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for (ReservedBandwidthE oneBW : portBWs) {
            if (oneBW.getUrn().equals("portA")) {
                assert (oneBW.getInBandwidth().equals(azBW));
                assert (oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            } else {
                assert (oneBW.getInBandwidth().equals(zaBW));
                assert (oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert (totalBwAIn == azBW * 4);
        assert (totalBwAEg == zaBW * 4);
        assert (totalBwZIn == zaBW * 4);
        assert (totalBwZEg == azBW * 4);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void sequentialResvNonPalindromicalPaths() {
        String testName = "sequentialResvNonPalindromicalPaths";
        log.info("Initializing test: \'" + testName + "\'.");

        asymmTopologyBuilder.buildAsymmTopo13_2();
        bwRepo.deleteAll();

        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE connection1;
        ConnectionE connection2;
        ConnectionE connection3;
        ConnectionE connection4;
        List<ConnectionE> allConnections = new ArrayList<>();

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        String dstDevice = "nodeR";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 34;
        Integer zaBW = 34;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1);
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        connection1 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn1", "First Connection");
        connection2 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn2", "Second Connection");
        connection3 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn3", "NonPal Connection");
        connection4 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn4", "Failed Connection");

        allConnections.add(connection1);
        allConnections.add(connection2);
        allConnections.add(connection3);
        allConnections.add(connection4);

        log.info("Beginning test: \'" + testName + "\'.");

        for (ConnectionE oneConnection : allConnections) {
            log.info("Connection ID: " + oneConnection.getConnectionId());

            try {
                resvService.hold(oneConnection);
            } catch (PCEException | PSSException pceE) {
                log.error("", pceE);
            }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();

            if (oneConnection.getConnectionId().equals("conn4")) {
                assert(oneConnection.getReserved().getVlanFlow().getEthPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getMplsPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getJunctions().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getAllPaths().isEmpty());
                continue;
            }

            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert (reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert (allResJunctions.size() == 0);
            assert (allResEthPipes.size() == 0);
            assert (allResMplsPipes.size() == 1);
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert (portBWs.size() == 6);

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for (ReservedBandwidthE oneBW : portBWs) {
            if (oneBW.getUrn().equals("portA")) {
                assert (oneBW.getInBandwidth().equals(azBW));
                assert (oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            } else {
                assert (oneBW.getInBandwidth().equals(zaBW));
                assert (oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert (totalBwAIn == azBW * 3);
        assert (totalBwAEg == zaBW * 3);
        assert (totalBwZIn == zaBW * 3);
        assert (totalBwZEg == azBW * 3);

        log.info("test \'" + testName + "\' passed.");
    }


    @Test
    public void sequentialResvIndependentSchedules1() {
        String testName = "sequentialResvIndependentSchedules1";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo8();
        bwRepo.deleteAll();

        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched1;
        ScheduleSpecificationE requestedSched2;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE connection1;
        ConnectionE connection2;
        ConnectionE connection3;
        List<ConnectionE> allConnections = new ArrayList<>();

        Date startDate1 = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        Date endDate1 = new Date(Instant.now().plus(2L, ChronoUnit.DAYS).getEpochSecond());

        Date startDate2 = new Date(Instant.now().plus(3L, ChronoUnit.DAYS).getEpochSecond());
        Date endDate2 = new Date(Instant.now().plus(4L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 334;
        Integer zaBW = 334;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1);
        requestedSched1 = testBuilder.buildSchedule(startDate1, endDate1);
        requestedSched2 = testBuilder.buildSchedule(startDate2, endDate2);

        connection1 = testBuilder.buildConnection(requestedBlueprint, requestedSched1, "conn1", "First Connection");
        connection2 = testBuilder.buildConnection(requestedBlueprint, requestedSched1, "conn2", "Next Connection");
        connection3 = testBuilder.buildConnection(requestedBlueprint, requestedSched2, "conn3", "Independent Connection");

        allConnections.add(connection1);
        allConnections.add(connection2);
        allConnections.add(connection3);

        log.info("Beginning test: \'" + testName + "\'.");

        for (ConnectionE oneConnection : allConnections) {
            log.info("Connection ID: " + oneConnection.getConnectionId());

            try {
                resvService.hold(oneConnection);
            } catch (PCEException | PSSException pceE) {
                log.error("", pceE);
            }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();

            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert (reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert (allResJunctions.size() == 0);
            assert (allResEthPipes.size() == 0);
            assert (allResMplsPipes.size() == 1);
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert (portBWs.size() == 6);

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for (ReservedBandwidthE oneBW : portBWs) {
            if (oneBW.getUrn().equals("portA")) {
                assert (oneBW.getInBandwidth().equals(azBW));
                assert (oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            } else {
                assert (oneBW.getInBandwidth().equals(zaBW));
                assert (oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert (totalBwAIn == azBW * 3);
        assert (totalBwAEg == zaBW * 3);
        assert (totalBwZIn == zaBW * 3);
        assert (totalBwZEg == azBW * 3);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void sequentialResvIndependentSchedules2() {
        String testName = "sequentialResvIndependentSchedules2";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo7();
        bwRepo.deleteAll();

        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched1;
        ScheduleSpecificationE requestedSched2;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE connection1;
        ConnectionE connection2;
        List<ConnectionE> allConnections = new ArrayList<>();

        Date startDate1 = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        Date endDate1 = new Date(Instant.now().plus(2L, ChronoUnit.DAYS).getEpochSecond());
        Date startDate2 = new Date(Instant.now().plus(3L, ChronoUnit.DAYS).getEpochSecond());
        Date endDate2 = new Date(Instant.now().plus(4L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeL";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 1;
        Integer zaBW = 1;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1);
        requestedSched1 = testBuilder.buildSchedule(startDate1, endDate1);
        requestedSched2 = testBuilder.buildSchedule(startDate2, endDate2);

        int numConnections = 12; // Enough to run out of VLANS in two different time-intervals

        for (int conn = 1; conn <= numConnections / 2; conn++) {
            String connID1 = "connT1-" + conn;
            String connID2 = "connT2-" + conn;
            connection1 = testBuilder.buildConnection(requestedBlueprint, requestedSched1, connID1, "T1 Connection");
            connection2 = testBuilder.buildConnection(requestedBlueprint, requestedSched2, connID2, "T2 Connection");
            allConnections.add(connection1);
            allConnections.add(connection2);
        }

        log.info("Beginning test: \'" + testName + "\'.");


        List<Integer> usedVlanIDsA_T1 = new ArrayList<>();
        List<Integer> usedVlanIDsZ_T1 = new ArrayList<>();
        List<Integer> usedVlanIDsA_T2 = new ArrayList<>();
        List<Integer> usedVlanIDsZ_T2 = new ArrayList<>();

        for (ConnectionE oneConnection : allConnections) {
            log.info("Connection ID: " + oneConnection.getConnectionId());

            try {
                resvService.hold(oneConnection);
            } catch (PCEException | PSSException pceE) {
                log.error("", pceE);
            }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();

            if (oneConnection.getConnectionId().equals("connT1-6") || oneConnection.getConnectionId().equals("connT2-6")) {
                assert(oneConnection.getReserved().getVlanFlow().getEthPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getMplsPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getJunctions().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getAllPaths().isEmpty());
                continue;
            }

            assert (reservedBlueprint != null);

            Date connectionStartDate = oneConnection.getSpecification().getScheduleSpec().getNotBefore();

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert (reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert (allResJunctions.size() == 0);
            assert (allResEthPipes.size() == 1);
            assert (allResMplsPipes.size() == 0);

            ReservedVlanFixtureE portA = allResEthPipes.iterator().next().getAJunction().getFixtures().iterator().next();
            ReservedVlanFixtureE portZ = allResEthPipes.iterator().next().getZJunction().getFixtures().iterator().next();
            Set<Integer> vlansA = portA.getReservedVlans().stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());
            Set<Integer> vlansZ = portZ.getReservedVlans().stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());


            if (connectionStartDate.equals(startDate1)) {
                assert (!usedVlanIDsA_T1.containsAll(vlansA));
                assert (!usedVlanIDsZ_T1.containsAll(vlansZ));

                usedVlanIDsA_T1.addAll(vlansA);
                usedVlanIDsZ_T1.addAll(vlansZ);
            } else {
                assert (!usedVlanIDsA_T2.containsAll(vlansA));
                assert (!usedVlanIDsZ_T2.containsAll(vlansZ));

                usedVlanIDsA_T2.addAll(vlansA);
                usedVlanIDsZ_T2.addAll(vlansZ);
            }
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert (portBWs.size() == 2 * (numConnections - 2));

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for (ReservedBandwidthE oneBW : portBWs) {
            if (oneBW.getUrn().equals("portA")) {
                assert (oneBW.getInBandwidth().equals(azBW));
                assert (oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            } else {
                assert (oneBW.getInBandwidth().equals(zaBW));
                assert (oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert (totalBwAIn == azBW * (numConnections - 2));
        assert (totalBwAEg == zaBW * (numConnections - 2));
        assert (totalBwZIn == zaBW * (numConnections - 2));
        assert (totalBwZEg == azBW * (numConnections - 2));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void sequentialResvSequentialPipes() {
        String testName = "sequentialResvSequentialPipes";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo8();
        bwRepo.deleteAll();

        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE connection1;
        ConnectionE connection2;
        ConnectionE connection3;
        List<ConnectionE> allConnections = new ArrayList<>();

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeP";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 167;
        Integer zaBW = 167;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;

        RequestedVlanPipeE pipeAZ1 = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        RequestedVlanPipeE pipeAZ2 = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW,
                palindrome, survivability, vlan, 1);
        reqPipes.add(pipeAZ1);
        reqPipes.add(pipeAZ2);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1);
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        connection1 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn1", "First Multipipe Connection");
        connection2 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn2", "Next Multipipe Connection");
        connection3 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn3", "Failed Multipipe Connection");

        allConnections.add(connection1);
        allConnections.add(connection2);
        allConnections.add(connection3);

        log.info("Beginning test: \'" + testName + "\'.");

        for (ConnectionE oneConnection : allConnections) {
            log.info("Connection ID: " + oneConnection.getConnectionId());

            try {
                resvService.hold(oneConnection);
            } catch (PCEException | PSSException pceE) {
                log.error("", pceE);
            }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();

            if (oneConnection.getConnectionId().equals("conn3")) {
                assert(oneConnection.getReserved().getVlanFlow().getEthPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getMplsPipes().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getJunctions().isEmpty());
                assert(oneConnection.getReserved().getVlanFlow().getAllPaths().isEmpty());
                continue;
            }

            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert (reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert (allResJunctions.size() == 0);
            assert (allResEthPipes.size() == 0);
            assert (allResMplsPipes.size() == 2);
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert (portBWs.size() == 8);

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for (ReservedBandwidthE oneBW : portBWs) {
            if (oneBW.getUrn().equals("portA")) {
                assert (oneBW.getInBandwidth().equals(azBW));
                assert (oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            } else {
                assert (oneBW.getInBandwidth().equals(zaBW));
                assert (oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert (totalBwAIn == azBW * 4);
        assert (totalBwAEg == zaBW * 4);
        assert (totalBwZIn == zaBW * 4);
        assert (totalBwZEg == azBW * 4);

        log.info("test \'" + testName + "\' passed.");
    }
}
