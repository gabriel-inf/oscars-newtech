package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.Interval;
import net.es.oscars.dto.rsrc.TopoResource;
import net.es.oscars.resv.dao.ReservedResourceRepository;
import net.es.oscars.resv.ent.ReservedResourceE;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Slf4j
public class Gatherer {
    @Autowired
    private TopoService topoService;

    @Autowired
    private ReservedResourceRepository rrRepo;

    public List<TopoResource> availableThroughoutInterval(Interval interval) {

        // these are the base resources that are available on the topology
        // and defined as path-constraining
        List<TopoResource> resources = topoService.constraining();

        // these are the reserved resources over that interval
        List<ReservedResourceE> reservedOverInterval = rrRepo
                .findOverlappingInterval(interval.getBeginning(), interval.getEnding())
                .orElse(new ArrayList<>());

        return TopoAssistant.baseMinusReserved(resources, reservedOverInterval);
    }


}
