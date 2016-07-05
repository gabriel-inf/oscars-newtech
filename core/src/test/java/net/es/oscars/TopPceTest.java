package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.Layer3FixtureType;
import net.es.oscars.dto.pss.Layer3JunctionType;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.*;
import net.es.oscars.topo.enums.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by jeremy on 6/30/16.
 *
 * Tests End-to-End correctness of the PCE modules
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class TopPceTest
{
    @Autowired
    private TopPCE topPCE;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    private RequestedBlueprintE requestedBlueprint;
    private ReservedBlueprintE reservedBlueprint;
    private ScheduleSpecificationE requestedSched;

    List<UrnE> urnList;
    List<UrnAdjcyE> adjcyList;

    @Test
    public void simpleTest()
    {
        log.info("Initializing test: 'simpleTest'.");

        this.buildSimpleTopo();
        this.buildDummySchedule();
        this.buildRequest();

        log.info("Beginning test: 'simpleTest'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException pceE){ log.error("", pceE); }
        catch(PSSException pssE){ log.error("", pssE); }

        assert(reservedBlueprint != null);

        log.info("test 'simpleTest' passed.");
    }

    private void buildSimpleTopo()
    {
        log.info("Building Test Topology");

        urnRepo.deleteAll();
        adjcyRepo.deleteAll();

        urnList = new ArrayList<>();
        adjcyList = new ArrayList<>();

        TopoVertex nodeK = new TopoVertex("nodeK", VertexType.SWITCH);
        TopoVertex nodeA = new TopoVertex("portA", VertexType.PORT);
        TopoVertex nodeZ = new TopoVertex("portZ", VertexType.PORT);

        //Internal Links
        TopoEdge edgeInt_A_K = new TopoEdge(nodeA, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_Z_K = new TopoEdge(nodeZ, nodeK, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_A = new TopoEdge(nodeK, nodeA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_K_Z = new TopoEdge(nodeK, nodeZ, 0L, Layer.INTERNAL);

        List<TopoVertex> topoNodes = new ArrayList<>();
        topoNodes.add(nodeK);
        topoNodes.add(nodeA);
        topoNodes.add(nodeZ);

        List<TopoEdge> topoLinks = new ArrayList<>();
        topoLinks.add(edgeInt_A_K);
        topoLinks.add(edgeInt_Z_K);
        topoLinks.add(edgeInt_K_A);
        topoLinks.add(edgeInt_K_Z);


        for(TopoVertex oneNode : topoNodes)
        {
            UrnType urnType;
            DeviceType deviceType = null;
            DeviceModel deviceModel = null;

            if(oneNode.getVertexType().equals(VertexType.SWITCH))
            {
                urnType = UrnType.DEVICE;
                deviceType = DeviceType.SWITCH;
                deviceModel = DeviceModel.JUNIPER_EX;
            }
            else if(oneNode.getVertexType().equals(VertexType.ROUTER))
            {
                urnType = UrnType.DEVICE;
                deviceType = DeviceType.ROUTER;
                deviceModel = DeviceModel.JUNIPER_MX;
            }
            else
            {
                urnType = UrnType.IFCE;

                VertexType deviceVertType = null;
                for(TopoEdge oneEdge : topoLinks)
                {
                    if(oneEdge.getA().getUrn().equals(oneNode.getUrn()))
                    {
                        deviceVertType = oneEdge.getZ().getVertexType();
                        break;
                    }
                }

                assert(deviceVertType != null);

                if(deviceVertType.equals(VertexType.SWITCH))
                {
                    deviceModel = DeviceModel.JUNIPER_EX;
                }
                else
                {
                    deviceModel = DeviceModel.JUNIPER_MX;
                }
            }

            IntRangeE onlyVlanRange = IntRangeE.builder()
                    .ceiling(100)
                    .floor(1)
                    .build();

            Set<IntRangeE> vlanRanges = new HashSet<>();
            vlanRanges.add(onlyVlanRange);

            ReservableBandwidthE resBW = ReservableBandwidthE.builder()
                    .ingressBw(100)
                    .egressBw(100)
                    .build();

            ReservableVlanE resVLAN = ReservableVlanE.builder()
                    .vlanRanges(vlanRanges)
                    .build();

            UrnE oneURN = UrnE.builder()
                    .urn(oneNode.getUrn())
                    .reservableBandwidth(resBW)
                    .reservableVlans(resVLAN)
                    .urnType(urnType)
                    .valid(true)
                    .build();

            if(deviceType == null)
            {
                oneURN.setIfceType(IfceType.PORT);
                oneURN.setDeviceModel(deviceModel);
            }
            else
            {
                oneURN.setDeviceType(deviceType);
                oneURN.setDeviceModel(deviceModel);
            }

            urnList.add(oneURN);

            log.info("Added Node: " + oneNode.getUrn());
        }

        for(TopoEdge oneLink : topoLinks)
        {
            Map<Layer, Long> linkMetrics = new HashMap<>();
            linkMetrics.put(oneLink.getLayer(), oneLink.getMetric());

            UrnE urnA = null;
            UrnE urnZ = null;

            boolean aFound = false;
            boolean zFound = false;

            for(UrnE oneURN : urnList)
            {
                if(aFound && zFound)
                    break;

                if(oneURN.getUrn().equals(oneLink.getA().getUrn()))
                {
                    urnA = oneURN;
                    aFound = true;
                    continue;
                }

                if(oneURN.getUrn().equals(oneLink.getZ().getUrn()))
                {
                    urnZ = oneURN;
                    zFound = true;
                    continue;
                }
            }

            assert(urnA != null);
            assert(urnZ != null);

            UrnAdjcyE oneAdjcy = UrnAdjcyE.builder()
                    .a(urnA)
                    .z(urnZ)
                    .metrics(linkMetrics)
                    .build();

            adjcyList.add(oneAdjcy);

            log.info("Added Link: (" + urnA.getUrn() + "," + urnZ.getUrn() + ")");
        }

        urnRepo.save(urnList);
        adjcyRepo.save(adjcyList);
    }




    private void buildDummySchedule()
    {
        log.info("Building Test Request Schedule");

        Date start = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date end = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        requestedSched = ScheduleSpecificationE.builder()
                .notBefore(start)
                .notAfter(end)
                .durationMinutes(30L)
                .build();
    }


    private void buildRequest()
    {
        log.info("Building Test Request");

        Set<Layer3FlowE> l3Flows = new HashSet<>();
        Set<Layer3JunctionE> l3Junx = new HashSet<>();
        Set<Layer3PipeE> l3Pipes = new HashSet<>();
        Set<Layer3FixtureE> l3Fixes = new HashSet<>();

        UrnE urnA = null;
        UrnE urnZ = null;
        UrnE urnJ1 = null;

        boolean aFound = false;
        boolean zFound = false;
        boolean j1Found = false;

        for(UrnE oneURN : urnList)
        {
            if(aFound && zFound && j1Found)
                break;

            if(oneURN.getUrn().equals("portA"))
            {
                urnA = oneURN;
                aFound = true;
                continue;
            }

            if(oneURN.getUrn().equals("portZ"))
            {
                urnZ = oneURN;
                zFound = true;
                continue;
            }

            if(oneURN.getUrn().equals("nodeK"))
            {
                urnJ1 = oneURN;
                j1Found = true;
                continue;
            }
        }

        assert(urnA != null);
        assert(urnZ != null);
        assert(urnJ1 != null);



        Layer3FixtureE l3FixA = Layer3FixtureE.builder()
                .portUrn("portA")
                .inMbps(10)
                .egMbps(10)
                .fixtureType(Layer3FixtureType.REQUESTED)
                .build();

        Layer3FixtureE l3FixZ = Layer3FixtureE.builder()
                .portUrn("portZ")
                .inMbps(10)
                .egMbps(10)
                .fixtureType(Layer3FixtureType.REQUESTED)
                .build();

        l3Fixes.add(l3FixA);
        l3Fixes.add(l3FixZ);

        Set<String> resources = new HashSet<>();

        Layer3JunctionE l3Junc = Layer3JunctionE.builder()
                .deviceUrn("nodeK")
                .fixtures(l3Fixes)
                .junctionType(Layer3JunctionType.REQUESTED)
                .resourceIds(resources)
                .build();

        l3Junx.add(l3Junc);

        Layer3FlowE l3Flow = Layer3FlowE.builder()
                .junctions(l3Junx)
                .pipes(l3Pipes)
                .build();

        l3Flows.add(l3Flow);


        Set<RequestedVlanFlowE> vlanFlows = new HashSet<>();
        Set<RequestedVlanJunctionE> vlanJunx = new HashSet<>();
        Set<RequestedVlanPipeE> vlanPipes = new HashSet<>();
        Set<RequestedVlanFixtureE> vlanFixes = new HashSet<>();

        RequestedVlanFixtureE requestedFixA = RequestedVlanFixtureE.builder()
                .portUrn(urnA)
                .inMbps(10)
                .egMbps(10)
                .fixtureType(EthFixtureType.REQUESTED)
                .build();

        RequestedVlanFixtureE requestedFixZ = RequestedVlanFixtureE.builder()
                .portUrn(urnZ)
                .inMbps(10)
                .egMbps(10)
                .fixtureType(EthFixtureType.REQUESTED)
                .build();

        vlanFixes.add(requestedFixA);
        vlanFixes.add(requestedFixZ);

        RequestedVlanJunctionE requestedJunc = RequestedVlanJunctionE.builder()
                .deviceUrn(urnJ1)
                .junctionType(EthJunctionType.REQUESTED)
                .fixtures(vlanFixes)
                .build();

        vlanJunx.add(requestedJunc);

        RequestedVlanFlowE requestedFlow = RequestedVlanFlowE.builder()
                .junctions(vlanJunx)
                .pipes(vlanPipes)
                .build();

        vlanFlows.add(requestedFlow);

        requestedBlueprint = RequestedBlueprintE.builder()
                .vlanFlows(vlanFlows)
                .layer3Flows(l3Flows)
                .build();
    }
}
