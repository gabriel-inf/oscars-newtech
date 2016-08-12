package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.svc.TopoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
        RequestedBlueprintE requestedBlueprint;
        ScheduleSpecificationE requestedSched;
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        String srcPort = "portA";
        String srcDevice = "nodeK";
        String dstPort = "portZ";
        String dstDevice = "nodeQ";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, blacklist);


        log.info("Beginning test: 'blacklistMplsTest'.");
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

        log.info("'blacklistMplsTest' passed.");
    }

}
