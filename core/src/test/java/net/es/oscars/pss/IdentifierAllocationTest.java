package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;

import net.es.oscars.QuickTestConfiguration;
import net.es.oscars.QuickTests;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.pce.helpers.RepoEntityBuilder;
import net.es.oscars.pce.helpers.ReservedConnectionBuilder;
import net.es.oscars.pss.svc.PssResourceService;
import net.es.oscars.resv.ent.ConnectionE;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=QuickTestConfiguration.class)
@Transactional
public class IdentifierAllocationTest
{
    @Autowired
    private PssResourceService resourceService;

    @Autowired
    private ReservedConnectionBuilder rcb;

    @Autowired
    private RepoEntityBuilder repoEntityBuilder;

    @Test
    @Category(QuickTests.class)
    public void testSingleJunction() throws PSSException {
        repoEntityBuilder.importEsnet();

        Map<String, Integer> fixtures = new HashMap<>();
        fixtures.put("newy-cr5:10/1/3", 2100);
        fixtures.put("newy-cr5:10/1/9", 2100);
        ConnectionE connE = rcb.singleAluJunction("xyzzy", "newy-cr5", fixtures);
        resourceService.reserve(connE);


    }
}
