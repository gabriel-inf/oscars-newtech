package net.es.oscars.acct.dao;

import net.es.oscars.acct.ent.ECustomer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<ECustomer, Long> {

    List<ECustomer> findAll();
    Optional<ECustomer> findByName(String name);


}