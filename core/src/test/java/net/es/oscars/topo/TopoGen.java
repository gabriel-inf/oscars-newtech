package net.es.oscars.topo;

import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.ReservableVlanE;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.*;

import java.util.*;


public class TopoGen {

    public class TopoGenResult {
        public List<UrnE> urns;
        public List<UrnAdjcyE> urnAdjcys;
        public TopoGenResult() {

        }
    }

    public TopoGen() {

    }


    public TopoGenResult singleSwitch() {
        TopoGenResult result = new TopoGenResult();
        result.urns = new ArrayList<>();
        result.urnAdjcys = new ArrayList<>();


        Set<Layer> switchCaps = new HashSet<>();
        switchCaps.add(Layer.ETHERNET);


        IntRangeE swVlanRange = IntRangeE.builder().floor(2).ceiling(100).build();
        Set<IntRangeE> swVlanRangeSet = new HashSet<>();
        swVlanRangeSet.add(swVlanRange);


        UrnE switchUrn = UrnE.builder()
                .urn("alpha")
                .urnType(UrnType.DEVICE)
                .capabilities(switchCaps)
                .deviceModel(DeviceModel.JUNIPER_EX)
                .deviceType(DeviceType.SWITCH)
                .valid(true)
                .build();

        ReservableVlanE swResvVlans = ReservableVlanE.builder()
                .vlanRanges(swVlanRangeSet)
                .build();

        switchUrn.setReservableVlans(swResvVlans);




        Set<Layer> portOneCaps = new HashSet<>();
        portOneCaps.add(Layer.ETHERNET);


        IntRangeE portOneVlanRange = IntRangeE.builder().floor(2).ceiling(100).build();
        Set<IntRangeE> portOneVlanRangeSet = new HashSet<>();
        portOneVlanRangeSet.add(portOneVlanRange);




        UrnE portOneUrn = UrnE.builder()
                .urn("alpha:1/1/1")
                .urnType(UrnType.IFCE)
                .ifceType(IfceType.PORT)
                .capabilities(portOneCaps)
                .valid(true)
                .build();

        ReservableVlanE portOneResvVlans = ReservableVlanE.builder()
                .vlanRanges(portOneVlanRangeSet)
                .build();
        portOneUrn.setReservableVlans(portOneResvVlans);




        Set<Layer> portTwoCaps = new HashSet<>();
        portTwoCaps.add(Layer.ETHERNET);


        IntRangeE portTwoVlanRange = IntRangeE.builder().floor(2).ceiling(100).build();
        Set<IntRangeE> portTwoVlanRangeSet = new HashSet<>();
        portTwoVlanRangeSet.add(portTwoVlanRange);



        UrnE portTwoUrn = UrnE.builder()
                .urn("alpha:2/1/1")
                .urnType(UrnType.IFCE)
                .ifceType(IfceType.PORT)
                .capabilities(portTwoCaps)
                .valid(true)
                .build();

        ReservableVlanE portTwoResvVlans = ReservableVlanE.builder()
                .vlanRanges(portTwoVlanRangeSet)
                .build();
        portTwoUrn.setReservableVlans(portTwoResvVlans);


        Map<Layer, Long> swToportOneMetrics = new HashMap<>();
        swToportOneMetrics.put(Layer.ETHERNET, 1L);

        UrnAdjcyE swToPortOne = UrnAdjcyE.builder()
                .a(switchUrn)
                .z(portOneUrn)
                .metrics(swToportOneMetrics)
                .build();

        Map<Layer, Long> swPortTwoMetrics = new HashMap<>();
        swPortTwoMetrics.put(Layer.ETHERNET, 1L);

        UrnAdjcyE swToPortTwo = UrnAdjcyE.builder()
                .a(switchUrn)
                .z(portTwoUrn)
                .metrics(swPortTwoMetrics)
                .build();

        result.urns.add(switchUrn);
        result.urns.add(portOneUrn);
        result.urns.add(portTwoUrn);

        result.urnAdjcys.add(swToPortOne);
        result.urnAdjcys.add(swToPortTwo);


        return result;

    }





}
