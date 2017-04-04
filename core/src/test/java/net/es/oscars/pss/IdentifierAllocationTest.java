package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;

import net.es.oscars.AbstractCoreTest;
import net.es.oscars.QuickTests;
import net.es.oscars.pce.helpers.RepoEntityBuilder;
import net.es.oscars.pce.helpers.ReservedConnectionBuilder;
import net.es.oscars.pss.svc.PssResourceService;
import net.es.oscars.resv.ent.ConnectionE;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Transactional
public class IdentifierAllocationTest extends AbstractCoreTest {
    @Autowired
    private PssResourceService resourceService;

    @Autowired
    private ReservedConnectionBuilder rcb;

    @Autowired
    private RepoEntityBuilder repoEntityBuilder;


    @Test
    public void testSingleJunction() throws PSSException {
        repoEntityBuilder.importEsnet();

        Map<String, Integer> fixtures = new HashMap<>();
        fixtures.put("newy-cr5:10/1/3", 2100);
        fixtures.put("newy-cr5:10/1/9", 2100);
        ConnectionE connE = rcb.singleAluJunction("xyzzy", "newy-cr5", fixtures);
        resourceService.reserve(connE);
    }
}
