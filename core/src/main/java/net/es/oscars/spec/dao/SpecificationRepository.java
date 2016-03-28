package net.es.oscars.spec.dao;
import net.es.oscars.spec.ent.ESpecification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SpecificationRepository extends CrudRepository<ESpecification, Long> {

    List<ESpecification> findAll();
    Optional<ESpecification> findBySpecificationId(String specificationId);


}