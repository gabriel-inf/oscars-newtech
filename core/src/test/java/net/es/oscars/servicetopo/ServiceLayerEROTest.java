package net.es.oscars.servicetopo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.pce.DijkstraPCE;
import net.es.oscars.pce.PruningService;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.ReservableVlanE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.enums.VertexType;
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

/**
 * Created by jeremy on 6/24/16.
 *
 * Primarily tests correctness of service-layer topology logical edge construction, initialization, and population during MPLS-layer routing
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class ServiceLayerEROTest
{
    @Autowired
    private ServiceLayerTopology serviceLayerTopo;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    private Set<TopoVertex> ethernetTopoVertices;
    private Set<TopoVertex> mplsTopoVertices;
    private Set<TopoVertex> internalTopoVertices;

    private Set<TopoEdge> ethernetTopoEdges;
    private Set<TopoEdge> mplsTopoEdges;
    private Set<TopoEdge> internalTopoEdges;

    private RequestedVlanPipeE requestedPipe;
    private ScheduleSpecificationE requestedSched;
    private List<UrnE> urnList;
    private List<ReservedBandwidthE> resvBW;
    private List<ReservedVlanE> resvVLAN;

    @Test
    public void verifyVirtualSrcDest()
    {
        this.buildLinearMPLSTopo();

        constructLayeredTopology();
        buildLinearRequestPipeMpls2Mpls();
        buildDummySchedule();

        TopoVertex srcDevice = null;
        TopoVertex dstDevice = null;
        TopoVertex srcPort = null;
        TopoVertex dstPort = null;

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

        log.info("Beginning test: 'verifyVirtualSrcDest'.");

        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);     // should create VIRTUAL nodes
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);     // should create VIRTUAL nodes

        TopoVertex virtSrc = serviceLayerTopo.getVirtualNode(srcDevice);
        TopoVertex virtDst = serviceLayerTopo.getVirtualNode(dstDevice);


        assert(virtSrc.getVertexType().equals(VertexType.VIRTUAL));
        assert(virtDst.getVertexType().equals(VertexType.VIRTUAL));
        assert(virtSrc.getUrn().equals(srcDevice.getUrn() + "-virtual"));
        assert(virtDst.getUrn().equals(dstDevice.getUrn() + "-virtual"));


        Topology slTopo = serviceLayerTopo.getSLTopology();
        Set<TopoVertex> correctVertices = serviceLayerTopo.getServiceLayerDevices().stream().collect(Collectors.toSet());
        correctVertices.addAll(serviceLayerTopo.getServiceLayerPorts());

        Set<TopoEdge> correctEdges = serviceLayerTopo.getServiceLayerLinks().stream().collect(Collectors.toSet());
        correctEdges.addAll(serviceLayerTopo.getLogicalLinks());

        assert(slTopo.getVertices().size() == 4);
        assert(slTopo.getEdges().size() == 10);
        assert(slTopo.getVertices().equals(correctVertices));
        assert(slTopo.getEdges().equals(correctEdges));
        assert (slTopo.getLayer().equals(Layer.ETHERNET));

        log.info("test 'verifyVirtualSrcDest' passed.");
    }

    @Test
    public void verifyVirtualDestOnly()
    {
        this.buildLinearEthToMPLSTopo();

        constructLayeredTopology();
        buildLinearRequestPipeEth2Mpls();
        buildDummySchedule();

        TopoVertex srcDevice = null;
        TopoVertex dstDevice = null;
        TopoVertex srcPort = null;
        TopoVertex dstPort = null;

        for(TopoVertex v : mplsTopoVertices)
        {
            if(v.getUrn().equals("routerE"))
                dstDevice = v;

            else if(v.getUrn().equals("routerE:2"))
                dstPort = v;
        }

        for(TopoVertex v : ethernetTopoVertices)
        {
            if (v.getUrn().equals("switchA"))
                srcDevice = v;
            else if (v.getUrn().equals("switchA:1"))
                srcPort = v;
        }

        log.info("Beginning test: 'verifyVirtualDestOnly'.");

        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);     // should NOT create VIRTUAL nodes
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);     // should create VIRTUAL nodes

        TopoVertex virtSrc = serviceLayerTopo.getVirtualNode(srcDevice);
        TopoVertex virtDst = serviceLayerTopo.getVirtualNode(dstDevice);


        assert(virtSrc == null);
        assert(virtDst.getVertexType().equals(VertexType.VIRTUAL));
        assert(virtDst.getUrn().equals(dstDevice.getUrn() + "-virtual"));

        Topology slTopo = serviceLayerTopo.getSLTopology();
        Set<TopoVertex> correctVertices = serviceLayerTopo.getServiceLayerDevices().stream().collect(Collectors.toSet());
        correctVertices.addAll(serviceLayerTopo.getServiceLayerPorts());

        Set<TopoEdge> correctEdges = serviceLayerTopo.getServiceLayerLinks().stream().collect(Collectors.toSet());
        correctEdges.addAll(serviceLayerTopo.getLogicalLinks());

        assert(slTopo.getVertices().size() == 5);
        assert(slTopo.getEdges().size() == 12);
        assert(slTopo.getVertices().equals(correctVertices));
        assert(slTopo.getEdges().equals(correctEdges));
        assert (slTopo.getLayer().equals(Layer.ETHERNET));

        log.info("test 'verifyVirtualDestOnly' passed.");
    }


    @Test
    public void verifyVirtualSrcOnly()
    {
        this.buildLinearMPLSToEthTopo();

        constructLayeredTopology();
        buildLinearRequestPipeMpls2Eth();
        buildDummySchedule();

        TopoVertex srcDevice = null;
        TopoVertex dstDevice = null;
        TopoVertex srcPort = null;
        TopoVertex dstPort = null;

        for(TopoVertex v : ethernetTopoVertices)
        {
            if(v.getUrn().equals("switchE"))
                dstDevice = v;

            else if(v.getUrn().equals("switchE:2"))
                dstPort = v;
        }

        for(TopoVertex v : mplsTopoVertices)
        {
            if (v.getUrn().equals("routerA"))
                srcDevice = v;
            else if (v.getUrn().equals("routerA:1"))
                srcPort = v;
        }

        log.info("Beginning test: 'verifyVirtualSrcOnly'.");

        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);     // should create VIRTUAL nodes
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);     // should NOT create VIRTUAL nodes

        TopoVertex virtSrc = serviceLayerTopo.getVirtualNode(srcDevice);
        TopoVertex virtDst = serviceLayerTopo.getVirtualNode(dstDevice);


        assert(virtSrc.getVertexType().equals(VertexType.VIRTUAL));
        assert(virtSrc.getUrn().equals(srcDevice.getUrn() + "-virtual"));
        assert(virtDst == null);

        Topology slTopo = serviceLayerTopo.getSLTopology();
        Set<TopoVertex> correctVertices = serviceLayerTopo.getServiceLayerDevices().stream().collect(Collectors.toSet());
        correctVertices.addAll(serviceLayerTopo.getServiceLayerPorts());

        Set<TopoEdge> correctEdges = serviceLayerTopo.getServiceLayerLinks().stream().collect(Collectors.toSet());
        correctEdges.addAll(serviceLayerTopo.getLogicalLinks());

        assert(slTopo.getVertices().size() == 5);
        assert(slTopo.getEdges().size() == 12);
        assert(slTopo.getVertices().equals(correctVertices));
        assert(slTopo.getEdges().equals(correctEdges));
        assert (slTopo.getLayer().equals(Layer.ETHERNET));

        log.info("test 'verifyVirtualSrcOnly' passed.");
    }


    @Test
    public void verifyNoVirtualSrcDest()
    {
        this.buildLinearTopo();

        constructLayeredTopology();
        buildLinearRequestPipeEth2Eth();
        buildDummySchedule();

        TopoVertex srcDevice = null;
        TopoVertex dstDevice = null;
        TopoVertex srcPort = null;
        TopoVertex dstPort = null;

        for(TopoVertex v : ethernetTopoVertices)
        {
            if (v.getUrn().equals("switchA"))
                srcDevice = v;
            else if(v.getUrn().equals("switchE"))
                dstDevice = v;
            else if (v.getUrn().equals("switchA:1"))
                srcPort = v;
            else if(v.getUrn().equals("switchE:2"))
                dstPort = v;
        }

        log.info("Beginning test: 'verifyNoVirtualSrcDest'.");

        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);     // should NOT create VIRTUAL nodes
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);     // should NOT create VIRTUAL nodes

        TopoVertex virtSrc = serviceLayerTopo.getVirtualNode(srcDevice);
        TopoVertex virtDst = serviceLayerTopo.getVirtualNode(dstDevice);


        assert(virtSrc == null);
        assert(virtDst == null);

        Topology slTopo = serviceLayerTopo.getSLTopology();
        Set<TopoVertex> correctVertices = serviceLayerTopo.getServiceLayerDevices().stream().collect(Collectors.toSet());
        correctVertices.addAll(serviceLayerTopo.getServiceLayerPorts());

        Set<TopoEdge> correctEdges = serviceLayerTopo.getServiceLayerLinks().stream().collect(Collectors.toSet());
        correctEdges.addAll(serviceLayerTopo.getLogicalLinks());

        assert(slTopo.getVertices().size() == 6);
        assert(slTopo.getEdges().size() == 14);
        assert(slTopo.getVertices().equals(correctVertices));
        assert(slTopo.getEdges().equals(correctEdges));
        assert (slTopo.getLayer().equals(Layer.ETHERNET));

        log.info("test 'verifyNoVirtualSrcDest' passed.");
    }

    // Test ERO accuracy when all devices are Ethernet - No logical MPLS links
    @Test
    public void verifyCorrectEROsNoLogicalLinks()
    {
        this.buildLinearEthernetTopo();
        constructLayeredTopology();
        buildLinearRequestPipeEth2Eth();
        buildDummySchedule();
        buildURNList();

        TopoVertex srcDevice = null;
        TopoVertex dstDevice = null;
        TopoVertex srcPort = null;
        TopoVertex dstPort = null;
        List<TopoVertex> verts = new ArrayList<>();

        List<List<TopoEdge>> theEROs;

        for(TopoVertex v : ethernetTopoVertices)
        {
            if (v.getUrn().equals("switchA"))
                srcDevice = v;
            else if(v.getUrn().equals("switchE"))
                dstDevice = v;
            else if (v.getUrn().equals("switchA:1"))
                srcPort = v;
            else if(v.getUrn().equals("switchE:2"))
                dstPort = v;
        }

        verts.add(srcDevice);
        verts.add(dstDevice);
        verts.add(srcPort);
        verts.add(dstPort);

        log.info("Beginning test: 'verifyCorrectEROs'.");

        theEROs = asymmPCEReplica(verts);
        List<TopoEdge> azERO = theEROs.get(0);
        List<TopoEdge> zaERO = theEROs.get(1);

        String correctEroAZ = "[switchA:1-switchA],[switchA-switchA:2],[switchA:2-switchB:1],[switchB:1-switchB],[switchB-switchB:2],[switchB:2-switchC:1],[switchC:1-switchC],[switchC-switchC:2],[switchC:2-switchD:1],[switchD:1-switchD],[switchD-switchD:2],[switchD:2-switchE:1],[switchE:1-switchE],[switchE-switchE:2],";
        String correctEroZA = "[switchE:2-switchE],[switchE-switchE:1],[switchE:1-switchD:2],[switchD:2-switchD],[switchD-switchD:1],[switchD:1-switchC:2],[switchC:2-switchC],[switchC-switchC:1],[switchC:1-switchB:2],[switchB:2-switchB],[switchB-switchB:1],[switchB:1-switchA:2],[switchA:2-switchA],[switchA-switchA:1],";

        String actualEroAZ = "";
        String actualEroZA = "";

        for(TopoEdge forwardEdge : azERO)
        {
            actualEroAZ = actualEroAZ + "[" + forwardEdge.getA().getUrn() + "-" + forwardEdge.getZ().getUrn() + "],";
        }

        for(TopoEdge reverseEdge : zaERO)
        {
            actualEroZA = actualEroZA + "[" + reverseEdge.getA().getUrn() + "-" + reverseEdge.getZ().getUrn() + "],";
        }

        assert(correctEroAZ.equals(actualEroAZ));
        assert(correctEroZA.equals(actualEroZA));

        log.info("test 'verifyCorrectEROs' passed.");
    }

    // Test ERO accuracy when Src and Dest are the only Ethernet devices
    @Test
    public void verifyCorrectEROsSimpleLogicalLinks()
    {
        this.buildLinearTopo();
        constructLayeredTopology();
        buildLinearRequestPipeEth2Eth();
        buildDummySchedule();
        buildURNList();

        TopoVertex srcDevice = null;
        TopoVertex dstDevice = null;
        TopoVertex srcPort = null;
        TopoVertex dstPort = null;
        List<TopoVertex> verts = new ArrayList<>();

        List<List<TopoEdge>> theEROs;

        for(TopoVertex v : ethernetTopoVertices)
        {
            if (v.getUrn().equals("switchA"))
                srcDevice = v;
            else if(v.getUrn().equals("switchE"))
                dstDevice = v;
            else if (v.getUrn().equals("switchA:1"))
                srcPort = v;
            else if(v.getUrn().equals("switchE:2"))
                dstPort = v;
        }

        verts.add(srcDevice);
        verts.add(dstDevice);
        verts.add(srcPort);
        verts.add(dstPort);

        log.info("Beginning test: 'verifyCorrectEROsSimpleLogicalLinks'.");

        theEROs = asymmPCEReplica(verts);
        List<TopoEdge> azERO = theEROs.get(0);
        List<TopoEdge> zaERO = theEROs.get(1);

        String correctEroAZ = "[switchA:1-switchA],[switchA-switchA:2],[switchA:2-routerB:1],[routerB:1-routerB],[routerB-routerB:2],[routerB:2-routerC:1],[routerC:1-routerC],[routerC-routerC:2],[routerC:2-routerD:1],[routerD:1-routerD],[routerD-routerD:2],[routerD:2-switchE:1],[switchE:1-switchE],[switchE-switchE:2],";
        String correctEroZA = "[switchE:2-switchE],[switchE-switchE:1],[switchE:1-routerD:2],[routerD:2-routerD],[routerD-routerD:1],[routerD:1-routerC:2],[routerC:2-routerC],[routerC-routerC:1],[routerC:1-routerB:2],[routerB:2-routerB],[routerB-routerB:1],[routerB:1-switchA:2],[switchA:2-switchA],[switchA-switchA:1],";

        String actualEroAZ = "";
        String actualEroZA = "";

        for(TopoEdge forwardEdge : azERO)
        {
            actualEroAZ = actualEroAZ + "[" + forwardEdge.getA().getUrn() + "-" + forwardEdge.getZ().getUrn() + "],";
        }

        for(TopoEdge reverseEdge : zaERO)
        {
            actualEroZA = actualEroZA + "[" + reverseEdge.getA().getUrn() + "-" + reverseEdge.getZ().getUrn() + "],";
        }

        assert(correctEroAZ.equals(actualEroAZ));
        assert(correctEroZA.equals(actualEroZA));

        log.info("test 'verifyCorrectEROsSimpleLogicalLinks' passed.");
    }

    // Test ERO accuracy when all devices are MPLS - Virtual Src/Dst created
    @Test
    public void verifyCorrectEROsWithVirtualNodes()
    {
        this.buildLinearMPLSTopo();
        constructLayeredTopology();
        buildLinearRequestPipeMpls2Mpls();
        buildDummySchedule();
        buildURNList();

        TopoVertex srcDevice = null;
        TopoVertex dstDevice = null;
        TopoVertex srcPort = null;
        TopoVertex dstPort = null;
        List<TopoVertex> verts = new ArrayList<>();

        List<List<TopoEdge>> theEROs;

        for(TopoVertex v : mplsTopoVertices)
        {
            if (v.getUrn().equals("routerA"))
                srcDevice = v;
            else if(v.getUrn().equals("routerE"))
                dstDevice = v;
            else if (v.getUrn().equals("routerA:1"))
                srcPort = v;
            else if(v.getUrn().equals("routerE:2"))
                dstPort = v;
        }

        verts.add(srcDevice);
        verts.add(dstDevice);
        verts.add(srcPort);
        verts.add(dstPort);

        log.info("Beginning test: 'verifyCorrectEROsWithVirtualNodes'.");

        theEROs = asymmPCEReplica(verts);
        List<TopoEdge> azERO = theEROs.get(0);
        List<TopoEdge> zaERO = theEROs.get(1);

        String correctEroAZ = "[routerA:1-routerA],[routerA-routerA:2],[routerA:2-routerB:1],[routerB:1-routerB],[routerB-routerB:2],[routerB:2-routerC:1],[routerC:1-routerC],[routerC-routerC:2],[routerC:2-routerD:1],[routerD:1-routerD],[routerD-routerD:2],[routerD:2-routerE:1],[routerE:1-routerE],[routerE-routerE:2],";
        String correctEroZA = "[routerE:2-routerE],[routerE-routerE:1],[routerE:1-routerD:2],[routerD:2-routerD],[routerD-routerD:1],[routerD:1-routerC:2],[routerC:2-routerC],[routerC-routerC:1],[routerC:1-routerB:2],[routerB:2-routerB],[routerB-routerB:1],[routerB:1-routerA:2],[routerA:2-routerA],[routerA-routerA:1],";

        String actualEroAZ = "";
        String actualEroZA = "";

        for(TopoEdge forwardEdge : azERO)
        {
            actualEroAZ = actualEroAZ + "[" + forwardEdge.getA().getUrn() + "-" + forwardEdge.getZ().getUrn() + "],";
        }

        for(TopoEdge reverseEdge : zaERO)
        {
            actualEroZA = actualEroZA + "[" + reverseEdge.getA().getUrn() + "-" + reverseEdge.getZ().getUrn() + "],";
        }

        assert(correctEroAZ.equals(actualEroAZ));
        assert(correctEroZA.equals(actualEroZA));

        log.info("test 'verifyCorrectEROsWithVirtualNodes' passed.");
    }

    // Test ERO accuracy when topology is non-linear, and azERO != zaERO
    @Test
    public void verifyCorrectEROsNonLinearAssymetric()
    {
        this.buildLinearTopoWithMultipleMPLSBranch();
        constructLayeredTopology();
        buildLinearRequestPipeEth2Eth();
        buildDummySchedule();
        buildURNList();

        TopoVertex srcDevice = null;
        TopoVertex dstDevice = null;
        TopoVertex srcPort = null;
        TopoVertex dstPort = null;
        List<TopoVertex> verts = new ArrayList<>();

        List<List<TopoEdge>> theEROs;

        for(TopoVertex v : ethernetTopoVertices)
        {
            if (v.getUrn().equals("switchA"))
                srcDevice = v;
            else if(v.getUrn().equals("switchE"))
                dstDevice = v;
            else if (v.getUrn().equals("switchA:1"))
                srcPort = v;
            else if(v.getUrn().equals("switchE:2"))
                dstPort = v;
        }

        verts.add(srcDevice);
        verts.add(dstDevice);
        verts.add(srcPort);
        verts.add(dstPort);

        log.info("Beginning test: 'verifyCorrectEROsNonLinearAssymetric'.");

        theEROs = asymmPCEReplica(verts);
        List<TopoEdge> azERO = theEROs.get(0);
        List<TopoEdge> zaERO = theEROs.get(1);

        String correctEroAZ = "[switchA:1-switchA],[switchA-switchA:2],[switchA:2-routerB:1],[routerB:1-routerB],[routerB-routerB:3],[routerB:3-routerF:1],[routerF:1-routerF],[routerF-routerF:2],[routerF:2-routerC:3],[routerC:3-routerC],[routerC-routerC:4],[routerC:4-routerG:1],[routerG:1-routerG],[routerG-routerG:2],[routerG:2-routerD:3],[routerD:3-routerD],[routerD-routerD:2],[routerD:2-switchE:1],[switchE:1-switchE],[switchE-switchE:2],";
        String correctEroZA = "[switchE:2-switchE],[switchE-switchE:1],[switchE:1-routerD:2],[routerD:2-routerD],[routerD-routerD:3],[routerD:3-routerG:2],[routerG:2-routerG],[routerG-routerG:1],[routerG:1-routerC:4],[routerC:4-routerC],[routerC-routerC:1],[routerC:1-routerB:2],[routerB:2-routerB],[routerB-routerB:1],[routerB:1-switchA:2],[switchA:2-switchA],[switchA-switchA:1],";

        String actualEroAZ = "";
        String actualEroZA = "";

        for(TopoEdge forwardEdge : azERO)
        {
            actualEroAZ = actualEroAZ + "[" + forwardEdge.getA().getUrn() + "-" + forwardEdge.getZ().getUrn() + "],";
        }

        for(TopoEdge reverseEdge : zaERO)
        {
            actualEroZA = actualEroZA + "[" + reverseEdge.getA().getUrn() + "-" + reverseEdge.getZ().getUrn() + "],";
        }

        assert(correctEroAZ.equals(actualEroAZ));
        assert(correctEroZA.equals(actualEroZA));

        log.info("test 'verifyCorrectEROsNonLinearAssymetric' passed.");
    }

    private List<List<TopoEdge>> asymmPCEReplica(List<TopoVertex> vertices)
    {
        TopoVertex srcDevice = vertices.get(0);
        TopoVertex dstDevice = vertices.get(1);
        TopoVertex srcPort = vertices.get(2);
        TopoVertex dstPort = vertices.get(3);

        // Handle MPLS-layer source/destination devices
        serviceLayerTopo.buildLogicalLayerSrcNodes(srcDevice, srcPort);     // should create VIRTUAL nodes
        serviceLayerTopo.buildLogicalLayerDstNodes(dstDevice, dstPort);     // should create VIRTUAL nodes

        // Performs shortest path routing on MPLS-layer to properly assign weights to each logical link on Service-Layer
        serviceLayerTopo.calculateLogicalLinkWeights(requestedPipe, requestedSched, urnList, resvBW, resvVLAN);

        Topology slTopo = serviceLayerTopo.getSLTopology();
        Topology prunedSlTopo = pruningService.pruneWithPipe(slTopo, requestedPipe, requestedSched);

        TopoVertex serviceLayerSrcNode;
        TopoVertex serviceLayerDstNode;

        if(srcDevice.getVertexType().equals(VertexType.SWITCH))
        {
            serviceLayerSrcNode = srcPort;
        }
        else
        {
            serviceLayerSrcNode = serviceLayerTopo.getVirtualNode(srcDevice);
            assert(serviceLayerSrcNode != null);
        }

        if(dstDevice.getVertexType().equals(VertexType.SWITCH))
        {
            serviceLayerDstNode = dstPort;
        }
        else
        {
            serviceLayerDstNode = serviceLayerTopo.getVirtualNode(dstDevice);
            assert(serviceLayerDstNode != null);
        }

        // Shortest path routing on Service-Layer
        List<TopoEdge> azServiceLayerERO = dijkstraPCE.computeShortestPathEdges(prunedSlTopo, serviceLayerSrcNode, serviceLayerDstNode);

        if (azServiceLayerERO.isEmpty())
        {
            assert false;
        }

        // Get symmetric Service-Layer path in reverse-direction
        List<TopoEdge> zaServiceLayerERO = new LinkedList<>();

        // 1. Reverse the links
        for(TopoEdge azEdge : azServiceLayerERO)
        {
            Optional<TopoEdge> reverseEdge = prunedSlTopo.getEdges().stream()
                    .filter(r -> r.getA().equals(azEdge.getZ()))
                    .filter(r -> r.getZ().equals(azEdge.getA()))
                    .findFirst();

            if(reverseEdge.isPresent())
                zaServiceLayerERO.add(reverseEdge.get());
        }

        // 2. Reverse the order
        Collections.reverse(zaServiceLayerERO);

        assert(azServiceLayerERO.size() == zaServiceLayerERO.size());

        // Obtain physical ERO from Service-Layer EROs
        List<TopoEdge> azERO = serviceLayerTopo.getActualERO(azServiceLayerERO);
        List<TopoEdge> zaERO = serviceLayerTopo.getActualERO(zaServiceLayerERO);

        List<List<TopoEdge>> eros = new ArrayList<>();
        eros.add(azERO);
        eros.add(zaERO);

        return eros;
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

        this.buildURNList();

        serviceLayerTopo.setTopology(dummyEthernetTopo);
        serviceLayerTopo.setTopology(dummyInternalTopo);
        serviceLayerTopo.setTopology(dummyMPLSTopo);

        serviceLayerTopo.createMultilayerTopology();

        resvBW = new ArrayList<>();
        resvVLAN = new ArrayList<>();
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

    // same as buildLinearTopo(), except all devices/ports/links are on ETHERNET layer
    private void buildLinearEthernetTopo()
    {
        ethernetTopoVertices = new HashSet<>();
        mplsTopoVertices = new HashSet<>();
        internalTopoVertices = new HashSet<>();

        ethernetTopoEdges = new HashSet<>();
        mplsTopoEdges = new HashSet<>();
        internalTopoEdges = new HashSet<>();


        //Devices
        TopoVertex nodeA = new TopoVertex("switchA", VertexType.SWITCH);
        TopoVertex nodeB = new TopoVertex("switchB", VertexType.SWITCH);
        TopoVertex nodeC = new TopoVertex("switchC", VertexType.SWITCH);
        TopoVertex nodeD = new TopoVertex("switchD", VertexType.SWITCH);
        TopoVertex nodeE = new TopoVertex("switchE", VertexType.SWITCH);

        //Ports
        TopoVertex portA1 = new TopoVertex("switchA:1", VertexType.PORT);
        TopoVertex portA2 = new TopoVertex("switchA:2", VertexType.PORT);
        TopoVertex portB1 = new TopoVertex("switchB:1", VertexType.PORT);
        TopoVertex portB2 = new TopoVertex("switchB:2", VertexType.PORT);
        TopoVertex portC1 = new TopoVertex("switchC:1", VertexType.PORT);
        TopoVertex portC2 = new TopoVertex("switchC:2", VertexType.PORT);
        TopoVertex portD1 = new TopoVertex("switchD:1", VertexType.PORT);
        TopoVertex portD2 = new TopoVertex("switchD:2", VertexType.PORT);
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
        TopoEdge edgeEth_B2_C1 = new TopoEdge(portB2, portC1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_C2_D1 = new TopoEdge(portC2, portD1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_D2_E1 = new TopoEdge(portD2, portE1, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_B1_A2 = new TopoEdge(portB1, portA2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_C1_B2 = new TopoEdge(portC1, portB2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_D1_C2 = new TopoEdge(portD1, portC2, 100L, Layer.ETHERNET);
        TopoEdge edgeEth_E1_D2 = new TopoEdge(portE1, portD2, 100L, Layer.ETHERNET);

        ethernetTopoVertices.add(nodeA);
        ethernetTopoVertices.add(nodeB);
        ethernetTopoVertices.add(nodeC);
        ethernetTopoVertices.add(nodeD);
        ethernetTopoVertices.add(nodeE);
        ethernetTopoVertices.add(portA1);
        ethernetTopoVertices.add(portA2);
        ethernetTopoVertices.add(portB1);
        ethernetTopoVertices.add(portB2);
        ethernetTopoVertices.add(portC1);
        ethernetTopoVertices.add(portC2);
        ethernetTopoVertices.add(portD1);
        ethernetTopoVertices.add(portD2);
        ethernetTopoVertices.add(portE1);
        ethernetTopoVertices.add(portE2);

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
        ethernetTopoEdges.add(edgeEth_B2_C1);
        ethernetTopoEdges.add(edgeEth_C1_B2);
        ethernetTopoEdges.add(edgeEth_C2_D1);
        ethernetTopoEdges.add(edgeEth_D1_C2);
        ethernetTopoEdges.add(edgeEth_D2_E1);
        ethernetTopoEdges.add(edgeEth_E1_D2);
    }
    
    // same as buildLinearTopo(), except all devices/ports/links are on MPLS layer
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

    // same as buildLinearMPLSTopo(), except device A and it's ports/links are on ETHERNET layer
    private void buildLinearEthToMPLSTopo()
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
        TopoVertex nodeE = new TopoVertex("routerE", VertexType.ROUTER);

        //Ports
        TopoVertex portA1 = new TopoVertex("switchA:1", VertexType.PORT);
        TopoVertex portA2 = new TopoVertex("switchA:2", VertexType.PORT);
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
        TopoEdge edgeEth_A2_B1 = new TopoEdge(portA2, portB1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_B2_C1 = new TopoEdge(portB2, portC1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_C2_D1 = new TopoEdge(portC2, portD1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_D2_E1 = new TopoEdge(portD2, portE1, 100L, Layer.MPLS);
        TopoEdge edgeEth_B1_A2 = new TopoEdge(portB1, portA2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_C1_B2 = new TopoEdge(portC1, portB2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_D1_C2 = new TopoEdge(portD1, portC2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_E1_D2 = new TopoEdge(portE1, portD2, 100L, Layer.MPLS);

        ethernetTopoVertices.add(nodeA);
        ethernetTopoVertices.add(portA1);
        ethernetTopoVertices.add(portA2);

        mplsTopoVertices.add(nodeB);
        mplsTopoVertices.add(nodeC);
        mplsTopoVertices.add(nodeD);
        mplsTopoVertices.add(nodeE);
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

        ethernetTopoEdges.add(edgeEth_A2_B1);
        ethernetTopoEdges.add(edgeEth_B1_A2);

        mplsTopoEdges.add(edgeMpls_B2_C1);
        mplsTopoEdges.add(edgeMpls_C1_B2);
        mplsTopoEdges.add(edgeMpls_C2_D1);
        mplsTopoEdges.add(edgeMpls_D1_C2);
        mplsTopoEdges.add(edgeMpls_D2_E1);
        mplsTopoEdges.add(edgeMpls_E1_D2);
    }


    // same as buildLinearMPLSTopo(), except device E and it's ports/links are on ETHERNET layer
    private void buildLinearMPLSToEthTopo()
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
        TopoVertex nodeE = new TopoVertex("switchE", VertexType.SWITCH);

        //Ports
        TopoVertex portA1 = new TopoVertex("routerA:1", VertexType.PORT);
        TopoVertex portA2 = new TopoVertex("routerA:2", VertexType.PORT);
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
        TopoEdge edgeMpls_A2_B1 = new TopoEdge(portA2, portB1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_B2_C1 = new TopoEdge(portB2, portC1, 100L, Layer.MPLS);
        TopoEdge edgeMpls_C2_D1 = new TopoEdge(portC2, portD1, 100L, Layer.MPLS);
        TopoEdge edgeEth_D2_E1 = new TopoEdge(portD2, portE1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_B1_A2 = new TopoEdge(portB1, portA2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_C1_B2 = new TopoEdge(portC1, portB2, 100L, Layer.MPLS);
        TopoEdge edgeMpls_D1_C2 = new TopoEdge(portD1, portC2, 100L, Layer.MPLS);
        TopoEdge edgeEth_E1_D2 = new TopoEdge(portE1, portD2, 100L, Layer.ETHERNET);

        ethernetTopoVertices.add(nodeE);
        ethernetTopoVertices.add(portE1);
        ethernetTopoVertices.add(portE2);

        mplsTopoVertices.add(nodeA);
        mplsTopoVertices.add(nodeB);
        mplsTopoVertices.add(nodeC);
        mplsTopoVertices.add(nodeD);
        mplsTopoVertices.add(portA1);
        mplsTopoVertices.add(portA2);
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

        ethernetTopoEdges.add(edgeEth_D2_E1);
        ethernetTopoEdges.add(edgeEth_E1_D2);

        mplsTopoEdges.add(edgeMpls_A2_B1);
        mplsTopoEdges.add(edgeMpls_B1_A2);
        mplsTopoEdges.add(edgeMpls_B2_C1);
        mplsTopoEdges.add(edgeMpls_C1_B2);
        mplsTopoEdges.add(edgeMpls_C2_D1);
        mplsTopoEdges.add(edgeMpls_D1_C2);
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
        TopoEdge edgeMpls_B3_F1 = new TopoEdge(portB3, portF1, 15L, Layer.ETHERNET);
        TopoEdge edgeMpls_B4_H1 = new TopoEdge(portB4, portH1, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_C3_F2 = new TopoEdge(portC3, portF2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_C4_G1 = new TopoEdge(portC4, portG1, 15L, Layer.ETHERNET);
        TopoEdge edgeMpls_D3_G2 = new TopoEdge(portD3, portG2, 40L, Layer.ETHERNET);
        TopoEdge edgeMpls_D4_H2 = new TopoEdge(portD4, portH2, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_F1_B3 = new TopoEdge(portF1, portB3, 100L, Layer.ETHERNET);
        TopoEdge edgeMpls_F2_C3 = new TopoEdge(portF2, portC3, 15L, Layer.ETHERNET);
        TopoEdge edgeMpls_G1_C4 = new TopoEdge(portG1, portC4, 40L, Layer.ETHERNET);
        TopoEdge edgeMpls_G2_D3 = new TopoEdge(portG2, portD3, 15L, Layer.ETHERNET);
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
        bwPipe.setEroPalindromic(PalindromicType.NON_PALINDROME);
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
        bwPipe.setEroPalindromic(PalindromicType.NON_PALINDROME);
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
        bwPipe.setEroPalindromic(PalindromicType.NON_PALINDROME);
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
        bwPipe.setEroPalindromic(PalindromicType.NON_PALINDROME);
        bwPipe.setPipeType(EthPipeType.REQUESTED);

        requestedPipe = bwPipe;
    }

    private void buildDummySchedule()
    {
        Date start = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date end = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        requestedSched = ScheduleSpecificationE.builder()
                .notBefore(start)
                .notAfter(end)
                .durationMinutes(30L)
                .build();
    }

    private void buildURNList()
    {
        urnList = new ArrayList<>();

        Set<TopoVertex> allVertices = ethernetTopoVertices.stream()
                .collect(Collectors.toSet());

        mplsTopoVertices.stream()
                .forEach(allVertices::add);

        for(TopoVertex oneVert : allVertices)
        {
            Set<IntRangeE> vlanRanges = new HashSet<>();
            IntRangeE onlyVlanRange = IntRangeE.builder()
                    .ceiling(100)
                    .floor(1)
                    .build();

            vlanRanges.add(onlyVlanRange);

            ReservableBandwidthE resBW = ReservableBandwidthE.builder()
                    .ingressBw(100)
                    .egressBw(100)
                    .build();

            ReservableVlanE resVLAN = ReservableVlanE.builder()
                    .vlanRanges(vlanRanges)
                    .build();

            UrnType urnType;

            if(oneVert.getVertexType().equals(VertexType.SWITCH))
                urnType = UrnType.DEVICE;
            else if(oneVert.getVertexType().equals(VertexType.ROUTER))
                urnType = UrnType.DEVICE;
            else
                urnType = UrnType.IFCE;

            UrnE oneURN = UrnE.builder()
                    .urn(oneVert.getUrn())
                    .reservableBandwidth(resBW)
                    .reservableVlans(resVLAN)
                    .urnType(urnType)
                    .valid(true)
                    .build();

            urnList.add(oneURN);
        }
    }
}
