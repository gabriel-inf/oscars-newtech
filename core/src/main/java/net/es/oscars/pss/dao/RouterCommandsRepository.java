package net.es.oscars.pss.dao;

import net.es.oscars.pss.ent.RouterCommandsE;
import net.es.oscars.pss.ent.TemplateE;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RouterCommandsRepository extends CrudRepository<RouterCommandsE, Long> {

    List<RouterCommandsE> findAll();
    Optional<RouterCommandsE> findByConnectionIdAndDeviceUrn(String connectionId, String deviceUrn);


}