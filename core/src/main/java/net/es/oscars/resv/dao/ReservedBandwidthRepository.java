package net.es.oscars.resv.dao;

import net.es.oscars.resv.ent.ReservedBandwidthE;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface ReservedBandwidthRepository extends CrudRepository<ReservedBandwidthE, Long> {

    List<ReservedBandwidthE> findAll();

    @Query(value = "SELECT rs FROM ReservedBandwidthE rs WHERE (rs.ending >= ?1 AND rs.beginning <= ?2)")
    Optional<List<ReservedBandwidthE>> findOverlappingInterval(Instant period_start, Instant period_end);


}