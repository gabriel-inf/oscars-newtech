package net.es.oscars.ds.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.topo.dao.DevGroupRepository;
import net.es.oscars.ds.topo.dao.DeviceRepository;
import net.es.oscars.ds.topo.ent.EDevGroup;
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
    private DevGroupRepository grpRepo;

    @Autowired
    private DeviceRepository devRepo;

    @PostConstruct
    public void fill() throws IOException {

        List<EDevGroup> groups = grpRepo.findAll();

        if (groups.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            EDevGroup group = mapper.readValue(new File("./config/topo.json"), EDevGroup.class);
            service.save(group);

        } else {
            log.info("db not empty");
        }
    }




}
