package net.es.oscars.ds.topo.dao;

import net.es.oscars.ds.topo.ent.ETopology;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopologyRepository extends RevisionRepository<ETopology, Long, Integer>, CrudRepository<ETopology, Long> {

    List<ETopology> findAll();


}