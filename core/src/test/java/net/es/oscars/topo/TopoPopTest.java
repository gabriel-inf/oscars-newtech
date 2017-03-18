package net.es.oscars.topo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class TopoPopTest extends AbstractCoreTest {

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private UrnAdjcyRepository adjRepo;


    @Test
    public void testSave() throws PCEException, PSSException {

        urnRepo.deleteAll();
        adjRepo.deleteAll();

        TopoGen tg = new TopoGen();
        TopoGen.TopoGenResult tgr = tg.singleSwitch();
        urnRepo.save(tgr.urns);
    }


}
