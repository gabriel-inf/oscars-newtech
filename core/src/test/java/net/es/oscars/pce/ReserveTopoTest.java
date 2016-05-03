package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.spec.VlanFixture;
import net.es.oscars.dto.spec.VlanJunction;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.ReservedResourceE;
import net.es.oscars.spec.ent.VlanFixtureE;
import net.es.oscars.spec.ent.VlanJunctionE;
import net.es.oscars.spec.ent.VlanPipeE;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.enums.DeviceModel;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

@Slf4j
public class ReserveTopoTest {

    @Test
    public void decideVCIDTest() throws PCEException {

        List<String> urns = new ArrayList<>();
        urns.add("alpha");
        urns.add("bravo");

        List<String> badUrns = new ArrayList<>();
        badUrns.add("charlie");

        List<ReservedResourceE> rrs = new ArrayList<>();


        rrs.add(ReservedResourceE.builder()
                .resource(100)
                .beginning(Instant.MIN)
                .ending(Instant.MAX)
                .resourceType(ResourceType.VC_ID)
                .urns(urns)
                .build());

        rrs.add(ReservedResourceE.builder()
                .resource(101)
                .beginning(Instant.MIN)
                .ending(Instant.MAX)
                .resourceType(ResourceType.VC_ID)
                .urns(urns)
                .build());


        Set<ReservedResourceE> allUrnsResv = TopoAssistant.reservedOfAllUrnsPlusType(urns, ResourceType.VC_ID, rrs);
        assert allUrnsResv.size() == 2;



    }


