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
    private ResvService resvService;

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

    @Autowired
    private EroPCE eroPCE;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    @Test
    public void eroPruningTestPalindrome()
    {
        String testName = "eroPruningTestPalindrome";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4();

        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
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

        azERO.add("portA");
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
        azERO.add("portZ");

        zaERO = azERO.stream()
                .collect(Collectors.toList());

        Collections.reverse(zaERO);

        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);
        reqPipes.add(pipeAZ);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        List<TopoEdge> computedAzEro = computedPaths.get("az");
        List<TopoEdge> computedZaEro = computedPaths.get("za");

        List<TopoVertex> azVerts = dijkstraPCE.translatePathEdgesToVertices(computedAzEro);
        List<TopoVertex> zaVerts = dijkstraPCE.translatePathEdgesToVertices(computedZaEro);
        List<String> azString = dijkstraPCE.translatePathVerticesToStrings(azVerts);
        List<String> zaString = dijkstraPCE.translatePathVerticesToStrings(zaVerts);

        assert(azString.equals(azERO));
        assert(zaString.equals(zaERO));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroPruningTestNonPalindrome()
    {
        String testName = "eroPruningTestNonPalindrome";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4_2();

        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
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

        azERO.add("portA");
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
        azERO.add("portZ");

        // ETHERNET-layer ports/devices not shared among azERO and zaERO --> PCE Failure!
        zaERO.add("portZ");
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
        zaERO.add("portA");


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);
        reqPipes.add(pipeAZ);

        requestedSched = testBuilder.buildSchedule(startDate, endDate);

        log.info("Beginning test: \'" + testName + "\'.");

        Map<String, List<TopoEdge>> computedPaths = null;

        try
        {
            computedPaths = eroPCE.computeSpecifiedERO(pipeAZ, requestedSched, new ArrayList<>(), new ArrayList<>());
        }
        catch(PCEException pceE){ log.error("", pceE); }

        List<TopoEdge> computedAzEro = computedPaths.get("az");
        List<TopoEdge> computedZaEro = computedPaths.get("za");

        List<TopoVertex> azVerts = dijkstraPCE.translatePathEdgesToVertices(computedAzEro);
        List<TopoVertex> zaVerts = dijkstraPCE.translatePathEdgesToVertices(computedZaEro);
        List<String> azString = dijkstraPCE.translatePathVerticesToStrings(azVerts);
        List<String> zaString = dijkstraPCE.translatePathVerticesToStrings(zaVerts);

        assert(azString.equals(azERO));
        assert(zaString.equals(zaERO));

        log.info("test \'" + testName + "\' passed.");
    }

    @Test
    public void eroPruningTestBadPalindrome()
    {
        String testName = "eroPruningTestBadPalindrome";
        log.info("Initializing test: \'" + testName + "\'.");

        topologyBuilder.buildTopo4();

        Set<RequestedVlanPipeE> reqPipes = new HashSet<>();
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

        azERO.add("portA");
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
        azERO.add("portZ");

        zaERO.add("portZ");
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
        zaERO.add("portA");


        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, vlan);
        pipeAZ.setAzERO(azERO);
        pipeAZ.setZaERO(zaERO);
        reqPipes.add(pipeAZ);

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
}
