package net.es.oscars.ds.topo.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.topo.dao.TopologyRepository;
import net.es.oscars.ds.topo.ent.ETopology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Slf4j
@Service
@Transactional
public class TopoService {

    @Autowired
    private TopologyRepository topoRepo;

    @Autowired
    private EntityManager entityManager;

    public ETopology save(ETopology group) {
        return topoRepo.save(group);
    }



}