    @Test
    public void testDecompose() throws PSSException {
        List<TopoEdge> edges = this.buildDecomposablePath();
        Map<String, DeviceModel> deviceModels = new HashMap<>();
        deviceModels.put("alpha", DeviceModel.JUNIPER_EX);
        deviceModels.put("bravo", DeviceModel.JUNIPER_MX);
        deviceModels.put("charlie", DeviceModel.JUNIPER_MX);
        deviceModels.put("delta", DeviceModel.JUNIPER_MX);
        List<Map<Layer, List<TopoEdge>>>  segments = PCEAssistant.decompose(edges, deviceModels);
        log.info(segments.toString());

        assert segments.size() == 2;
        assert segments.get(0).size() == 1;
        assert segments.get(0).containsKey(Layer.ETHERNET);
        assert segments.get(0).get(Layer.ETHERNET).size() == 2;
        assert segments.get(0).get(Layer.ETHERNET).get(0).getA().getUrn().equals("alpha");
        assert segments.get(0).get(Layer.ETHERNET).get(0).getZ().getUrn().equals("alpha:1/1/1");

        assert segments.get(0).get(Layer.ETHERNET).get(1).getA().getUrn().equals("alpha:1/1/1");
        assert segments.get(0).get(Layer.ETHERNET).get(1).getZ().getUrn().equals("bravo:1/1/1");


        assert segments.get(1).size() == 1;
        assert segments.get(1).containsKey(Layer.MPLS);
        assert segments.get(1).get(Layer.MPLS).size() == 7;

        assert segments.get(1).get(Layer.MPLS).get(0).getA().getUrn().equals("bravo:1/1/1");
        assert segments.get(1).get(Layer.MPLS).get(0).getZ().getUrn().equals("bravo");

        assert segments.get(1).get(Layer.MPLS).get(1).getA().getUrn().equals("bravo");
        assert segments.get(1).get(Layer.MPLS).get(1).getZ().getUrn().equals("bravo:2/1/1");

        assert segments.get(1).get(Layer.MPLS).get(2).getA().getUrn().equals("bravo:2/1/1");
        assert segments.get(1).get(Layer.MPLS).get(2).getZ().getUrn().equals("charlie:2/1/1");

        assert segments.get(1).get(Layer.MPLS).get(3).getA().getUrn().equals("charlie:2/1/1");
        assert segments.get(1).get(Layer.MPLS).get(3).getZ().getUrn().equals("charlie");

        assert segments.get(1).get(Layer.MPLS).get(4).getA().getUrn().equals("charlie");
        assert segments.get(1).get(Layer.MPLS).get(4).getZ().getUrn().equals("charlie:1/1/1");

        assert segments.get(1).get(Layer.MPLS).get(5).getA().getUrn().equals("charlie:1/1/1");
        assert segments.get(1).get(Layer.MPLS).get(5).getZ().getUrn().equals("delta:1/1/1");

        assert segments.get(1).get(Layer.MPLS).get(6).getA().getUrn().equals("delta:1/1/1");
        assert segments.get(1).get(Layer.MPLS).get(6).getZ().getUrn().equals("delta");


        List<TopoEdge> ethSegment = segments.get(0).get(Layer.ETHERNET);
        VlanJunctionE aJunction = VlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn("alpha")
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .build();

        VlanFixtureE aFixture = VlanFixtureE.builder()
                .inMbps(10)
                .egMbps(10)
                .fixtureType(EthFixtureType.REQUESTED)
                .portUrn("alpha:0/1/0")
                .vlanExpression("")
                .vlanId(null)
                .build();
        aJunction.getFixtures().add(aFixture);

        PCEAssistant asst = new PCEAssistant();
        List<VlanJunctionE> ethJunctions = asst.makeEthernetJunctions(ethSegment, 10, 10,
                Optional.of(aJunction), Optional.empty(),deviceModels );

        assert ethJunctions.size() == 1;

        VlanJunctionE vj = ethJunctions.get(0);
        assert vj.getDeviceUrn().equals("alpha");
        assert vj.getJunctionType().equals(EthJunctionType.JUNOS_SWITCH);
        Set<VlanFixtureE> fixtures = vj.getFixtures();
        assert fixtures.size() == 2;

        for (VlanFixtureE fx : fixtures) {
            assert fx.getPortUrn().equals("alpha:1/1/1") || fx.getPortUrn().equals("alpha:0/1/0");
            assert fx.getFixtureType().equals(EthFixtureType.JUNOS_IFCE);
        }

        List<TopoEdge> mplsSegment = segments.get(1).get(Layer.MPLS);



        VlanJunctionE zJunction = VlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn("delta")
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .build();

        VlanFixtureE zFixture = VlanFixtureE.builder()
                .inMbps(10)
                .egMbps(10)
                .fixtureType(EthFixtureType.REQUESTED)
                .portUrn("delta:0/1/0")
                .vlanExpression("")
                .vlanId(null)
                .build();
        zJunction.getFixtures().add(zFixture);

        VlanPipeE vp = asst.makeVplsPipe(mplsSegment, 10, 10, Optional.empty(), Optional.of(zJunction), deviceModels);
        List<String> azEro = vp.getAzERO();
        List<String> zaEro = vp.getZaERO();
        log.info(azEro.toString());
        log.info(zaEro.toString());

        assert azEro.size() == 7;
        assert azEro.get(0).equals("bravo");
        assert azEro.get(6).equals("delta");
        assert zaEro.size() == 7;
        assert zaEro.get(0).equals("delta");
        assert zaEro.get(6).equals("bravo");



        assert vp.getPipeType().equals(EthPipeType.JUNOS_TO_JUNOS_VPLS);

        assert vp.getAJunction().getDeviceUrn().equals("bravo");

        assert vp.getAJunction().getFixtures().size() == 1;
        VlanFixtureE fx = vp.getAJunction().getFixtures().iterator().next();
        assert fx.getPortUrn().equals("bravo:1/1/1");
        assert fx.getFixtureType().equals(EthFixtureType.JUNOS_IFCE);


        assert vp.getZJunction().getFixtures().size() == 1;
        fx = vp.getZJunction().getFixtures().iterator().next();
        assert fx.getPortUrn().equals("delta:0/1/0");
        assert fx.getFixtureType().equals(EthFixtureType.JUNOS_IFCE);

    }


    @Test
    public void testEroFromTopoEdge() {
        List<TopoEdge> edges = this.buildAbcEro();


        List<String> ero = TopoAssistant.makeEro(edges, false);
        assert ero.size() == 3;
        assert ero.get(0).equals("alpha");
        assert ero.get(1).equals("bravo");
        assert ero.get(2).equals("charlie");

        ero = TopoAssistant.makeEro(edges, true);
        assert ero.size() == 3;
        assert ero.get(0).equals("charlie");
        assert ero.get(1).equals("bravo");
        assert ero.get(2).equals("alpha");


    }

