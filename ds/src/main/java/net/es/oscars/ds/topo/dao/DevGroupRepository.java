package net.es.oscars.ds.topo.dao;

import net.es.oscars.ds.topo.ent.EDevGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevGroupRepository extends RevisionRepository<EDevGroup, Long, Integer>, CrudRepository<EDevGroup, Long> {

    List<EDevGroup> findAll();
    Optional<EDevGroup> findByName(String name);


}