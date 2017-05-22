package net.es.oscars.resv;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.authnz.pop.AuthnzPopulator;
import net.es.oscars.conf.pop.ConfigPopulator;
import net.es.oscars.conf.rest.ConfigController;
import net.es.oscars.topo.pop.TopoFileImporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * Created by bmah on 4/25/17.
 */
@Slf4j
@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FubarTest {

    @LocalServerPort
    private int port;

    // XXX what other importers do we need wired in here?
    @Autowired
    private ConfigPopulator configPopulator;

    @Autowired
    private AuthnzPopulator authnzPopulator;

    @Autowired
    private TopoFileImporter topoFileImporter;

    @Autowired
    private TestRestTemplate restTemplate = new TestRestTemplate("oscars", "oscars-shared");

    @Autowired
    private ConfigController configController;

    @Before
    public void startup() throws IOException {
        topoFileImporter.importFromFile(true, "config/topo/basic-devices.json", "config/topo/basic-adjcies.json");
    }

    @Test
    public void testOne() throws Exception {
//        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/",
//                String.class)).contains("Hello World");
        String url = "https://localhost:" + port + "/configs/all";
        System.out.println("Query URL:  " + url);
        Thread.sleep(10000);
        System.out.println(this.restTemplate.getForObject(url, String.class));
    }

}
