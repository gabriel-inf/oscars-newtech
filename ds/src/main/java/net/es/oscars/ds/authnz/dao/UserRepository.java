package net.es.oscars.ds.authnz.dao;

import net.es.oscars.ds.authnz.ent.EUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<EUser, Long> {

    List<EUser> findAll();
    Optional<EUser> findByUsername(String username);
    Optional<EUser> findByCertSubject(String certSubject);

}