    @Test
    public void testSubtract() {

        List<String> resourceUrns = new ArrayList<>();
        resourceUrns.add("alpha:1");
        resourceUrns.add("alpha:2");

        TopoResource tr = TopoResource.builder()
                .reservableQties(new HashMap<>())
                .reservableRanges(new HashMap<>())
                .topoVertexUrns(resourceUrns)
                .build();

        Set<IntRange> vlanRanges = new HashSet<>();
        vlanRanges.add(IntRange.builder().floor(99).ceiling(100).build());
        tr.getReservableRanges().put(ResourceType.VLAN, vlanRanges);


        List<String> reservedUrns = new ArrayList<>();
        resourceUrns.add("alpha:1");
        resourceUrns.add("beta:1");

        Set<ReservedResourceE> reserved = new HashSet<>();


        ReservedResourceE rr = ReservedResourceE.builder()
                .urns(reservedUrns)
                .beginning(Instant.MIN)
                .ending(Instant.MAX)
                .resource(100)
                .resourceType(ResourceType.VLAN)
                .build();

        reserved.add(rr);

        // first, remove 100 fom 99-100
        TopoResource avail = TopoAssistant.subtractReserved(tr, rr,ResourceType.VLAN);
        assert avail.getReservableRanges().get(ResourceType.VLAN).size() == 1;
        assert avail.getReservableRanges().get(ResourceType.VLAN).iterator().next().contains(99);
    }

    @Test
    public void testIntRangeParsing() {
        assert IntRangeParsing.isValidIntRangeInput("1");
        assert IntRangeParsing.isValidIntRangeInput("2:5");
        assert IntRangeParsing.isValidIntRangeInput("2,2:5");
        assert IntRangeParsing.isValidIntRangeInput("2:5,6");
        assert IntRangeParsing.isValidIntRangeInput("2:5,3:7");
        assert IntRangeParsing.isValidIntRangeInput("1:2,2:5");
        assert !IntRangeParsing.isValidIntRangeInput("1:2,2:a");

        List<IntRange> ranges;
        IntRange range;

        ranges = IntRangeParsing.retrieveIntRanges("2:12");
        assert ranges.size() == 1;
        range = ranges.get(0);
        assert range.getFloor() == 2;
        assert range.getCeiling() == 12;

        ranges = IntRangeParsing.retrieveIntRanges("2:12,3");
        assert ranges.size() == 1;
        range = ranges.get(0);
        assert range.getFloor() == 2;
        assert range.getCeiling() == 12;

        ranges = IntRangeParsing.retrieveIntRanges("2:12,13:14");
        assert ranges.size() == 1;
        range = ranges.get(0);
        assert range.getFloor() == 2;
        assert range.getCeiling() == 14;

        ranges = IntRangeParsing.retrieveIntRanges("2:12,14");
        assert ranges.size() == 2;
        range = ranges.get(0);
        assert range.getFloor() == 2;
        assert range.getCeiling() == 12;

        range = ranges.get(1);
        log.info(range.toString());
        assert range.getFloor() == 14;
        assert range.getCeiling() == 14;



    }

    @Test
    public void testIntRangeMerging(){
        IntRange onethree = IntRange.builder().floor(1).ceiling(3).build();
        IntRange twofive = IntRange.builder().floor(2).ceiling(5).build();
        IntRange six = IntRange.builder().floor(6).ceiling(6).build();
        IntRange ninetynine = IntRange.builder().floor(99).ceiling(99).build();

        List<IntRange> testList;
        List<IntRange> result;

        testList = new ArrayList<>();
        testList.add(onethree);
        testList.add(twofive);
        result = IntRangeParsing.mergeIntRanges(testList);
        assert result.size() == 1;
        assert result.get(0).getFloor() == 1;
        assert result.get(0).getCeiling() == 5;

        testList = new ArrayList<>();
        testList.add(six);
        testList.add(twofive);
        result = IntRangeParsing.mergeIntRanges(testList);
        assert result.size() == 1;
        assert result.get(0).getFloor() == 2;
        assert result.get(0).getCeiling() == 6;

        testList = new ArrayList<>();
        testList.add(onethree);
        testList.add(ninetynine);
        result = IntRangeParsing.mergeIntRanges(testList);
        assert result.size() == 2;
        assert result.get(0).getFloor() == 1;
        assert result.get(0).getCeiling() == 3;
        assert result.get(1).getFloor() == 99;
        assert result.get(1).getCeiling() == 99;


        testList = new ArrayList<>();
        testList.add(onethree);
        testList.add(six);
        result = IntRangeParsing.mergeIntRanges(testList);
        assert result.size() == 2;
        assert result.get(0).getFloor() == 1;
        assert result.get(0).getCeiling() == 3;
        assert result.get(1).getFloor() == 6;
        assert result.get(1).getCeiling() == 6;



    }

