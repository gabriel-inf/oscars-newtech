package net.es.oscars.ds.resv.dao;

import net.es.oscars.ds.resv.ent.EReservedString;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReservedStrRepository extends CrudRepository<EReservedString, Long> {

    List<EReservedString> findAll();
    Optional<List<EReservedString>> findByUrn(String urn);


}