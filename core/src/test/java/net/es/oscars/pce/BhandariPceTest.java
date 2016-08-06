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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private BellmanFordService bellmanFordService;

    @Test
    public void bhandariTest1(){
        topologyBuilder.buildTopo4();
        Topology topo = topoService.getMultilayerTopology();

        String sourceName = "nodeK";
        String destName = "nodeQ";

        Optional<TopoVertex> optSource = topo.getVertexByUrn(sourceName);
        Optional<TopoVertex> optDest = topo.getVertexByUrn(destName);
        if(optSource.isPresent() && optDest.isPresent()){
            List<List<TopoEdge>> pathPair = bhandariPCE.computePathPair(topo, optSource.get(), optDest.get());
            if(optSource.get().equals(optDest.get())){
                assert(pathPair.isEmpty());
            }
            else{
                assert(pathPair.size() == 2);
                assert(!pathPair.get(0).equals(pathPair.get(1)));
                assert(pathPair.stream().flatMap(Collection::stream).distinct().count() == pathPair.get(0).size() + pathPair.get(1).size());
                for(List<TopoEdge> path : pathPair){
                    log.info(path.stream().map(e -> "(" + e.getA().getUrn() + ", " + e.getZ().getUrn() + ")").collect(Collectors.toList()).toString());
                }
            }
        }
    }

    @Test
    public void bhandariTest2(){
        topologyBuilder.buildTopoFourPaths();
        Topology topo = topoService.getMultilayerTopology();

        String sourceName = "nodeK";
        String destName = "nodeQ";

        Optional<TopoVertex> source = topo.getVertexByUrn(sourceName);
        Optional<TopoVertex> dest = topo.getVertexByUrn(destName);

        if(source.isPresent() && dest.isPresent()){
            List<List<TopoEdge>> paths = bhandariPCE.computeKDisjointPaths(topo, source.get(), dest.get(), 1);
            testPaths(paths, source.get(), dest.get(), 1);
            logPaths(paths);
            paths = bhandariPCE.computeKDisjointPaths(topo, source.get(), dest.get(), 2);
            testPaths(paths, source.get(), dest.get(), 2);
            logPaths(paths);
            paths = bhandariPCE.computeKDisjointPaths(topo, source.get(), dest.get(), 3);
            testPaths(paths, source.get(), dest.get(), 3);
            logPaths(paths);
            paths = bhandariPCE.computeKDisjointPaths(topo, source.get(), dest.get(), 4);
            testPaths(paths, source.get(), dest.get(), 4);
            logPaths(paths);
            // 5 paths should not be possible, only 4 should return
            paths = bhandariPCE.computeKDisjointPaths(topo, source.get(), dest.get(), 5);
            testPaths(paths, source.get(), dest.get(), 4);
            logPaths(paths);
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
            List<TopoEdge> path = bellmanFordService.shortestPath(topo, optSource.get(), optDest.get());
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
            Map<TopoVertex, List<TopoEdge>> allShortestPaths = bellmanFordService.allShortestPaths(topo, source);
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

    private void testPaths(List<List<TopoEdge>> paths, TopoVertex source, TopoVertex dest, Integer expectedNumber){
        assert(paths.size() == expectedNumber);
        assert(paths.stream().allMatch(path -> path.size() > 0));
        assert(paths.stream().flatMap(Collection::stream).distinct().count() == paths.stream().flatMap(Collection::stream).count());
        assert(paths.stream().flatMap(Collection::stream).noneMatch(e -> e.getMetric() < 0));
        assert(paths.stream().allMatch(path -> path.get(0).getA().equals(source) && path.get(path.size()-1).getZ().equals(dest)));
    }
}
