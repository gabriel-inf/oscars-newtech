package net.es.oscars.pss.dao;

import net.es.oscars.pss.ent.ETemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TemplateRepository extends CrudRepository<ETemplate, Long> {

    List<ETemplate> findAll();
    Optional<ETemplate> findByName(String name);


}