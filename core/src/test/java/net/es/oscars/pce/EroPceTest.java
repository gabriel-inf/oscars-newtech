package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.AsymmTopologyBuilder;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
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
 * Created by jeremy on 7/22/16.
 *
 * Tests End-to-End correctness of the PCE modules with specified EROs
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class EroPceTest
{
    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private TopoService topoService;

    @Autowired
    private ResvService resvService;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private EroPCE eroPCE;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    @Autowired
    private TopPCE topPCE;

    @Test
    public void eroPceTestPalindrome()
    {
        String testName = "eroPceTestPalindrome";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO;

        azERO.add("nodeK");
        azERO.add("nodeK:2");
        azERO.add("nodeM:1");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeR:1");
        azERO.add("nodeR");
        azERO.add("nodeR:2");
        azERO.add("nodeP:3");
        azERO.add("nodeP");
        azERO.add("nodeP:2");
        azERO.add("nodeQ:1");
        azERO.add("nodeQ");

        zaERO = azERO.stream()
                .collect(Collectors.toList());

        Collections.reverse(zaERO);

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        assert(computedPaths != null);

        List<TopoEdge> computedAzEro = computedPaths.get("az");
        List<TopoEdge> computedZaEro = computedPaths.get("za");

        List<TopoVertex> azVerts = dijkstraPCE.translatePathEdgesToVertices(computedAzEro);
        List<TopoVertex> zaVerts = dijkstraPCE.translatePathEdgesToVertices(computedZaEro);
        List<String> azString = dijkstraPCE.translatePathVerticesToStrings(azVerts);
        List<String> zaString = dijkstraPCE.translatePathVerticesToStrings(zaVerts);

        // Computed EROs also include port URNs. Add those to requested EROs from comparison.
        azERO.add(0, srcPorts.get(0));
        azERO.add(dstPorts.get(0));
        zaERO.add(0, dstPorts.get(0));
        zaERO.add(srcPorts.get(0));

        assert(azString.equals(azERO));
        assert(zaString.equals(zaERO));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroPceTestNonPalindrome()
    {
        String testName = "eroPceTestNonPalindrome";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4_2();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:2");
        azERO.add("nodeM:1");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeR:1");
        azERO.add("nodeR");
        azERO.add("nodeR:2");
        azERO.add("nodeP:3");
        azERO.add("nodeP");
        azERO.add("nodeP:2");
        azERO.add("nodeQ:1");
        azERO.add("nodeQ");

        zaERO.add("nodeQ");
        zaERO.add("nodeQ:1");
        zaERO.add("nodeP:2");
        zaERO.add("nodeP");
        zaERO.add("nodeP:1");
        zaERO.add("nodeL:3");
        zaERO.add("nodeL");
        zaERO.add("nodeL:1");
        zaERO.add("nodeK:1");
        zaERO.add("nodeK");


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        assert(computedPaths != null);

        List<TopoEdge> computedAzEro = computedPaths.get("az");
        List<TopoEdge> computedZaEro = computedPaths.get("za");

        List<TopoVertex> azVerts = dijkstraPCE.translatePathEdgesToVertices(computedAzEro);
        List<TopoVertex> zaVerts = dijkstraPCE.translatePathEdgesToVertices(computedZaEro);
        List<String> azString = dijkstraPCE.translatePathVerticesToStrings(azVerts);
        List<String> zaString = dijkstraPCE.translatePathVerticesToStrings(zaVerts);

        // Computed EROs also include port URNs. Add those to requested EROs from comparison.
        azERO.add(0, srcPorts.get(0));
        azERO.add(dstPorts.get(0));
        zaERO.add(0, dstPorts.get(0));
        zaERO.add(srcPorts.get(0));

        assert(azString.equals(azERO));
        assert(zaString.equals(zaERO));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroPceTestBadNonPalindrome1()
    {
        String testName = "eroPceTestBadNonPalindrome1";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:2");
        azERO.add("nodeM:1");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeR:1");
        azERO.add("nodeR");
        azERO.add("nodeR:2");
        azERO.add("nodeP:3");
        azERO.add("nodeP");
        azERO.add("nodeP:2");
        azERO.add("nodeQ:1");
        azERO.add("nodeQ");

        // ETHERNET-layer ports/devices not shared among azERO and zaERO --> PCE Failure!
        zaERO.add("nodeQ");
        zaERO.add("nodeQ:1");
        zaERO.add("nodeP:2");
        zaERO.add("nodeP");
        zaERO.add("nodeP:1");
        zaERO.add("nodeL:3");
        zaERO.add("nodeL");
        zaERO.add("nodeL:1");
        zaERO.add("nodeK:1");
        zaERO.add("nodeK");


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        assert(computedPaths == null);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroPceTestBadNonPalindrome2()
    {
        String testName = "eroPceTestBadNonPalindrome2";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildSharedLinkTopo1();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeN";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:1");
        azERO.add("nodeL:1");
        azERO.add("nodeL");
        azERO.add("nodeL:3");
        azERO.add("nodeM:2");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeN:2");
        azERO.add("nodeN");

        // ETHERNET-layer ports/devices not shared among azERO and zaERO --> PCE Failure!
        zaERO.add("nodeN");
        zaERO.add("nodeN:2");
        zaERO.add("nodeM:3");
        zaERO.add("nodeM");
        zaERO.add("nodeM:1");
        zaERO.add("nodeK:2");
        zaERO.add("nodeK");

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        assert(computedPaths == null);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroPceTestNonPalindrome2()
    {
        String testName = "eroPceTestNonPalindrome2";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildSharedLinkTopo2();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeN";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:1");
        azERO.add("nodeL:1");
        azERO.add("nodeL");
        azERO.add("nodeL:3");
        azERO.add("nodeM:2");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeN:2");
        azERO.add("nodeN");

        zaERO.add("nodeN");
        zaERO.add("nodeN:2");
        zaERO.add("nodeM:3");
        zaERO.add("nodeM");
        zaERO.add("nodeM:1");
        zaERO.add("nodeK:2");
        zaERO.add("nodeK");

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        assert(computedPaths != null);

        List<TopoEdge> computedAzEro = computedPaths.get("az");
        List<TopoEdge> computedZaEro = computedPaths.get("za");

        List<TopoVertex> azVerts = dijkstraPCE.translatePathEdgesToVertices(computedAzEro);
        List<TopoVertex> zaVerts = dijkstraPCE.translatePathEdgesToVertices(computedZaEro);
        List<String> azString = dijkstraPCE.translatePathVerticesToStrings(azVerts);
        List<String> zaString = dijkstraPCE.translatePathVerticesToStrings(zaVerts);

        // Computed EROs also include port URNs. Add those to requested EROs from comparison.
        azERO.add(0, srcPorts.get(0));
        azERO.add(dstPorts.get(0));
        zaERO.add(0, dstPorts.get(0));
        zaERO.add(srcPorts.get(0));

        assert(azString.equals(azERO));
        assert(zaString.equals(zaERO));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroPceTestSharedLink()
    {
        String testName = "eroPceTestSharedLink";
        log.info("Initializing test: \'" + testName + "\'.");
        bwRepo.deleteAll();

        topologyBuilder.buildSharedLinkTopo2();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeN";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:1");
        azERO.add("nodeL:1");
        azERO.add("nodeL");
        azERO.add("nodeL:3");
        azERO.add("nodeM:2");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeN:2");
        azERO.add("nodeN");

        zaERO.add("nodeN");
        zaERO.add("nodeN:1");
        zaERO.add("nodeL:2");
        zaERO.add("nodeL");
        zaERO.add("nodeL:3");
        zaERO.add("nodeM:2");
        zaERO.add("nodeM");
        zaERO.add("nodeM:1");
        zaERO.add("nodeK:2");
        zaERO.add("nodeK");

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        assert(computedPaths != null);

        List<TopoEdge> computedAzEro = computedPaths.get("az");
        List<TopoEdge> computedZaEro = computedPaths.get("za");

        List<TopoVertex> azVerts = dijkstraPCE.translatePathEdgesToVertices(computedAzEro);
        List<TopoVertex> zaVerts = dijkstraPCE.translatePathEdgesToVertices(computedZaEro);
        List<String> azString = dijkstraPCE.translatePathVerticesToStrings(azVerts);
        List<String> zaString = dijkstraPCE.translatePathVerticesToStrings(zaVerts);

        // Computed EROs also include port URNs. Add those to requested EROs from comparison.
        azERO.add(0, srcPorts.get(0));
        azERO.add(dstPorts.get(0));
        zaERO.add(0, dstPorts.get(0));
        zaERO.add(srcPorts.get(0));

        assert(azString.equals(azERO));
        assert(zaString.equals(zaERO));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroSpecTestSharedLinkSufficientBW()
    {
        String testName = "eroPceTestSharedLinkSufficientBW";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildSharedLinkTopo2();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Connection to test
        ConnectionE connectionTest;
        RequestedBlueprintE blueprintTest;
        Set<RequestedVlanPipeE> pipesTest = new HashSet<>();

        String srcDevice = "nodeK";
        String dstDevice = "nodeN";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:1");
        azERO.add("nodeL:1");
        azERO.add("nodeL");
        azERO.add("nodeL:3");
        azERO.add("nodeM:2");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeN:2");
        azERO.add("nodeN");

        zaERO.add("nodeN");
        zaERO.add("nodeN:1");
        zaERO.add("nodeL:2");
        zaERO.add("nodeL");
        zaERO.add("nodeL:3");
        zaERO.add("nodeM:2");
        zaERO.add("nodeM");
        zaERO.add("nodeM:1");
        zaERO.add("nodeK:2");
        zaERO.add("nodeK");

        RequestedVlanPipeE pipeAZTest = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZTest.setAzERO(azERO);
        pipeAZTest.setZaERO(zaERO);
        pipesTest.add(pipeAZTest);
        blueprintTest = testBuilder.buildRequest(pipesTest);
        connectionTest = testBuilder.buildConnection(blueprintTest, requestedSched, "connTest", "End-to-End Connection");

        // Initial request which allows us to avoid altering BandwidthRepo -- Reserves a bunch of critical B/W
        ConnectionE connectionBig;
        RequestedBlueprintE blueprintBig;
        Set<RequestedVlanPipeE> pipesBig = new HashSet<>();

        String srcDeviceBig = "nodeL";
        String dstDeviceBig = "nodeM";
        List<String> srcPortsBig = Stream.of("nodeL:1").collect(Collectors.toList());
        List<String> dstPortsBig = Stream.of("nodeM:3").collect(Collectors.toList());
        Integer azBWBig = 901;
        Integer zaBWBig = 901;

        RequestedVlanPipeE pipeAZBig = testBuilder.buildRequestedPipe(srcPortsBig, srcDeviceBig, dstPortsBig, dstDeviceBig, azBWBig, zaBWBig, palindrome, survivability, vlan);
        pipesBig.add(pipeAZBig);
        blueprintBig = testBuilder.buildRequest(pipesBig);
        connectionBig = testBuilder.buildConnection(blueprintBig, requestedSched, "connBig", "Big Single-link Connection");


        log.info("Beginning test: \'" + testName + "\'.");
        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            resvService.hold(connectionBig);
            resvService.hold(connectionTest);
        }
        catch(PCEException pceE){ log.error("", pceE); }
        catch(PSSException pssE){ log.error("", pssE); }

        assert(!connectionBig.getReserved().getVlanFlow().getMplsPipes().isEmpty());
        assert(!connectionTest.getReserved().getVlanFlow().getMplsPipes().isEmpty());

        List<String> computedAZEro = connectionTest.getReserved().getVlanFlow().getMplsPipes().iterator().next().getAzERO();
        List<String> computedZAEro = connectionTest.getReserved().getVlanFlow().getMplsPipes().iterator().next().getZaERO();

        // Each reserved pipe will have EROs not containing src/dst devices. Add those for comparison with requested EROs
        computedAZEro.add(0, srcDevice);
        computedAZEro.add(dstDevice);
        computedZAEro.add(0, dstDevice);
        computedZAEro.add(srcDevice);

        assert(azERO.equals(computedAZEro));
        assert(zaERO.equals(computedZAEro));

        // Why on Earth do these two statements fail?!
        //assert(computedAZEro.equals(azERO));
        //assert(computedZAEro.equals(zaERO));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroSpecTestSharedLinkInsufficientBW()
    {
        String testName = "eroPceTestSharedLinkInsufficientBW";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildSharedLinkTopo2();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        // Connection to test
        ConnectionE connectionTest;
        RequestedBlueprintE blueprintTest;
        Set<RequestedVlanPipeE> pipesTest = new HashSet<>();

        String srcDevice = "nodeK";
        String dstDevice = "nodeN";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 50;  // Enough bandwidth for AZ, but not for (AZ + ZA) on shared link!
        Integer zaBW = 50;  // Enough bandwidth for ZA, but not for (AZ + ZA) on shared link!
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:1");
        azERO.add("nodeL:1");
        azERO.add("nodeL");
        azERO.add("nodeL:3");
        azERO.add("nodeM:2");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeN:2");
        azERO.add("nodeN");

        zaERO.add("nodeN");
        zaERO.add("nodeN:1");
        zaERO.add("nodeL:2");
        zaERO.add("nodeL");
        zaERO.add("nodeL:3");
        zaERO.add("nodeM:2");
        zaERO.add("nodeM");
        zaERO.add("nodeM:1");
        zaERO.add("nodeK:2");
        zaERO.add("nodeK");

        RequestedVlanPipeE pipeAZTest = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZTest.setAzERO(azERO);
        pipeAZTest.setZaERO(zaERO);
        pipesTest.add(pipeAZTest);
        blueprintTest = testBuilder.buildRequest(pipesTest);
        connectionTest = testBuilder.buildConnection(blueprintTest, requestedSched, "connTest", "End-to-End Connection");

        // Initial request which allows us to avoid altering BandwidthRepo -- Reserves a bunch of critical B/W
        ConnectionE connectionBig;
        RequestedBlueprintE blueprintBig;
        Set<RequestedVlanPipeE> pipesBig = new HashSet<>();

        String srcDeviceBig = "nodeL";
        String dstDeviceBig = "nodeM";
        List<String> srcPortsBig = Stream.of("nodeL:1").collect(Collectors.toList());
        List<String> dstPortsBig = Stream.of("nodeM:3").collect(Collectors.toList());
        Integer azBWBig = 901;
        Integer zaBWBig = 901;

        RequestedVlanPipeE pipeAZBig = testBuilder.buildRequestedPipe(srcPortsBig, srcDeviceBig, dstPortsBig, dstDeviceBig, azBWBig, zaBWBig, palindrome, survivability, vlan);
        pipesBig.add(pipeAZBig);
        blueprintBig = testBuilder.buildRequest(pipesBig);
        connectionBig = testBuilder.buildConnection(blueprintBig, requestedSched, "connBig", "Big Single-link Connection");


        log.info("Beginning test: \'" + testName + "\'.");
        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            resvService.hold(connectionBig);
            resvService.hold(connectionTest);
        }
        catch(PCEException pceE){ log.error("", pceE); }
        catch(PSSException pssE){ log.error("", pssE); }

        assert(!connectionBig.getReserved().getVlanFlow().getMplsPipes().isEmpty());
        assert(connectionTest.getReserved() == null);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroPceTestDuplicateNode1()
    {
        String testName = "eroPceTestDuplicateNode1";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4_2();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:2");
        azERO.add("nodeM:1");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeR:1");
        azERO.add("nodeR");
        azERO.add("nodeR:2");
        azERO.add("nodeP:3");
        azERO.add("nodeP");
        azERO.add("nodeP");
        azERO.add("nodeP:2");
        azERO.add("nodeQ:1");
        azERO.add("nodeQ");

        zaERO.add("nodeQ");
        zaERO.add("nodeQ:1");
        zaERO.add("nodeP:2");
        zaERO.add("nodeP");
        zaERO.add("nodeP:1");
        zaERO.add("nodeL:3");
        zaERO.add("nodeL");
        zaERO.add("nodeL:1");
        zaERO.add("nodeK:1");
        zaERO.add("nodeK");


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        assert(computedPaths == null);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroPceTestDuplicateNode2()
    {
        String testName = "eroPceTestDuplicateNode2";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4_2();

        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcDevice = "nodeK";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:2");
        azERO.add("nodeM:1");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeR:1");
        azERO.add("nodeR");
        azERO.add("nodeR:2");
        azERO.add("nodeP:3");
        azERO.add("nodeP");
        azERO.add("nodeP:2");
        azERO.add("nodeQ:1");
        azERO.add("nodeQ");

        zaERO.add("nodeQ");
        zaERO.add("nodeQ:1");
        zaERO.add("nodeP:2");
        zaERO.add("nodeP");
        zaERO.add("nodeP:1");
        zaERO.add("nodeL:3");
        zaERO.add("nodeL");
        zaERO.add("nodeL");
        zaERO.add("nodeL:1");
        zaERO.add("nodeK:1");
        zaERO.add("nodeK");


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        assert(computedPaths == null);

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroSpecTestEmptyAZ()
    {
        String testName = "eroPceTestEmptyAZ";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4_2();

        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint;
        ConnectionE connection;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        String srcDevice = "nodeK";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        zaERO.add("nodeQ");
        zaERO.add("nodeQ:1");
        zaERO.add("nodeP:2");
        zaERO.add("nodeP");
        zaERO.add("nodeP:1");
        zaERO.add("nodeL:3");
        zaERO.add("nodeL");
        zaERO.add("nodeL:2");
        zaERO.add("nodeM:2");
        zaERO.add("nodeM");
        zaERO.add("nodeM:1");
        zaERO.add("nodeK:2");
        zaERO.add("nodeK");


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        pipes.add(pipeAZ);
        requestedBlueprint = testBuilder.buildRequest(pipes);
        connection = testBuilder.buildConnection(requestedBlueprint, requestedSched, "connBig", "Big Single-link Connection");

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            resvService.hold(connection);
        }
        catch(PCEException pceE){ log.error("", pceE); }
        catch(PSSException pssE){ log.error("", pssE); }

        reservedBlueprint = connection.getReserved();

        assert(reservedBlueprint != null);
        List<String> computedZaEro = reservedBlueprint.getVlanFlow().getMplsPipes().iterator().next().getZaERO();

        // Each reserved pipe will have EROs not containing src/dst devices. Add those for comparison with requested EROs
        computedZaEro.add(0, dstDevice);
        computedZaEro.add(srcDevice);

        assert(!zaERO.equals(computedZaEro));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroSpecTestEmptyZA()
    {
        String testName = "eroPceTestEmptyZA";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4_2();

        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> pipes = new HashSet<>();
        RequestedBlueprintE requestedBlueprint;
        ReservedBlueprintE reservedBlueprint;
        ConnectionE connection;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        String srcDevice = "nodeK";
        String dstDevice = "nodeQ";
        List<String> srcPorts = Stream.of("portA").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("portZ").collect(Collectors.toList());
        Integer azBW = 25;
        Integer zaBW = 25;
        String vlan = "any";
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        List<String> azERO = new ArrayList<>();
        List<String> zaERO = new ArrayList<>();

        azERO.add("nodeK");
        azERO.add("nodeK:2");
        azERO.add("nodeM:1");
        azERO.add("nodeM");
        azERO.add("nodeM:3");
        azERO.add("nodeR:1");
        azERO.add("nodeR");
        azERO.add("nodeR:2");
        azERO.add("nodeP:3");
        azERO.add("nodeP");
        azERO.add("nodeP:2");
        azERO.add("nodeQ:1");
        azERO.add("nodeQ");

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        pipes.add(pipeAZ);
        requestedBlueprint = testBuilder.buildRequest(pipes);
        connection = testBuilder.buildConnection(requestedBlueprint, requestedSched, "connBig", "Big Single-link Connection");

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            resvService.hold(connection);
        }
        catch(PCEException pceE){ log.error("", pceE); }
        catch(PSSException pssE){ log.error("", pssE); }

        reservedBlueprint = connection.getReserved();

        assert(reservedBlueprint != null);
        List<String> computedAzEro = reservedBlueprint.getVlanFlow().getMplsPipes().iterator().next().getAzERO();

        // Each reserved pipe will have EROs not containing src/dst devices. Add those for comparison with requested EROs
        computedAzEro.add(0, srcDevice);
        computedAzEro.add(dstDevice);

        assert(!azERO.equals(computedAzEro));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void multiMplsPipeTestNonPal()
    {
        log.info("Initializing test: 'multiMplsPipeTestNonPal'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;
        Set<RequestedVlanPipeE> pipes = new HashSet<>();

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        List<String> srcPorts = Arrays.asList("portA");
        List<String> dstPorts = Arrays.asList("portZ");
        String srcDevice = "nodeK";
        String dstDevice = "nodeT";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";
        List<String> azRequested = new ArrayList<>();
        List<String> zaRequested = new ArrayList<>();

        azRequested.add("nodeK");
        azRequested.add("nodeK:1");
        azRequested.add("nodeL:1");
        azRequested.add("nodeL");
        azRequested.add("nodeL:3");
        azRequested.add("nodeP:1");
        azRequested.add("nodeP");
        azRequested.add("nodeP:2");
        azRequested.add("nodeQ:1");
        azRequested.add("nodeQ");
        azRequested.add("nodeQ:3");
        azRequested.add("nodeS:1");
        azRequested.add("nodeS");
        azRequested.add("nodeS:2");
        azRequested.add("nodeT:1");
        azRequested.add("nodeT");

        zaRequested.add("nodeT");
        zaRequested.add("nodeT:2");
        zaRequested.add("nodeU:2");
        zaRequested.add("nodeU");
        zaRequested.add("nodeU:1");
        zaRequested.add("nodeS:3");
        zaRequested.add("nodeS");
        zaRequested.add("nodeS:1");
        zaRequested.add("nodeQ:3");
        zaRequested.add("nodeQ");
        zaRequested.add("nodeQ:1");
        zaRequested.add("nodeP:2");
        zaRequested.add("nodeP");
        zaRequested.add("nodeP:1");
        zaRequested.add("nodeL:3");
        zaRequested.add("nodeL");
        zaRequested.add("nodeL:2");
        zaRequested.add("nodeM:2");
        zaRequested.add("nodeM");
        zaRequested.add("nodeM:1");
        zaRequested.add("nodeK:2");
        zaRequested.add("nodeK");
        
        topologyBuilder.buildMultiMplsTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan);
        pipeAZ.setAzERO(azRequested);
        pipeAZ.setZaERO(zaRequested);

        pipes.add(pipeAZ);
        requestedBlueprint = testBuilder.buildRequest(pipes);

        log.info("Beginning test: 'multiMplsPipeTestNonPal'.");

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
        assert (allResEthPipes.size() == 3);
        assert (allResMplsPipes.size() == 2);

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

            assert (aJunc.getDeviceUrn().getUrn().equals("nodeL") || aJunc.getDeviceUrn().getUrn().equals("nodeP") || aJunc.getDeviceUrn().getUrn().equals("nodeQ"));
            assert (zJunc.getDeviceUrn().getUrn().equals("nodeP") || zJunc.getDeviceUrn().getUrn().equals("nodeQ") || zJunc.getDeviceUrn().getUrn().equals("nodeS"));

            assert (aFixes.size() == 0);
            assert (zFixes.size() == 0);

            String expectedAzERO;
            String expectedZaERO;

            if(aJunc.getDeviceUrn().getUrn().equals("nodeL"))
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeP"));
                expectedAzERO = "nodeL-nodeL:3-nodeP:1-nodeP";
                expectedZaERO = "nodeP-nodeP:1-nodeL:3-nodeL";
            }
            else if(aJunc.getDeviceUrn().getUrn().equals("nodeP"))
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeQ"));
                expectedAzERO = "nodeP-nodeP:2-nodeQ:1-nodeQ";
                expectedZaERO = "nodeQ-nodeQ:1-nodeP:2-nodeP";
            }
            else
            {
                assert(zJunc.getDeviceUrn().getUrn().equals("nodeS"));
                expectedAzERO = "nodeQ-nodeQ:3-nodeS:1-nodeS";
                expectedZaERO = "nodeS-nodeS:1-nodeQ:3-nodeQ";
            }

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

            assert ((aJunc.getDeviceUrn().getUrn().equals("nodeK") && zJunc.getDeviceUrn().getUrn().equals("nodeL"))
                    || (aJunc.getDeviceUrn().getUrn().equals("nodeS") && zJunc.getDeviceUrn().getUrn().equals("nodeT")));

            if(aJunc.getDeviceUrn().getUrn().equals("nodeK"))
            {
                assert (aFixes.size() == 1);
                assert (zFixes.size() == 0);
                ReservedVlanFixtureE theFix = aFixes.iterator().next();

                assert (theFix.getIfceUrn().getUrn().equals("portA"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(azBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(zaBW));

                String expectedAzERO = "nodeK-nodeK:1-nodeL:1-nodeL";
                String expectedZaERO = "nodeL-nodeL:2-nodeM:2-nodeM-nodeM:1-nodeK:2-nodeK";

                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
            else
            {
                assert (aFixes.size() == 0);
                assert (zFixes.size() == 1);
                ReservedVlanFixtureE theFix = zFixes.iterator().next();

                assert (theFix.getIfceUrn().getUrn().equals("portZ"));
                assert (theFix.getReservedBandwidth().getInBandwidth().equals(zaBW));
                assert (theFix.getReservedBandwidth().getEgBandwidth().equals(azBW));

                String expectedAzERO = "nodeS-nodeS:2-nodeT:1-nodeT";
                String expectedZaERO = "nodeT-nodeT:2-nodeU:2-nodeU-nodeU:1-nodeS:3-nodeS";

                assert (actualAzERO.equals(expectedAzERO));
                assert (actualZaERO.equals(expectedZaERO));
            }
        }

        log.info("test 'multiMplsPipeTestNonPal' passed.");
    }
}
