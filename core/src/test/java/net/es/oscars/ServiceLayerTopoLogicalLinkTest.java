package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.resv.ent.RequestedVlanFixtureE;
import net.es.oscars.resv.ent.RequestedVlanJunctionE;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.servicetopo.LogicalEdge;
import net.es.oscars.servicetopo.SLTopoUnitTestConfiguration;
import net.es.oscars.servicetopo.ServiceLayerTopology;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.enums.VertexType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jeremy on 6/15/16.
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
public class ServiceLayerTopoLogicalLinkTest
{
    @Autowired
    private ServiceLayerTopology serviceLayerTopo;

    private Set<TopoVertex> ethernetTopoVertices = new HashSet<>();
    private Set<TopoVertex> mplsTopoVertices = new HashSet<>();
    private Set<TopoVertex> internalTopoVertices = new HashSet<>();

    private Set<TopoEdge> ethernetTopoEdges = new HashSet<>();
    private Set<TopoEdge> mplsTopoEdges = new HashSet<>();
    private Set<TopoEdge> internalTopoEdges = new HashSet<>();

    private RequestedVlanPipeE requestedPipe = new RequestedVlanPipeE();

    @Test
    public void verifyLogicalLinkWeightsLinear()
    {
        buildLinearTopo();
        constructLayeredTopology();
        buildLinearRequestPipe();

        Set<LogicalEdge> logicalLinks = serviceLayerTopo.getLogicalLinks();
        Set<LogicalEdge> llBackup = serviceLayerTopo.getLlBackup();

        assert(logicalLinks.size() == 2);
        assert(llBackup.size() == 2);

        serviceLayerTopo.getLogicalLinks().stream()
                .forEach(ll -> {
                    String aURN = ll.getA().getUrn();
                    String zURN = ll.getZ().getUrn();
                    if(aURN.equals("switchA:2"))
                        assert(zURN.equals("switchE:1"));
                    else if(aURN.equals("switchE:1"))
                        assert(zURN.equals("switchA:2"));
                    else
                        assert(false);
                });

        TopoVertex srcDevice = null, dstDevice = null, srcPort = null, dstPort = null;

        for(TopoVertex v : ethernetTopoVertices)
        {
            if(v.getUrn().equals("switchA"))
                srcDevice = v;
            else if(v.getUrn().equals("switchE"))
                dstDevice = v;
            else if(v.getUrn().equals("switchA:1"))
                srcPort = v;
            else if(v.getUrn().equals("switchE:2"))
                dstPort = v;
        }

        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);
        serviceLayerTopo.calculateLogicalLinkWeights(requestedPipe);

        log.info("Beginning test: 'verifyLogicalLinkWeightsLinear'.");

        logicalLinks = serviceLayerTopo.getLogicalLinks();
        llBackup = serviceLayerTopo.getLlBackup();

        assert(serviceLayerTopo.getNonAdjacentPorts().size() == 2);
        assert(logicalLinks.size() == 2);   // No change after assigning weights
        assert(llBackup.size() == 2);

        TopoVertex portA2 = null;
        TopoVertex portE1 = null;

        for(TopoVertex vert : ethernetTopoVertices)
        {
            if(vert.getUrn().equals("switchA:2"))
                portA2 = vert;
            else if(vert.getUrn().equals("switchE:1"))
                portE1 = vert;
        }

        for(LogicalEdge ll : logicalLinks)
        {
            List<TopoEdge> physicalEdges = ll.getCorrespondingTopoEdges();

            assert(ll.getA().equals(portA2) || ll.getZ().equals(portA2));
            assert(ll.getA().equals(portE1) || ll.getZ().equals(portE1));
            assert(ll.getMetric() == 400);
            assert(physicalEdges.size() == 10);

            String physicalURNs = "";
            String correctURNs = "";

            if(ll.getA().equals(portA2))
            {
                assert(physicalEdges.get(0).getA().equals(portA2));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portE1));

