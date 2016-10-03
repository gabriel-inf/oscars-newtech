package net.es.oscars.servicetopo;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.VertexType;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ServiceLayerTopoConstructionTest {

    private ServiceLayerTopology serviceLayerTopology = new ServiceLayerTopology();

    private Set<TopoVertex> ethernetTopoVertices = new HashSet<>();
    private Set<TopoVertex> mplsTopoVertices = new HashSet<>();
    private Set<TopoVertex> internalTopoVertices = new HashSet<>();

    private Set<TopoEdge> ethernetTopoEdges = new HashSet<>();
    private Set<TopoEdge> mplsTopoEdges = new HashSet<>();
    private Set<TopoEdge> internalTopoEdges = new HashSet<>();

    @Test
    public void testAllEthernet()
    {
        log.info("testing simple Service-Layer topology: 3-nodes, all switches.");

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

        this.constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 3);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 6);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 16);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 0);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 0);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 0);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 0);
        assert (serviceLayerTopology.getLogicalLinks().size() == 0);

        log.info("test complete.");
    }

    @Test
    public void testAllMPLS()
    {
        log.info("testing simple Service-Layer topology: 3-nodes, all routers.");

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

        this.constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 0);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 0);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 0);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 3);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 6);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 16);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 0);
        assert (serviceLayerTopology.getLogicalLinks().size() == 0);

        log.info("test complete.");
    }

    @Test
    public void testSingleSwitch()
    {
        log.info("testing simple Service-Layer topology: 3-nodes, router-switch-router.");

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

        this.constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 1);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 2);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 8);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 2);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 4);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 8);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 2);
        assert (serviceLayerTopology.getLogicalLinks().size() == 2);

        log.info("test complete.");
    }

    @Test
    public void testSingleRouter()
    {
        log.info("testing simple Service-Layer topology: 3-nodes, switch-router-switch.");

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

        this.constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 2);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 4);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 12);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 1);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 2);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 4);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 2);

        assert (serviceLayerTopology.getLogicalLinks().size() == 2);

        log.info("test complete.");
    }

    @Test
    public void testFourSwitchOneRouter()
    {
        log.info("testing simple Service-Layer topology: 5-nodes, S-S-R-S-S.");
        Topology networkTopology = this.buildFiveNodeTopo();
        Set<TopoVertex> topoNodes = networkTopology.getVertices();
        Set<TopoEdge> topoEdges = networkTopology.getEdges();

        // Manually set layers for various devices
        for(TopoVertex oneVertex : topoNodes)
        {
            String vURN = oneVertex.getUrn();
            if(vURN.equals("nodeC"))
            {
                oneVertex.setVertexType(VertexType.ROUTER);
            }

            if(oneVertex.getVertexType().equals(VertexType.SWITCH))
            {
                ethernetTopoVertices.add(oneVertex);
            }
            else if(oneVertex.getVertexType().equals(VertexType.ROUTER))
            {
                mplsTopoVertices.add(oneVertex);
            }
            else
            {
                if(vURN.equals("nodeC:1") || vURN.equals("nodeC:2"))
                {
                    mplsTopoVertices.add(oneVertex);
                }
                else
                {
                    ethernetTopoVertices.add(oneVertex);
                }
            }
        }

        // Manually set layers for various edges
        for(TopoEdge oneEdge : topoEdges)
        {
            ethernetTopoEdges.add(oneEdge);
        }


        this.constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 4);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 8);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 24);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 1);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 2);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 4);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 2);

        assert (serviceLayerTopology.getLogicalLinks().size() == 2);

        serviceLayerTopology.getLogicalLinks().stream()
                .forEach(ll -> {
                    String aURN = ll.getA().getUrn();
                    String zURN = ll.getZ().getUrn();
                    if(aURN.equals("nodeB:2"))
                        assert(zURN.equals("nodeD:1"));
                    else if(aURN.equals("nodeD:1"))
                        assert(zURN.equals("nodeB:2"));
                    else
                        assert false;
                });

        log.info("test complete.");
    }


    @Test
    public void testFourRoutersOneSwitch()
    {
        log.info("testing simple Service-Layer topology: 5-nodes, R-R-S-R-R.");

        Topology networkTopology = this.buildFiveNodeTopo();
        Set<TopoVertex> topoNodes = networkTopology.getVertices();
        Set<TopoEdge> topoEdges = networkTopology.getEdges();

        // Manually set layers for various devices
        for(TopoVertex oneVertex : topoNodes)
        {
            String vURN = oneVertex.getUrn();
            if(vURN.equals("nodeA") || vURN.equals("nodeB") || vURN.equals("nodeD") || vURN.equals("nodeE"))
            {
                oneVertex.setVertexType(VertexType.ROUTER);
            }

            if(oneVertex.getVertexType().equals(VertexType.SWITCH))
            {
                ethernetTopoVertices.add(oneVertex);
            }
            else if(oneVertex.getVertexType().equals(VertexType.ROUTER))
            {
                mplsTopoVertices.add(oneVertex);
            }
            else
            {
                if(vURN.equals("nodeA:1") || vURN.equals("nodeA:2") || vURN.equals("nodeB:1") || vURN.equals("nodeB:2") || vURN.equals("nodeD:1") || vURN.equals("nodeD:2") || vURN.equals("nodeE:1") || vURN.equals("nodeE:2"))
                {
                    mplsTopoVertices.add(oneVertex);
                }
                else
                {
                    ethernetTopoVertices.add(oneVertex);
                }
            }
        }

        // Manually set layers for various edges
        for(TopoEdge oneEdge : topoEdges)
        {
            String aURN = oneEdge.getA().getUrn();
            String zURN = oneEdge.getZ().getUrn();

            if((aURN.equals("nodeA:2") && zURN.equals("nodeB:1")) || (aURN.equals("nodeB:1") && zURN.equals("nodeA:2")) || (aURN.equals("nodeD:2") && zURN.equals("nodeE:1")) || (aURN.equals("nodeE:1") && zURN.equals("nodeD:2")))
            {
                oneEdge.setLayer(Layer.MPLS);
            }

            if(oneEdge.getLayer().equals(Layer.ETHERNET))
                ethernetTopoEdges.add(oneEdge);
            else
                mplsTopoEdges.add(oneEdge);
        }


        this.constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 1);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 2);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 8);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 4);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 8);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 20);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 2);

        assert (serviceLayerTopology.getLogicalLinks().size() == 2);

        serviceLayerTopology.getLogicalLinks().stream()
                .forEach(ll -> {
                    String aURN = ll.getA().getUrn();
                    String zURN = ll.getZ().getUrn();
                    if(aURN.equals("nodeC:1"))
                        assert(zURN.equals("nodeC:2"));
                    else if(aURN.equals("nodeC:2"))
                        assert(zURN.equals("nodeC:1"));
                    else
                        assert false;
                });

        log.info("test complete.");
    }

    @Test
    public void testTwoRoutersThreeSwitches()
    {
        log.info("testing simple Service-Layer topology: 5-nodes, S-R-R-S-S.");

        Topology networkTopology = this.buildFiveNodeTopo();
        Set<TopoVertex> topoNodes = networkTopology.getVertices();
        Set<TopoEdge> topoEdges = networkTopology.getEdges();

        // Manually set layers for various devices
        for(TopoVertex oneVertex : topoNodes)
        {
            String vURN = oneVertex.getUrn();
            if(vURN.equals("nodeB") || vURN.equals("nodeC"))
            {
                oneVertex.setVertexType(VertexType.ROUTER);
            }

            if(oneVertex.getVertexType().equals(VertexType.SWITCH))
            {
                ethernetTopoVertices.add(oneVertex);
            }
            else if(oneVertex.getVertexType().equals(VertexType.ROUTER))
            {
                mplsTopoVertices.add(oneVertex);
            }
            else
            {
                if(vURN.equals("nodeB:1") || vURN.equals("nodeB:2") || vURN.equals("nodeC:1") || vURN.equals("nodeC:2"))
                {
                    mplsTopoVertices.add(oneVertex);
                }
                else
                {
                    ethernetTopoVertices.add(oneVertex);
                }
            }
        }

        // Manually set layers for various edges
        for(TopoEdge oneEdge : topoEdges)
        {
            String aURN = oneEdge.getA().getUrn();
            String zURN = oneEdge.getZ().getUrn();

            if((aURN.equals("nodeB:2") && zURN.equals("nodeC:1")) || (aURN.equals("nodeC:1") && zURN.equals("nodeB:2")))
            {
                oneEdge.setLayer(Layer.MPLS);
            }

            if(oneEdge.getLayer().equals(Layer.ETHERNET))
                ethernetTopoEdges.add(oneEdge);
            else
                mplsTopoEdges.add(oneEdge);
        }


        this.constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 3);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 6);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 18);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 2);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 4);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 10);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 2);

        assert (serviceLayerTopology.getLogicalLinks().size() == 2);

        serviceLayerTopology.getLogicalLinks().stream()
                .forEach(ll -> {
                    String aURN = ll.getA().getUrn();
                    String zURN = ll.getZ().getUrn();
                    if(aURN.equals("nodeA:2"))
                        assert(zURN.equals("nodeD:1"));
                    else if(aURN.equals("nodeD:1"))
                        assert(zURN.equals("nodeA:2"));
                    else
                        assert false;
                });

        log.info("test complete.");
    }

    @Test
    public void testTwoRoutersThreeSwitchesAdditionalLogicalLinks()
    {
        log.info("testing more complex Service-Layer topology: 5-nodes, S==R-R-S-S.");

        Topology networkTopology = this.buildFiveNodeTopo();
        Set<TopoVertex> topoNodes = networkTopology.getVertices();
        Set<TopoEdge> topoEdges = networkTopology.getEdges();

        /* Add two additional ports (with internal links) and two links -> Will result in additional logical links */
        TopoVertex nodeA = null;
        TopoVertex nodeB = null;
        TopoVertex portA3 = new TopoVertex("nodeA:3", VertexType.PORT);
        TopoVertex portB3 = new TopoVertex("nodeB:3", VertexType.PORT);

        for(TopoVertex n : topoNodes)
        {
            if(n.getUrn().equals("nodeA"))
                nodeA = n;
            if(n.getUrn().equals("nodeB"))
                nodeB = n;

            if(nodeA != null && nodeB != null)
                break;
        }

        TopoEdge edgeInt_A_A3 = new TopoEdge(nodeA, portA3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A3_A = new TopoEdge(portA3, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B3 = new TopoEdge(nodeB, portB3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B3_B = new TopoEdge(portB3, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeNet_A3_B3 = new TopoEdge(portA3, portB3, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_B3_A3 = new TopoEdge(portB3, portA3, 100L, Layer.ETHERNET);

        topoNodes.add(portA3);
        topoNodes.add(portB3);
        topoEdges.add(edgeNet_A3_B3);
        topoEdges.add(edgeNet_B3_A3);

        internalTopoEdges.add(edgeInt_A_A3);
        internalTopoEdges.add(edgeInt_A3_A);
        internalTopoEdges.add(edgeInt_B_B3);
        internalTopoEdges.add(edgeInt_B3_B);
        /* * */

        // Manually set layers for various devices
        for(TopoVertex oneVertex : topoNodes)
        {
            String vURN = oneVertex.getUrn();
            if(vURN.equals("nodeB") || vURN.equals("nodeC"))
            {
                oneVertex.setVertexType(VertexType.ROUTER);
            }

            if(oneVertex.getVertexType().equals(VertexType.SWITCH))
            {
                ethernetTopoVertices.add(oneVertex);
            }
            else if(oneVertex.getVertexType().equals(VertexType.ROUTER))
            {
                mplsTopoVertices.add(oneVertex);
            }
            else
            {
                if(vURN.equals("nodeB:1") || vURN.equals("nodeB:2") || vURN.equals("nodeC:1") || vURN.equals("nodeC:2") || vURN.equals("nodeB:3"))
                {
                    mplsTopoVertices.add(oneVertex);
                }
                else
                {
                    ethernetTopoVertices.add(oneVertex);
                }
            }
        }

        // Manually set layers for various edges
        for(TopoEdge oneEdge : topoEdges)
        {
            String aURN = oneEdge.getA().getUrn();
            String zURN = oneEdge.getZ().getUrn();

            if((aURN.equals("nodeB:2") && zURN.equals("nodeC:1")) || (aURN.equals("nodeC:1") && zURN.equals("nodeB:2")))
            {
                oneEdge.setLayer(Layer.MPLS);
            }

            if(oneEdge.getLayer().equals(Layer.ETHERNET))
                ethernetTopoEdges.add(oneEdge);
            else
                mplsTopoEdges.add(oneEdge);
        }

        this.constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 3);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 7);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 22);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 2);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 5);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 12);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 3);

        assert (serviceLayerTopology.getLogicalLinks().size() == 6);

        serviceLayerTopology.getLogicalLinks().stream()
                .forEach(ll -> {
                    String aURN = ll.getA().getUrn();
                    String zURN = ll.getZ().getUrn();
                    if(aURN.equals("nodeA:2"))
                        assert(zURN.equals("nodeD:1") || zURN.equals("nodeA:3"));
                    else if(aURN.equals("nodeD:1"))
                        assert(zURN.equals("nodeA:2") || zURN.equals("nodeA:3"));
                    else if(aURN.equals("nodeA:3"))
                        assert(zURN.equals("nodeA:2") || zURN.equals("nodeD:1"));
                    else
                        assert false;
                });

        log.info("test complete.");
    }


    @Test
    public void testRouterHubStar()
    {
        log.info("testing star-shaped Service-Layer topology: 5-nodes, 4 switches, connected to same router.");

        this.buildStarTopo();

        this.constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 4);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 8);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 24);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 1);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 4);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 8);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 4);

        assert (serviceLayerTopology.getLogicalLinks().size() == 12);

        int ASrcNum = 0, BSrcNum = 0, CSrcNum = 0, DSrcNum = 0, wrongSrcNum = 0;

        for(LogicalEdge ll : serviceLayerTopology.getLogicalLinks())
        {
            String aURN = ll.getA().getUrn();

            if(aURN.equals("nodeA:2"))
                ASrcNum++;
            else if(aURN.equals("nodeB:2"))
                BSrcNum++;
            else if(aURN.equals("nodeC:2"))
                CSrcNum++;
            else if(aURN.equals("nodeD:2"))
                DSrcNum++;
            else
                wrongSrcNum++;
        };

        assert(ASrcNum == 3);
        assert(BSrcNum == 3);
        assert(CSrcNum == 3);
        assert(DSrcNum == 3);
        assert(wrongSrcNum == 0);

        log.info("test complete.");
    }

    @Test
    public void testComplexTopo()
    {
        log.info("testing complex Service-Layer topology: you'll have to see the picture for description.");

        buildComplexTopo();

        constructLayeredTopology();

        assert (serviceLayerTopology.getServiceLayerDevices().size() == 4);
        assert (serviceLayerTopology.getServiceLayerPorts().size() == 12);
        assert (serviceLayerTopology.getServiceLayerLinks().size() == 38);

        assert (serviceLayerTopology.getMplsLayerDevices().size() == 6);
        assert (serviceLayerTopology.getMplsLayerPorts().size() == 15);
        assert (serviceLayerTopology.getMplsLayerLinks().size() == 38);

        assert (serviceLayerTopology.getNonAdjacentPorts().size() == 5);

        assert (serviceLayerTopology.getLogicalLinks().size() == 20);

        int A3SrcNum = 0, B3SrcNum = 0, B4SrcNum = 0, C1SrcNum = 0, D3SrcNum = 0, wrongSrcNum = 0;

        for(LogicalEdge ll : serviceLayerTopology.getLogicalLinks())
        {
            String aURN = ll.getA().getUrn();
            String zURN = ll.getZ().getUrn();

            if(aURN.equals("nodeA:3"))
            {
                A3SrcNum++;
                assert(zURN.equals("nodeB:3") || zURN.equals("nodeB:4") || zURN.equals("nodeC:1") || zURN.equals("nodeD:3"));
            }
            else if(aURN.equals("nodeB:3"))
            {
                B3SrcNum++;
                assert(zURN.equals("nodeA:3") || zURN.equals("nodeB:4") || zURN.equals("nodeC:1") || zURN.equals("nodeD:3"));
            }
            else if(aURN.equals("nodeB:4"))
            {
                B4SrcNum++;
                assert(zURN.equals("nodeA:3") || zURN.equals("nodeB:3") || zURN.equals("nodeC:1") || zURN.equals("nodeD:3"));
            }
            else if(aURN.equals("nodeC:1"))
            {
                C1SrcNum++;
                assert(zURN.equals("nodeA:3") || zURN.equals("nodeB:3") || zURN.equals("nodeB:4") || zURN.equals("nodeD:3"));
            }
            else if(aURN.equals("nodeD:3"))
            {
                D3SrcNum++;
                assert(zURN.equals("nodeA:3") || zURN.equals("nodeB:3") || zURN.equals("nodeB:4") || zURN.equals("nodeC:1"));
            }
            else
                wrongSrcNum++;
        };

        assert(A3SrcNum == 4);
        assert(B3SrcNum == 4);
        assert(B4SrcNum == 4);
        assert(C1SrcNum == 4);
        assert(D3SrcNum == 4);
        assert(wrongSrcNum == 0);

        log.info("test complete.");
    }

    private void constructLayeredTopology()
    {
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

        serviceLayerTopology.setTopology(dummyEthernetTopo);
        serviceLayerTopology.setTopology(dummyInternalTopo);
        serviceLayerTopology.setTopology(dummyMPLSTopo);

        serviceLayerTopology.createMultilayerTopology();
    }

    private Topology buildFiveNodeTopo()
    {
        Set<TopoVertex> nodesAndPorts = new HashSet<>();
        Set<TopoEdge> networkEdges = new HashSet<>();
        
        //Devices
        TopoVertex nodeA = new TopoVertex("nodeA", VertexType.SWITCH);
        TopoVertex nodeB = new TopoVertex("nodeB", VertexType.SWITCH);
        TopoVertex nodeC = new TopoVertex("nodeC", VertexType.SWITCH);
        TopoVertex nodeD = new TopoVertex("nodeD", VertexType.SWITCH);
        TopoVertex nodeE = new TopoVertex("nodeE", VertexType.SWITCH);


        //Ports
        TopoVertex portA1 = new TopoVertex("nodeA:1", VertexType.PORT);
        TopoVertex portA2 = new TopoVertex("nodeA:2", VertexType.PORT);
        TopoVertex portB1 = new TopoVertex("nodeB:1", VertexType.PORT);
        TopoVertex portB2 = new TopoVertex("nodeB:2", VertexType.PORT);
        TopoVertex portC1 = new TopoVertex("nodeC:1", VertexType.PORT);
        TopoVertex portC2 = new TopoVertex("nodeC:2", VertexType.PORT);
        TopoVertex portD1 = new TopoVertex("nodeD:1", VertexType.PORT);
        TopoVertex portD2 = new TopoVertex("nodeD:2", VertexType.PORT);
        TopoVertex portE1 = new TopoVertex("nodeE:1", VertexType.PORT);
        TopoVertex portE2 = new TopoVertex("nodeE:2", VertexType.PORT);

        // Internal Directed edges
        TopoEdge edgeInt_A1_A = new TopoEdge(portA1, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_A2 = new TopoEdge(nodeA, portA2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B1_B = new TopoEdge(portB1, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B2 = new TopoEdge(nodeB, portB2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C1_C = new TopoEdge(portC1, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C2 = new TopoEdge(nodeC, portC2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D1_D = new TopoEdge(portD1, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D2 = new TopoEdge(nodeD, portD2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E1_E = new TopoEdge(portE1, nodeE, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E2 = new TopoEdge(nodeE, portE2, 0L, Layer.INTERNAL);

        // Internal Reverse edges
        TopoEdge edgeInt_A_A1 = new TopoEdge(nodeA, portA1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A2_A = new TopoEdge(portA2, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B1 = new TopoEdge(nodeB, portB1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B2_B = new TopoEdge(portB2, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C1 = new TopoEdge(nodeC, portC1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C2_C = new TopoEdge(portC2, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D1 = new TopoEdge(nodeD, portD1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D2_D = new TopoEdge(portD2, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E1 = new TopoEdge(nodeE, portE1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E2_E = new TopoEdge(portE2, nodeE, 0L, Layer.INTERNAL);

        // Network Directed edges
        TopoEdge edgeNet_A2_B1 = new TopoEdge(portA2, portB1, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_B2_C1 = new TopoEdge(portB2, portC1, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_C2_D1 = new TopoEdge(portC2, portD1, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_D2_E1 = new TopoEdge(portD2, portE1, 100L, Layer.ETHERNET);

        // Network Reverse edges
        TopoEdge edgeNet_B1_A2 = new TopoEdge(portB1, portA2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_C1_B2 = new TopoEdge(portC1, portB2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_D1_C2 = new TopoEdge(portD1, portC2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_E1_D2 = new TopoEdge(portE1, portD2, 100L, Layer.ETHERNET);


        nodesAndPorts.add(nodeA);
        nodesAndPorts.add(nodeB);
        nodesAndPorts.add(nodeC);
        nodesAndPorts.add(nodeD);
        nodesAndPorts.add(nodeE);
        nodesAndPorts.add(portA1);
        nodesAndPorts.add(portA2);
        nodesAndPorts.add(portB1);
        nodesAndPorts.add(portB2);
        nodesAndPorts.add(portC1);
        nodesAndPorts.add(portC2);
        nodesAndPorts.add(portD1);
        nodesAndPorts.add(portD2);
        nodesAndPorts.add(portE1);
        nodesAndPorts.add(portE2);

        internalTopoEdges.add(edgeInt_A1_A);
        internalTopoEdges.add(edgeInt_B1_B);
        internalTopoEdges.add(edgeInt_C1_C);
        internalTopoEdges.add(edgeInt_D1_D);
        internalTopoEdges.add(edgeInt_E1_E);
        internalTopoEdges.add(edgeInt_A2_A);
        internalTopoEdges.add(edgeInt_B2_B);
        internalTopoEdges.add(edgeInt_C2_C);
        internalTopoEdges.add(edgeInt_D2_D);
        internalTopoEdges.add(edgeInt_E2_E);
        internalTopoEdges.add(edgeInt_A_A1);
        internalTopoEdges.add(edgeInt_B_B1);
        internalTopoEdges.add(edgeInt_C_C1);
        internalTopoEdges.add(edgeInt_D_D1);
        internalTopoEdges.add(edgeInt_E_E1);
        internalTopoEdges.add(edgeInt_A_A2);
        internalTopoEdges.add(edgeInt_B_B2);
        internalTopoEdges.add(edgeInt_C_C2);
        internalTopoEdges.add(edgeInt_D_D2);
        internalTopoEdges.add(edgeInt_E_E2);

        networkEdges.add(edgeNet_A2_B1);
        networkEdges.add(edgeNet_B2_C1);
        networkEdges.add(edgeNet_C2_D1);
        networkEdges.add(edgeNet_D2_E1);
        networkEdges.add(edgeNet_B1_A2);
        networkEdges.add(edgeNet_C1_B2);
        networkEdges.add(edgeNet_D1_C2);
        networkEdges.add(edgeNet_E1_D2);

        Topology theTopology = new Topology();
        theTopology.setVertices(nodesAndPorts);
        theTopology.setEdges(networkEdges);

        return theTopology;
    }

    private void buildStarTopo()
    {
        //Devices
        TopoVertex nodeA = new TopoVertex("nodeA", VertexType.SWITCH);
        TopoVertex nodeB = new TopoVertex("nodeB", VertexType.SWITCH);
        TopoVertex nodeC = new TopoVertex("nodeC", VertexType.SWITCH);
        TopoVertex nodeD = new TopoVertex("nodeD", VertexType.SWITCH);
        TopoVertex nodeE = new TopoVertex("nodeE", VertexType.ROUTER);


        //Ports
        TopoVertex portA1 = new TopoVertex("nodeA:1", VertexType.PORT);
        TopoVertex portA2 = new TopoVertex("nodeA:2", VertexType.PORT);
        TopoVertex portB1 = new TopoVertex("nodeB:1", VertexType.PORT);
        TopoVertex portB2 = new TopoVertex("nodeB:2", VertexType.PORT);
        TopoVertex portC1 = new TopoVertex("nodeC:1", VertexType.PORT);
        TopoVertex portC2 = new TopoVertex("nodeC:2", VertexType.PORT);
        TopoVertex portD1 = new TopoVertex("nodeD:1", VertexType.PORT);
        TopoVertex portD2 = new TopoVertex("nodeD:2", VertexType.PORT);
        TopoVertex portE1 = new TopoVertex("nodeE:1", VertexType.PORT);
        TopoVertex portE2 = new TopoVertex("nodeE:2", VertexType.PORT);
        TopoVertex portE3 = new TopoVertex("nodeE:3", VertexType.PORT);
        TopoVertex portE4 = new TopoVertex("nodeE:4", VertexType.PORT);

        // Internal Directed edges
        TopoEdge edgeInt_A1_A = new TopoEdge(portA1, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_A2 = new TopoEdge(nodeA, portA2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B1_B = new TopoEdge(portB1, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B2 = new TopoEdge(nodeB, portB2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C1_C = new TopoEdge(portC1, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C2 = new TopoEdge(nodeC, portC2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D1_D = new TopoEdge(portD1, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D2 = new TopoEdge(nodeD, portD2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E1_E = new TopoEdge(portE1, nodeE, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E2_E = new TopoEdge(portE2, nodeE, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E3_E = new TopoEdge(portE3, nodeE, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E4_E = new TopoEdge(portE4, nodeE, 0L, Layer.INTERNAL);


        // Internal Reverse edges
        TopoEdge edgeInt_A_A1 = new TopoEdge(nodeA, portA1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A2_A = new TopoEdge(portA2, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B1 = new TopoEdge(nodeB, portB1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B2_B = new TopoEdge(portB2, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C1 = new TopoEdge(nodeC, portC1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C2_C = new TopoEdge(portC2, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D1 = new TopoEdge(nodeD, portD1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D2_D = new TopoEdge(portD2, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E1 = new TopoEdge(nodeE, portE1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E2 = new TopoEdge(nodeE, portE2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E3 = new TopoEdge(nodeE, portE3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E4 = new TopoEdge(nodeE, portE4, 0L, Layer.INTERNAL);


        // Network Directed edges
        TopoEdge edgeNet_A2_E1 = new TopoEdge(portA2, portE1, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_B2_E2 = new TopoEdge(portB2, portE2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_C2_E3 = new TopoEdge(portC2, portE3, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_D2_E4 = new TopoEdge(portD2, portE4, 100L, Layer.ETHERNET);

        // Network Reverse edges
        TopoEdge edgeNet_E1_A2 = new TopoEdge(portE1, portA2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_E2_B2 = new TopoEdge(portE2, portB2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_E3_C2 = new TopoEdge(portE3, portC2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_E4_D2 = new TopoEdge(portE4, portD2, 100L, Layer.ETHERNET);


        ethernetTopoVertices.add(nodeA);
        ethernetTopoVertices.add(nodeB);
        ethernetTopoVertices.add(nodeC);
        ethernetTopoVertices.add(nodeD);
        ethernetTopoVertices.add(portA1);
        ethernetTopoVertices.add(portA2);
        ethernetTopoVertices.add(portB1);
        ethernetTopoVertices.add(portB2);
        ethernetTopoVertices.add(portC1);
        ethernetTopoVertices.add(portC2);
        ethernetTopoVertices.add(portD1);
        ethernetTopoVertices.add(portD2);
        
        mplsTopoVertices.add(nodeE);
        mplsTopoVertices.add(portE1);
        mplsTopoVertices.add(portE2);
        mplsTopoVertices.add(portE3);
        mplsTopoVertices.add(portE4);

        internalTopoEdges.add(edgeInt_A1_A);
        internalTopoEdges.add(edgeInt_B1_B);
        internalTopoEdges.add(edgeInt_C1_C);
        internalTopoEdges.add(edgeInt_D1_D);
        internalTopoEdges.add(edgeInt_E1_E);
        internalTopoEdges.add(edgeInt_A2_A);
        internalTopoEdges.add(edgeInt_B2_B);
        internalTopoEdges.add(edgeInt_C2_C);
        internalTopoEdges.add(edgeInt_D2_D);
        internalTopoEdges.add(edgeInt_E2_E);
        internalTopoEdges.add(edgeInt_E3_E);
        internalTopoEdges.add(edgeInt_E4_E);
        internalTopoEdges.add(edgeInt_A_A1);
        internalTopoEdges.add(edgeInt_B_B1);
        internalTopoEdges.add(edgeInt_C_C1);
        internalTopoEdges.add(edgeInt_D_D1);
        internalTopoEdges.add(edgeInt_E_E1);
        internalTopoEdges.add(edgeInt_A_A2);
        internalTopoEdges.add(edgeInt_B_B2);
        internalTopoEdges.add(edgeInt_C_C2);
        internalTopoEdges.add(edgeInt_D_D2);
        internalTopoEdges.add(edgeInt_E_E2);
        internalTopoEdges.add(edgeInt_E_E3);
        internalTopoEdges.add(edgeInt_E_E4);

        ethernetTopoEdges.add(edgeNet_A2_E1);
        ethernetTopoEdges.add(edgeNet_B2_E2);
        ethernetTopoEdges.add(edgeNet_C2_E3);
        ethernetTopoEdges.add(edgeNet_D2_E4);
        ethernetTopoEdges.add(edgeNet_E1_A2);
        ethernetTopoEdges.add(edgeNet_E2_B2);
        ethernetTopoEdges.add(edgeNet_E3_C2);
        ethernetTopoEdges.add(edgeNet_E4_D2);
    }

    private void buildComplexTopo() {
        //Devices
        TopoVertex nodeA = new TopoVertex("nodeA", VertexType.SWITCH);
        TopoVertex nodeB = new TopoVertex("nodeB", VertexType.SWITCH);
        TopoVertex nodeC = new TopoVertex("nodeC", VertexType.SWITCH);
        TopoVertex nodeD = new TopoVertex("nodeD", VertexType.SWITCH);
        TopoVertex nodeE = new TopoVertex("nodeE", VertexType.ROUTER);
        TopoVertex nodeF = new TopoVertex("nodeF", VertexType.ROUTER);
        TopoVertex nodeG = new TopoVertex("nodeG", VertexType.ROUTER);
        TopoVertex nodeH = new TopoVertex("nodeH", VertexType.ROUTER);
        TopoVertex nodeI = new TopoVertex("nodeI", VertexType.ROUTER);
        TopoVertex nodeJ = new TopoVertex("nodeJ", VertexType.ROUTER);


        //Switch-Ports
        TopoVertex portA1 = new TopoVertex("nodeA:1", VertexType.PORT);
        TopoVertex portA2 = new TopoVertex("nodeA:2", VertexType.PORT);
        TopoVertex portA3 = new TopoVertex("nodeA:3", VertexType.PORT);
        TopoVertex portB1 = new TopoVertex("nodeB:1", VertexType.PORT);
        TopoVertex portB2 = new TopoVertex("nodeB:2", VertexType.PORT);
        TopoVertex portB3 = new TopoVertex("nodeB:3", VertexType.PORT);
        TopoVertex portB4 = new TopoVertex("nodeB:4", VertexType.PORT);
        TopoVertex portC1 = new TopoVertex("nodeC:1", VertexType.PORT);
        TopoVertex portC2 = new TopoVertex("nodeC:2", VertexType.PORT);
        TopoVertex portD1 = new TopoVertex("nodeD:1", VertexType.PORT);
        TopoVertex portD2 = new TopoVertex("nodeD:2", VertexType.PORT);
        TopoVertex portD3 = new TopoVertex("nodeD:3", VertexType.PORT);

        //Router-Ports
        TopoVertex portE1 = new TopoVertex("nodeE:1", VertexType.PORT);
        TopoVertex portE2 = new TopoVertex("nodeE:2", VertexType.PORT);
        TopoVertex portF1 = new TopoVertex("nodeF:1", VertexType.PORT);
        TopoVertex portF2 = new TopoVertex("nodeF:2", VertexType.PORT);
        TopoVertex portF3 = new TopoVertex("nodeF:3", VertexType.PORT);
        TopoVertex portF4 = new TopoVertex("nodeF:4", VertexType.PORT);
        TopoVertex portG1 = new TopoVertex("nodeG:1", VertexType.PORT);
        TopoVertex portG2 = new TopoVertex("nodeG:2", VertexType.PORT);
        TopoVertex portH1 = new TopoVertex("nodeH:1", VertexType.PORT);
        TopoVertex portH2 = new TopoVertex("nodeH:2", VertexType.PORT);
        TopoVertex portI1 = new TopoVertex("nodeI:1", VertexType.PORT);
        TopoVertex portI2 = new TopoVertex("nodeI:2", VertexType.PORT);
        TopoVertex portJ1 = new TopoVertex("nodeJ:1", VertexType.PORT);
        TopoVertex portJ2 = new TopoVertex("nodeJ:2", VertexType.PORT);
        TopoVertex portJ3 = new TopoVertex("nodeJ:3", VertexType.PORT);

        // Ethernet Internal Directed edges
        TopoEdge edgeInt_A1_A = new TopoEdge(portA1, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A2_A = new TopoEdge(portA2, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A3_A = new TopoEdge(portA3, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B1_B = new TopoEdge(portB1, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B2_B = new TopoEdge(portB2, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B3_B = new TopoEdge(portB3, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B4_B = new TopoEdge(portB4, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C1_C = new TopoEdge(portC1, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C2_C = new TopoEdge(portC2, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D1_D = new TopoEdge(portD1, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D2_D = new TopoEdge(portD2, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D3_D = new TopoEdge(portD3, nodeD, 0L, Layer.INTERNAL);

        // Ethernet Internal Reverse edges
        TopoEdge edgeInt_A_A1 = new TopoEdge(nodeA, portA1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_A2 = new TopoEdge(nodeA, portA2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_A3 = new TopoEdge(nodeA, portA3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B1 = new TopoEdge(nodeB, portB1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B2 = new TopoEdge(nodeB, portB2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B3 = new TopoEdge(nodeB, portB3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B4 = new TopoEdge(nodeB, portB4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C1 = new TopoEdge(nodeC, portC1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C2 = new TopoEdge(nodeC, portC2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D1 = new TopoEdge(nodeD, portD1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D2 = new TopoEdge(nodeD, portD2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D3 = new TopoEdge(nodeD, portD3, 0L, Layer.INTERNAL);

        // MPLS Internal Directed edges
        TopoEdge edgeInt_E1_E = new TopoEdge(portE1, nodeE, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E2_E = new TopoEdge(portE2, nodeE, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F1_F = new TopoEdge(portF1, nodeF, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F2_F = new TopoEdge(portF2, nodeF, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F3_F = new TopoEdge(portF3, nodeF, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F4_F = new TopoEdge(portF4, nodeF, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G1_G = new TopoEdge(portG1, nodeG, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G2_G = new TopoEdge(portG2, nodeG, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_H1_H = new TopoEdge(portH1, nodeH, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_H2_H = new TopoEdge(portH2, nodeH, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_I1_I = new TopoEdge(portI1, nodeI, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_I2_I = new TopoEdge(portI2, nodeI, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_J1_J = new TopoEdge(portJ1, nodeJ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_J2_J = new TopoEdge(portJ2, nodeJ, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_J3_J = new TopoEdge(portJ3, nodeJ, 0L, Layer.INTERNAL);

        // MPLS Internal Reverse edges
        TopoEdge edgeInt_E_E1 = new TopoEdge(nodeE, portE1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E2 = new TopoEdge(nodeE, portE2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F_F1 = new TopoEdge(nodeF, portF1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F_F2 = new TopoEdge(nodeF, portF2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F_F3 = new TopoEdge(nodeF, portF3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F_F4 = new TopoEdge(nodeF, portF4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G_G1 = new TopoEdge(nodeG, portG1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G_G2 = new TopoEdge(nodeG, portG2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_H_H1 = new TopoEdge(nodeH, portH1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_H_H2 = new TopoEdge(nodeH, portH2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_I_I1 = new TopoEdge(nodeI, portI1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_I_I2 = new TopoEdge(nodeI, portI2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_J_J1 = new TopoEdge(nodeJ, portJ1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_J_J2 = new TopoEdge(nodeJ, portJ2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_J_J3 = new TopoEdge(nodeJ, portJ3, 0L, Layer.INTERNAL);

        // Ethernet Directed edges
        TopoEdge edgeNet_A2_B1 = new TopoEdge(portA2, portB1, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_B1_A2 = new TopoEdge(portB1, portA2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_C2_D1 = new TopoEdge(portC2, portD1, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_D1_C2 = new TopoEdge(portD1, portC2, 100L, Layer.ETHERNET);

        // MPLS Directed edges
        TopoEdge edgeNet_E2_F2 = new TopoEdge(portE2, portF2, 100L, Layer.MPLS);
        TopoEdge edgeNet_F2_E2 = new TopoEdge(portF2, portE2, 100L, Layer.MPLS);
        TopoEdge edgeNet_F3_G1 = new TopoEdge(portF3, portG1, 100L, Layer.MPLS);
        TopoEdge edgeNet_G1_F3 = new TopoEdge(portG1, portF3, 100L, Layer.MPLS);
        TopoEdge edgeNet_F4_H1 = new TopoEdge(portF4, portH1, 100L, Layer.MPLS);
        TopoEdge edgeNet_H1_F4 = new TopoEdge(portH1, portF4, 100L, Layer.MPLS);
        TopoEdge edgeNet_I2_J1 = new TopoEdge(portI2, portJ1, 100L, Layer.MPLS);
        TopoEdge edgeNet_J1_I2 = new TopoEdge(portJ1, portI2, 100L, Layer.MPLS);

        // Ethernet-MPLS Directed edges
        TopoEdge edgeNet_A3_F1 = new TopoEdge(portA3, portF1, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_F1_A3 = new TopoEdge(portF1, portA3, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_B3_G2 = new TopoEdge(portB3, portG2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_G2_B3 = new TopoEdge(portG2, portB3, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_B4_I1 = new TopoEdge(portB4, portI1, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_I1_B4 = new TopoEdge(portI1, portB4, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_C1_H2 = new TopoEdge(portC1, portH2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_H2_C1 = new TopoEdge(portH2, portC1, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_D3_J2 = new TopoEdge(portD3, portJ2, 100L, Layer.ETHERNET);
        TopoEdge edgeNet_J2_D3 = new TopoEdge(portJ2, portD3, 100L, Layer.ETHERNET);


        ethernetTopoVertices.add(nodeA);
        ethernetTopoVertices.add(nodeB);
        ethernetTopoVertices.add(nodeC);
        ethernetTopoVertices.add(nodeD);
        ethernetTopoVertices.add(portA1);
        ethernetTopoVertices.add(portA2);
        ethernetTopoVertices.add(portA3);
        ethernetTopoVertices.add(portB1);
        ethernetTopoVertices.add(portB2);
        ethernetTopoVertices.add(portB3);
        ethernetTopoVertices.add(portB4);
        ethernetTopoVertices.add(portC1);
        ethernetTopoVertices.add(portC2);
        ethernetTopoVertices.add(portD1);
        ethernetTopoVertices.add(portD2);
        ethernetTopoVertices.add(portD3);

        mplsTopoVertices.add(nodeE);
        mplsTopoVertices.add(nodeF);
        mplsTopoVertices.add(nodeG);
        mplsTopoVertices.add(nodeH);
        mplsTopoVertices.add(nodeI);
        mplsTopoVertices.add(nodeJ);
        mplsTopoVertices.add(portE1);
        mplsTopoVertices.add(portE2);
        mplsTopoVertices.add(portF1);
        mplsTopoVertices.add(portF2);
        mplsTopoVertices.add(portF3);
        mplsTopoVertices.add(portF4);
        mplsTopoVertices.add(portG1);
        mplsTopoVertices.add(portG2);
        mplsTopoVertices.add(portH1);
        mplsTopoVertices.add(portH2);
        mplsTopoVertices.add(portI1);
        mplsTopoVertices.add(portI2);
        mplsTopoVertices.add(portJ1);
        mplsTopoVertices.add(portJ2);
        mplsTopoVertices.add(portJ3);

        internalTopoEdges.add(edgeInt_A1_A);
        internalTopoEdges.add(edgeInt_A2_A);
        internalTopoEdges.add(edgeInt_A3_A);
        internalTopoEdges.add(edgeInt_B1_B);
        internalTopoEdges.add(edgeInt_B2_B);
        internalTopoEdges.add(edgeInt_B3_B);
        internalTopoEdges.add(edgeInt_B4_B);
        internalTopoEdges.add(edgeInt_C1_C);
        internalTopoEdges.add(edgeInt_C2_C);
        internalTopoEdges.add(edgeInt_D1_D);
        internalTopoEdges.add(edgeInt_D2_D);
        internalTopoEdges.add(edgeInt_D3_D);
        internalTopoEdges.add(edgeInt_E1_E);
        internalTopoEdges.add(edgeInt_E2_E);
        internalTopoEdges.add(edgeInt_F1_F);
        internalTopoEdges.add(edgeInt_F2_F);
        internalTopoEdges.add(edgeInt_F3_F);
        internalTopoEdges.add(edgeInt_F4_F);
        internalTopoEdges.add(edgeInt_G1_G);
        internalTopoEdges.add(edgeInt_G2_G);
        internalTopoEdges.add(edgeInt_H1_H);
        internalTopoEdges.add(edgeInt_H2_H);
        internalTopoEdges.add(edgeInt_I1_I);
        internalTopoEdges.add(edgeInt_I2_I);
        internalTopoEdges.add(edgeInt_J1_J);
        internalTopoEdges.add(edgeInt_J2_J);
        internalTopoEdges.add(edgeInt_J3_J);
        internalTopoEdges.add(edgeInt_A_A1);
        internalTopoEdges.add(edgeInt_A_A2);
        internalTopoEdges.add(edgeInt_A_A3);
        internalTopoEdges.add(edgeInt_B_B1);
        internalTopoEdges.add(edgeInt_B_B2);
        internalTopoEdges.add(edgeInt_B_B3);
        internalTopoEdges.add(edgeInt_B_B4);
        internalTopoEdges.add(edgeInt_C_C1);
        internalTopoEdges.add(edgeInt_C_C2);
        internalTopoEdges.add(edgeInt_D_D1);
        internalTopoEdges.add(edgeInt_D_D2);
        internalTopoEdges.add(edgeInt_D_D3);
        internalTopoEdges.add(edgeInt_E_E1);
        internalTopoEdges.add(edgeInt_E_E2);
        internalTopoEdges.add(edgeInt_F_F1);
        internalTopoEdges.add(edgeInt_F_F2);
        internalTopoEdges.add(edgeInt_F_F3);
        internalTopoEdges.add(edgeInt_F_F4);
        internalTopoEdges.add(edgeInt_G_G1);
        internalTopoEdges.add(edgeInt_G_G2);
        internalTopoEdges.add(edgeInt_H_H1);
        internalTopoEdges.add(edgeInt_H_H2);
        internalTopoEdges.add(edgeInt_I_I1);
        internalTopoEdges.add(edgeInt_I_I2);
        internalTopoEdges.add(edgeInt_J_J1);
        internalTopoEdges.add(edgeInt_J_J2);
        internalTopoEdges.add(edgeInt_J_J3);

        ethernetTopoEdges.add(edgeNet_A2_B1);
        ethernetTopoEdges.add(edgeNet_A3_F1);
        ethernetTopoEdges.add(edgeNet_B1_A2);
        ethernetTopoEdges.add(edgeNet_B3_G2);
        ethernetTopoEdges.add(edgeNet_B4_I1);
        ethernetTopoEdges.add(edgeNet_C1_H2);
        ethernetTopoEdges.add(edgeNet_C2_D1);
        ethernetTopoEdges.add(edgeNet_D1_C2);
        ethernetTopoEdges.add(edgeNet_D3_J2);
        ethernetTopoEdges.add(edgeNet_F1_A3);
        ethernetTopoEdges.add(edgeNet_G2_B3);
        ethernetTopoEdges.add(edgeNet_H2_C1);
        ethernetTopoEdges.add(edgeNet_I1_B4);
        ethernetTopoEdges.add(edgeNet_J2_D3);

        mplsTopoEdges.add(edgeNet_E2_F2);
        mplsTopoEdges.add(edgeNet_F2_E2);
        mplsTopoEdges.add(edgeNet_F3_G1);
        mplsTopoEdges.add(edgeNet_F4_H1);
        mplsTopoEdges.add(edgeNet_G1_F3);
        mplsTopoEdges.add(edgeNet_H1_F4);
        mplsTopoEdges.add(edgeNet_I2_J1);
        mplsTopoEdges.add(edgeNet_J1_I2);
    }
}