package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.resv.ent.ReservedResourceE;

import java.util.*;

@Slf4j
public class TopoAssistant {


    public static List<TopoResource> baseMinusReserved(List<TopoResource> resources, List<ReservedResourceE> reserved) {

        // look at each resource; each applies to a set of URNs
        // and consists of either:
        // bandwidth , we subtract the reserved BW from the available
        // a VLAN from which we subtract the reserved ones
        // or a set

        List<TopoResource> availableResources = new ArrayList<>();

        for (TopoResource tr : resources) {
            // for each resource, collect all the reserved things over it
            Set<ReservedResourceE> matched = reservedOn(tr, reserved);
            if (matched.isEmpty()) {
                // if nothing matched, the base is what is available
                availableResources.add(tr);

            } else {
                for (ReservedResourceE rr: matched) {
                    // now subtract the reserved from the available, depending on type
                    ResourceType resourceType = rr.getResourceType();
                    switch (resourceType) {
                        case BANDWIDTH:
                            tr = subtractBandwidth(tr, rr);
                            break;
                        case VLAN:
                            tr = subtractVlan(tr, rr);
                            break;
                        // TODO: subtract other resource types
                    }
                }
                // we have by now subtracted all the matched from the base
                availableResources.add(tr);
            }
        }

        return availableResources;
    }

    public static TopoResource subtractBandwidth(TopoResource tr, ReservedResourceE rr) {

        Integer reservedBw = rr.getIntResource();
        IntRange bwQty = tr.getReservableQties().get(ResourceType.BANDWIDTH);
        Integer availBw = bwQty.getCeiling();
        bwQty.setCeiling(availBw - reservedBw);

        return tr;
    }


    public static TopoResource subtractVlan(TopoResource tr, ReservedResourceE rr) {
        Integer vlan = rr.getIntResource();
        Set<IntRange> availVlans = new HashSet<>();

        Set<IntRange> baseVlans = tr.getReservableRanges().get(ResourceType.VLAN);

        for (IntRange range : baseVlans) {
            if (range.contains(vlan)) {
                Set<IntRange> subtracted = IntRange.subtract(range, vlan);
                availVlans.addAll(subtracted);
            } else {
                availVlans.add(range);
            }
        }

        tr.getReservableRanges().put(ResourceType.VLAN, availVlans);
        return tr;
    }


    public static Set<ReservedResourceE> reservedOn(TopoResource tr, Collection<ReservedResourceE> reserved) {
        Set<ReservedResourceE> matched = new HashSet<>();
        for (ReservedResourceE rr: reserved) {
            boolean reservedOnThis = false;

            // if any the resource URNs match any of the reserved URNs, we have a match
            for (String rrUrn : rr.getUrns()) {
                if (tr.getTopoVertexUrns().contains(rrUrn)) {
                    reservedOnThis = true;
                }
            }

            if (reservedOnThis) {
                matched.add(rr);

            }
        }
        return matched;
    }


}
