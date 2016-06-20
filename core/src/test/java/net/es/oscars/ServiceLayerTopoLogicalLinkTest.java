package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.resv.ent.RequestedVlanFixtureE;
import net.es.oscars.resv.ent.RequestedVlanJunctionE;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.servicetopo.LogicalEdge;
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
import java.util.stream.Collectors;

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

    private Set<TopoVertex> ethernetTopoVertices;
    private Set<TopoVertex> mplsTopoVertices;
    private Set<TopoVertex> internalTopoVertices;

    private Set<TopoEdge> ethernetTopoEdges;
    private Set<TopoEdge> mplsTopoEdges;
    private Set<TopoEdge> internalTopoEdges;

    private RequestedVlanPipeE requestedPipe;

    @Test
    public void verifyLogicalLinksLinear()
    {
        buildLinearTopo();
        constructLayeredTopology();
        buildLinearRequestPipeEth2Eth();

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

        log.info("Beginning test: 'verifyLogicalLinksLinear'.");

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

        log.info("test 'verifyLogicalLinksLinear' passed.");
    }

    @Test
    public void verifyLogicalLinksMultipath()
    {
        buildLinearTopoWithMultipleMPLSBranch();
        constructLayeredTopology();
        buildLinearRequestPipeEth2Eth();

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

        log.info("Beginning test: 'verifyLogicalLinksMultipath'.");

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
            String correctURNsOption1 = "";
            String correctURNsOption2 = "";

            if(ll.getA().equals(portA2))
            {
                assert(physicalEdges.get(0).getA().equals(portA2));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portE1));

                correctURNsOption1 = "switchA:2]-->[routerB:1-routerB:1]-->[routerB-routerB]-->[routerB:2-routerB:2]-->[routerC:1-routerC:1]-->[routerC-routerC]-->[routerC:2-routerC:2]-->[routerD:1-routerD:1]-->[routerD-routerD]-->[routerD:2-routerD:2]-->[switchE:1-";
                correctURNsOption2 = "switchA:2]-->[routerB:1-routerB:1]-->[routerB-routerB]-->[routerB:4-routerB:4]-->[routerH:1-routerH:1]-->[routerH-routerH]-->[routerH:2-routerH:2]-->[routerD:4-routerD:4]-->[routerD-routerD]-->[routerD:2-routerD:2]-->[switchE:1-";
            }
            else
            {
                assert(physicalEdges.get(0).getA().equals(portE1));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portA2));

                correctURNsOption1 = "switchE:1]-->[routerD:2-routerD:2]-->[routerD-routerD]-->[routerD:1-routerD:1]-->[routerC:2-routerC:2]-->[routerC-routerC]-->[routerC:1-routerC:1]-->[routerB:2-routerB:2]-->[routerB-routerB]-->[routerB:1-routerB:1]-->[switchA:2-";
                correctURNsOption2 = "switchE:1]-->[routerD:2-routerD:2]-->[routerD-routerD]-->[routerD:4-routerD:4]-->[routerH:2-routerH:2]-->[routerH-routerH]-->[routerH:1-routerH:1]-->[routerB:4-routerB:4]-->[routerB-routerB]-->[routerB:1-routerB:1]-->[switchA:2-";
            }

            for(TopoEdge physEdge : physicalEdges)
            {
                physicalURNs = physicalURNs + physEdge.getA().getUrn();
                physicalURNs = physicalURNs + "]-->[";
                physicalURNs = physicalURNs + physEdge.getZ().getUrn();
                physicalURNs = physicalURNs + "-";
            }

            assert(physicalURNs.equals(correctURNsOption1) || physicalURNs.equals(correctURNsOption2));
        }

        log.info("test 'verifyLogicalLinksMultipath' passed.");
    }

    @Test
    public void verifyLogicalLinksLongerPath()
    {
        buildLinearTopoWithMultipleMPLSBranch();

        // Lower cost of longest path - This should be the path taken
        for(TopoEdge e : mplsTopoEdges)
        {
            String srcURN = e.getA().getUrn();
            String dstURN = e.getZ().getUrn();
            
            if(srcURN.equals("routerF:1") || srcURN.equals("routerF:2") || srcURN.equals("routerG:1") || srcURN.equals("routerG:2"))
                e.setMetric(10L);
            else if(dstURN.equals("routerF:1") || dstURN.equals("routerF:2") || dstURN.equals("routerG:1") || dstURN.equals("routerG:2"))
                e.setMetric(10L);
        }

        constructLayeredTopology();
        buildLinearRequestPipeEth2Eth();

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

        log.info("Beginning test: 'verifyLogicalLinksLongerPath'.");

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
            assert(ll.getMetric() == 240);
            assert(physicalEdges.size() == 16);

            String physicalURNs = "";
            String correctURNs = "";

            if(ll.getA().equals(portA2))
            {
                assert(physicalEdges.get(0).getA().equals(portA2));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portE1));

                correctURNs = "switchA:2]-->[routerB:1-routerB:1]-->[routerB-routerB]-->[routerB:3-routerB:3]-->[routerF:1-routerF:1]-->[routerF-routerF]-->[routerF:2-routerF:2]-->[routerC:3-routerC:3]-->[routerC-routerC]-->[routerC:4-routerC:4]-->[routerG:1-routerG:1]-->[routerG-routerG]-->[routerG:2-routerG:2]-->[routerD:3-routerD:3]-->[routerD-routerD]-->[routerD:2-routerD:2]-->[switchE:1-";
            }
            else
            {
                assert(physicalEdges.get(0).getA().equals(portE1));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portA2));

                correctURNs = "switchE:1]-->[routerD:2-routerD:2]-->[routerD-routerD]-->[routerD:3-routerD:3]-->[routerG:2-routerG:2]-->[routerG-routerG]-->[routerG:1-routerG:1]-->[routerC:4-routerC:4]-->[routerC-routerC]-->[routerC:3-routerC:3]-->[routerF:2-routerF:2]-->[routerF-routerF]-->[routerF:1-routerF:1]-->[routerB:3-routerB:3]-->[routerB-routerB]-->[routerB:1-routerB:1]-->[switchA:2-";
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

        log.info("test 'verifyLogicalLinksLongerPath' passed.");
    }


    @Test
    public void verifyLogicalLinksAsymmetric()
    {
        buildLinearTopoWithMultipleMPLSBranch();

        // Lower cost of longest path (in one direction) - This should be the path taken
        // Lower cost of another path in the other direction
        // Result: Assymmetric Logical Link Weights!
        for(TopoEdge e : mplsTopoEdges)
        {
            String srcURN = e.getA().getUrn();
            String dstURN = e.getZ().getUrn();

            if((srcURN.equals("routerB:3") || srcURN.equals("routerF:2") || srcURN.equals("routerC:4") || srcURN.equals("routerG:2")) && e.getZ().getVertexType().equals(VertexType.PORT))
                e.setMetric(10L);
            if((srcURN.equals("routerD:4") || srcURN.equals("routerH:1")) && e.getZ().getVertexType().equals(VertexType.PORT))
                e.setMetric(5L);
        }

        constructLayeredTopology();
        buildLinearRequestPipeEth2Eth();

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

        log.info("Beginning test: 'verifyLogicalLinksAsymmetric'.");

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

            String physicalURNs = "";
            String correctURNs = "";

            if(ll.getA().equals(portA2))
            {
                assert(physicalEdges.get(0).getA().equals(portA2));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portE1));
                assert(ll.getMetric() == 240);
                assert(physicalEdges.size() == 16);

                correctURNs = "switchA:2]-->[routerB:1-routerB:1]-->[routerB-routerB]-->[routerB:3-routerB:3]-->[routerF:1-routerF:1]-->[routerF-routerF]-->[routerF:2-routerF:2]-->[routerC:3-routerC:3]-->[routerC-routerC]-->[routerC:4-routerC:4]-->[routerG:1-routerG:1]-->[routerG-routerG]-->[routerG:2-routerG:2]-->[routerD:3-routerD:3]-->[routerD-routerD]-->[routerD:2-routerD:2]-->[switchE:1-";
            }
            else
            {
                assert(physicalEdges.get(0).getA().equals(portE1));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().equals(portA2));
                assert(ll.getMetric() == 210);
                assert(physicalEdges.size() == 10);

                correctURNs = "switchE:1]-->[routerD:2-routerD:2]-->[routerD-routerD]-->[routerD:4-routerD:4]-->[routerH:2-routerH:2]-->[routerH-routerH]-->[routerH:1-routerH:1]-->[routerB:4-routerB:4]-->[routerB-routerB]-->[routerB:1-routerB:1]-->[switchA:2-";
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

        log.info("test 'verifyLogicalLinksAsymmetric' passed.");
    }


    @Test
    public void verifyLogicalLinksDisjointMpls()
    {
        buildTwoMPlsPathTopo();
        constructLayeredTopology();
        buildLinearRequestPipeEth2Eth();

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

        log.info("Beginning test: 'verifyLogicalLinksDisjointMpls'.");

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

        log.info("test 'verifyLogicalLinksDisjointMpls' passed.");
    }


    /* Expected behavior: Add two VIRTUAL nodes and to VIRTUAL ports to Service-Layer topology to represent source and destination end-points. */
    @Test
    public void verifyLogicalLinksAllMPLS()
    {
        this.buildLinearMPLSTopo();

        constructLayeredTopology();
        buildLinearRequestPipeMpls2Mpls();

        Set<LogicalEdge> logicalLinks = serviceLayerTopo.getLogicalLinks();
        Set<LogicalEdge> llBackup = serviceLayerTopo.getLlBackup();

        log.info("Beginning test: 'verifyLogicalLinksAllMPLS'.");

        assert(logicalLinks.size() == 0);       // No Service-Layer nodes yet.
        assert(llBackup.size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 0);
        assert(serviceLayerTopo.getServiceLayerDevices().size() == 0);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 0);
        assert(serviceLayerTopo.getMplsLayerDevices().size() == 5);
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 10);
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 28);

        TopoVertex srcDevice = null, dstDevice = null, srcPort = null, dstPort = null;

        for(TopoVertex v : mplsTopoVertices)
        {
            if(v.getUrn().equals("routerA"))
                srcDevice = v;
            else if(v.getUrn().equals("routerE"))
                dstDevice = v;
            else if(v.getUrn().equals("routerA:1"))
                srcPort = v;
            else if(v.getUrn().equals("routerE:2"))
                dstPort = v;
        }

        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);     // should create VIRTUAL nodes
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);     // should create VIRTUAL nodes

        logicalLinks = serviceLayerTopo.getLogicalLinks();
        llBackup = serviceLayerTopo.getLlBackup();

        assert(logicalLinks.size() == 2);       // From VIRTUAL src -> dest and VIRTUAL dest -> src
        assert(llBackup.size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 8);
        assert(serviceLayerTopo.getServiceLayerDevices().size() == 2);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 2);
        assert(serviceLayerTopo.getMplsLayerDevices().size() == 5);     // No change
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 10);      // No change
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 28);      // No change

        serviceLayerTopo.getLogicalLinks().stream()
            .forEach(ll -> {
                String aURN = ll.getA().getUrn();
                String zURN = ll.getZ().getUrn();
                if(aURN.equals("routerA:1-virtual"))
                    assert(zURN.equals("routerE:2-virtual"));
                else if(aURN.equals("routerE:2-virtual"))
                    assert(zURN.equals("routerA:1-virtual"));
                else
                    assert(false);
            });

        serviceLayerTopo.calculateLogicalLinkWeights(requestedPipe);

        logicalLinks = serviceLayerTopo.getLogicalLinks();
        llBackup = serviceLayerTopo.getLlBackup();

        assert(serviceLayerTopo.getNonAdjacentPorts().size() == 2);
        assert(logicalLinks.size() == 2);   // No change after assigning weights
        assert(llBackup.size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 8);    // No change
        assert(serviceLayerTopo.getServiceLayerDevices().size() == 2);  // No change
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 2);    // No change
        assert(serviceLayerTopo.getMplsLayerDevices().size() == 5);     // Still no change
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 10);      // Still no change
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 28);      // Still no change

        for(LogicalEdge ll : logicalLinks)
        {
            List<TopoEdge> physicalEdges = ll.getCorrespondingTopoEdges();

            String physicalURNs = "";
            String correctURNs = "";

            if(ll.getA().getUrn().equals("routerA:1-virtual"))
            {
                assert(physicalEdges.get(0).getA().getUrn().equals("routerA:1-virtual"));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().getUrn().equals("routerE:2-virtual"));
                assert(ll.getMetric() == 400);
                assert(physicalEdges.size() == 16);

                correctURNs = "routerA:1-virtual]-->[routerA:1-routerA:1]-->[routerA-routerA]-->[routerA:2-routerA:2]-->[routerB:1-routerB:1]-->[routerB-routerB]-->[routerB:2-routerB:2]-->[routerC:1-routerC:1]-->[routerC-routerC]-->[routerC:2-routerC:2]-->[routerD:1-routerD:1]-->[routerD-routerD]-->[routerD:2-routerD:2]-->[routerE:1-routerE:1]-->[routerE-routerE]-->[routerE:2-routerE:2]-->[routerE:2-virtual-";
            }
            else
            {
                assert(physicalEdges.get(0).getA().getUrn().equals("routerE:2-virtual"));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().getUrn().equals("routerA:1-virtual"));
                assert(ll.getMetric() == 400);
                assert(physicalEdges.size() == 16);

                correctURNs = "routerE:2-virtual]-->[routerE:2-routerE:2]-->[routerE-routerE]-->[routerE:1-routerE:1]-->[routerD:2-routerD:2]-->[routerD-routerD]-->[routerD:1-routerD:1]-->[routerC:2-routerC:2]-->[routerC-routerC]-->[routerC:1-routerC:1]-->[routerB:2-routerB:2]-->[routerB-routerB]-->[routerB:1-routerB:1]-->[routerA:2-routerA:2]-->[routerA-routerA]-->[routerA:1-routerA:1]-->[routerA:1-virtual-";
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

        log.info("test 'verifyLogicalLinksAllMPLS' passed.");
    }


    /* Expected behavior: Add one VIRTUAL nodes and to VIRTUAL ports to Service-Layer topology to represent source end-points. */
    @Test
    public void verifyLogicalLinksSrcMPLS()
    {
        this.buildLinearMPLSTopo();

        Set<TopoVertex> keptInMPLSVert;
        Set<TopoEdge> keptInMPLSEdge;

        // Put nodeE, portE1, portE2 and the respective links on the ethernet layer
        for(TopoVertex v : mplsTopoVertices)
        {
            boolean relevantNode = false;

            if(v.getUrn().equals("routerE"))
            {
                v.setVertexType(VertexType.SWITCH);
                v.setUrn("switchE");
                relevantNode = true;

            }
            else if(v.getUrn().equals("routerE:1"))
            {
                v.setUrn("switchE:1");
                relevantNode = true;
            }
            else if(v.getUrn().equals("routerE:2"))
            {
                v.setUrn("switchE:2");
                relevantNode = true;
            }

            if(relevantNode)
            {
                ethernetTopoVertices.add(v);
            }
        }

        keptInMPLSVert = mplsTopoVertices.stream()
                .filter(v -> !v.getUrn().contains("switch"))
                .collect(Collectors.toSet());

        mplsTopoVertices = new HashSet<>();
        mplsTopoVertices.addAll(keptInMPLSVert);

        for(TopoEdge e : mplsTopoEdges)
        {
            String srcURN = e.getA().getUrn();
            String dstURN = e.getZ().getUrn();

            if(srcURN.equals("switchE:1") && dstURN.equals("routerD:2"))
            {
                    e.setLayer(Layer.ETHERNET);
                    ethernetTopoEdges.add(e);
            }
            else if(srcURN.equals("routerD:2") && dstURN.equals("switchE:1"))
            {
                e.setLayer(Layer.ETHERNET);
                ethernetTopoEdges.add(e);
            }
        }

        keptInMPLSEdge = mplsTopoEdges.stream()
                .filter(e -> !e.getLayer().equals(Layer.ETHERNET))
                .collect(Collectors.toSet());

        mplsTopoEdges = new HashSet<>();
        mplsTopoEdges.addAll(keptInMPLSEdge);

        constructLayeredTopology();
        buildLinearRequestPipeMpls2Eth();

        Set<LogicalEdge> logicalLinks = serviceLayerTopo.getLogicalLinks();
        Set<LogicalEdge> llBackup = serviceLayerTopo.getLlBackup();

        log.info("Beginning test: 'verifyLogicalLinksSrcMPLS'.");

        assert(logicalLinks.size() == 0);       // No Service-Layer nodes yet.
        assert(llBackup.size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 6);
        assert(serviceLayerTopo.getServiceLayerDevices().size() == 1);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 2);
        assert(serviceLayerTopo.getMplsLayerDevices().size() == 4);
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 8);
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 22);

        TopoVertex srcDevice = null, dstDevice = null, srcPort = null, dstPort = null;

        for(TopoVertex v : mplsTopoVertices)
        {
            if(v.getUrn().equals("routerA"))
                srcDevice = v;
            else if(v.getUrn().equals("routerA:1"))
                srcPort = v;
        }

        for(TopoVertex v : ethernetTopoVertices)
        {
            if(v.getUrn().equals("switchE"))
                dstDevice = v;
            else if(v.getUrn().equals("switchE:2"))
                dstPort = v;
        }

        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);     // should create VIRTUAL nodes
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);     // should NOT create VIRTUAL nodes

        logicalLinks = serviceLayerTopo.getLogicalLinks();
        llBackup = serviceLayerTopo.getLlBackup();

        assert(logicalLinks.size() == 2);       // From VIRTUAL src -> dest and dest -> VIRTUAL src
        assert(llBackup.size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 10);
        assert(serviceLayerTopo.getServiceLayerDevices().size() == 2);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 3);
        assert(serviceLayerTopo.getMplsLayerDevices().size() == 4);     // No change
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 8);       // No change
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 22);      // No change

        serviceLayerTopo.getLogicalLinks().stream()
                .forEach(ll -> {
                    String aURN = ll.getA().getUrn();
                    String zURN = ll.getZ().getUrn();
                    if(aURN.equals("routerA:1-virtual"))
                        assert(zURN.equals("switchE:1"));
                    else if(aURN.equals("switchE:1"))
                        assert(zURN.equals("routerA:1-virtual"));
                    else
                        assert(false);
                });

        serviceLayerTopo.calculateLogicalLinkWeights(requestedPipe);

        logicalLinks = serviceLayerTopo.getLogicalLinks();
        llBackup = serviceLayerTopo.getLlBackup();

        assert(serviceLayerTopo.getNonAdjacentPorts().size() == 2);
        assert(logicalLinks.size() == 2);   // No change after assigning weights
        assert(llBackup.size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 10);   // No change
        assert(serviceLayerTopo.getServiceLayerDevices().size() == 2);  // No change
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 3);    // No change
        assert(serviceLayerTopo.getMplsLayerDevices().size() == 4);     // Still no change
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 8);       // Still no change
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 22);      // Still no change

        for(LogicalEdge ll : logicalLinks)
        {
            List<TopoEdge> physicalEdges = ll.getCorrespondingTopoEdges();

            String physicalURNs = "";
            String correctURNs = "";

            if(ll.getA().getUrn().equals("routerA:1-virtual"))
            {
                assert(physicalEdges.get(0).getA().getUrn().equals("routerA:1-virtual"));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().getUrn().equals("switchE:1"));
                assert(ll.getMetric() == 400);
                assert(physicalEdges.size() == 13);

                correctURNs = "routerA:1-virtual]-->[routerA:1-routerA:1]-->[routerA-routerA]-->[routerA:2-routerA:2]-->[routerB:1-routerB:1]-->[routerB-routerB]-->[routerB:2-routerB:2]-->[routerC:1-routerC:1]-->[routerC-routerC]-->[routerC:2-routerC:2]-->[routerD:1-routerD:1]-->[routerD-routerD]-->[routerD:2-routerD:2]-->[switchE:1-";
            }
            else
            {
                assert(physicalEdges.get(0).getA().getUrn().equals("switchE:1"));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().getUrn().equals("routerA:1-virtual"));
                assert(ll.getMetric() == 400);
                assert(physicalEdges.size() == 13);

                correctURNs = "switchE:1]-->[routerD:2-routerD:2]-->[routerD-routerD]-->[routerD:1-routerD:1]-->[routerC:2-routerC:2]-->[routerC-routerC]-->[routerC:1-routerC:1]-->[routerB:2-routerB:2]-->[routerB-routerB]-->[routerB:1-routerB:1]-->[routerA:2-routerA:2]-->[routerA-routerA]-->[routerA:1-routerA:1]-->[routerA:1-virtual-";
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

        log.info("test 'verifyLogicalLinksSrcMPLS' passed.");
    }

    /* Expected behavior: Add one VIRTUAL nodes and to VIRTUAL ports to Service-Layer topology to represent destination end-points. */
    @Test
    public void verifyLogicalLinksDstMPLS()
    {
        this.buildLinearMPLSTopo();

        Set<TopoVertex> keptInMPLSVert;
        Set<TopoEdge> keptInMPLSEdge;

        // Put nodeA, portA1, portA2 and the respective links on the ethernet layer
        for(TopoVertex v : mplsTopoVertices)
        {
            boolean relevantNode = false;

            if(v.getUrn().equals("routerA"))
            {
                v.setVertexType(VertexType.SWITCH);
                v.setUrn("switchA");
                relevantNode = true;

            }
            else if(v.getUrn().equals("routerA:1"))
            {
                v.setUrn("switchA:1");
                relevantNode = true;
            }
            else if(v.getUrn().equals("routerA:2"))
            {
                v.setUrn("switchA:2");
                relevantNode = true;
            }

            if(relevantNode)
            {
                ethernetTopoVertices.add(v);
            }
        }

        keptInMPLSVert = mplsTopoVertices.stream()
                .filter(v -> !v.getUrn().contains("switch"))
                .collect(Collectors.toSet());

        mplsTopoVertices = new HashSet<>();
        mplsTopoVertices.addAll(keptInMPLSVert);

        for(TopoEdge e : mplsTopoEdges)
        {
            String srcURN = e.getA().getUrn();
            String dstURN = e.getZ().getUrn();

            if(srcURN.equals("switchA:2") && dstURN.equals("routerB:1"))
            {
                e.setLayer(Layer.ETHERNET);
                ethernetTopoEdges.add(e);
            }
            else if(srcURN.equals("routerB:1") && dstURN.equals("switchA:2"))
            {
                e.setLayer(Layer.ETHERNET);
                ethernetTopoEdges.add(e);
            }
        }

        keptInMPLSEdge = mplsTopoEdges.stream()
                .filter(e -> !e.getLayer().equals(Layer.ETHERNET))
                .collect(Collectors.toSet());

        mplsTopoEdges = new HashSet<>();
        mplsTopoEdges.addAll(keptInMPLSEdge);

        constructLayeredTopology();
        buildLinearRequestPipeEth2Mpls();

        Set<LogicalEdge> logicalLinks = serviceLayerTopo.getLogicalLinks();
        Set<LogicalEdge> llBackup = serviceLayerTopo.getLlBackup();

        log.info("Beginning test: 'verifyLogicalLinksDstMPLS'.");

        assert(logicalLinks.size() == 0);       // No Service-Layer nodes yet.
        assert(llBackup.size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 6);
        assert(serviceLayerTopo.getServiceLayerDevices().size() == 1);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 2);
        assert(serviceLayerTopo.getMplsLayerDevices().size() == 4);
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 8);
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 22);

        TopoVertex srcDevice = null, dstDevice = null, srcPort = null, dstPort = null;

        for(TopoVertex v : ethernetTopoVertices)
        {
            if(v.getUrn().equals("switchA"))
                srcDevice = v;
            else if(v.getUrn().equals("switchA:1"))
                srcPort = v;
        }

        for(TopoVertex v : mplsTopoVertices)
        {
            if(v.getUrn().equals("routerE"))
                dstDevice = v;
            else if(v.getUrn().equals("routerE:2"))
                dstPort = v;
        }

        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);     // should NOT create VIRTUAL nodes
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);     // should create VIRTUAL nodes

        logicalLinks = serviceLayerTopo.getLogicalLinks();
        llBackup = serviceLayerTopo.getLlBackup();

        assert(logicalLinks.size() == 2);       // From src -> VIRTUAL dest and VIRTUAL dest -> src
        assert(llBackup.size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 10);
        assert(serviceLayerTopo.getServiceLayerDevices().size() == 2);
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 3);
        assert(serviceLayerTopo.getMplsLayerDevices().size() == 4);     // No change
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 8);       // No change
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 22);      // No change

        serviceLayerTopo.getLogicalLinks().stream()
                .forEach(ll -> {
                    String aURN = ll.getA().getUrn();
                    String zURN = ll.getZ().getUrn();
                    if(aURN.equals("switchA:2"))
                        assert(zURN.equals("routerE:2-virtual"));
                    else if(aURN.equals("routerE:2-virtual"))
                        assert(zURN.equals("switchA:2"));
                    else
                        assert(false);
                });

        serviceLayerTopo.calculateLogicalLinkWeights(requestedPipe);

        logicalLinks = serviceLayerTopo.getLogicalLinks();
        llBackup = serviceLayerTopo.getLlBackup();

        assert(serviceLayerTopo.getNonAdjacentPorts().size() == 2);
        assert(logicalLinks.size() == 2);   // No change after assigning weights
        assert(llBackup.size() == 0);
        assert(serviceLayerTopo.getServiceLayerLinks().size() == 10);   // No change
        assert(serviceLayerTopo.getServiceLayerDevices().size() == 2);  // No change
        assert(serviceLayerTopo.getServiceLayerPorts().size() == 3);    // No change
        assert(serviceLayerTopo.getMplsLayerDevices().size() == 4);     // Still no change
        assert(serviceLayerTopo.getMplsLayerPorts().size() == 8);       // Still no change
        assert(serviceLayerTopo.getMplsLayerLinks().size() == 22);      // Still no change

        for(LogicalEdge ll : logicalLinks)
        {
            List<TopoEdge> physicalEdges = ll.getCorrespondingTopoEdges();

            String physicalURNs = "";
            String correctURNs = "";

            if(ll.getA().getUrn().equals("switchA:2"))
            {
                assert(physicalEdges.get(0).getA().getUrn().equals("switchA:2"));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().getUrn().equals("routerE:2-virtual"));
                assert(ll.getMetric() == 400);
                assert(physicalEdges.size() == 13);

                correctURNs = "switchA:2]-->[routerB:1-routerB:1]-->[routerB-routerB]-->[routerB:2-routerB:2]-->[routerC:1-routerC:1]-->[routerC-routerC]-->[routerC:2-routerC:2]-->[routerD:1-routerD:1]-->[routerD-routerD]-->[routerD:2-routerD:2]-->[routerE:1-routerE:1]-->[routerE-routerE]-->[routerE:2-routerE:2]-->[routerE:2-virtual-";
            }
            else
            {
                assert(physicalEdges.get(0).getA().getUrn().equals("routerE:2-virtual"));
                assert(physicalEdges.get(physicalEdges.size()-1).getZ().getUrn().equals("switchA:2"));
                assert(ll.getMetric() == 400);
                assert(physicalEdges.size() == 13);

                correctURNs = "routerE:2-virtual]-->[routerE:2-routerE:2]-->[routerE-routerE]-->[routerE:1-routerE:1]-->[routerD:2-routerD:2]-->[routerD-routerD]-->[routerD:1-routerD:1]-->[routerC:2-routerC:2]-->[routerC-routerC]-->[routerC:1-routerC:1]-->[routerB:2-routerB:2]-->[routerB-routerB]-->[routerB:1-routerB:1]-->[switchA:2-";
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

        log.info("test 'verifyLogicalLinksDstMPLS' passed.");
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
        ethernetTopoVertices = new HashSet<>();
        mplsTopoVertices = new HashSet<>();
        internalTopoVertices = new HashSet<>();

        ethernetTopoEdges = new HashSet<>();
        mplsTopoEdges = new HashSet<>();
        internalTopoEdges = new HashSet<>();


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

    // same as buildLinearTopo(), except all devices/ports/links are on Ethernet layer
    private void buildLinearMPLSTopo()
    {
        ethernetTopoVertices = new HashSet<>();
        mplsTopoVertices = new HashSet<>();
        internalTopoVertices = new HashSet<>();

        ethernetTopoEdges = new HashSet<>();
        mplsTopoEdges = new HashSet<>();
        internalTopoEdges = new HashSet<>();


        //Devices
        TopoVertex nodeA = new TopoVertex("routerA", VertexType.ROUTER);
        TopoVertex nodeB = new TopoVertex("routerB", VertexType.ROUTER);
        TopoVertex nodeC = new TopoVertex("routerC", VertexType.ROUTER);
        TopoVertex nodeD = new TopoVertex("routerD", VertexType.ROUTER);
        TopoVertex nodeE = new TopoVertex("routerE", VertexType.ROUTER);

        //Ports
        TopoVertex portA1 = new TopoVertex("routerA:1", VertexType.PORT);
        TopoVertex portA2 = new TopoVertex("routerA:2", VertexType.PORT);
        TopoVertex portB1 = new TopoVertex("routerB:1", VertexType.PORT);
        TopoVertex portB2 = new TopoVertex("routerB:2", VertexType.PORT);
        TopoVertex portC1 = new TopoVertex("routerC:1", VertexType.PORT);
        TopoVertex portC2 = new TopoVertex("routerC:2", VertexType.PORT);
        TopoVertex portD1 = new TopoVertex("routerD:1", VertexType.PORT);
        TopoVertex portD2 = new TopoVertex("routerD:2", VertexType.PORT);
        TopoVertex portE1 = new TopoVertex("routerE:1", VertexType.PORT);
        TopoVertex portE2 = new TopoVertex("routerE:2", VertexType.PORT);

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
        TopoEdge edgeMpls_A2_B1 = new TopoEdge(portA2, portB1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_B2_C1 = new TopoEdge(portB2, portC1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_C2_D1 = new TopoEdge(portC2, portD1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_D2_E1 = new TopoEdge(portD2, portE1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_B1_A2 = new TopoEdge(portB1, portA2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_C1_B2 = new TopoEdge(portC1, portB2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_D1_C2 = new TopoEdge(portD1, portC2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_E1_D2 = new TopoEdge(portE1, portD2, 100L, Layer.MPLS);

        mplsTopoVertices.add(nodeA);
        mplsTopoVertices.add(nodeB);
        mplsTopoVertices.add(nodeC);
        mplsTopoVertices.add(nodeD);
        mplsTopoVertices.add(nodeE);
        mplsTopoVertices.add(portA1);
        mplsTopoVertices.add(portA2);
        mplsTopoVertices.add(portB1);
        mplsTopoVertices.add(portB2);
        mplsTopoVertices.add(portC1);
        mplsTopoVertices.add(portC2);
        mplsTopoVertices.add(portD1);
        mplsTopoVertices.add(portD2);
        mplsTopoVertices.add(portE1);
        mplsTopoVertices.add(portE2);

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

        mplsTopoEdges.add(edgeMpls_A2_B1);
        mplsTopoEdges.add(edgeMpls_B1_A2);
        mplsTopoEdges.add(edgeMpls_B2_C1);
        mplsTopoEdges.add(edgeMpls_C1_B2);
        mplsTopoEdges.add(edgeMpls_C2_D1);
        mplsTopoEdges.add(edgeMpls_D1_C2);
        mplsTopoEdges.add(edgeMpls_D2_E1);
        mplsTopoEdges.add(edgeMpls_E1_D2);
    }



    private void buildLinearTopoWithMultipleMPLSBranch()
    {
        buildLinearTopo();

        TopoVertex nodeB = null;
        TopoVertex nodeC = null;
        TopoVertex nodeD = null;

        for(TopoVertex n : mplsTopoVertices)
        {
            if(n.getUrn().equals("routerB"))
                nodeB = n;
            else if(n.getUrn().equals("routerC"))
                nodeC = n;
            else if(n.getUrn().equals("routerD"))
                nodeD = n;
        }


        //Additional Devices
        TopoVertex nodeF = new TopoVertex("routerF", VertexType.ROUTER);
        TopoVertex nodeG = new TopoVertex("routerG", VertexType.ROUTER);
        TopoVertex nodeH = new TopoVertex("routerH", VertexType.ROUTER);

        //Additional Ports
        TopoVertex portB3 = new TopoVertex("routerB:3", VertexType.PORT);
        TopoVertex portB4 = new TopoVertex("routerB:4", VertexType.PORT);
        TopoVertex portC3 = new TopoVertex("routerC:3", VertexType.PORT);
        TopoVertex portC4 = new TopoVertex("routerC:4", VertexType.PORT);
        TopoVertex portD3 = new TopoVertex("routerD:3", VertexType.PORT);
        TopoVertex portD4 = new TopoVertex("routerD:4", VertexType.PORT);
        TopoVertex portF1 = new TopoVertex("routerF:1", VertexType.PORT);
        TopoVertex portF2 = new TopoVertex("routerF:2", VertexType.PORT);
        TopoVertex portG1 = new TopoVertex("routerG:1", VertexType.PORT);
        TopoVertex portG2 = new TopoVertex("routerG:2", VertexType.PORT);
        TopoVertex portH1 = new TopoVertex("routerH:1", VertexType.PORT);
        TopoVertex portH2 = new TopoVertex("routerH:2", VertexType.PORT);

        //Additional Internal Links
        TopoEdge edgeInt_B3_B = new TopoEdge(portB3, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B4_B = new TopoEdge(portB4, nodeB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C3_C = new TopoEdge(portC3, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C4_C = new TopoEdge(portC4, nodeC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D3_D = new TopoEdge(portD3, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D4_D = new TopoEdge(portD4, nodeD, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F1_F = new TopoEdge(portF1, nodeF, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F2_F = new TopoEdge(portF2, nodeF, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G1_G = new TopoEdge(portG1, nodeG, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G2_G = new TopoEdge(portG2, nodeG, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_H1_H = new TopoEdge(portH1, nodeH, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_H2_H = new TopoEdge(portH2, nodeH, 0L, Layer.INTERNAL);

        //Additional Internal Reverse Links
        TopoEdge edgeInt_B_B3 = new TopoEdge(nodeB, portB3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_B4 = new TopoEdge(nodeB, portB4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C3 = new TopoEdge(nodeC, portC3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_C4 = new TopoEdge(nodeC, portC4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D3 = new TopoEdge(nodeD, portD3, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_D_D4 = new TopoEdge(nodeD, portD4, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F_F1 = new TopoEdge(nodeF, portF1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_F_F2 = new TopoEdge(nodeF, portF2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G_G1 = new TopoEdge(nodeG, portG1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_G_G2 = new TopoEdge(nodeG, portG2, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_H_H1 = new TopoEdge(nodeH, portH1, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_H_H2 = new TopoEdge(nodeH, portH2, 0L, Layer.INTERNAL);

        //Additional Network Links
        TopoEdge edgeMpls_B3_F1 = new TopoEdge(portB3, portF1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_B4_H1 = new TopoEdge(portB4, portH1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_C3_F2 = new TopoEdge(portC3, portF2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_C4_G1 = new TopoEdge(portC4, portG1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_D3_G2 = new TopoEdge(portD3, portG2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_D4_H2 = new TopoEdge(portD4, portH2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_F1_B3 = new TopoEdge(portF1, portB3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_F2_C3 = new TopoEdge(portF2, portC3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_G1_C4 = new TopoEdge(portG1, portC4, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_G2_D3 = new TopoEdge(portG2, portD3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_H1_B4 = new TopoEdge(portH1, portB4, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_H2_D4 = new TopoEdge(portH2, portD4, 100L, Layer.ETHERNET);



        mplsTopoVertices.add(nodeF);
        mplsTopoVertices.add(nodeG);
        mplsTopoVertices.add(nodeH);
        mplsTopoVertices.add(portB3);
        mplsTopoVertices.add(portB4);
        mplsTopoVertices.add(portC3);
        mplsTopoVertices.add(portC4);
        mplsTopoVertices.add(portD3);
        mplsTopoVertices.add(portD4);
        mplsTopoVertices.add(portF1);
        mplsTopoVertices.add(portF2);
        mplsTopoVertices.add(portG1);
        mplsTopoVertices.add(portG2);
        mplsTopoVertices.add(portH1);
        mplsTopoVertices.add(portH2);

        internalTopoEdges.add(edgeInt_B3_B);
        internalTopoEdges.add(edgeInt_B4_B);
        internalTopoEdges.add(edgeInt_C3_C);
        internalTopoEdges.add(edgeInt_C4_C);
        internalTopoEdges.add(edgeInt_D3_D);
        internalTopoEdges.add(edgeInt_D4_D);
        internalTopoEdges.add(edgeInt_F1_F);
        internalTopoEdges.add(edgeInt_F2_F);
        internalTopoEdges.add(edgeInt_G1_G);
        internalTopoEdges.add(edgeInt_G2_G);
        internalTopoEdges.add(edgeInt_H1_H);
        internalTopoEdges.add(edgeInt_H2_H);
        internalTopoEdges.add(edgeInt_B_B3);
        internalTopoEdges.add(edgeInt_B_B4);
        internalTopoEdges.add(edgeInt_C_C3);
        internalTopoEdges.add(edgeInt_C_C4);
        internalTopoEdges.add(edgeInt_D_D3);
        internalTopoEdges.add(edgeInt_D_D4);
        internalTopoEdges.add(edgeInt_F_F1);
        internalTopoEdges.add(edgeInt_F_F2);
        internalTopoEdges.add(edgeInt_G_G1);
        internalTopoEdges.add(edgeInt_G_G2);
        internalTopoEdges.add(edgeInt_H_H1);
        internalTopoEdges.add(edgeInt_H_H2);

        mplsTopoEdges.add(edgeMpls_B3_F1);
        mplsTopoEdges.add(edgeMpls_B4_H1);
        mplsTopoEdges.add(edgeMpls_C3_F2);
        mplsTopoEdges.add(edgeMpls_C4_G1);
        mplsTopoEdges.add(edgeMpls_D3_G2);
        mplsTopoEdges.add(edgeMpls_D4_H2);
        mplsTopoEdges.add(edgeMpls_F1_B3);
        mplsTopoEdges.add(edgeMpls_F2_C3);
        mplsTopoEdges.add(edgeMpls_G1_C4);
        mplsTopoEdges.add(edgeMpls_G2_D3);
        mplsTopoEdges.add(edgeMpls_H1_B4);
        mplsTopoEdges.add(edgeMpls_H2_D4);
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


    private void buildLinearRequestPipeEth2Eth()
    {
        requestedPipe = new RequestedVlanPipeE();

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

        zFixURN.setUrn("switchE:2");
        zFixURN.setUrnType(UrnType.IFCE);

        aJuncURN.setUrn("switchA");
        aJuncURN.setUrnType(UrnType.DEVICE);

        zJuncURN.setUrn("switchE");
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

    private void buildLinearRequestPipeMpls2Mpls()
    {
        requestedPipe = new RequestedVlanPipeE();

        RequestedVlanPipeE bwPipe = new RequestedVlanPipeE();
        RequestedVlanJunctionE aJunc = new RequestedVlanJunctionE();
        RequestedVlanJunctionE zJunc = new RequestedVlanJunctionE();
        RequestedVlanFixtureE aFix = new RequestedVlanFixtureE();
        RequestedVlanFixtureE zFix = new RequestedVlanFixtureE();
        UrnE aFixURN = new UrnE();
        UrnE zFixURN = new UrnE();
        UrnE aJuncURN = new UrnE();
        UrnE zJuncURN = new UrnE();

        aFixURN.setUrn("routerA:1");
        aFixURN.setUrnType(UrnType.IFCE);

        zFixURN.setUrn("routerE:2");
        zFixURN.setUrnType(UrnType.IFCE);

        aJuncURN.setUrn("routerA");
        aJuncURN.setUrnType(UrnType.DEVICE);

        zJuncURN.setUrn("routerE");
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

    private void buildLinearRequestPipeMpls2Eth()
    {
        requestedPipe = new RequestedVlanPipeE();

        RequestedVlanPipeE bwPipe = new RequestedVlanPipeE();
        RequestedVlanJunctionE aJunc = new RequestedVlanJunctionE();
        RequestedVlanJunctionE zJunc = new RequestedVlanJunctionE();
        RequestedVlanFixtureE aFix = new RequestedVlanFixtureE();
        RequestedVlanFixtureE zFix = new RequestedVlanFixtureE();
        UrnE aFixURN = new UrnE();
        UrnE zFixURN = new UrnE();
        UrnE aJuncURN = new UrnE();
        UrnE zJuncURN = new UrnE();

        aFixURN.setUrn("routerA:1");
        aFixURN.setUrnType(UrnType.IFCE);

        zFixURN.setUrn("switchE:2");
        zFixURN.setUrnType(UrnType.IFCE);

        aJuncURN.setUrn("routerA");
        aJuncURN.setUrnType(UrnType.DEVICE);

        zJuncURN.setUrn("switchE");
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

    private void buildLinearRequestPipeEth2Mpls()
    {
        requestedPipe = new RequestedVlanPipeE();

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

        zFixURN.setUrn("routerE:2");
        zFixURN.setUrnType(UrnType.IFCE);

        aJuncURN.setUrn("switchA");
        aJuncURN.setUrnType(UrnType.DEVICE);

        zJuncURN.setUrn("routerE");
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
