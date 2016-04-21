package net.es.oscars.resv.dao;

import net.es.oscars.resv.ent.ReservedResourceE;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface ReservedResourceRepository extends CrudRepository<ReservedResourceE, Long> {

    List<ReservedResourceE> findAll();

    @Query(value = "SELECT rs FROM ReservedResourceE rs WHERE (rs.ending >= ?1 AND rs.beginning <= ?2)")
    Optional<List<ReservedResourceE>> findOverlappingInterval(Instant period_start, Instant period_end);


}