package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
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

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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

        RequestedVlanPipeE pipeAZTest = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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

        RequestedVlanPipeE pipeAZBig = testBuilder.buildRequestedPipe(srcPortsBig, srcDeviceBig, dstPortsBig, dstDeviceBig, azBWBig, zaBWBig, palindrome, vlan);
        pipesBig.add(pipeAZBig);
        blueprintBig = testBuilder.buildRequest(pipesBig);
        connectionBig = testBuilder.buildConnection(blueprintBig, requestedSched, "connBig", "Big Single-link Connection");


        log.info("Beginning test: \'" + testName + "\'.");
        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            connectionBig = resvService.hold(connectionBig);
            connectionTest = resvService.hold(connectionTest);
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

        RequestedVlanPipeE pipeAZTest = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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

        RequestedVlanPipeE pipeAZBig = testBuilder.buildRequestedPipe(srcPortsBig, srcDeviceBig, dstPortsBig, dstDeviceBig, azBWBig, zaBWBig, palindrome, vlan);
        pipesBig.add(pipeAZBig);
        blueprintBig = testBuilder.buildRequest(pipesBig);
        connectionBig = testBuilder.buildConnection(blueprintBig, requestedSched, "connBig", "Big Single-link Connection");


        log.info("Beginning test: \'" + testName + "\'.");
        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            connectionBig = resvService.hold(connectionBig);
            connectionTest = resvService.hold(connectionTest);
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


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
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


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        pipes.add(pipeAZ);
        requestedBlueprint = testBuilder.buildRequest(pipes);
        connection = testBuilder.buildConnection(requestedBlueprint, requestedSched, "connBig", "Big Single-link Connection");

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            connection = resvService.hold(connection);
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

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);

        pipes.add(pipeAZ);
        requestedBlueprint = testBuilder.buildRequest(pipes);
        connection = testBuilder.buildConnection(requestedBlueprint, requestedSched, "connBig", "Big Single-link Connection");

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            connection = resvService.hold(connection);
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
}
