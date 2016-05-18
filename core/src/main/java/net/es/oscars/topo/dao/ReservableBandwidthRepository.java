package net.es.oscars.topo.dao;

import net.es.oscars.topo.ent.ReservableBandwidthE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReservableBandwidthRepository extends CrudRepository<ReservableBandwidthE, Long> {

    List<ReservableBandwidthE> findAll();



}