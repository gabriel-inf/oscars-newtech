package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.enums.PalindromicType;
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
 *
 * Tests End-to-End correctness of the PCE modules and reservation persistence through processing of sequential connections.
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class SequentialResvTest
{
    @Autowired
    private ResvService resvService;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private TestEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private TopoService topoService;


    @Test
    public void sequentialResvTest1()
    {
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

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        connection1 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn1", "First Connection");
        connection2 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn2", "Next Connection");
        connection3 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn3", "Last Connection");

        allConnections.add(connection1);
        allConnections.add(connection2);
        allConnections.add(connection3);

        log.info("Beginning test: \'" + testName + "\'.");

        for(ConnectionE oneConnection : allConnections)
        {
            try
            {
                oneConnection = resvService.hold(oneConnection);
            }
            catch(PCEException | PSSException pceE){ log.error("", pceE); }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();
            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert(reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert(allResJunctions.size() == 0);
            assert(allResEthPipes.size() == 0);
            assert(allResMplsPipes.size() == 1);
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert(portBWs.size() == 6);

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for(ReservedBandwidthE oneBW : portBWs)
        {
            if(oneBW.getUrn().getUrn().equals("portA"))
            {
                assert(oneBW.getInBandwidth().equals(azBW));
                assert(oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            }
            else
            {
                assert(oneBW.getInBandwidth().equals(zaBW));
                assert(oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert(totalBwAIn == azBW * 3);
        assert(totalBwAEg == zaBW * 3);
        assert(totalBwZIn == zaBW * 3);
        assert(totalBwZEg == azBW * 3);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void sequentialResvTest2()
    {
        String testName = "sequentialResvTest2";
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

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes);
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        connection1 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn1", "First Connection");
        connection2 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn2", "Next Connection");
        connection3 = testBuilder.buildConnection(requestedBlueprint, requestedSched, "conn3", "Last Connection");

        allConnections.add(connection1);
        allConnections.add(connection2);
        allConnections.add(connection3);

        log.info("Beginning test: \'" + testName + "\'.");

        for(ConnectionE oneConnection : allConnections)
        {
            log.info("Connection ID: " + oneConnection.getConnectionId());

            try
            {
                oneConnection = resvService.hold(oneConnection);
            }
            catch(PCEException | PSSException pceE){ log.error("", pceE); }


            ReservedBlueprintE reservedBlueprint = oneConnection.getReserved();

            if(oneConnection.getConnectionId().equals("conn3"))
            {
                assert (reservedBlueprint == null);
                continue;
            }

            assert (reservedBlueprint != null);

            ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
            assert(reservedFlow != null);

            Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
            Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
            Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();

            assert(allResJunctions.size() == 0);
            assert(allResEthPipes.size() == 0);
            assert(allResMplsPipes.size() == 1);

            log.info("BW Reserved: ");
            bwRepo.findAll().stream()
                    .filter(bw -> bw.getUrn().getUrn().contains("port"))
                    .forEach(bw -> log.info("URN: " + bw.getUrn().getUrn() + ", In: " + bw.getInBandwidth() + "Mbps, Eg: " + bw.getEgBandwidth() + "Mbps"));
        }

        Set<ReservedBandwidthE> portBWs = bwRepo.findAll().stream()
                .filter(bw -> bw.getUrn().getUrn().contains("port"))
                .collect(Collectors.toSet());

        assert(portBWs.size() == 4);

        int totalBwAIn = 0;
        int totalBwAEg = 0;
        int totalBwZIn = 0;
        int totalBwZEg = 0;

        for(ReservedBandwidthE oneBW : portBWs)
        {
            if(oneBW.getUrn().getUrn().equals("portA"))
            {
                assert(oneBW.getInBandwidth().equals(azBW));
                assert(oneBW.getInBandwidth().equals(zaBW));
                totalBwAIn += oneBW.getInBandwidth();
                totalBwAEg += oneBW.getEgBandwidth();
            }
            else
            {
                assert(oneBW.getInBandwidth().equals(zaBW));
                assert(oneBW.getInBandwidth().equals(azBW));
                totalBwZIn += oneBW.getInBandwidth();
                totalBwZEg += oneBW.getEgBandwidth();
            }
        }

        assert(totalBwAIn == azBW * 2);
        assert(totalBwAEg == zaBW * 2);
        assert(totalBwZIn == zaBW * 2);
        assert(totalBwZEg == azBW * 2);

        log.info("test \'" + testName + "\' passed.");
    }
}
