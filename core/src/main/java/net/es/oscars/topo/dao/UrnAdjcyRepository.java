package net.es.oscars.topo.dao;

import net.es.oscars.topo.ent.EUrnAdjcy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrnAdjcyRepository extends CrudRepository<EUrnAdjcy, Long> {

    List<EUrnAdjcy> findAll();

}