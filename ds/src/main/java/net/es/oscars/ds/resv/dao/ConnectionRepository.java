package net.es.oscars.ds.resv.dao;

import net.es.oscars.ds.resv.ent.EConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ConnectionRepository extends RevisionRepository<EConnection, Long, Integer>, CrudRepository<EConnection, Long> {

    List<EConnection> findAll();
    Optional<EConnection> findByConnectionId(String connectionId);


}