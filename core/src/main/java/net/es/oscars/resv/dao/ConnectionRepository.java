package net.es.oscars.resv.dao;

import net.es.oscars.resv.ent.EConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ConnectionRepository extends CrudRepository<EConnection, Long> {

    List<EConnection> findAll();
    Optional<EConnection> findByConnectionId(String connectionId);


}