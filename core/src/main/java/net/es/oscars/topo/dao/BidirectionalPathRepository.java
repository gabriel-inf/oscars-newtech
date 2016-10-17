package net.es.oscars.topo.dao;

import net.es.oscars.topo.ent.BidirectionalPathE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidirectionalPathRepository extends CrudRepository<BidirectionalPathE, Long> {

    List<BidirectionalPathE> findAll();
}
