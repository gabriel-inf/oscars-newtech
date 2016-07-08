package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.VertexType;
import net.es.oscars.topo.svc.TopoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeremy on 6/30/16.
 *
 * Tests End-to-End correctness of the PCE modules
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class TopologyPruneTest
{
    @Autowired
    private TopoService topoService;

    @Autowired
    private TopologyBuilder topoBuilder;

    Topology ethTopo;
    Topology intTopo;
    Topology mplsTopo;

    @Test
    public void NonPalTopoLayering1()
    {
        log.info("Beginning test: 'NonPalTopoLayering1'.");

        topoBuilder.buildTopo1();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 3);
        assert(mplsVertices.size() == 0);

        assert(intEdges.size() == 4);
        assert(ethEdges.size() == 0);
        assert(mplsEdges.size() == 0);

        log.info("test 'NonPalTopoLayering1' passed.");
    }

    @Test
    public void NonPalTopoLayering2()
    {
        log.info("Beginning test: 'NonPalTopoLayering2'.");

        topoBuilder.buildTopo2();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        ethVertices.stream().forEach(v -> log.info(v.getUrn()));

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 11);
        assert(mplsVertices.size() == 3);

        assert(intEdges.size() == 20);
        assert(ethEdges.size() == 8);
        assert(mplsEdges.size() == 0);

        log.info("test 'NonPalTopoLayering2' passed.");
    }

    @Test
    public void NonPalTopoLayering3()
    {
        log.info("Beginning test: 'NonPalTopoLayering3'.");

        topoBuilder.buildTopo3();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 3);
        assert(mplsVertices.size() == 11);

        assert(intEdges.size() == 20);
        assert(ethEdges.size() == 2);
        assert(mplsEdges.size() == 6);

        log.info("test 'NonPalTopoLayering3' passed.");
    }

    @Test
    public void NonPalTopoLayering4()
    {
        log.info("Beginning test: 'NonPalTopoLayering4'.");

        topoBuilder.buildTopo4();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 12);
        assert(mplsVertices.size() == 12);

        assert(intEdges.size() == 36);
        assert(ethEdges.size() == 10);
        assert(mplsEdges.size() == 6);

        log.info("test 'NonPalTopoLayering4' passed.");
    }

    @Test
    public void NonPalTopoLayering5()
    {
        log.info("Beginning test: 'NonPalTopoLayering5'.");

        topoBuilder.buildTopo5();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 11);
        assert(mplsVertices.size() == 13);

        assert(intEdges.size() == 36);
        assert(ethEdges.size() == 10);
        assert(mplsEdges.size() == 6);

        log.info("test 'NonPalTopoLayering5' passed.");
    }

    @Test
    public void NonPalTopoLayering6()
    {
        log.info("Beginning test: 'NonPalTopoLayering6'.");

        topoBuilder.buildTopo6();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 0);
        assert(mplsVertices.size() == 3);

        assert(intEdges.size() == 4);
        assert(ethEdges.size() == 0);
        assert(mplsEdges.size() == 0);

        log.info("test 'NonPalTopoLayering6' passed.");
    }

    @Test
    public void NonPalTopoLayering7()
    {
        log.info("Beginning test: 'NonPalTopoLayering7'.");

        topoBuilder.buildTopo7();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 6);
        assert(mplsVertices.size() == 0);

        assert(intEdges.size() == 8);
        assert(ethEdges.size() == 2);
        assert(mplsEdges.size() == 0);

        log.info("test 'NonPalTopoLayering7' passed.");
    }

    @Test
    public void NonPalTopoLayering8()
    {
        log.info("Beginning test: 'NonPalTopoLayering8'.");

        topoBuilder.buildTopo8();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 0);
        assert(mplsVertices.size() == 6);

        assert(intEdges.size() == 8);
        assert(ethEdges.size() == 0);
        assert(mplsEdges.size() == 2);

        log.info("test 'NonPalTopoLayering8' passed.");
    }

    @Test
    public void NonPalTopoLayering9()
    {
        log.info("Beginning test: 'NonPalTopoLayering9'.");

        topoBuilder.buildTopo9();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 3);
        assert(mplsVertices.size() == 3);

        assert(intEdges.size() == 8);
        assert(ethEdges.size() == 2);
        assert(mplsEdges.size() == 0);

        log.info("test 'NonPalTopoLayering9' passed.");
    }

    @Test
    public void NonPalTopoLayering10()
    {
        log.info("Beginning test: 'NonPalTopoLayering10'.");

        topoBuilder.buildTopo10();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 14);
        assert(mplsVertices.size() == 0);

        assert(intEdges.size() == 20);
        assert(ethEdges.size() == 8);
        assert(mplsEdges.size() == 0);

        log.info("test 'NonPalTopoLayering10' passed.");
    }

    @Test
    public void NonPalTopoLayering11()
    {
        log.info("Beginning test: 'NonPalTopoLayering11'.");

        topoBuilder.buildTopo11();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 0);
        assert(mplsVertices.size() == 14);

        assert(intEdges.size() == 20);
        assert(ethEdges.size() == 0);
        assert(mplsEdges.size() == 8);

        log.info("test 'NonPalTopoLayering11' passed.");
    }

    @Test
    public void NonPalTopoLayering12()
    {
        log.info("Beginning test: 'NonPalTopoLayering12'.");

        topoBuilder.buildTopo12();
        this.performLayering();

        Set<TopoVertex> ethVertices = ethTopo.getVertices();
        Set<TopoVertex> intVertices = intTopo.getVertices();
        Set<TopoVertex> mplsVertices = mplsTopo.getVertices();
        Set<TopoEdge> ethEdges = ethTopo.getEdges();
        Set<TopoEdge> intEdges = intTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();

        log.info("IntVerts SIZE = " + intVertices.size());
        log.info("EthVerts SIZE = " + ethVertices.size());
        log.info("MplsVerts SIZE = " + mplsVertices.size());
        log.info("IntEdges SIZE = " + intEdges.size());
        log.info("EthEdges SIZE = " + ethEdges.size());
        log.info("MplsEdges SIZE = " + mplsEdges.size());

        assert(intVertices.isEmpty());
        assert(ethVertices.size() == 10);
        assert(mplsVertices.size() == 7);

        assert(intEdges.size() == 24);
        assert(ethEdges.size() == 8);
        assert(mplsEdges.size() == 2);

        log.info("test 'NonPalTopoLayering12' passed.");
    }

    private void performLayering()
    {
        ethTopo = topoService.layer(Layer.ETHERNET);
        intTopo = topoService.layer(Layer.INTERNAL);
        mplsTopo = topoService.layer(Layer.MPLS);

        // Filter MPLS-ports and MPLS-devices out of ethTopo
        Set<TopoVertex> portsOnly = ethTopo.getVertices().stream()
                .filter(v -> v.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());

        for(TopoEdge intEdge : intTopo.getEdges())
        {
            TopoVertex vertA = intEdge.getA();
            TopoVertex vertZ = intEdge.getZ();

            if(portsOnly.isEmpty())
            {
                break;
            }

            if(portsOnly.contains(vertA))
            {
                if(!vertZ.getVertexType().equals(VertexType.ROUTER))
                {
                    portsOnly.remove(vertA);
                }
            }
        }

        ethTopo.getVertices().removeIf(v -> v.getVertexType().equals(VertexType.ROUTER));
        ethTopo.getVertices().removeAll(portsOnly);

        // Filter Devices and Ports out of intTopo
        intTopo.getVertices().removeAll(intTopo.getVertices());
    }
}
