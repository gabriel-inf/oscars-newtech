package net.es.oscars.spec.dao;
import net.es.oscars.spec.ent.SpecificationE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SpecificationRepository extends CrudRepository<SpecificationE, Long> {

    List<SpecificationE> findAll();
    Optional<SpecificationE> findById(Long id);


}