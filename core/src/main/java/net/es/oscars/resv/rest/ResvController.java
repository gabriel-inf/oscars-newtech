package net.es.oscars.resv.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.*;
import net.es.oscars.dto.spec.*;
import net.es.oscars.resv.dao.ConnectionRepository;
import net.es.oscars.resv.dao.ReservedPssResourceRepository;
import net.es.oscars.resv.ent.ConnectionE;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.resv.ent.SpecificationE;
import net.es.oscars.st.oper.OperState;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Controller
public class ResvController {
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private ResvService service;

    @Autowired
    private ReservedPssResourceRepository resRepo;


    @Autowired
    private ConnectionRepository connRepo;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }


    @RequestMapping(value = "/resv/get/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public Connection getResv(@PathVariable("connectionId") String connectionId) {
        log.info("retrieving " + connectionId);

        return convertConnToDto(service.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new));

    }

    // TODO: make better
    @RequestMapping(value = "/resv", method = RequestMethod.GET)
    @ResponseBody
    public List<Connection> listResvs() {

        log.info("listing all resvs");
        List<Connection> dtoItems = new ArrayList<>();

        for (ConnectionE eItem : service.findAll()) {
            Connection dtoItem = convertConnToDto(eItem);
            dtoItems.add(dtoItem);
        }
        return dtoItems;

    }


    @RequestMapping(value = "/resv/basic_vlan/add", method = RequestMethod.POST)
    @ResponseBody
    public Connection basic_vlan_add(@RequestBody BasicVlanSpecification dtoSpec) {
        log.info("saving a new basic spec");
        log.info(dtoSpec.toString());


        return makeConnectionFromBasic(dtoSpec);
    }


    private Connection makeConnectionFromBasic(BasicVlanSpecification dtoSpec) {
        log.info("making a new connection with id "+dtoSpec.getConnectionId());

        Specification spec = basicVlanToFull(dtoSpec);
        SpecificationE specE = convertSpecToEnt(spec);

        States states = States.builder()
                .oper(OperState.ADMIN_DOWN_OPER_DOWN)
                .prov(ProvState.INITIAL)
                .resv(ResvState.SUBMITTED)
                .build();

        Schedule sch = Schedule.builder()
                .setup(new Date())
                .submitted(new Date())
                .teardown(new Date())
                .build();

        Blueprint reserved = Blueprint.builder()
                .layer3Flows(new HashSet<>())
                .vlanFlows(new HashSet<>())
                .build();

        Connection conn = Connection.builder()
                .specification(spec)
                .schedule(sch)
                .states(states)
                .reserved(reserved)
                .connectionId(spec.getConnectionId())
                .build();

        ConnectionE connE = modelMapper.map(conn, ConnectionE.class);
        connE.setSpecification(specE);
        connE = connRepo.save(connE);
        log.info("saved connection, connectionId "+specE.getConnectionId());
        log.info(connE.toString());


        conn = modelMapper.map(connE, Connection.class);
        log.info(conn.toString());


        return conn;

    }

    private Specification basicVlanToFull(BasicVlanSpecification bvs) {



        VlanFlow vf = VlanFlow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        BasicVlanFlow bvf = bvs.getBasicVlanFlow();

        VlanFixture vfa = VlanFixture.builder()
                .fixtureType(EthFixtureType.REQUESTED)
                .portUrn(bvf.getAUrn())
                .inMbps(bvf.getAzMbps())
                .egMbps(bvf.getZaMbps())
                .vlanExpression(bvf.getAVlanExpression())
                .build();

        VlanFixture vfz = VlanFixture.builder()
                .fixtureType(EthFixtureType.REQUESTED)
                .portUrn(bvf.getZUrn())
                .inMbps(bvf.getZaMbps())
                .egMbps(bvf.getAzMbps())
                .vlanExpression(bvf.getZVlanExpression())
                .build();

        VlanJunction vja = VlanJunction.builder()
                .deviceUrn(bvf.getADeviceUrn())
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .junctionType(EthJunctionType.REQUESTED)
                .build();

        VlanJunction vjz = VlanJunction.builder()
                .deviceUrn(bvf.getZDeviceUrn())
                .fixtures(new HashSet<>())
                .resourceIds(new HashSet<>())
                .junctionType(EthJunctionType.REQUESTED)
                .build();

        if (bvf.getADeviceUrn().equals(bvf.getZDeviceUrn())) {
            vja.getFixtures().add(vfa);
            vja.getFixtures().add(vfz);
            vf.getJunctions().add(vja);

        } else {
            vja.getFixtures().add(vfa);
            vjz.getFixtures().add(vfz);

            VlanPipe vpaz = VlanPipe.builder()
                    .aJunction(vja)
                    .zJunction(vjz)
                    .azERO(new ArrayList<>())
                    .zaERO(new ArrayList<>())
                    .azMbps(bvf.getAzMbps())
                    .zaMbps(bvf.getZaMbps())
                    .eroPalindromic(bvf.getEroPalindromic())
                    .pipeType(EthPipeType.REQUESTED)
                    .build();

            vf.getPipes().add(vpaz);
        }


        Blueprint requested = Blueprint.builder()
                .layer3Flows(new HashSet<>())
                .vlanFlows(new HashSet<>())
                .build();


        requested.getVlanFlows().add(vf);


        return Specification.builder()
                .connectionId(bvs.getConnectionId())
                .username(bvs.getUsername())
                .description(bvs.getDescription())
                .scheduleSpec(bvs.getScheduleSpec())
                .requested(requested)
                .version(0)
                .build();

    }



    private SpecificationE convertSpecToEnt(Specification dtoSpec) {
        return modelMapper.map(dtoSpec, SpecificationE.class);
    }

    private Connection convertConnToDto(ConnectionE connectionE) {
        return modelMapper.map(connectionE, Connection.class);
    }

}