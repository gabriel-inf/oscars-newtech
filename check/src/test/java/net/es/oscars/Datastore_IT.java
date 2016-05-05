package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.resv.dao.ReservedPssResourceRepository;
import net.es.oscars.resv.ent.ReservedPssResourceE;
import net.es.oscars.topo.pop.TopoImporter;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Slf4j

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest
public class Datastore_IT {


    @Autowired
    private TopoImporter importer;

    @Autowired
    private ReservedPssResourceRepository resvRepo;


    @Before
    public void prepare() throws IOException {
        importer.importFromFile(true, "config/topo-basic/devices.json", "config/topo-basic/adjcies.json");

        ReservedPssResourceE rr = ReservedPssResourceE.builder()
                .beginning(Instant.ofEpochSecond(0))
                .ending(Instant.ofEpochSecond(5000))
                .resource(10)
                .urn("star-tb1")
                .resourceType(ResourceType.VC_ID)
                .build();

        resvRepo.save(rr);
    }

}
