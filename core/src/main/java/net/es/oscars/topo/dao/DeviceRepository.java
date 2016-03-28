package net.es.oscars.topo.dao;

import net.es.oscars.topo.ent.EDevice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends CrudRepository<EDevice, Long> {

    List<EDevice> findAll();
    Optional<EDevice> findByUrn(String urn);


}