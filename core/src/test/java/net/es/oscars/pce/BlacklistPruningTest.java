package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
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

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class BlacklistPruningTest {

    @Autowired
    private TopPCE topPCE;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private TopoService topoService;

    @Test
    public void blacklistMplsTest(){
        log.info("Initializing test: 'blacklistMplsTest'.");

        topologyBuilder.buildTopo4();
        Topology topo = topoService.getMultilayerTopology();
        Set<TopoEdge> origEdges = new HashSet<>(topo.getEdges());
        Set<TopoVertex> origVerts = new HashSet<>(topo.getVertices());

        Set<String> blacklist = new HashSet<>();
        blacklist.add("nodeP");
        blacklist.add("nodeR");

        // Pipe Setup
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);

        RequestedBlueprintE requestedBlueprint =  setupTest("portA", "nodeK", "portZ", "nodeQ", blacklist);

        log.info("Beginning test: 'blacklistMplsTest'.");
        pruneTest(requestedBlueprint, topo, requestedSched, origEdges, origVerts, blacklist);
        log.info("'blacklistMplsTest' passed.");
    }

    @Test
    public void blacklistEthTest(){
        log.info("Initializing test: 'blacklistEthTest'.");

        topologyBuilder.buildTopo4();
        Topology topo = topoService.getMultilayerTopology();
        Set<TopoEdge> origEdges = new HashSet<>(topo.getEdges());
        Set<TopoVertex> origVerts = new HashSet<>(topo.getVertices());

        Set<String> blacklist = new HashSet<>();
        blacklist.add("nodeL");
        blacklist.add("nodeM");

        // Pipe Setup
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);

        RequestedBlueprintE requestedBlueprint =  setupTest("portA", "nodeK", "portZ", "nodeQ", blacklist);

        log.info("Beginning test: 'blacklistEthTest'.");
        pruneTest(requestedBlueprint, topo, requestedSched, origEdges, origVerts, blacklist);
        log.info("'blacklistEthTest' passed.");
    }

    @Test
    public void blacklistEthMplsTest(){
        log.info("Initializing test: 'blacklistEthMplsTest'.");

        topologyBuilder.buildTopo4();
        Topology topo = topoService.getMultilayerTopology();
        Set<TopoEdge> origEdges = new HashSet<>(topo.getEdges());
        Set<TopoVertex> origVerts = new HashSet<>(topo.getVertices());

        Set<String> blacklist = new HashSet<>();
        blacklist.add("nodeL");
        blacklist.add("nodeM");
        blacklist.add("nodeP");
        blacklist.add("nodeR");

        // Pipe Setup
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);

        RequestedBlueprintE requestedBlueprint =  setupTest("portA", "nodeK", "portZ", "nodeQ", blacklist);

        log.info("Beginning test: 'blacklistEthMplsTest'.");
        pruneTest(requestedBlueprint, topo, requestedSched, origEdges, origVerts, blacklist);
        log.info("'blacklistEthMplsTest' passed.");
    }

    @Test
    public void blacklistAllTest(){
        log.info("Initializing test: 'blacklistAllTest'.");

        topologyBuilder.buildTopo4();
        Topology topo = topoService.getMultilayerTopology();
        Set<TopoEdge> origEdges = new HashSet<>(topo.getEdges());
        Set<TopoVertex> origVerts = new HashSet<>(topo.getVertices());

        Set<String> blacklist = new HashSet<>();
        blacklist.add("nodeK");
        blacklist.add("nodeL");
        blacklist.add("nodeM");
        blacklist.add("nodeP");
        blacklist.add("nodeQ");
        blacklist.add("nodeR");

        // Pipe Setup
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);

        RequestedBlueprintE requestedBlueprint =  setupTest("portA", "nodeK", "portZ", "nodeQ", blacklist);

        log.info("Beginning test: 'blacklistAllTest'.");
        pruneTest(requestedBlueprint, topo, requestedSched, origEdges, origVerts, blacklist);
        log.info("'blacklistAllTest' passed.");
    }

    @Test
    public void pceSubmitBlacklistAllTest(){
        log.info("Initializing test: 'pceSubmitBlacklistAllTest'.");

        topologyBuilder.buildTopo4();
        Topology topo = topoService.getMultilayerTopology();

        Set<String> blacklist = new HashSet<>();
        blacklist.add("nodeK");
        blacklist.add("nodeL");
        blacklist.add("nodeM");
        blacklist.add("nodeP");
        blacklist.add("nodeQ");
        blacklist.add("nodeR");

        // Pipe Setup
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);

        RequestedBlueprintE requestedBlueprint =  setupTest("portA", "nodeK", "portZ", "nodeQ", blacklist);

        log.info("Beginning test: 'pceSubmitBlacklistAllTest'.");
        try{
            Optional<ReservedBlueprintE> opt = topPCE.makeReserved(requestedBlueprint, requestedSched);
            assert(!opt.isPresent());
        } catch (Exception e){
            log.error(e.toString());
        }
        log.info("'pceSubmitBlacklistAllTest' passed.");
    }

    @Test
    public void pceSubmitBlacklistIntermediateTest(){
        log.info("Initializing test: 'pceSubmitBlacklistAllTest'.");

        topologyBuilder.buildTopo4();

        Set<String> blacklist = new HashSet<>();
        blacklist.add("nodeL");
        blacklist.add("nodeP");

        // Pipe Setup
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);
        RequestedBlueprintE requestedBlueprint =  setupTest("portA", "nodeK", "portZ", "nodeQ", blacklist);

        log.info("Beginning test: 'pceSubmitBlacklistAllTest'.");
        ReservedBlueprintE resBlueprint = null;
        try{
            Optional<ReservedBlueprintE> opt = topPCE.makeReserved(requestedBlueprint, requestedSched);
            assert(opt.isPresent());
            resBlueprint = opt.get();
        } catch (Exception e){
            log.error(e.toString());
        }

        assert(resBlueprint != null);
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();

        Set<String> combinedAzERO = new HashSet<>();
        Set<String> combinedZaERO = new HashSet<>();
        for(ReservedEthPipeE ethPipe : ethPipes){
            combinedAzERO.add(ethPipe.getAJunction().getDeviceUrn());
            combinedAzERO.addAll(ethPipe.getAzERO());
            combinedAzERO.add(ethPipe.getZJunction().getDeviceUrn());

            combinedZaERO.add(ethPipe.getZJunction().getDeviceUrn());
            combinedZaERO.addAll(ethPipe.getZaERO());
            combinedZaERO.add(ethPipe.getAJunction().getDeviceUrn());
        }
        for(ReservedMplsPipeE mplsPipe : mplsPipes){
            combinedAzERO.add(mplsPipe.getAJunction().getDeviceUrn());
            combinedAzERO.addAll(mplsPipe.getAzERO());
            combinedAzERO.add(mplsPipe.getZJunction().getDeviceUrn());

            combinedZaERO.add(mplsPipe.getZJunction().getDeviceUrn());
            combinedZaERO.addAll(mplsPipe.getZaERO());
            combinedZaERO.add(mplsPipe.getAJunction().getDeviceUrn());
        }

        assert(combinedAzERO.stream().noneMatch(blacklist::contains));
        assert(combinedZaERO.stream().noneMatch(blacklist::contains));
        log.info("'pceSubmitBlacklistAllTest' passed.");
    }

    public RequestedBlueprintE setupTest(String srcPort, String srcDevice, String dstPort, String dstDevice, Set<String> blacklist){
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";
        return testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, blacklist, 1, 1, 1);
    }

    public void pruneTest(RequestedBlueprintE requestedBlueprint, Topology topo, ScheduleSpecificationE requestedSched,
                        Set<TopoEdge> origEdges, Set<TopoVertex> origVerts, Set<String> blacklist){
        RequestedVlanPipeE reqPipe = requestedBlueprint.getVlanFlow().getPipes().iterator().next();
        topo = pruningService.pruneWithPipe(topo, reqPipe, requestedSched);
        Set<TopoEdge> newEdges = topo.getEdges();
        Set<TopoVertex> newVerts = topo.getVertices();

        Set<TopoEdge> removedEdges = origEdges.stream()
                .filter(edge -> blacklist.stream().anyMatch(urn -> edge.getA().getUrn().contains(urn) || edge.getZ().getUrn().contains(urn)))
                .collect(Collectors.toSet());

        assert(origEdges.size() == newEdges.size() + removedEdges.size());
        assert(origVerts.size() == newVerts.size());
        assert(newEdges.stream().noneMatch(edge -> blacklist.contains(edge.getA().getUrn()) || blacklist.contains(edge.getZ().getUrn())));
    }

}
