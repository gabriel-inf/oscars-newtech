package net.es.oscars.ds.topo.dao;

import net.es.oscars.ds.topo.ent.EDevice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends RevisionRepository<EDevice, Long, Integer>, CrudRepository<EDevice, Long> {

    List<EDevice> findAll();
    Optional<EDevice> findByUrn(String urn);


}