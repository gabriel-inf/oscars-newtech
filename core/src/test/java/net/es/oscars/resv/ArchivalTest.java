package net.es.oscars.resv;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.MplsPipeType;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.pce.helpers.TopologyBuilder;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.ent.BidirectionalPathE;
import net.es.oscars.topo.ent.EdgeE;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Transactional
public class ArchivalTest extends AbstractCoreTest
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

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 1, 1);
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

        // Verify correct reservation heirarchy
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

        String expectedStringAZ = "portA,nodeP;nodeP,nodeP:1;nodeP:1,nodeQ:1;nodeQ:1,nodeQ;nodeQ,portZ;";
        String expectedStringZA = "portZ,nodeQ;nodeQ,nodeQ:1;nodeQ:1,nodeP:1;nodeP:1,nodeP;nodeP,portA;";

        String resStringAZ = "";
        String resStringZA = "";

        assert(resBiRoutes.size() == 1);

        BidirectionalPathE resBiRoute = resBiRoutes.stream().findFirst().get();

        List<EdgeE> resAZ = resBiRoute.getAzPath();
        List<EdgeE> resZA = resBiRoute.getZaPath();

        for(EdgeE oneAzEdge : resAZ)
            resStringAZ += oneAzEdge.getOrigin() + "," + oneAzEdge.getTarget() + ";";

        for(EdgeE oneZaEdge : resZA)
            resStringZA += oneZaEdge.getOrigin() + "," + oneZaEdge.getTarget() + ";";

        assert(resStringAZ.equals(expectedStringAZ));
        assert(resStringZA.equals(expectedStringZA));

        assert (allResJunctions.size() == 0);
        assert (allResEthPipes.size() == 0);
        assert (allResMplsPipes.size() == 1);

        ReservedMplsPipeE resPipe = allResMplsPipes.stream().findFirst().get();

        long resPipe_id = resPipe.getId();

        List<String> resPipeAzEro = resPipe.getAzERO();
        List<String> resPipeZaEro = resPipe.getZaERO();

        assert(resPipeAzEro.size() == 2);
        assert(resPipeZaEro.size() == 2);
        assert(resPipeAzEro.contains("nodeP:1"));
        assert(resPipeAzEro.contains("nodeQ:1"));
        assert(resPipeZaEro.contains("nodeP:1"));
        assert(resPipeZaEro.contains("nodeQ:1"));

        MplsPipeType resPipeType = resPipe.getPipeType();


        ReservedVlanJunctionE resJuncA = resPipe.getAJunction();
        ReservedVlanJunctionE resJuncZ = resPipe.getZJunction();

        long aJunc_id = resJuncA.getId();
        long zJunc_id = resJuncZ.getId();

        Set<ReservedVlanE> aJuncVlans = resJuncA.getReservedVlans();
        Set<ReservedVlanE> zJuncVlans = resJuncZ.getReservedVlans();

        assert(aJuncVlans.size() == 2);
        assert(zJuncVlans.size() == 2);

        for(ReservedVlanE oneVlan : aJuncVlans)
        {
            assert (oneVlan.getUrn().equals("portA") || oneVlan.getUrn().equals("nodeP:1"));
            assert (oneVlan.getBeginning().equals(startDate.toInstant()));
            assert (oneVlan.getEnding().equals(endDate.toInstant()));
        }

        for(ReservedVlanE oneVlan : zJuncVlans)
        {
            assert (oneVlan.getUrn().equals("portZ") || oneVlan.getUrn().equals("nodeQ:1"));
            assert (oneVlan.getBeginning().equals(startDate.toInstant()));
            assert (oneVlan.getEnding().equals(endDate.toInstant()));
        }

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

        EthFixtureType aFixType = aFix.getFixtureType();
        EthFixtureType zFixType = zFix.getFixtureType();

        long aFix_id = aFix.getId();
        long zFix_id = zFix.getId();

        assert(aFix.getIfceUrn().equals("portA"));
        assert(zFix.getIfceUrn().equals("portZ"));

        Set<ReservedVlanE> aVlans = aFix.getReservedVlans();
        Set<ReservedVlanE> zVlans = zFix.getReservedVlans();

        assert(aVlans.size() == 1);
        assert(zVlans.size() == 1);

        ReservedVlanE aVlan = aVlans.stream().findFirst().get();
        ReservedVlanE zVlan = zVlans.stream().findFirst().get();

        long aVlan_id = aVlan.getId();
        long zVlan_id = aVlan.getId();

        Integer aVlan_value = aVlan.getVlan();
        Integer zVlan_value = zVlan.getVlan();

        assert(aVlan.getUrn().equals("portA"));
        assert(zVlan.getUrn().equals("portZ"));
        assert(aVlan.getBeginning().equals(startDate.toInstant()));
        assert(zVlan.getBeginning().equals(startDate.toInstant()));
        assert(aVlan.getEnding().equals(endDate.toInstant()));
        assert(zVlan.getEnding().equals(endDate.toInstant()));

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

        long aBW_id = aBW.getId();
        long zBW_id = zBW.getId();

        Set<ReservedPssResourceE> aPSS = aFix.getReservedPssResources();
        Set<ReservedPssResourceE> zPSS = zFix.getReservedPssResources();

        assert(aPSS.size() == 0);
        assert(zPSS.size() == 0);



        // Archive the connection
        resvService.archiveReservation(conn);



        // Archival heirarchy should be equal to Reserved heirarchy
        archivalBlueprint = conn.getArchivedResv();
        assert (archivalBlueprint != null);

        ArchivedVlanFlowE archivedFlow = archivalBlueprint.getVlanFlow();
        assert (archivedFlow != null);

        Set<ArchivedEthPipeE> allArchEthPipes = archivedFlow.getEthPipes();
        Set<ArchivedMplsPipeE> allArchMplsPipes = archivedFlow.getMplsPipes();
        Set<ArchivedVlanJunctionE> allArchJunctions = archivedFlow.getJunctions();
        Set<BidirectionalPathE> archBiRoutes = archivedFlow.getAllPaths();

        assert(archBiRoutes.size() == 1);
        BidirectionalPathE archBiRoute = archBiRoutes.stream().findFirst().get();

        List<EdgeE> archAZ = archBiRoute.getAzPath();
        List<EdgeE> archZA = archBiRoute.getZaPath();

        String archStringAZ = "";
        String archStringZA = "";

        for(EdgeE oneAzEdge : archAZ)
            archStringAZ += oneAzEdge.getOrigin() + "," + oneAzEdge.getTarget() + ";";

        for(EdgeE oneZaEdge : archZA)
            archStringZA += oneZaEdge.getOrigin() + "," + oneZaEdge.getTarget() + ";";

        assert(archStringAZ.equals(resStringAZ));
        assert(archStringZA.equals(resStringZA));

        assert (allArchJunctions.size() == allResJunctions.size());
        assert (allArchEthPipes.size() == allResEthPipes.size());
        assert (allArchMplsPipes.size() == allResMplsPipes.size());

        ArchivedMplsPipeE archPipe = allArchMplsPipes.stream().findFirst().get();

        long archPipe_id = archPipe.getId();

        List<String> archPipeAzEro = archPipe.getAzERO();
        List<String> archPipeZaEro = archPipe.getZaERO();

        MplsPipeType archPipeType = archPipe.getPipeType();

        assert(archPipe_id == resPipe_id);
        assert(archPipeAzEro.equals(resPipeAzEro));
        assert(archPipeZaEro.equals(resPipeZaEro));
        assert(archPipeType.equals(resPipeType));

        ArchivedVlanJunctionE archJuncA = archPipe.getAJunction();
        ArchivedVlanJunctionE archJuncZ = archPipe.getZJunction();

        long aJunc_id_arch = archJuncA.getId();
        long zJunc_id_arch = archJuncZ.getId();

        assert(aJunc_id_arch == aJunc_id);
        assert(zJunc_id_arch == zJunc_id);

        Set<ArchivedVlanE> aArchJuncVlans = archJuncA.getReservedVlans();
        Set<ArchivedVlanE> zArchJuncVlans = archJuncZ.getReservedVlans();

        assert(aArchJuncVlans.size() == aJuncVlans.size());
        assert(zArchJuncVlans.size() == zJuncVlans.size());

        for(ArchivedVlanE oneVlan : aArchJuncVlans)
        {
            assert (oneVlan.getUrn().equals("portA") || oneVlan.getUrn().equals("nodeP:1"));
            assert (oneVlan.getBeginning().equals(startDate.toInstant()));
            assert (oneVlan.getEnding().equals(endDate.toInstant()));
        }

        for(ArchivedVlanE oneVlan : zArchJuncVlans)
        {
            assert (oneVlan.getUrn().equals("portZ") || oneVlan.getUrn().equals("nodeQ:1"));
            assert (oneVlan.getBeginning().equals(startDate.toInstant()));
            assert (oneVlan.getEnding().equals(endDate.toInstant()));
        }

        String aArchJuncURN = archJuncA.getDeviceUrn();
        String zArchJuncURN = archJuncZ.getDeviceUrn();

        assert(aArchJuncURN.equals(aJuncURN));
        assert(zArchJuncURN.equals(zJuncURN));

        EthJunctionType aArchJuncType = archJuncA.getJunctionType();
        EthJunctionType zArchJuncType = archJuncZ.getJunctionType();

        assert(aArchJuncType.equals(aJuncType));
        assert(zArchJuncType.equals(zJuncType));

        Set<ArchivedVlanFixtureE> aArchFixes = archJuncA.getFixtures();
        Set<ArchivedVlanFixtureE> zArchFixes = archJuncZ.getFixtures();

        assert(aArchFixes.size() == aFixes.size());
        assert(zArchFixes.size() == zFixes.size());

        ArchivedVlanFixtureE aArchFix = aArchFixes.stream().findFirst().get();
        ArchivedVlanFixtureE zArchFix = zArchFixes.stream().findFirst().get();

        EthFixtureType aArchFixType = aArchFix.getFixtureType();
        EthFixtureType zArchFixType = zArchFix.getFixtureType();

        long aArchFix_id = aArchFix.getId();
        long zArchFix_id = zArchFix.getId();

        assert(aArchFix_id == aFix_id);
        assert(zArchFix_id == zFix_id);
        assert(aArchFix.getIfceUrn().equals(aFix.getIfceUrn()));
        assert(zArchFix.getIfceUrn().equals(zFix.getIfceUrn()));

        Set<ArchivedVlanE> aArchVlans = aArchFix.getReservedVlans();
        Set<ArchivedVlanE> zArchVlans = zArchFix.getReservedVlans();

        assert(aArchVlans.size() == aVlans.size());
        assert(zArchVlans.size() == zVlans.size());

        ArchivedVlanE aArchVlan = aArchVlans.stream().findFirst().get();
        ArchivedVlanE zArchVlan = zArchVlans.stream().findFirst().get();

        long aArchVlan_id = aArchVlan.getId();
        long zArchVlan_id = aArchVlan.getId();
        Integer aArchVlan_value = aArchVlan.getVlan();
        Integer zArchVlan_value = zArchVlan.getVlan();

        assert(aArchVlan_id == aVlan_id);
        assert(zArchVlan_id == zVlan_id);
        assert(aArchVlan_value == aVlan_value);
        assert(zArchVlan_value == zVlan_value);
        assert(aArchVlan.getUrn().equals(aVlan.getUrn()));
        assert(zArchVlan.getUrn().equals(zVlan.getUrn()));
        assert(aArchVlan.getBeginning().equals(aVlan.getBeginning()));
        assert(zArchVlan.getBeginning().equals(zVlan.getBeginning()));
        assert(aArchVlan.getEnding().equals(aVlan.getEnding()));
        assert(zArchVlan.getEnding().equals(zVlan.getEnding()));

        ArchivedBandwidthE aArchBW = aArchFix.getReservedBandwidth();
        ArchivedBandwidthE zArchBW = zArchFix.getReservedBandwidth();

        assert(aArchBW.getUrn().equals(aBW.getUrn()));
        assert(zArchBW.getUrn().equals(zBW.getUrn()));
        assert(aArchBW.getInBandwidth() == aBW.getInBandwidth());
        assert(aArchBW.getEgBandwidth() == aBW.getEgBandwidth());
        assert(zArchBW.getInBandwidth() == zBW.getInBandwidth());
        assert(zArchBW.getEgBandwidth() == zBW.getEgBandwidth());
        assert(aArchBW.getBeginning().equals(aBW.getBeginning()));
        assert(zArchBW.getBeginning().equals(zBW.getBeginning()));
        assert(aArchBW.getEnding().equals(aBW.getEnding()));
        assert(zArchBW.getEnding().equals(zBW.getEnding()));

        long aArchBW_id = aArchBW.getId();
        long zArchBW_id = zArchBW.getId();

        assert(aArchBW_id == aBW_id);
        assert(zArchBW_id == zBW_id);

        Set<ArchivedPssResourceE> aArchPSS = aArchFix.getReservedPssResources();
        Set<ArchivedPssResourceE> zArchPSS = zArchFix.getReservedPssResources();

        assert(aArchPSS.size() == aPSS.size());
        assert(zArchPSS.size() == zPSS.size());
    }
}
