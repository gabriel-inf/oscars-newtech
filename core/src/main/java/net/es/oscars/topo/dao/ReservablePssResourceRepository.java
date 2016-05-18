package net.es.oscars.topo.dao;

import net.es.oscars.topo.ent.ReservablePssResourceE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReservablePssResourceRepository extends CrudRepository<ReservablePssResourceE, Long> {

    List<ReservablePssResourceE> findAll();


}