    public List<TopoEdge> buildAbcEro() {
        TopoEdge ab = TopoEdge.builder()
                .a(TopoVertex.builder().urn("alpha").build())
                .z(TopoVertex.builder().urn("bravo").build())
                .layer(Layer.ETHERNET)
                .metric(100L)
                .build();

        TopoEdge bc = TopoEdge.builder()
                .a(TopoVertex.builder().urn("bravo").build())
                .z(TopoVertex.builder().urn("charlie").build())
                .layer(Layer.ETHERNET)
                .metric(100L)
                .build();

        List<TopoEdge> edges = new ArrayList<>();
        edges.add(ab);
        edges.add(bc);

        return edges;
    }


    public List<TopoEdge> buildDecomposablePath() {
        TopoEdge a_to_a_one = TopoEdge.builder()
                .a(TopoVertex.builder().urn("alpha").build())
                .z(TopoVertex.builder().urn("alpha:1/1/1").build())
                .layer(Layer.INTERNAL)
                .metric(1L)
                .build();

        TopoEdge a_one_to_b_one = TopoEdge.builder()
                .a(TopoVertex.builder().urn("alpha:1/1/1").build())
                .z(TopoVertex.builder().urn("bravo:1/1/1").build())
                .layer(Layer.ETHERNET)
                .metric(100L)
                .build();

        TopoEdge b_one_to_b = TopoEdge.builder()
                .a(TopoVertex.builder().urn("bravo:1/1/1").build())
                .z(TopoVertex.builder().urn("bravo").build())
                .layer(Layer.INTERNAL)
                .metric(100L)
                .build();

        TopoEdge b_to_b_two = TopoEdge.builder()
                .a(TopoVertex.builder().urn("bravo").build())
                .z(TopoVertex.builder().urn("bravo:2/1/1").build())
                .layer(Layer.INTERNAL)
                .metric(100L)
                .build();

        TopoEdge b_two_to_c_two = TopoEdge.builder()
                .a(TopoVertex.builder().urn("bravo:2/1/1").build())
                .z(TopoVertex.builder().urn("charlie:2/1/1").build())
                .layer(Layer.MPLS)
                .metric(100L)
                .build();

        TopoEdge c_two_to_c = TopoEdge.builder()
                .a(TopoVertex.builder().urn("charlie:2/1/1").build())
                .z(TopoVertex.builder().urn("charlie").build())
                .layer(Layer.INTERNAL)
                .metric(100L)
                .build();

        TopoEdge c_to_c_one = TopoEdge.builder()
                .a(TopoVertex.builder().urn("charlie").build())
                .z(TopoVertex.builder().urn("charlie:1/1/1").build())
                .layer(Layer.INTERNAL)
                .metric(100L)
                .build();

        TopoEdge c_one_to_d_one = TopoEdge.builder()
                .a(TopoVertex.builder().urn("charlie:1/1/1").build())
                .z(TopoVertex.builder().urn("delta:1/1/1").build())
                .layer(Layer.MPLS)
                .metric(100L)
                .build();

        TopoEdge d_one_to_d = TopoEdge.builder()
                .a(TopoVertex.builder().urn("delta:1/1/1").build())
                .z(TopoVertex.builder().urn("delta").build())
                .layer(Layer.INTERNAL)
                .metric(100L)
                .build();




        List<TopoEdge> edges = new ArrayList<>();
        edges.add(a_to_a_one);
        edges.add(a_one_to_b_one);
        edges.add(b_one_to_b);
        edges.add(b_to_b_two);
        edges.add(b_two_to_c_two);
        edges.add(c_two_to_c);
        edges.add(c_to_c_one);
        edges.add(c_one_to_d_one);
        edges.add(d_one_to_d);

        return edges;
    }

}
