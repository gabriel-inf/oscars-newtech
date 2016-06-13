package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class PruningService {

    @Autowired
    private TopoService topoService;

    public Topology pruneTopology(Topology topo, Integer azBw, Integer zaBw){
        Topology pruned = new Topology();
        pruned.setLayer(topo.getLayer());
        pruned.setVertices(topo.getVertices());
        List<ReservableBandwidthE> bandwidths = topoService.reservableBandwidths();
        Set<TopoEdge> availableEdges = topo.getEdges().stream().filter(e -> availableBW(e, azBw, zaBw, bandwidths))
                .collect(Collectors.toSet());
        pruned.setEdges(availableEdges);
        return pruned;
    }

    private boolean availableBW(TopoEdge edge, Integer azBw, Integer zaBw, List<ReservableBandwidthE> bandwidths){
        String aUrn = edge.getA().getUrn();
        String zUrn = edge.getZ().getUrn();
        log.debug("checking if " + aUrn + " and " + zUrn + " have enough bandwidth ");
        List<ReservableBandwidthE> aMatching = bandwidths.stream().filter(bw -> bw.getUrn().getUrn().equals(aUrn))
                .collect(Collectors.toList());
        List<ReservableBandwidthE> zMatching = bandwidths.stream().filter(bw -> bw.getUrn().getUrn().equals(zUrn))
                .collect(Collectors.toList());

        assert aMatching.size() <= 1 && zMatching.size() <=1;
        if (aMatching.isEmpty() || zMatching.isEmpty()) {
            log.info("bandwidth does not apply to " + aUrn + " or " + zUrn);
            return true;
        } else {
            return aMatching.get(0).getEgressBw() >= azBw && aMatching.get(0).getIngressBw() >= zaBw
                    && zMatching.get(0).getIngressBw() >= azBw && zMatching.get(0).getEgressBw() >= zaBw;

        }
    }

}
