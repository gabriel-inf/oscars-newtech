package net.es.oscars.resv.dao;

import net.es.oscars.resv.ent.ReservedPssResourceE;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface ReservedPssResourceRepository extends CrudRepository<ReservedPssResourceE, Long> {

    List<ReservedPssResourceE> findAll();

    @Query(value = "SELECT rs FROM ReservedPssResourceE rs WHERE (rs.ending >= ?1 AND rs.beginning <= ?2)")
    Optional<List<ReservedPssResourceE>> findOverlappingInterval(Instant period_start, Instant period_end);


}