package net.es.oscars.topo.dao;

import net.es.oscars.topo.ent.ReservableVlanE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReservableVlanRepository extends CrudRepository<ReservableVlanE, Long> {

    List<ReservableVlanE> findAll();



}