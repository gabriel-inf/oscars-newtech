package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class BhandariPceTest {

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private TopoService topoService;

    @Autowired
    private BhandariPCE bhandariPCE;

    @Autowired
    private BellmanFordPCE bellmanFordPCE;

    @Test
    public void bhandariTestKPaths(){
        topologyBuilder.buildTopoFourPaths();
        Topology topo = topoService.getMultilayerTopology();

        String sourceName = "nodeK";
        String destName = "nodeQ";

        Optional<TopoVertex> source = topo.getVertexByUrn(sourceName);
        Optional<TopoVertex> dest = topo.getVertexByUrn(destName);

        if(source.isPresent() && dest.isPresent()){
            List<List<TopoEdge>> paths = bhandariPCE.computeDisjointPaths(topo, source.get(), dest.get(), 1);
            logPaths(paths);
            testPaths(paths, Collections.singletonList(source.get()), Collections.singletonList(dest.get()), 1);

            paths = bhandariPCE.computeDisjointPaths(topo, source.get(), dest.get(), 2);
            logPaths(paths);
            testPaths(paths, Collections.singletonList(source.get()), Collections.singletonList(dest.get()), 2);

            paths = bhandariPCE.computeDisjointPaths(topo, source.get(), dest.get(), 3);
            logPaths(paths);
            testPaths(paths, Collections.singletonList(source.get()), Collections.singletonList(dest.get()), 3);

            paths = bhandariPCE.computeDisjointPaths(topo, source.get(), dest.get(), 4);
            logPaths(paths);
            testPaths(paths, Collections.singletonList(source.get()), Collections.singletonList(dest.get()), 4);

            // 5 paths should not be possible, only 4 should return
            paths = bhandariPCE.computeDisjointPaths(topo, source.get(), dest.get(), 5);
            logPaths(paths);
            testPaths(paths, Collections.singletonList(source.get()), Collections.singletonList(dest.get()), 4);
        }
    }

    @Test
    public void bhandariTestMultiplePairs(){
        topologyBuilder.buildTopoMultipleDisjointPaths();
        Topology topo = topoService.getMultilayerTopology();

        // Test 3 Paths from (2, 8)
        List<String> sourceNames = Arrays.asList("2", "2", "2");
        List<String> destNames = Arrays.asList("8", "8", "8");

        List<TopoVertex> sources = sourceNames.stream().map(topo::getVertexByUrn).filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        List<TopoVertex> dests = destNames.stream().map(topo::getVertexByUrn).filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if(sources.size() == dests.size()){
            List<List<TopoVertex>> pairs = makePairs(sources, dests);
            List<List<TopoEdge>> paths = bhandariPCE.computeDisjointPaths(topo, pairs);
            logPaths(paths);
            testPaths(paths, sources, dests, sources.size());
        }
    }


    @Test
    public void bellmanFordTest(){

        topologyBuilder.buildTopo4();

        Topology topo = topoService.getMultilayerTopology();

        String sourceName = "portA";
        String destName = "portZ";

        // Test a single path
        Optional<TopoVertex> optSource = topo.getVertexByUrn(sourceName);
        Optional<TopoVertex> optDest = topo.getVertexByUrn(destName);
        if(optSource.isPresent() && optDest.isPresent()){
            List<TopoEdge> path = bellmanFordPCE.shortestPath(topo, optSource.get(), optDest.get());
            if(optSource.get().equals(optDest.get())){
                assert(path.isEmpty());
            }
            else{
                assert(path.get(0).getA().equals(optSource.get()));
                assert(path.get(path.size()-1).getZ().equals(optDest.get()));
            }
            //log.info(path.stream().map(edge -> "(" + edge.getA().getUrn() + ", " + edge.getZ().getUrn() + ")").collect(Collectors.toList()).toString());
        }

        // Test all shortest paths from each source
        for(TopoVertex source : topo.getVertices()){
            Map<TopoVertex, List<TopoEdge>> allShortestPaths = bellmanFordPCE.allShortestPaths(topo, source);
            //log.info("~~~~~~~~~~~~~~");
            //log.info("Source: " + source.getUrn());
            for(TopoVertex dest : allShortestPaths.keySet()) {
                List<TopoEdge> path = allShortestPaths.get(dest);
                if (dest.equals(source)) {
                    assert(path.isEmpty());
                }
                else{
                    assert(path.get(0).getA().equals(source));
                    assert(path.get(path.size()-1).getZ().equals(dest));
                }
                //log.info("Destination: " + dest.getUrn() + ": " + path.stream().map(edge -> "(" + edge.getA().getUrn() + ", " + edge.getZ().getUrn() + ")").collect(Collectors.toList()).toString());
            }
        }

    }

    private void logPath(List<TopoEdge> path){
        log.info(path.stream().map(e -> "(" + e.getA().getUrn() + ", " + e.getZ().getUrn() + ")").collect(Collectors.toList()).toString());
    }

    private void logPaths(List<List<TopoEdge>> paths){
        paths.forEach(this::logPath);
    }

    private void testPaths(List<List<TopoEdge>> paths, List<TopoVertex> sources, List<TopoVertex> dests, Integer expectedNumber){
        assert(paths.size() == expectedNumber);
        assert(paths.stream().allMatch(path -> path.size() > 0));
        assert(paths.stream().flatMap(Collection::stream).distinct().count() == paths.stream().flatMap(Collection::stream).count());
        assert(paths.stream().flatMap(Collection::stream).noneMatch(e -> e.getMetric() < 0));
        for(Integer pIndex = 0; pIndex < sources.size(); pIndex++){
            TopoVertex source = sources.get(pIndex);
            TopoVertex dest = dests.get(pIndex);
            assert(paths.stream().allMatch(path -> path.get(0).getA().equals(source) && path.get(path.size()-1).getZ().equals(dest)));
        }
    }


    private List<List<TopoVertex>> makePairs(List<TopoVertex> sources, List<TopoVertex> dests) {
        List<List<TopoVertex>> pairs = new ArrayList<>();
        for(Integer index = 0; index < sources.size(); index++){
            pairs.add(Arrays.asList(sources.get(index), dests.get(index)));
        }
        return pairs;
    }
}
