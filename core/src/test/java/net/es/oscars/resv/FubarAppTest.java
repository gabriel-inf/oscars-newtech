package net.es.oscars.resv;

import net.es.oscars.authnz.pop.AuthnzPopulator;
import net.es.oscars.conf.pop.ConfigPopulator;
import net.es.oscars.topo.pop.TopoFileImporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by bmah on 4/26/17.
 */
@RunWith(SpringRunner.class)
@WebMvcTest
public class FubarAppTest {

    @Autowired
    private MockMvc mockMvc;

    // XXX what other importers do we need wired in here?
    @Autowired
    private ConfigPopulator configPopulator;

    @Autowired
    private AuthnzPopulator authnzPopulator;

    @Autowired
    private TopoFileImporter topoFileImporter;

    @Before
    public void startup() throws IOException {
        topoFileImporter.importFromFile(true, "config/topo/basic-devices.json", "config/topo/basic-adjcies.json");

        WebApplicationContext wac;

    }


    @Test
    public void testOneApp() throws Exception {
        this.mockMvc.perform(get("/configs/all")).andDo(print()).andExpect(status().isOk());
    }
}
