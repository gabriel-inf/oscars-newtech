package net.es.oscars.topo.dao;

import net.es.oscars.topo.ent.UrnE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrnRepository extends CrudRepository<UrnE, Long> {

    List<UrnE> findAll();
    Optional<UrnE> findByUrn(String urn);


}