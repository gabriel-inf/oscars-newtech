package net.es.oscars.consistency;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.QuickTests;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.dto.topo.enums.DeviceType;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.dto.viz.Position;
import net.es.oscars.pce.helpers.RepoEntityBuilder;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.pss.ent.UrnAddressE;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.prop.TopoProperties;
import net.es.oscars.topo.svc.ConsistencyChecker;
import net.es.oscars.ui.pop.UIPopulator;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;


@Slf4j
@Transactional
public class ConsistencyCheckingTest extends AbstractCoreTest {

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private UrnAddressRepository urnAddrRepo;

    private UIPopulator ui;

    private void clear() {
        TopoProperties topoProperties = new TopoProperties();
        ui = new UIPopulator(topoProperties);
        ui.setPositions(new HashMap<>());
        urnRepo.deleteAll();
        adjcyRepo.deleteAll();
        urnAddrRepo.deleteAll();
    }

    @Test
    @Category(QuickTests.class)
    public void testConsistencyOk() {
        this.clear();
        Position p = Position.builder().x(1).y(1).build();
        ui.getPositions().put("foo-cr1", p);

        UrnE foo_cr1 = UrnE.builder()
                .capabilities(new HashSet<>())
                .deviceModel(DeviceModel.ALCATEL_SR7750)
                .deviceType(DeviceType.ROUTER)
                .urn("foo-cr1")
                .urnType(UrnType.DEVICE)
                .valid(true)
                .build();
        urnRepo.save(foo_cr1);
        UrnAddressE addr = UrnAddressE.builder()
                .ipv4Address("10.1.1.1")
                .ipv6Address("")
                .urn("foo-cr1")
                .build();
        urnAddrRepo.save(addr);

        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);

        assert consistencyChecker.checkConsistency();
    }

    @Test
    @Category(QuickTests.class)
    public void testBadPositions() {
        this.clear();

        Position p = Position.builder().x(1).y(1).build();
        ui.getPositions().put("foo-cr2", p);

        UrnE foo_cr1 = UrnE.builder()
                .capabilities(new HashSet<>())
                .deviceModel(DeviceModel.ALCATEL_SR7750)
                .deviceType(DeviceType.ROUTER)
                .urn("foo-cr1")
                .urnType(UrnType.DEVICE)
                .valid(true)
                .build();
        urnRepo.save(foo_cr1);

        UrnAddressE addr = UrnAddressE.builder()
                .ipv4Address("10.1.1.1")
                .ipv6Address("")
                .urn("foo-cr1")
                .build();
        urnAddrRepo.save(addr);

        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);

        assert !consistencyChecker.checkConsistency();
    }

    @Test
    @Category(QuickTests.class)
    public void testBadAddress() {
        this.clear();

        Position p = Position.builder().x(1).y(1).build();
        ui.getPositions().put("foo-cr1", p);

        UrnE foo_cr1 = UrnE.builder()
                .capabilities(new HashSet<>())
                .deviceModel(DeviceModel.ALCATEL_SR7750)
                .deviceType(DeviceType.ROUTER)
                .urn("foo-cr1")
                .urnType(UrnType.DEVICE)
                .valid(true)
                .build();
        urnRepo.save(foo_cr1);

        UrnAddressE addr = UrnAddressE.builder()
                .ipv4Address("10.1.1.1")
                .ipv6Address("")
                .urn("foo-cr2")
                .build();
        urnAddrRepo.save(addr);

        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);

        assert !consistencyChecker.checkConsistency();
    }
}
