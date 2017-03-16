package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.pce.helpers.TopologyBuilder;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.topo.svc.TopoService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional
public class BhandariPceTest extends AbstractCoreTest {

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
            testPaths(paths, source.get(), dest.get(), 1);

            paths = bhandariPCE.computeDisjointPaths(topo, source.get(), dest.get(), 2);
            logPaths(paths);
            testPaths(paths, source.get(), dest.get(), 2);

            paths = bhandariPCE.computeDisjointPaths(topo, source.get(), dest.get(), 3);
            logPaths(paths);
            testPaths(paths, source.get(), dest.get(), 3);

            paths = bhandariPCE.computeDisjointPaths(topo, source.get(), dest.get(), 4);
            logPaths(paths);
            testPaths(paths, source.get(), dest.get(), 4);

            // 5 paths should not be possible, only 4 should return
            paths = bhandariPCE.computeDisjointPaths(topo, source.get(), dest.get(), 5);
            logPaths(paths);
            testPaths(paths, source.get(), dest.get(), 4);
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
        }

        // Test all shortest paths from each source
        for(TopoVertex source : topo.getVertices()){
            Map<TopoVertex, List<TopoEdge>> allShortestPaths = bellmanFordPCE.allShortestPaths(topo, source);
            for(TopoVertex dest : allShortestPaths.keySet()) {
                List<TopoEdge> path = allShortestPaths.get(dest);
                if (dest.equals(source)) {
                    assert(path.isEmpty());
                }
                else{
                    assert(path.get(0).getA().equals(source));
                    assert(path.get(path.size()-1).getZ().equals(dest));
                }
            }
        }

    }


    private void testPaths(List<List<TopoEdge>> paths, TopoVertex source, TopoVertex dest, Integer expectedNumber){
        assert(paths.size() == expectedNumber);
        assert(paths.stream().allMatch(path -> path.size() > 0));
        assert(paths.stream().flatMap(Collection::stream).distinct().count() == paths.stream().flatMap(Collection::stream).count());
        assert(paths.stream().flatMap(Collection::stream).noneMatch(e -> e.getMetric() < 0));
        assert(paths.stream().anyMatch(path -> source.equals(path.get(0).getA())));
        assert(paths.stream().anyMatch(path -> dest.equals(path.get(path.size()-1).getZ())));
    }

    private void logPath(List<TopoEdge> path){
        log.info("Path: " + path.stream().map(e -> "(" + e.getA().getUrn() + ", " + e.getZ().getUrn() + ")").collect(Collectors.toList()).toString());
    }

    private void logPaths(List<List<TopoEdge>> paths){
        paths.forEach(this::logPath);
    }
}
