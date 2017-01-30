package net.es.oscars.pss.dao;

import net.es.oscars.pss.ent.UrnAddressE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UrnAddressRepository extends CrudRepository<UrnAddressE, Long> {

    List<UrnAddressE> findAll();
    Optional<UrnAddressE> findByUrn(String urn);


}