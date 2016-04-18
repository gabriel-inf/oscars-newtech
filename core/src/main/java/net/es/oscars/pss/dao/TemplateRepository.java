package net.es.oscars.pss.dao;

import net.es.oscars.pss.ent.TemplateE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TemplateRepository extends CrudRepository<TemplateE, Long> {

    List<TemplateE> findAll();
    Optional<TemplateE> findByName(String name);


}