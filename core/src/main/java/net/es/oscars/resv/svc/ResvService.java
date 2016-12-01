package net.es.oscars.resv.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.svc.PssResourceService;
import net.es.oscars.resv.dao.ConnectionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional
@Slf4j
public class ResvService {

    @Autowired
    public ResvService(TopPCE topPCE, ConnectionRepository connRepo, PssResourceService pssResourceService) {
        this.topPCE = topPCE;
        this.connRepo = connRepo;
        this.pssResourceService = pssResourceService;
    }

    private TopPCE topPCE;

    private ConnectionRepository connRepo;

    private PssResourceService pssResourceService;


    // basically DB stuff

    public void save(ConnectionE resv) {
        connRepo.save(resv);
    }

    public void delete(ConnectionE resv) {
        connRepo.delete(resv);
    }

    public List<ConnectionE> findAll() {
        return connRepo.findAll();
    }

    public Optional<ConnectionE> findByConnectionId(String connectionId) {
        return connRepo.findByConnectionId(connectionId);
    }

    public Stream<ConnectionE> ofResvState(ResvState resvState) {
        return connRepo.findAll().stream().filter(c -> c.getStates().getResv().equals(resvState));

    }
    public Stream<ConnectionE> ofHeldTimeout(Integer timeoutMs) {
        return connRepo.findAll().stream()
                .filter(c -> c.getStates().getResv().equals(ResvState.HELD))
                .filter(c -> (c.getSchedule().getSubmitted().getTime() + timeoutMs < new Date().getTime()));

    }
    public Stream<ConnectionE> ofProvState(ProvState provState) {
        return connRepo.findAll().stream().filter(c -> c.getStates().getProv().equals(provState));

    }


    // business logic


    public void abort(ConnectionE c) {
        this.deleteReserved(c);

        pssResourceService.release(c);
        c.getStates().setResv(ResvState.IDLE_WAIT);
        connRepo.save(c);
    }

    public void timeout(ConnectionE c) {
        this.deleteReserved(c);
        pssResourceService.release(c);

        c.getStates().setResv(ResvState.IDLE_WAIT);
        connRepo.save(c);
    }

    public void commit(ConnectionE c) {
        c.getStates().setResv(ResvState.IDLE_WAIT);
        pssResourceService.reserve(c);
        c.getStates().setProv(ProvState.READY_TO_GENERATE);

        connRepo.save(c);
    }

    public void hold(ConnectionE c) throws PSSException, PCEException {

        RequestedBlueprintE req = c.getSpecification().getRequested();

        List<Date> reservedSched = new ArrayList<>();
        Optional<ReservedBlueprintE> res = topPCE.makeReserved(req, c.getSpecification().getScheduleSpec(), reservedSched);

        // Reserved schedule list will contain [startDate, endDate]
        // Will be empty if the reservation failed
        c.setReservedSchedule(reservedSched);

        if (res.isPresent()) {
            c.setReserved(res.get());
            c.getStates().setResv(ResvState.HELD);
            c = connRepo.save(c);

            try {
                String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(c);
                log.info(pretty);     // commented for output readability
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            log.error("Reservation Unsuccessful!");
            c.setReserved(ReservedBlueprintE.builder()
                    .vlanFlow(ReservedVlanFlowE.builder()
                            .junctions(new HashSet<>())
                            .mplsPipes(new HashSet<>())
                            .ethPipes(new HashSet<>())
                            .allPaths(new HashSet<>())
                            .build())
                    .build());
            c.getStates().setResv(ResvState.ABORTING);
            connRepo.save(c);
        }

    }

    // internal convenience

    private ConnectionE deleteReserved(ConnectionE c) {
        ReservedVlanFlowE emptyFlow = ReservedVlanFlowE.builder().build();
        ReservedBlueprintE reserved = ReservedBlueprintE.builder().vlanFlow(emptyFlow).build();
        c.setReserved(reserved);
        return c;
    }

    // Submits connection request to TopPCE but does NOT trigger persistence!
    public Boolean preCheck(ConnectionE c) throws PSSException, PCEException
    {
        RequestedBlueprintE req = c.getSpecification().getRequested();

        List<Date> chosenDates = new ArrayList<>();
        Optional<ReservedBlueprintE> res = topPCE.makeReserved(req, c.getSpecification().getScheduleSpec(), chosenDates);

        if (res.isPresent())
        {
            log.info("Pre-check on ConnectionID: " + c.getConnectionId() + " Successful");

            c.setReserved(res.get());
            return Boolean.TRUE;
        }

        log.info("Pre-check on ConnectionID: " + c.getConnectionId() + " Unsuccessful");
        return Boolean.FALSE;
    }

}