                correctURNs = "switchA:2]-->[routerB:1-routerB:1]-->[routerB-routerB]-->[routerB:2-routerB:2]-->[routerC:1-routerC:1]-->[routerC-routerC]-->[routerC:2-routerC:2]-->[routerD:1-routerD:1]-->[routerD-routerD]-->[routerD:2-routerD:2]-->[switchE:1-";
            }
            else
            {
                assert(physicalEdges.get(0).getA().equals(portE1));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portA2));

                correctURNs = "switchE:1]-->[routerD:2-routerD:2]-->[routerD-routerD]-->[routerD:1-routerD:1]-->[routerC:2-routerC:2]-->[routerC-routerC]-->[routerC:1-routerC:1]-->[routerB:2-routerB:2]-->[routerB-routerB]-->[routerB:1-routerB:1]-->[switchA:2-";
            }

            for(TopoEdge physEdge : physicalEdges)
            {
                physicalURNs = physicalURNs + physEdge.getA().getUrn();
                physicalURNs = physicalURNs + "]-->[";
                physicalURNs = physicalURNs + physEdge.getZ().getUrn();
                physicalURNs = physicalURNs + "-";
            }

            assert(physicalURNs.equals(correctURNs));
        }

        log.info("test 'verifyLogicalLinkWeightsLinear' passed.");
    }

    @Test
    public void verifyLogicalLinkWeightsTwoPath()
    {
        buildTwoMPlsPathTopo();
        constructLayeredTopology();
        buildLinearRequestPipe();

        Set<LogicalEdge> logicalLinks = serviceLayerTopo.getLogicalLinks();
        Set<LogicalEdge> llBackup = serviceLayerTopo.getLlBackup();

        assert(logicalLinks.size() == 12);
        assert(llBackup.size() == 12);

        serviceLayerTopo.getLogicalLinks().stream()
                .forEach(ll -> {
                    String aURN = ll.getA().getUrn();
                    String zURN = ll.getZ().getUrn();
                    if(aURN.equals("switchA:2"))
                        assert(zURN.equals("switchE:1") || zURN.equals("switchA:3") || zURN.equals("switchE:3"));
                    else if(aURN.equals("switchE:1"))
                        assert(zURN.equals("switchA:2") || zURN.equals("switchA:3") || zURN.equals("switchE:3"));
                    else if(aURN.equals("switchA:3"))
                        assert(zURN.equals("switchA:2") || zURN.equals("switchE:1") || zURN.equals("switchE:3"));
                    else if(aURN.equals("switchE:3"))
                        assert(zURN.equals("switchA:2") || zURN.equals("switchA:3") || zURN.equals("switchE:1"));
                    else
                        assert(false);
                });

        TopoVertex srcDevice = null, dstDevice = null, srcPort = null, dstPort = null;

        for(TopoVertex v : ethernetTopoVertices)
        {
            if(v.getUrn().equals("switchA"))
                srcDevice = v;
            else if(v.getUrn().equals("switchE"))
                dstDevice = v;
            else if(v.getUrn().equals("switchA:1"))
                srcPort = v;
            else if(v.getUrn().equals("switchE:2"))
                dstPort = v;
        }

        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);
        serviceLayerTopo.calculateLogicalLinkWeights(requestedPipe);

        log.info("Beginning test: 'verifyLogicalLinkWeightsTwoPath'.");

        logicalLinks = serviceLayerTopo.getLogicalLinks();
        llBackup = serviceLayerTopo.getLlBackup();

        assert(serviceLayerTopo.getNonAdjacentPorts().size() == 4);
        assert(logicalLinks.size() == 4);   // No logical link between (switchA:2 <--> switchA:3) or (switchE:1 <--> switchE:3) or (switchA:2 <--> switchE:3) or (switchA:3 <--> switchE:1)
        assert(llBackup.size() == 12);

        TopoVertex portA2 = null;
        TopoVertex portA3 = null;
        TopoVertex portE1 = null;
        TopoVertex portE3 = null;

        for(TopoVertex vert : ethernetTopoVertices)
        {
            if(vert.getUrn().equals("switchA:2"))
                portA2 = vert;
            else if(vert.getUrn().equals("switchA:3"))
                portA3 = vert;
            else if(vert.getUrn().equals("switchE:1"))
                portE1 = vert;
            else if(vert.getUrn().equals("switchE:3"))
                portE3 = vert;
        }

        for(LogicalEdge ll : logicalLinks)
        {
            List<TopoEdge> physicalEdges = ll.getCorrespondingTopoEdges();

            assert(ll.getA().equals(portA2) || ll.getA().equals(portA3) || ll.getA().equals(portE1) || ll.getA().equals(portE3));
            assert(ll.getZ().equals(portA2) || ll.getZ().equals(portA3) || ll.getZ().equals(portE1) || ll.getZ().equals(portE3));

            if(ll.getA().equals(portA2) || ll.getA().equals(portE1))
            {
                assert (ll.getZ().equals(portE1) || ll.getZ().equals(portA2));
                assert(ll.getMetric() == 400);
                assert(physicalEdges.size() == 10);
            }
            else if(ll.getA().equals(portA3) || ll.getA().equals(portE3))
            {
                assert (ll.getZ().equals(portE3) || ll.getZ().equals(portA3));
                assert(ll.getMetric() == 300);
                assert(physicalEdges.size() == 7);
            }
            else
            {
                assert(false);
            }

            String physicalURNs = "";
            String correctURNs = "";

            if(ll.getA().equals(portA2))
            {
                assert(physicalEdges.get(0).getA().equals(portA2));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portE1));

                correctURNs = "switchA:2]-->[routerB:1-routerB:1]-->[routerB-routerB]-->[routerB:2-routerB:2]-->[routerC:1-routerC:1]-->[routerC-routerC]-->[routerC:2-routerC:2]-->[routerD:1-routerD:1]-->[routerD-routerD]-->[routerD:2-routerD:2]-->[switchE:1-";
            }
            else if(ll.getA().equals(portA3))
            {
                assert(physicalEdges.get(0).getA().equals(portA3));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portE3));

                correctURNs = "switchA:3]-->[routerF:1-routerF:1]-->[routerF-routerF]-->[routerF:2-routerF:2]-->[routerG:1-routerG:1]-->[routerG-routerG]-->[routerG:2-routerG:2]-->[switchE:3-";
            }
            else if(ll.getA().equals(portE1))
            {
                assert(physicalEdges.get(0).getA().equals(portE1));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portA2));

                correctURNs = "switchE:1]-->[routerD:2-routerD:2]-->[routerD-routerD]-->[routerD:1-routerD:1]-->[routerC:2-routerC:2]-->[routerC-routerC]-->[routerC:1-routerC:1]-->[routerB:2-routerB:2]-->[routerB-routerB]-->[routerB:1-routerB:1]-->[switchA:2-";
            }
            else if(ll.getA().equals(portE3))
            {
                assert(physicalEdges.get(0).getA().equals(portE3));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portA3));

                correctURNs = "switchE:3]-->[routerG:2-routerG:2]-->[routerG-routerG]-->[routerG:1-routerG:1]-->[routerF:2-routerF:2]-->[routerF-routerF]-->[routerF:1-routerF:1]-->[switchA:3-";
            }

            for(TopoEdge physEdge : physicalEdges)
            {
                physicalURNs = physicalURNs + physEdge.getA().getUrn();
                physicalURNs = physicalURNs + "]-->[";
                physicalURNs = physicalURNs + physEdge.getZ().getUrn();
                physicalURNs = physicalURNs + "-";
            }

            assert(physicalURNs.equals(correctURNs));
        }

        log.info("test 'verifyLogicalLinkWeightsTwoPath' passed.");
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

        serviceLayerTopo.setTopology(dummyEthernetTopo);
        serviceLayerTopo.setTopology(dummyInternalTopo);
        serviceLayerTopo.setTopology(dummyMPLSTopo);

        serviceLayerTopo.createMultilayerTopology();
    }

    private void buildLinearTopo()
    {
        //Devices
        TopoVertex nodeA = new TopoVertex("switchA", VertexType.SWITCH);
        TopoVertex nodeB = new TopoVertex("routerB", VertexType.ROUTER);
        TopoVertex nodeC = new TopoVertex("routerC", VertexType.ROUTER);
        TopoVertex nodeD = new TopoVertex("routerD", VertexType.ROUTER);
        TopoVertex nodeE = new TopoVertex("switchE", VertexType.SWITCH);

        //Ports
        TopoVertex portA1 = new TopoVertex("switchA:1", VertexType.PORT);
        TopoVertex portA2 = new TopoVertex("switchA:2", VertexType.PORT);
        TopoVertex portB1 = new TopoVertex("routerB:1", VertexType.PORT);
        TopoVertex portB2 = new TopoVertex("routerB:2", VertexType.PORT);
        TopoVertex portC1 = new TopoVertex("routerC:1", VertexType.PORT);
        TopoVertex portC2 = new TopoVertex("routerC:2", VertexType.PORT);
        TopoVertex portD1 = new TopoVertex("routerD:1", VertexType.PORT);
        TopoVertex portD2 = new TopoVertex("routerD:2", VertexType.PORT);
        TopoVertex portE1 = new TopoVertex("switchE:1", VertexType.PORT);
        TopoVertex portE2 = new TopoVertex("switchE:2", VertexType.PORT);

        //Internal Links
        TopoEdge edgeInt_A1_A = new TopoEdge(portA1, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A2_A = new TopoEdge(portA2, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B1_B = new TopoEdge(portB1, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B2_B = new TopoEdge(portB2, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C1_C = new TopoEdge(portC1, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C2_C = new TopoEdge(portC2, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D1_D = new TopoEdge(portD1, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D2_D = new TopoEdge(portD2, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E1_E = new TopoEdge(portE1, nodeE, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E2_E = new TopoEdge(portE2, nodeE, 0L, Layer.INTERNAL);

        //Internal Reverse Links
        TopoEdge edgeInt_A_A1 = new TopoEdge(nodeA, portA1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_A2 = new TopoEdge(nodeA, portA2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B1 = new TopoEdge(nodeB, portB1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B2 = new TopoEdge(nodeB, portB2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C1 = new TopoEdge(nodeC, portC1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C2 = new TopoEdge(nodeC, portC2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D1 = new TopoEdge(nodeD, portD1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D2 = new TopoEdge(nodeD, portD2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E1 = new TopoEdge(nodeE, portE1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E2 = new TopoEdge(nodeE, portE2, 0L, Layer.INTERNAL);

        //Network Links
        TopoEdge edgeEth_A2_B1 = new TopoEdge(portA2, portB1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_B2_C1 = new TopoEdge(portB2, portC1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_C2_D1 = new TopoEdge(portC2, portD1, 100L, Layer.MPLS);
        TopoEdge edgeEth_D2_E1 = new TopoEdge(portD2, portE1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_B1_A2 = new TopoEdge(portB1, portA2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_C1_B2 = new TopoEdge(portC1, portB2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_D1_C2 = new TopoEdge(portD1, portC2, 100L, Layer.MPLS);
        TopoEdge edgeEth_E1_D2 = new TopoEdge(portE1, portD2, 100L, Layer.ETHERNET);

        ethernetTopoVertices.add(nodeA);
        ethernetTopoVertices.add(nodeE);
        ethernetTopoVertices.add(portA1);
        ethernetTopoVertices.add(portA2);
        ethernetTopoVertices.add(portE1);
        ethernetTopoVertices.add(portE2);

        mplsTopoVertices.add(nodeB);
        mplsTopoVertices.add(nodeC);
        mplsTopoVertices.add(nodeD);
        mplsTopoVertices.add(portB1);
        mplsTopoVertices.add(portB2);
        mplsTopoVertices.add(portC1);
        mplsTopoVertices.add(portC2);
        mplsTopoVertices.add(portD1);
        mplsTopoVertices.add(portD2);

        internalTopoEdges.add(edgeInt_A1_A);
        internalTopoEdges.add(edgeInt_A2_A);
        internalTopoEdges.add(edgeInt_B1_B);
        internalTopoEdges.add(edgeInt_B2_B);
        internalTopoEdges.add(edgeInt_C1_C);
        internalTopoEdges.add(edgeInt_C2_C);
        internalTopoEdges.add(edgeInt_D1_D);
        internalTopoEdges.add(edgeInt_D2_D);
        internalTopoEdges.add(edgeInt_E1_E);
        internalTopoEdges.add(edgeInt_E2_E);
        internalTopoEdges.add(edgeInt_A_A1);
        internalTopoEdges.add(edgeInt_A_A2);
        internalTopoEdges.add(edgeInt_B_B1);
        internalTopoEdges.add(edgeInt_B_B2);
        internalTopoEdges.add(edgeInt_C_C1);
        internalTopoEdges.add(edgeInt_C_C2);
        internalTopoEdges.add(edgeInt_D_D1);
        internalTopoEdges.add(edgeInt_D_D2);
        internalTopoEdges.add(edgeInt_E_E1);
        internalTopoEdges.add(edgeInt_E_E2);

        ethernetTopoEdges.add(edgeEth_A2_B1);
        ethernetTopoEdges.add(edgeEth_B1_A2);
        ethernetTopoEdges.add(edgeEth_D2_E1);
        ethernetTopoEdges.add(edgeEth_E1_D2);

        mplsTopoEdges.add(edgeMpls_B2_C1);
        mplsTopoEdges.add(edgeMpls_C1_B2);
        mplsTopoEdges.add(edgeMpls_C2_D1);
        mplsTopoEdges.add(edgeMpls_D1_C2);
    }

    private void buildTwoMPlsPathTopo()
    {
        buildLinearTopo();

        TopoVertex nodeA = null;
        TopoVertex nodeE = null;

        for(TopoVertex n : ethernetTopoVertices)
        {
            if(n.getUrn().equals("switchA"))
                nodeA = n;
            else if(n.getUrn().equals("switchE"))
                nodeE = n;
        }


        //Additional Devices
        TopoVertex nodeF = new TopoVertex("routerF", VertexType.ROUTER);
        TopoVertex nodeG = new TopoVertex("routerG", VertexType.ROUTER);

        //Additional Ports
        TopoVertex portA3 = new TopoVertex("switchA:3", VertexType.PORT);
        TopoVertex portE3 = new TopoVertex("switchE:3", VertexType.PORT);
        TopoVertex portF1 = new TopoVertex("routerF:1", VertexType.PORT);
        TopoVertex portF2 = new TopoVertex("routerF:2", VertexType.PORT);
        TopoVertex portG1 = new TopoVertex("routerG:1", VertexType.PORT);
        TopoVertex portG2 = new TopoVertex("routerG:2", VertexType.PORT);

        //Additional Internal Links
        TopoEdge edgeInt_A3_A = new TopoEdge(portA3, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E3_E = new TopoEdge(portE3, nodeE, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F1_F = new TopoEdge(portF1, nodeF, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F2_F = new TopoEdge(portF2, nodeF, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G1_G = new TopoEdge(portG1, nodeG, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G2_G = new TopoEdge(portG2, nodeG, 0L, Layer.INTERNAL);

        //Additional Internal Reverse Links
        TopoEdge edgeInt_A_A3 = new TopoEdge(nodeA, portA3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_E_E3 = new TopoEdge(nodeE, portE3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F_F1 = new TopoEdge(nodeF, portF1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F_F2 = new TopoEdge(nodeF, portF2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G_G1 = new TopoEdge(nodeG, portG1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G_G2 = new TopoEdge(nodeG, portG2, 0L, Layer.INTERNAL);

        //Additional Network Links
        TopoEdge edgeEth_A3_F1 = new TopoEdge(portA3, portF1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_E3_G2 = new TopoEdge(portE3, portG2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_F1_A3 = new TopoEdge(portF1, portA3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_F2_G1 = new TopoEdge(portF2, portG1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_G1_F2 = new TopoEdge(portG1, portF2, 100L, Layer.MPLS);
        TopoEdge edgeEth_G2_E3 = new TopoEdge(portG2, portE3, 100L, Layer.ETHERNET);

        ethernetTopoVertices.add(portA3);
        ethernetTopoVertices.add(portE3);

        mplsTopoVertices.add(nodeF);
        mplsTopoVertices.add(nodeG);
        mplsTopoVertices.add(portF1);
        mplsTopoVertices.add(portF2);
        mplsTopoVertices.add(portG1);
        mplsTopoVertices.add(portG2);

        internalTopoEdges.add(edgeInt_A3_A);
        internalTopoEdges.add(edgeInt_E3_E);
        internalTopoEdges.add(edgeInt_F1_F);
        internalTopoEdges.add(edgeInt_F2_F);
        internalTopoEdges.add(edgeInt_G1_G);
        internalTopoEdges.add(edgeInt_G2_G);
        internalTopoEdges.add(edgeInt_A_A3);
        internalTopoEdges.add(edgeInt_E_E3);
        internalTopoEdges.add(edgeInt_F_F1);
        internalTopoEdges.add(edgeInt_F_F2);
        internalTopoEdges.add(edgeInt_G_G1);
        internalTopoEdges.add(edgeInt_G_G2);

        ethernetTopoEdges.add(edgeEth_A3_F1);
        ethernetTopoEdges.add(edgeEth_E3_G2);
        ethernetTopoEdges.add(edgeEth_F1_A3);
        ethernetTopoEdges.add(edgeEth_G2_E3);

        mplsTopoEdges.add(edgeMpls_F2_G1);
        mplsTopoEdges.add(edgeMpls_G1_F2);
    }


    private void buildLinearRequestPipe()
    {
        RequestedVlanPipeE bwPipe = new RequestedVlanPipeE();
        RequestedVlanJunctionE aJunc = new RequestedVlanJunctionE();
        RequestedVlanJunctionE zJunc = new RequestedVlanJunctionE();
        RequestedVlanFixtureE aFix = new RequestedVlanFixtureE();
        RequestedVlanFixtureE zFix = new RequestedVlanFixtureE();
        UrnE aFixURN = new UrnE();
        UrnE zFixURN = new UrnE();
        UrnE aJuncURN = new UrnE();
        UrnE zJuncURN = new UrnE();

        aFixURN.setUrn("switchA:1");
        aFixURN.setUrnType(UrnType.IFCE);

        zFixURN.setUrn("switchE:1");
        zFixURN.setUrnType(UrnType.IFCE);

        aJuncURN.setUrn("switchA");
        aJuncURN.setUrnType(UrnType.DEVICE);

        zJuncURN.setUrn("switchE:1");
        zJuncURN.setUrnType(UrnType.DEVICE);


        aFix.setPortUrn(aFixURN);
        aFix.setVlanExpression("1234");
        aFix.setFixtureType(EthFixtureType.JUNOS_IFCE);
        aFix.setInMbps(100);
        aFix.setEgMbps(100);

        zFix.setPortUrn(zFixURN);
        zFix.setVlanExpression("1234");
        zFix.setFixtureType(EthFixtureType.JUNOS_IFCE);
        zFix.setInMbps(100);
        zFix.setEgMbps(100);

        Set<RequestedVlanFixtureE> aFixes = new HashSet<>();
        Set<RequestedVlanFixtureE> zFixes = new HashSet<>();

        aFixes.add(aFix);
        zFixes.add(zFix);

        aJunc.setDeviceUrn(aJuncURN);
        aJunc.setJunctionType(EthJunctionType.JUNOS_SWITCH);
        aJunc.setFixtures(aFixes);

        zJunc.setDeviceUrn(zJuncURN);
        zJunc.setJunctionType(EthJunctionType.JUNOS_SWITCH);
        zJunc.setFixtures(zFixes);


        bwPipe.setAzMbps(20);
        bwPipe.setZaMbps(20);
        bwPipe.setAJunction(aJunc);
        bwPipe.setZJunction(zJunc);
        bwPipe.setPipeType(EthPipeType.REQUESTED);

        requestedPipe = bwPipe;
    }
}
