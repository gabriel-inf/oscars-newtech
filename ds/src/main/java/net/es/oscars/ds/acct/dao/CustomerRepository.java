package net.es.oscars.ds.acct.dao;

import net.es.oscars.ds.acct.ent.ECustomer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends RevisionRepository<ECustomer, Long, Integer>, CrudRepository<ECustomer, Long> {

    List<ECustomer> findAll();
    Optional<ECustomer> findByName(String name);


}