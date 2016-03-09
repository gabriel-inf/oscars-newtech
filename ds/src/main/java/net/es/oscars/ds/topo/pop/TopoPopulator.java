package net.es.oscars.ds.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.topo.dao.TopologyRepository;
import net.es.oscars.ds.topo.dao.DeviceRepository;
import net.es.oscars.ds.topo.ent.ETopology;
import net.es.oscars.ds.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;


@Slf4j
@Component
public class TopoPopulator {

    @Autowired
    private TopoService service;

    @Autowired
    private TopologyRepository grpRepo;

    @Autowired
    private DeviceRepository devRepo;

    @PostConstruct
    public void fill() {

        List<ETopology> groups = grpRepo.findAll();

        if (groups.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ETopology group = mapper.readValue(new File("./config/topo.json"), ETopology.class);
                service.save(group);
            } catch (IOException ex) {
                log.error("Error opening file!", ex);
            }

        } else {
            log.info("db not empty");
        }
    }




}
