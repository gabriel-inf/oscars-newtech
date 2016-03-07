package net.es.oscars.ds.resv.dao;

import net.es.oscars.ds.resv.ent.EReservedInteger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReservedIntRepository extends CrudRepository<EReservedInteger, Long> {

    List<EReservedInteger> findAll();
    Optional<List<EReservedInteger>> findByUrn(String urn);


}