package net.es.oscars.ds.topo.dao;

import net.es.oscars.ds.topo.ent.EUrnAdjcy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrnAdjcyRepository extends RevisionRepository<EUrnAdjcy, Long, Integer>, CrudRepository<EUrnAdjcy, Long> {

    List<EUrnAdjcy> findAll();

}