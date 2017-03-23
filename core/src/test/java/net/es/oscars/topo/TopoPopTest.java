package net.es.oscars.topo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
