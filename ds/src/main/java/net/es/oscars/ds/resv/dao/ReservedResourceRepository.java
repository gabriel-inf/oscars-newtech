package net.es.oscars.ds.resv.dao;

import net.es.oscars.ds.resv.ent.EReservedResource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface ReservedResourceRepository extends CrudRepository<EReservedResource, Long> {

    List<EReservedResource> findAll();

    @Query(value = "SELECT rs FROM EReservedResource rs WHERE (rs.ending >= ?1 AND rs.beginning <= ?2)")
    Optional<List<EReservedResource>> findOverlappingInterval(Instant period_start, Instant period_end);


}