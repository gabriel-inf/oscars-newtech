package net.es.oscars.consistency;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.QuickTests;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.dto.topo.enums.DeviceType;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.dto.viz.Position;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.pss.ent.UrnAddressE;
import net.es.oscars.pss.pop.UrnAddressImporter;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.pop.TopoFileImporter;
import net.es.oscars.topo.prop.TopoProperties;
import net.es.oscars.topo.svc.ConsistencyChecker;
import net.es.oscars.ui.pop.UIPopulator;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


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
        adjcyRepo.deleteAll();

    }

    @Test
    @Category(QuickTests.class)
    public void checkAllTopologies() throws IOException {

        List<String> prefixes = new ArrayList<>();
        prefixes.add("esnet");
        prefixes.add("netlab");
        prefixes.add("testbed");
        for (String prefix : prefixes) {
            this.clear();

            TopoProperties topoProperties = new TopoProperties();
            topoProperties.setPrefix(prefix);

            ui = new UIPopulator(topoProperties);
            UrnAddressImporter uai = new UrnAddressImporter(topoProperties, urnAddrRepo);
            TopoFileImporter tfi = new TopoFileImporter(urnRepo, adjcyRepo, topoProperties);

            ui.startup();
            uai.startup();
            tfi.startup();

            ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
            assert consistencyChecker.checkConsistency();
            log.info("topology "+prefix+ " is consistent");
        }

    }

    @Test
    @Category(QuickTests.class)
    public void testEverythingOk() {
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

        UrnE a = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:a")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        UrnE z = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:z")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        urnRepo.save(a);
        urnRepo.save(z);

        UrnAdjcyE az = UrnAdjcyE.builder()
                .a(a)
                .z(z)
                .metrics(new HashMap<>())
                .build();
        az.getMetrics().put(Layer.ETHERNET, 100L);
        UrnAdjcyE za = UrnAdjcyE.builder()
                .a(z)
                .z(a)
                .metrics(new HashMap<>())
                .build();
        za.getMetrics().put(Layer.ETHERNET, 100L);
        adjcyRepo.save(az);
        adjcyRepo.save(za);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);

        assert consistencyChecker.checkConsistency();
    }

    @Test
    @Category(QuickTests.class)
    public void testNoInverseAdjcies() {
        this.clear();

        UrnE a = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:a")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        UrnE b = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:b")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        UrnE z = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:z")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        urnRepo.save(a);
        urnRepo.save(b);
        urnRepo.save(z);

        UrnAdjcyE ab = UrnAdjcyE.builder()
                .a(a)
                .z(b)
                .metrics(new HashMap<>())
                .build();
        ab.getMetrics().put(Layer.ETHERNET, 100L);
        UrnAdjcyE za = UrnAdjcyE.builder()
                .a(z)
                .z(a)
                .metrics(new HashMap<>())
                .build();
        za.getMetrics().put(Layer.ETHERNET, 100L);
        adjcyRepo.save(ab);
        adjcyRepo.save(za);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        assert !consistencyChecker.checkConsistency();

    }


    @Test
    @Category(QuickTests.class)
    public void testAdcjyLayerMismatch() {
        this.clear();
        UrnE a = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:a")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        UrnE z = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:z")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        urnRepo.save(a);
        urnRepo.save(z);

        UrnAdjcyE az = UrnAdjcyE.builder()
                .a(a)
                .z(z)
                .metrics(new HashMap<>())
                .build();
        az.getMetrics().put(Layer.INTERNAL, 100L);
        az.getMetrics().put(Layer.ETHERNET, 100L);
        UrnAdjcyE za = UrnAdjcyE.builder()
                .a(z)
                .z(a)
                .metrics(new HashMap<>())
                .build();
        za.getMetrics().put(Layer.ETHERNET, 100L);
        adjcyRepo.save(az);
        adjcyRepo.save(za);


        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        assert !consistencyChecker.checkConsistency();

    }

    @Test
    @Category(QuickTests.class)
    public void testDeviceWithoutPosition() {
        this.clear();

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
    public void testPositionWithoutDevice() {
        this.clear();

        Position p = Position.builder().x(1).y(1).build();
        ui.getPositions().put("foo-cr1", p);

        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);

        assert !consistencyChecker.checkConsistency();
    }


    @Test
    @Category(QuickTests.class)
    public void testNoDeviceAddress() {
        this.clear();

        UrnE foo_cr1 = UrnE.builder()
                .capabilities(new HashSet<>())
                .deviceModel(DeviceModel.ALCATEL_SR7750)
                .deviceType(DeviceType.ROUTER)
                .urn("foo-cr1")
                .urnType(UrnType.DEVICE)
                .valid(true)
                .build();
        urnRepo.save(foo_cr1);
        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);
        assert !consistencyChecker.checkConsistency();
    }

    @Test
    @Category(QuickTests.class)
    public void testAddressUrnNotExist() {
        this.clear();

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
    public void testNoMplsIfceAddress() {
        this.clear();

        UrnE foo = UrnE.builder()
                .capabilities(new HashSet<>())
                .urn("foo-cr1:a")
                .urnType(UrnType.IFCE)
                .valid(true)
                .build();
        foo.getCapabilities().add(Layer.MPLS);
        urnRepo.save(foo);

        ConsistencyChecker consistencyChecker = new ConsistencyChecker(adjcyRepo, urnRepo, urnAddrRepo, ui);

        assert !consistencyChecker.checkConsistency();
    }
}
