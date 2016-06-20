package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.resv.ent.RequestedVlanFlowE;
import net.es.oscars.resv.ent.RequestedVlanJunctionE;
import net.es.oscars.resv.ent.SpecificationE;
import net.es.oscars.resv.dao.SpecificationRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.DeviceType;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.UrnType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class CoreTest {

    @Autowired
    private SpecificationRepository specRepo;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private TopPCE topPCE;

    //@Test
    public void testSpecification() throws PCEException, PSSException {

        if (specRepo.findAll().isEmpty()) {
            SpecificationE spec = SpecPopTest.getBasicSpec();

            SpecPopTest.addEndpoints(spec);

            log.info("spec: " + spec.toString());

            this.populateTopo(spec);


            topPCE.makeReserved(spec.getRequested(), spec.getScheduleSpec());
            log.info("got schematic");


        } else {
            log.info("db not empty");
        }
    }

    @Test(expected = PCEException.class)
    public void testNoFixtures() throws PCEException {
        SpecificationE spec = SpecPopTest.getBasicSpec();
        UrnE urnE = UrnE.builder()
                .deviceModel(DeviceModel.JUNIPER_EX)
                .capabilities(new HashSet<>())
                .deviceType(DeviceType.SWITCH)
                .urnType(UrnType.DEVICE)
                .urn("star-tb1")
                .valid(true)
                .build();


        RequestedVlanFlowE flow = spec.getRequested().getVlanFlows().iterator().next();

        RequestedVlanJunctionE somejunction = RequestedVlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn(urnE)
                .fixtures(new HashSet<>())
                .build();

        flow.getJunctions().add(somejunction);

        topPCE.verifyRequested(spec.getRequested());

    }


    private void populateTopo(SpecificationE spec) {
        spec.getRequested().getVlanFlows().stream().forEach(t -> {
            t.getJunctions().forEach(this::makeDeviceUrn);
            t.getPipes().forEach(p -> {
                makeDeviceUrn(p.getAJunction());
                makeDeviceUrn(p.getZJunction());
            });

        });
    }

    private void makeDeviceUrn(RequestedVlanJunctionE junction) {
        String urn = junction.getDeviceUrn().getUrn();
        if (!urnRepo.findByUrn(urn).isPresent()) {
            UrnE urnE = UrnE.builder()
                    .deviceModel(DeviceModel.JUNIPER_EX)
                    .capabilities(new HashSet<>())
                    .deviceType(DeviceType.SWITCH)
                    .urnType(UrnType.DEVICE)
                    .urn(urn)
                    .valid(true)
                    .build();
            urnE.getCapabilities().add(Layer.ETHERNET);
            urnRepo.save(urnE);

        }


    }


}
