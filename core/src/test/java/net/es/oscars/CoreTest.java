package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.spec.SpecPopTest;
import net.es.oscars.spec.dao.SpecificationRepository;
import net.es.oscars.spec.ent.*;
import net.es.oscars.topo.dao.DeviceRepository;
import net.es.oscars.topo.ent.DeviceType;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.enums.DeviceModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
public class CoreTest {

    @Autowired
    private SpecificationRepository specRepo;

    @Autowired
    private DeviceRepository devRepo;

    @Autowired
    private TopPCE topPCE;

    @Test
    public void testSpecification() throws PCEException, PSSException {

        if (specRepo.findAll().isEmpty()) {
            SpecificationE spec = SpecPopTest.getBasicSpec();

            SpecPopTest.addEndpoints(spec);

            this.populateTopo(spec);


            topPCE.makeReserved(spec.getRequested());
            log.info("got schematic");


        } else {
            log.info("db not empty");
        }
    }

    @Test(expected = PCEException.class)
    public void testNoFixtures() throws PCEException {
        SpecificationE spec = SpecPopTest.getBasicSpec();

        VlanFlowE flow = spec.getRequested().getVlanFlows().iterator().next();

        VlanJunctionE somejunction = VlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .deviceUrn("star-tb1")
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .build();

        flow.getJunctions().add(somejunction);

        topPCE.verifyRequested(spec.getRequested());

    }


    private void populateTopo(SpecificationE spec) {
        spec.getRequested().getVlanFlows().stream().forEach(t-> {
            t.getJunctions().forEach(this::makeDevice);
            t.getPipes().forEach( p -> {
                makeDevice(p.getAJunction());
                makeDevice(p.getZJunction());
            });

        });
    }

    private void makeDevice(VlanJunctionE junction) {
        String urn = junction.getDeviceUrn();
        if (!devRepo.findByUrn(urn).isPresent()) {
           devRepo.save(EDevice.builder()
                    .model(DeviceModel.JUNIPER_EX)
                    .capabilities(new HashSet<>())
                    .ifces(new HashSet<>())
                    .reservableVlans(new ArrayList<>())
                    .type(DeviceType.SWITCH)
                    .urn(urn)
                    .build());

        }


    }


}
