package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.dao.ReservedPssResourceRepository;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class Gatherer {
    @Autowired
    private TopoService topoService;

    @Autowired
    private ReservedPssResourceRepository rrRepo;

}
