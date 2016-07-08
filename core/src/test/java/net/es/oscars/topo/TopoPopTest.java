package net.es.oscars.topo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.SpecificationRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.DeviceType;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class TopoPopTest {

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
