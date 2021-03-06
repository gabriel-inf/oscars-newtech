package net.es.oscars.topo.dao;

import net.es.oscars.topo.ent.UrnAdjcyE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrnAdjcyRepository extends CrudRepository<UrnAdjcyE, Long> {

    List<UrnAdjcyE> findAll();

}