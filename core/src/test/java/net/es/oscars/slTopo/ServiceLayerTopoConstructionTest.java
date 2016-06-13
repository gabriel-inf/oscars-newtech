package net.es.oscars.slTopo;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.servicetopo.ServiceLayerTopology;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.enums.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.ReservedPssResourceE;
import net.es.oscars.resv.ent.RequestedVlanFixtureE;
import net.es.oscars.resv.ent.RequestedVlanJunctionE;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.topo.ent.UrnE;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ServiceLayerTopoConstructionTest
{
    ServiceLayerTopology serviceLayerTopo = new ServiceLayerTopology();

    Set<TopoVertex> ethernetTopoVertices = new HashSet<>();
    Set<TopoVertex> mplsTopoVertices = new HashSet<>();
    Set<TopoVertex> internalTopoVertices = new HashSet<>();

    Set<TopoEdge> ethernetTopoEdges = new HashSet<>();
    Set<TopoEdge> mplsTopoEdges = new HashSet<>();
    Set<TopoEdge> internalTopoEdges = new HashSet<>();

    @Test
    public void testAllEthernet()
    {
        TopoVertex switchA = new TopoVertex("swA", VertexType.SWITCH);
        TopoVertex switchB = new TopoVertex("swB", VertexType.SWITCH);
        TopoVertex switchC = new TopoVertex("swC", VertexType.SWITCH);

        TopoVertex switchAIn = new TopoVertex("swA:1", VertexType.PORT);
        TopoVertex switchAOut = new TopoVertex("swA:2", VertexType.PORT);
        TopoVertex switchBIn = new TopoVertex("swB:1", VertexType.PORT);
        TopoVertex switchBOut = new TopoVertex("swB:2", VertexType.PORT);
        TopoVertex switchCIn = new TopoVertex("swC:1", VertexType.PORT);
        TopoVertex switchCOut = new TopoVertex("swC:2", VertexType.PORT);

        // Directed edges
        TopoEdge edgeInt_Ain_A = new TopoEdge(switchAIn, switchA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_Aout = new TopoEdge(switchA, switchAOut, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Aout_Bin = new TopoEdge(switchAOut, switchBIn, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_Bin_B = new TopoEdge(switchBIn, switchB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_Bout = new TopoEdge(switchB, switchBOut, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Bout_Cin = new TopoEdge(switchBOut, switchCIn, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_Cin_C = new TopoEdge(switchCIn, switchC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_Cout = new TopoEdge(switchC, switchCOut, 0L, Layer.INTERNAL);

        // Reverse edges
        TopoEdge edgeInt_A_Ain = new TopoEdge(switchA, switchAIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Aout_A = new TopoEdge(switchAOut, switchA, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Bin_Aout = new TopoEdge(switchBIn, switchAOut, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_B_Bin = new TopoEdge(switchB, switchBIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Bout_B = new TopoEdge(switchBOut, switchB, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Cin_Bout = new TopoEdge(switchCIn, switchBOut, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_C_Cin = new TopoEdge(switchC, switchCIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Cout_C = new TopoEdge(switchCOut, switchC, 0L, Layer.INTERNAL);

        ethernetTopoVertices.add(switchA);
        ethernetTopoVertices.add(switchB);
        ethernetTopoVertices.add(switchC);
        ethernetTopoVertices.add(switchAIn);
        ethernetTopoVertices.add(switchBIn);
        ethernetTopoVertices.add(switchCIn);
        ethernetTopoVertices.add(switchAOut);
        ethernetTopoVertices.add(switchBOut);
        ethernetTopoVertices.add(switchCOut);

        internalTopoEdges.add(edgeInt_Ain_A);
        internalTopoEdges.add(edgeInt_Bin_B);
        internalTopoEdges.add(edgeInt_Cin_C);
        internalTopoEdges.add(edgeInt_A_Aout);
        internalTopoEdges.add(edgeInt_B_Bout);
        internalTopoEdges.add(edgeInt_C_Cout);
        internalTopoEdges.add(edgeInt_A_Ain);
        internalTopoEdges.add(edgeInt_B_Bin);
        internalTopoEdges.add(edgeInt_C_Cin);
        internalTopoEdges.add(edgeInt_Aout_A);
        internalTopoEdges.add(edgeInt_Bout_B);
        internalTopoEdges.add(edgeInt_Cout_C);
        ethernetTopoEdges.add(edgeEth_Aout_Bin);
        ethernetTopoEdges.add(edgeEth_Bout_Cin);
        ethernetTopoEdges.add(edgeEth_Bin_Aout);
        ethernetTopoEdges.add(edgeEth_Cin_Bout);

        Topology dummyEthernetTopo = new Topology();
        Topology dummyInternalTopo = new Topology();
        Topology dummyMPLSTopo = new Topology();

        dummyEthernetTopo.setLayer(Layer.ETHERNET);
        dummyEthernetTopo.setVertices(ethernetTopoVertices);
        dummyEthernetTopo.setEdges(ethernetTopoEdges);

        dummyInternalTopo.setLayer(Layer.INTERNAL);
        dummyInternalTopo.setVertices(internalTopoVertices);
        dummyInternalTopo.setEdges(internalTopoEdges);

        dummyMPLSTopo.setLayer(Layer.MPLS);
        dummyMPLSTopo.setVertices(mplsTopoVertices);
        dummyMPLSTopo.setEdges(mplsTopoEdges);

        serviceLayerTopo.setTopology(dummyEthernetTopo);
        serviceLayerTopo.setTopology(dummyInternalTopo);
        serviceLayerTopo.setTopology(dummyMPLSTopo);

        serviceLayerTopo.createMultilayerTopology();

        assert(serviceLayerTopo.getServiceLayerDevices().size() == 3);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 6);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 16);

        assert(serviceLayerTopo.getMplsLayerDevices().size() == 0);
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 0);
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 0);

        assert(serviceLayerTopo.getNonAdjacentPorts().size() == 0);
        assert(serviceLayerTopo.getLogicalLinks().size() == 0);
    }

    @Test
    public void testAllMPLS()
    {
        TopoVertex routerA = new TopoVertex("roA", VertexType.ROUTER);
        TopoVertex routerB = new TopoVertex("roB", VertexType.ROUTER);
        TopoVertex routerC = new TopoVertex("roC", VertexType.ROUTER);

        TopoVertex routerAIn = new TopoVertex("roA:1", VertexType.PORT);
        TopoVertex routerAOut = new TopoVertex("roA:2", VertexType.PORT);
        TopoVertex routerBIn = new TopoVertex("roB:1", VertexType.PORT);
        TopoVertex routerBOut = new TopoVertex("roB:2", VertexType.PORT);
        TopoVertex routerCIn = new TopoVertex("roC:1", VertexType.PORT);
        TopoVertex routerCOut = new TopoVertex("roC:2", VertexType.PORT);

        // Directed edges
        TopoEdge edgeInt_Ain_A = new TopoEdge(routerAIn, routerA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_Aout = new TopoEdge(routerA, routerAOut, 0L, Layer.INTERNAL);

        TopoEdge edgeMpls_Aout_Bin = new TopoEdge(routerAOut, routerBIn, 100L, Layer.MPLS);

        TopoEdge edgeInt_Bin_B = new TopoEdge(routerBIn, routerB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_Bout = new TopoEdge(routerB, routerBOut, 0L, Layer.INTERNAL);

        TopoEdge edgeMpls_Bout_Cin = new TopoEdge(routerBOut, routerCIn, 100L, Layer.MPLS);

        TopoEdge edgeInt_Cin_C = new TopoEdge(routerCIn, routerC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_Cout = new TopoEdge(routerC, routerCOut, 0L, Layer.INTERNAL);

        // Reverse edges
        TopoEdge edgeInt_A_Ain = new TopoEdge(routerA, routerAIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Aout_A = new TopoEdge(routerAOut, routerA, 0L, Layer.INTERNAL);

        TopoEdge edgeMpls_Bin_Aout = new TopoEdge(routerBIn, routerAOut, 100L, Layer.MPLS);

        TopoEdge edgeInt_B_Bin = new TopoEdge(routerB, routerBIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Bout_B = new TopoEdge(routerBOut, routerB, 0L, Layer.INTERNAL);

        TopoEdge edgeMpls_Cin_Bout = new TopoEdge(routerCIn, routerBOut, 100L, Layer.MPLS);

        TopoEdge edgeInt_C_Cin = new TopoEdge(routerC, routerCIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Cout_C = new TopoEdge(routerCOut, routerC, 0L, Layer.INTERNAL);

        mplsTopoVertices.add(routerA);
        mplsTopoVertices.add(routerB);
        mplsTopoVertices.add(routerC);
        mplsTopoVertices.add(routerAIn);
        mplsTopoVertices.add(routerBIn);
        mplsTopoVertices.add(routerCIn);
        mplsTopoVertices.add(routerAOut);
        mplsTopoVertices.add(routerBOut);
        mplsTopoVertices.add(routerCOut);

        internalTopoEdges.add(edgeInt_Ain_A);
        internalTopoEdges.add(edgeInt_Bin_B);
        internalTopoEdges.add(edgeInt_Cin_C);
        internalTopoEdges.add(edgeInt_A_Aout);
        internalTopoEdges.add(edgeInt_B_Bout);
        internalTopoEdges.add(edgeInt_C_Cout);
        internalTopoEdges.add(edgeInt_A_Ain);
        internalTopoEdges.add(edgeInt_B_Bin);
        internalTopoEdges.add(edgeInt_C_Cin);
        internalTopoEdges.add(edgeInt_Aout_A);
        internalTopoEdges.add(edgeInt_Bout_B);
        internalTopoEdges.add(edgeInt_Cout_C);
        mplsTopoEdges.add(edgeMpls_Aout_Bin);
        mplsTopoEdges.add(edgeMpls_Bout_Cin);
        mplsTopoEdges.add(edgeMpls_Bin_Aout);
        mplsTopoEdges.add(edgeMpls_Cin_Bout);

        Topology dummyEthernetTopo = new Topology();
        Topology dummyInternalTopo = new Topology();
        Topology dummyMPLSTopo = new Topology();

        dummyEthernetTopo.setLayer(Layer.ETHERNET);
        dummyEthernetTopo.setVertices(ethernetTopoVertices);
        dummyEthernetTopo.setEdges(ethernetTopoEdges);

        dummyInternalTopo.setLayer(Layer.INTERNAL);
        dummyInternalTopo.setVertices(internalTopoVertices);
        dummyInternalTopo.setEdges(internalTopoEdges);

        dummyMPLSTopo.setLayer(Layer.MPLS);
        dummyMPLSTopo.setVertices(mplsTopoVertices);
        dummyMPLSTopo.setEdges(mplsTopoEdges);

        serviceLayerTopo.setTopology(dummyEthernetTopo);
        serviceLayerTopo.setTopology(dummyInternalTopo);
        serviceLayerTopo.setTopology(dummyMPLSTopo);

        serviceLayerTopo.createMultilayerTopology();

        assert(serviceLayerTopo.getServiceLayerDevices().size() == 0);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 0);

        assert(serviceLayerTopo.getMplsLayerDevices().size() == 3);
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 6);
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 16);

        assert(serviceLayerTopo.getNonAdjacentPorts().size() == 0);
        assert(serviceLayerTopo.getLogicalLinks().size() == 0);
    }

    @Test
    public void testSingleSwitch()
    {
        TopoVertex routerA = new TopoVertex("roA", VertexType.ROUTER);
        TopoVertex switchB = new TopoVertex("swB", VertexType.SWITCH);
        TopoVertex routerC = new TopoVertex("roC", VertexType.ROUTER);

        TopoVertex routerAIn = new TopoVertex("roA:1", VertexType.PORT);
        TopoVertex routerAOut = new TopoVertex("roA:2", VertexType.PORT);
        TopoVertex switchBIn = new TopoVertex("swB:1", VertexType.PORT);
        TopoVertex switchBOut = new TopoVertex("swB:2", VertexType.PORT);
        TopoVertex routerCIn = new TopoVertex("roC:1", VertexType.PORT);
        TopoVertex routerCOut = new TopoVertex("roC:2", VertexType.PORT);

        // Directed edges
        TopoEdge edgeInt_Ain_A = new TopoEdge(routerAIn, routerA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_Aout = new TopoEdge(routerA, routerAOut, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Aout_Bin = new TopoEdge(routerAOut, switchBIn, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_Bin_B = new TopoEdge(switchBIn, switchB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_Bout = new TopoEdge(switchB, switchBOut, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Bout_Cin = new TopoEdge(switchBOut, routerCIn, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_Cin_C = new TopoEdge(routerCIn, routerC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_Cout = new TopoEdge(routerC, routerCOut, 0L, Layer.INTERNAL);

        // Reverse edges
        TopoEdge edgeInt_A_Ain = new TopoEdge(routerA, routerAIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Aout_A = new TopoEdge(routerAOut, routerA, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Bin_Aout = new TopoEdge(switchBIn, routerAOut, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_B_Bin = new TopoEdge(switchB, switchBIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Bout_B = new TopoEdge(switchBOut, switchB, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Cin_Bout = new TopoEdge(routerCIn, switchBOut, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_C_Cin = new TopoEdge(routerC, routerCIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Cout_C = new TopoEdge(routerCOut, routerC, 0L, Layer.INTERNAL);

        ethernetTopoVertices.add(switchB);
        ethernetTopoVertices.add(switchBIn);
        ethernetTopoVertices.add(switchBOut);
        
        mplsTopoVertices.add(routerA);
        mplsTopoVertices.add(routerC);
        mplsTopoVertices.add(routerAIn);
        mplsTopoVertices.add(routerCIn);
        mplsTopoVertices.add(routerAOut);
        mplsTopoVertices.add(routerCOut);

        internalTopoEdges.add(edgeInt_Ain_A);
        internalTopoEdges.add(edgeInt_Bin_B);
        internalTopoEdges.add(edgeInt_Cin_C);
        internalTopoEdges.add(edgeInt_A_Aout);
        internalTopoEdges.add(edgeInt_B_Bout);
        internalTopoEdges.add(edgeInt_C_Cout);
        internalTopoEdges.add(edgeInt_A_Ain);
        internalTopoEdges.add(edgeInt_B_Bin);
        internalTopoEdges.add(edgeInt_C_Cin);
        internalTopoEdges.add(edgeInt_Aout_A);
        internalTopoEdges.add(edgeInt_Bout_B);
        internalTopoEdges.add(edgeInt_Cout_C);
        
        ethernetTopoEdges.add(edgeEth_Aout_Bin);
        ethernetTopoEdges.add(edgeEth_Bout_Cin);
        ethernetTopoEdges.add(edgeEth_Bin_Aout);
        ethernetTopoEdges.add(edgeEth_Cin_Bout);

        Topology dummyEthernetTopo = new Topology();
        Topology dummyInternalTopo = new Topology();
        Topology dummyMPLSTopo = new Topology();

        dummyEthernetTopo.setLayer(Layer.ETHERNET);
        dummyEthernetTopo.setVertices(ethernetTopoVertices);
        dummyEthernetTopo.setEdges(ethernetTopoEdges);

        dummyInternalTopo.setLayer(Layer.INTERNAL);
        dummyInternalTopo.setVertices(internalTopoVertices);
        dummyInternalTopo.setEdges(internalTopoEdges);

        dummyMPLSTopo.setLayer(Layer.MPLS);
        dummyMPLSTopo.setVertices(mplsTopoVertices);
        dummyMPLSTopo.setEdges(mplsTopoEdges);

        serviceLayerTopo.setTopology(dummyEthernetTopo);
        serviceLayerTopo.setTopology(dummyInternalTopo);
        serviceLayerTopo.setTopology(dummyMPLSTopo);

        serviceLayerTopo.createMultilayerTopology();

        assert(serviceLayerTopo.getServiceLayerDevices().size() == 1);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 2);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 8);

        assert(serviceLayerTopo.getMplsLayerDevices().size() == 2);
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 4);
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 8);

        assert(serviceLayerTopo.getNonAdjacentPorts().size() == 2);
        assert(serviceLayerTopo.getLogicalLinks().size() == 2);
    }

    @Test
    public void testSingleRouter()
    {
        TopoVertex switchA = new TopoVertex("swA", VertexType.SWITCH);
        TopoVertex routerB = new TopoVertex("roB", VertexType.ROUTER);
        TopoVertex switchC = new TopoVertex("swC", VertexType.SWITCH);

        TopoVertex switchAIn = new TopoVertex("swA:1", VertexType.PORT);
        TopoVertex switchAOut = new TopoVertex("swA:2", VertexType.PORT);
        TopoVertex routerBIn = new TopoVertex("roB:1", VertexType.PORT);
        TopoVertex routerBOut = new TopoVertex("roB:2", VertexType.PORT);
        TopoVertex switchCIn = new TopoVertex("swC:1", VertexType.PORT);
        TopoVertex switchCOut = new TopoVertex("swC:2", VertexType.PORT);

        // Directed edges
        TopoEdge edgeInt_Ain_A = new TopoEdge(switchAIn, switchA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_Aout = new TopoEdge(switchA, switchAOut, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Aout_Bin = new TopoEdge(switchAOut, routerBIn, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_Bin_B = new TopoEdge(routerBIn, routerB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_Bout = new TopoEdge(routerB, routerBOut, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Bout_Cin = new TopoEdge(routerBOut, switchCIn, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_Cin_C = new TopoEdge(switchCIn, switchC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_Cout = new TopoEdge(switchC, switchCOut, 0L, Layer.INTERNAL);

        // Reverse edges
        TopoEdge edgeInt_A_Ain = new TopoEdge(switchA, switchAIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Aout_A = new TopoEdge(switchAOut, switchA, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Bin_Aout = new TopoEdge(routerBIn, switchAOut, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_B_Bin = new TopoEdge(routerB, routerBIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Bout_B = new TopoEdge(routerBOut, routerB, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Cin_Bout = new TopoEdge(switchCIn, routerBOut, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_C_Cin = new TopoEdge(switchC, switchCIn, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Cout_C = new TopoEdge(switchCOut, switchC, 0L, Layer.INTERNAL);

        ethernetTopoVertices.add(switchA);
        ethernetTopoVertices.add(switchAIn);
        ethernetTopoVertices.add(switchAOut);
        ethernetTopoVertices.add(switchC);
        ethernetTopoVertices.add(switchCIn);
        ethernetTopoVertices.add(switchCOut);

        mplsTopoVertices.add(routerB);
        mplsTopoVertices.add(routerBIn);
        mplsTopoVertices.add(routerBOut);

        internalTopoEdges.add(edgeInt_Ain_A);
        internalTopoEdges.add(edgeInt_Bin_B);
        internalTopoEdges.add(edgeInt_Cin_C);
        internalTopoEdges.add(edgeInt_A_Aout);
        internalTopoEdges.add(edgeInt_B_Bout);
        internalTopoEdges.add(edgeInt_C_Cout);
        internalTopoEdges.add(edgeInt_A_Ain);
        internalTopoEdges.add(edgeInt_B_Bin);
        internalTopoEdges.add(edgeInt_C_Cin);
        internalTopoEdges.add(edgeInt_Aout_A);
        internalTopoEdges.add(edgeInt_Bout_B);
        internalTopoEdges.add(edgeInt_Cout_C);

        ethernetTopoEdges.add(edgeEth_Aout_Bin);
        ethernetTopoEdges.add(edgeEth_Bout_Cin);
        ethernetTopoEdges.add(edgeEth_Bin_Aout);
        ethernetTopoEdges.add(edgeEth_Cin_Bout);

        Topology dummyEthernetTopo = new Topology();
        Topology dummyInternalTopo = new Topology();
        Topology dummyMPLSTopo = new Topology();

        dummyEthernetTopo.setLayer(Layer.ETHERNET);
        dummyEthernetTopo.setVertices(ethernetTopoVertices);
        dummyEthernetTopo.setEdges(ethernetTopoEdges);

        dummyInternalTopo.setLayer(Layer.INTERNAL);
        dummyInternalTopo.setVertices(internalTopoVertices);
        dummyInternalTopo.setEdges(internalTopoEdges);

        dummyMPLSTopo.setLayer(Layer.MPLS);
        dummyMPLSTopo.setVertices(mplsTopoVertices);
        dummyMPLSTopo.setEdges(mplsTopoEdges);

        serviceLayerTopo.setTopology(dummyEthernetTopo);
        serviceLayerTopo.setTopology(dummyInternalTopo);
        serviceLayerTopo.setTopology(dummyMPLSTopo);

        serviceLayerTopo.createMultilayerTopology();

        assert(serviceLayerTopo.getServiceLayerDevices().size() == 2);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 4);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 12);

        assert(serviceLayerTopo.getMplsLayerDevices().size() == 1);
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 2);
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 4);

        assert(serviceLayerTopo.getNonAdjacentPorts().size() == 2);

        assert(serviceLayerTopo.getLogicalLinks().size() == 2);
    }
}