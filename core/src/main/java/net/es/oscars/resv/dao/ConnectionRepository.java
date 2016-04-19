package net.es.oscars.resv.dao;

import net.es.oscars.resv.ent.ConnectionE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ConnectionRepository extends CrudRepository<ConnectionE, Long> {

    List<ConnectionE> findAll();
    Optional<ConnectionE> findByConnectionId(String connectionId);


}