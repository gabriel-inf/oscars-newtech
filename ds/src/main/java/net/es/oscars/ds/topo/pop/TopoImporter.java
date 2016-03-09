package net.es.oscars.ds.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.topo.dao.TopologyRepository;
import net.es.oscars.ds.topo.ent.ETopology;
import net.es.oscars.ds.topo.prop.TopoProperties;
import net.es.oscars.ds.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;


@Slf4j
@Component
public class TopoImporter {

    @Autowired
    private TopoService service;

    @Autowired
    private TopologyRepository grpRepo;


    @Autowired
    private TopoProperties topoProperties;


    @PostConstruct
    public void importFromFile() {

        List<ETopology> groups = grpRepo.findAll();

        if (groups.isEmpty()) {


            try {
                File topoFile = new File(topoProperties.getImportFrom());
                ObjectMapper mapper = new ObjectMapper();
                ETopology group = mapper.readValue(topoFile, ETopology.class);
                service.save(group);
            } catch (IOException ex) {
                log.error("Error opening topology file!", ex);
            }

        } else {
            log.info("db not empty");
        }
    }




}
