package net.es.oscars.ds.resv.dao;

import net.es.oscars.ds.resv.ent.EReservation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ResvRepository extends RevisionRepository<EReservation, Long, Integer>, CrudRepository<EReservation, Long> {

    List<EReservation> findAll();
    Optional<EReservation> findByGri(String gri);
    List<EReservation> findByStatesResv(String resvState);


}