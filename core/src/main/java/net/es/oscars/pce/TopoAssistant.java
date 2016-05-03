package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.resv.ent.ReservedResourceE;

import java.util.*;

@Slf4j
public class TopoAssistant {

    public static Set<ReservedResourceE> reservedOfAllUrnsPlusType(List<String> urns, ResourceType rt, List<ReservedResourceE> reserved) {

        assert (!urns.isEmpty());

        String oneUrn = urns.get(0);

        Set<ReservedResourceE> result = new HashSet<>();
        for (ReservedResourceE rr : reserved) {
            if (rr.getResourceType().equals(rt) && rr.getUrns().contains(oneUrn)) {
                boolean notAllFound = false;
                for (String urn : urns) {
                    if (!rr.getUrns().contains(urn)) {
                        notAllFound = true;
                    }
                }
                assert !notAllFound;
                result.add(rr);
            }
        }


        return result;

    }

    public static Optional<TopoResource> resourcefAllUrnsPlusType(List<String> urns, ResourceType rt, List<TopoResource> resources) {
        assert (!urns.isEmpty());
        assert (!resources.isEmpty());



        Optional<TopoResource> result = Optional.empty();

        String oneUrn = urns.get(0);


        for (TopoResource tr : resources) {
            if (tr.getReservableRanges().containsKey(rt) && tr.getTopoVertexUrns().contains(oneUrn)) {
                result = result.of(tr);
                break;
            }
        }

        if (result.isPresent()) {
            for (String urn : urns) {
                assert result.get().getTopoVertexUrns().contains(urn);

            }
        }


        return result;


    }



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
                            tr = subtractReserved(tr, rr, resourceType);
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


    public static TopoResource subtractBandwidth(TopoResource tr, ReservedResourceE rr) {

        Integer reservedBw = rr.getResource();
        IntRange bwQty = tr.getReservableQties().get(ResourceType.BANDWIDTH);
        Integer availBw = bwQty.getCeiling();
        bwQty.setCeiling(availBw - reservedBw);

        return tr;
    }


    public static TopoResource subtractReserved(TopoResource tr, ReservedResourceE rr, ResourceType rt) {
        Integer resource = rr.getResource();
        Set<IntRange> availVlans = new HashSet<>();

        Set<IntRange> baseRanges = tr.getReservableRanges().get(rt);

        for (IntRange range : baseRanges) {
            if (range.contains(resource)) {
                Set<IntRange> subtracted = IntRange.subtract(range, resource);
                availVlans.addAll(subtracted);
            } else {
                availVlans.add(range);
            }
        }

        tr.getReservableRanges().put(rt, availVlans);
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
