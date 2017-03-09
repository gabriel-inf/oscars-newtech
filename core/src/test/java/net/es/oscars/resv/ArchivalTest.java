package net.es.oscars.resv;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.helpers.test.TopologyBuilder;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.ent.BidirectionalPathE;
import net.es.oscars.topo.ent.EdgeE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=CoreUnitTestConfiguration.class)
@Transactional
public class ArchivalTest
{
    @Autowired
    private ResvService resvService;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private TopPCE topPCE;

    @Test
    public void basicArchivalTest()
    {
        String testName = "basicArchivalTest";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo8();

        RequestedBlueprintE requestedBlueprint;
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
        ConnectionE conn;

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
                palindrome, survivability, vlan, 1, Integer.MAX_VALUE);
        reqPipes.add(pipeAZ);

        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1, "reusedBlueprint");

        conn = testBuilder.buildConnection(requestedBlueprint, testBuilder.buildSchedule(startDate, endDate), "conn1", "The Connection");

        log.info("Beginning test: \'" + testName + "\'.");

        // Reserve the connection
        try {
            resvService.hold(conn);
        }
        catch (PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        ReservedBlueprintE reservedBlueprint = conn.getReserved();
        ArchivedBlueprintE archivalBlueprint = conn.getArchivedResv();
        assert (reservedBlueprint != null);
        assert (archivalBlueprint == null);

        ReservedVlanFlowE reservedFlow = reservedBlueprint.getVlanFlow();
        assert (reservedFlow != null);

        Set<ReservedEthPipeE> allResEthPipes = reservedFlow.getEthPipes();
        Set<ReservedMplsPipeE> allResMplsPipes = reservedFlow.getMplsPipes();
        Set<ReservedVlanJunctionE> allResJunctions = reservedFlow.getJunctions();
        Set<BidirectionalPathE> resBiRoutes = reservedFlow.getAllPaths();

        assert(resBiRoutes.size() == 1);
        for(BidirectionalPathE biRoute : resBiRoutes)
        {
            List<EdgeE> resAZ = biRoute.getAzPath();
            List<EdgeE> resZA = biRoute.getZaPath();

            String resStringAZ = "portA,nodeP;nodeP,nodeP:2;nodeP:2,nodeQ:1;nodeQ:1,nodeQ;nodeQ,portZ";
            String resStringZA = "portZ,nodeQ;nodeQ,nodeQ:1;nodeQ:1,nodeP:1;nodeP:1,nodeP;nodeP,portA";

            for(EdgeE oneAzEdge : resAZ)
                resStringAZ += oneAzEdge.getOrigin() + "," + oneAzEdge.getTarget() + ";";

            for(EdgeE oneZaEdge : resZA)
                resStringZA += oneZaEdge.getOrigin() + "," + oneZaEdge.getTarget() + ";";
        }

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 0);
        assert (allResMplsPipes.size() == 1);

        ReservedMplsPipeE resPipe = allResMplsPipes.stream().findFirst().get();
        ReservedVlanJunctionE resJuncA = resPipe.getAJunction();
        ReservedVlanJunctionE resJuncZ = resPipe.getZJunction();

        String aJuncURN = resJuncA.getDeviceUrn();
        String zJuncURN = resJuncZ.getDeviceUrn();

        assert(aJuncURN.equals("nodeP"));
        assert(zJuncURN.equals("nodeQ"));

        EthJunctionType aJuncType = resJuncA.getJunctionType();
        EthJunctionType zJuncType = resJuncZ.getJunctionType();

        Set<ReservedVlanFixtureE> aFixes = resJuncA.getFixtures();
        Set<ReservedVlanFixtureE> zFixes = resJuncZ.getFixtures();

        assert(aFixes.size() == 1);
        assert(zFixes.size() == 1);

        ReservedVlanFixtureE aFix = aFixes.stream().findFirst().get();
        ReservedVlanFixtureE zFix = zFixes.stream().findFirst().get();

        long aFix_id = aFix.getId();
        long zFix_id = zFix.getId();

        assert(aFix.getIfceUrn().equals("portA"));
        assert(zFix.getIfceUrn().equals("portZ"));

        Set<ReservedVlanE> aVlans = aFix.getReservedVlans();
        Set<ReservedVlanE> zVlans = zFix.getReservedVlans();

        assert(aVlans.size() == 1);
        assert(zVlans.size() == 1);

        ReservedBandwidthE aBW = aFix.getReservedBandwidth();
        ReservedBandwidthE zBW = zFix.getReservedBandwidth();

        assert(aBW.getUrn().equals("portA"));
        assert(zBW.getUrn().equals("portZ"));
        assert(aBW.getInBandwidth() == 25);
        assert(aBW.getEgBandwidth() == 25);
        assert(zBW.getInBandwidth() == 25);
        assert(zBW.getEgBandwidth() == 25);
        assert(aBW.getBeginning().equals(startDate.toInstant()));
        assert(zBW.getBeginning().equals(startDate.toInstant()));
        assert(aBW.getEnding().equals(endDate.toInstant()));
        assert(zBW.getEnding().equals(endDate.toInstant()));
        //assert(aBW.getContainerConnectionId().equals(conn.getConnectionId()) || aBW.getContainerConnectionId() == null);
        //assert(zBW.getContainerConnectionId().equals(conn.getConnectionId()) || zBW.getContainerConnectionId() == null);

        long aBW_id = aBW.getId();
        long zBW_id = zBW.getId();



        // Archive the connection
        resvService.archiveReservation(conn);

        archivalBlueprint = conn.getArchivedResv();
        assert (archivalBlueprint != null);
    }
}
