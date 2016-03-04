package net.es.oscars.ds.resv.dao;

import net.es.oscars.ds.resv.ent.EUrnReserved;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UrnReservedRepository extends CrudRepository<EUrnReserved, Long> {

    List<EUrnReserved> findAll();
    Optional<List<EUrnReserved>> findByGri(String gri);
    Optional<List<EUrnReserved>> findByUrn(String urn);


}