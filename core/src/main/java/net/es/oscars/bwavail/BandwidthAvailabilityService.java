package net.es.oscars.bwavail;

import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.pce.DijkstraPCE;
import net.es.oscars.pce.PruningService;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BandwidthAvailabilityService {

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    @Autowired
    private TopoService topoService;

    public BandwidthAvailabilityResponse getBandwidthAvailabilityMap(BandwidthAvailabilityRequest request){

        return BandwidthAvailabilityResponse.builder()
                .build();
    }
}
