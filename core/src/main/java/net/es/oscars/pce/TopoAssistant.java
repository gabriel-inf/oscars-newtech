package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.rsrc.ReservableVlan;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.resv.ent.ReservedVlanE;

import java.util.*;

@Slf4j
public class TopoAssistant {


    public static List<String> makeEro(List<TopoEdge> topoEdges, boolean reverse) {

        List<String> ero = new ArrayList<>();
        // all the As plus the last Z
        topoEdges.stream().forEach(t -> ero.add(t.getA().getUrn()));
        ero.add(topoEdges.get(topoEdges.size()-1).getZ().getUrn());

        if (reverse) {
            Collections.reverse(ero);
        }

        log.debug("ERO: "+ero.toString());
        return ero;
    }




    public static ReservableVlan subtractVlan(ReservableVlan reservable, ReservedVlanE reserved) {

        Set<IntRange> availVlans = new HashSet<>();

        Set<IntRange> baseRanges = reservable.getVlanRanges();

        Integer vlan = reserved.getVlan();

        for (IntRange range : baseRanges) {
            if (range.contains(vlan)) {
                Set<IntRange> subtracted = IntRange.subtract(range, vlan);
                availVlans.addAll(subtracted);
            } else {
                availVlans.add(range);
            }
        }

        ReservableVlan result = ReservableVlan.builder()
                .topoVertexUrn(reservable.getTopoVertexUrn())
                .vlanRanges(availVlans)
                .build();

        return result;
    }



}
