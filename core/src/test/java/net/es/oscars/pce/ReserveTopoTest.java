package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.topo.Layer;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.resv.ent.ReservedResourceE;
import net.es.oscars.topo.enums.DeviceModel;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

@Slf4j
public class ReserveTopoTest {

    @Test
    public void testDecompose() {
        List<TopoEdge> edges = this.buildDecomposablePath();
        Map<String, DeviceModel> deviceModels = new HashMap<>();
        deviceModels.put("alpha", DeviceModel.JUNIPER_EX);
        deviceModels.put("bravo", DeviceModel.JUNIPER_MX);
        deviceModels.put("charlie", DeviceModel.JUNIPER_MX);
        deviceModels.put("delta", DeviceModel.JUNIPER_MX);
        PCEAssistant.decompose(null, edges, deviceModels);


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
                .intResource(100)
                .resourceType(ResourceType.VLAN)
                .build();

        reserved.add(rr);

        // first, remove 100 fom 99-100
        TopoResource avail = TopoAssistant.subtractVlan(tr, rr);
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
