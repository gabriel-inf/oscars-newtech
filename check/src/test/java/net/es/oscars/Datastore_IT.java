package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.resv.ResourceType;
import net.es.oscars.pce.Gatherer;
import net.es.oscars.dto.resv.Interval;
import net.es.oscars.resv.dao.ReservedResourceRepository;
import net.es.oscars.resv.ent.EReservedResource;
import net.es.oscars.topo.pop.TopoImporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Slf4j

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest
public class Datastore_IT {


    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TopoImporter importer;

    @Autowired
    private ReservedResourceRepository resvRepo;


    @Before
    public void prepare() throws IOException {
        importer.importFromFile(true, "config/topo-basic/devices.json", "config/topo-basic/adjcies.json");

        EReservedResource rr = EReservedResource.builder()
                .beginning(Instant.ofEpochSecond(0))
                .ending(Instant.ofEpochSecond(5000))
                .intResource(10)
                .urns(new ArrayList<>())
                .resourceType(ResourceType.VLAN)
                .build();
        rr.getUrns().add("star-tb1:3/1/1");

        resvRepo.save(rr);
    }

    @Test
    public void testHello() throws Exception {

        Interval interval = Interval.builder().beginning(Instant.ofEpochSecond(100)).ending(Instant.ofEpochSecond(1000)).build();

        Gatherer g = new Gatherer();
        g.gatherReserved(interval, restTemplate);
        g.gatherConstraining(restTemplate);

    }

}
