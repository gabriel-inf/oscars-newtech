package net.es.oscars.resv.dao;

import net.es.oscars.resv.ent.ReservedVlanE;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface ReservedVlanRepository extends CrudRepository<ReservedVlanE, Long> {

    List<ReservedVlanE> findAll();

    @Query(value = "SELECT rs FROM ReservedVlanE rs WHERE (rs.ending >= ?1 AND rs.beginning <= ?2)")
    Optional<List<ReservedVlanE>> findOverlappingInterval(Instant period_start, Instant period_end);


}