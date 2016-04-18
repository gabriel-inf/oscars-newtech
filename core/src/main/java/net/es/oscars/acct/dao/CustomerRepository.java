package net.es.oscars.acct.dao;

import net.es.oscars.acct.ent.CustomerE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<CustomerE, Long> {

    List<CustomerE> findAll();
    Optional<CustomerE> findByName(String name);


}