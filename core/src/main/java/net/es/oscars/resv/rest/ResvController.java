package net.es.oscars.resv.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.bwavail.BandwidthAvailabilityService;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.*;
import net.es.oscars.dto.spec.*;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.st.oper.OperState;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
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
    public ResvController(ResvService resvService) {
        this.resvService = resvService;
    }

    @Autowired
    private UrnRepository urnRepo;

    private ResvService resvService;

    @Autowired
    private BandwidthAvailabilityService bwAvailService;


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

        return convertConnToDto(resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new));

    }

    // TODO: make better
    @RequestMapping(value = "/resv", method = RequestMethod.GET)
    @ResponseBody
    public List<Connection> listResvs() {

        log.info("listing all resvs");
        List<Connection> dtoItems = new ArrayList<>();

        for (ConnectionE eItem : resvService.findAll()) {
            Connection dtoItem = convertConnToDto(eItem);
            dtoItems.add(dtoItem);
        }
        return dtoItems;

    }


    @RequestMapping(value = "/resv/basic_vlan/add", method = RequestMethod.POST)
    @ResponseBody
    public Connection basic_vlan_add(@RequestBody BasicVlanSpecification dtoSpec) throws PSSException, PCEException {
        log.info("saving a new basic spec");
        log.info(dtoSpec.toString());



        return makeConnectionFromBasic(dtoSpec);
    }

    @RequestMapping(value = "/resv/bwAvail/", method = RequestMethod.POST)
    @ResponseBody
    public BandwidthAvailabilityResponse getBandwidthAvailability(@RequestBody BandwidthAvailabilityRequest request) {
        log.info("Retrieving Bandwidth Availability Map");
        log.info("Request Details: " + request.toString());

        BandwidthAvailabilityResponse response = bwAvailService.getBandwidthAvailabilityMap(request);
        log.info("Resonse Details: " + response.toString());
        return response;
    }

    @RequestMapping(value = "/resv/commit/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public Connection commit(@PathVariable("connectionId") String connectionId) {
        log.info("attempting to commit " + connectionId);
        ConnectionE connE = resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new);
        if (connE.getStates().getResv().equals(ResvState.HELD)) {
            connE.getStates().setResv(ResvState.COMMITTING);
            resvService.save(connE);
        }

        return this.convertConnToDto(connE);
    }


    @RequestMapping(value = "/resv/abort/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public Connection abort(@PathVariable("connectionId") String connectionId) {
        log.info("attempting to commit " + connectionId);
        ConnectionE connE = resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new);
        if (connE.getStates().getResv().equals(ResvState.HELD)) {
            connE.getStates().setResv(ResvState.ABORTING);
            resvService.save(connE);
        }

        return this.convertConnToDto(connE);

    }

    private Connection makeConnectionFromBasic(BasicVlanSpecification dtoSpec) throws PCEException, PSSException {
        log.info("making a new connection with id " + dtoSpec.getConnectionId());

        StatesE states = StatesE.builder()
                .oper(OperState.ADMIN_DOWN_OPER_DOWN)
                .prov(ProvState.INITIAL)
                .resv(ResvState.SUBMITTED)
                .build();

        ScheduleE sch = ScheduleE.builder()
                .setup(new Date())
                .submitted(new Date())
                .teardown(new Date())
                .build();
        SpecificationE specE = this.basicVlanToFull(dtoSpec);

        ConnectionE connE = ConnectionE.builder()
                .connectionId(specE.getConnectionId())
                .schedule(sch)
                .specification(specE)
                .states(states)
                .build();

        connE.setSpecification(specE);
        resvService.hold(connE);

        log.info("saved connection, connectionId " + specE.getConnectionId());
        log.info(connE.toString());


        Connection conn = modelMapper.map(connE, Connection.class);
        log.info(conn.toString());


        return conn;

    }

    private SpecificationE basicVlanToFull(BasicVlanSpecification bvs) {


        RequestedVlanFlowE vf = RequestedVlanFlowE.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();

        BasicVlanFlow bvf = bvs.getBasicVlanFlow();

        UrnE aUrn = urnRepo.findByUrn(bvf.getAUrn()).orElseThrow(NoSuchElementException::new);
        UrnE aDevUrn = urnRepo.findByUrn(bvf.getADeviceUrn()).orElseThrow(NoSuchElementException::new);
        UrnE zUrn = urnRepo.findByUrn(bvf.getZUrn()).orElseThrow(NoSuchElementException::new);
        UrnE zDevUrn = urnRepo.findByUrn(bvf.getZDeviceUrn()).orElseThrow(NoSuchElementException::new);


        RequestedVlanFixtureE vfa = RequestedVlanFixtureE.builder()
                .fixtureType(EthFixtureType.REQUESTED)
                .portUrn(aUrn)
                .inMbps(bvf.getAzMbps())
                .egMbps(bvf.getZaMbps())
                .vlanExpression(bvf.getAVlanExpression())
                .build();

        RequestedVlanFixtureE vfz = RequestedVlanFixtureE.builder()
                .fixtureType(EthFixtureType.REQUESTED)
                .portUrn(zUrn)
                .inMbps(bvf.getZaMbps())
                .egMbps(bvf.getAzMbps())
                .vlanExpression(bvf.getZVlanExpression())
                .build();

        RequestedVlanJunctionE vja = RequestedVlanJunctionE.builder()
                .deviceUrn(aDevUrn)
                .fixtures(new HashSet<>())
                .junctionType(EthJunctionType.REQUESTED)
                .build();

        RequestedVlanJunctionE vjz = RequestedVlanJunctionE.builder()
                .deviceUrn(zDevUrn)
                .fixtures(new HashSet<>())
                .junctionType(EthJunctionType.REQUESTED)
                .build();

        if (bvf.getADeviceUrn().equals(bvf.getZDeviceUrn())) {
            vja.getFixtures().add(vfa);
            vja.getFixtures().add(vfz);
            vf.getJunctions().add(vja);

        } else {
            vja.getFixtures().add(vfa);
            vjz.getFixtures().add(vfz);

            RequestedVlanPipeE vpaz = RequestedVlanPipeE.builder()
                    .aJunction(vja)
                    .zJunction(vjz)
                    .azERO(new ArrayList<>())
                    .zaERO(new ArrayList<>())
                    .azMbps(bvf.getAzMbps())
                    .zaMbps(bvf.getZaMbps())
                    .eroPalindromic(bvf.getPalindromic())
                    .pipeType(EthPipeType.REQUESTED)
                    .build();

            vf.getPipes().add(vpaz);
        }



        RequestedBlueprintE requested = RequestedBlueprintE.builder()
                .vlanFlow(vf)
                .build();

        ScheduleSpecification ss = bvs.getScheduleSpec();
        ScheduleSpecificationE sse = ScheduleSpecificationE.builder()
                .durationMinutes(ss.getDurationMinutes())
                .notAfter(ss.getNotAfter())
                .notBefore(ss.getNotBefore())
                .build();

        return SpecificationE.builder()
                .connectionId(bvs.getConnectionId())
                .username(bvs.getUsername())
                .description(bvs.getDescription())
                .scheduleSpec(sse)
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