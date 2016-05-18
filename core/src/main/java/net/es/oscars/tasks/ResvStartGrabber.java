package net.es.oscars.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pce.TopPCE;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ConnectionRepository;
import net.es.oscars.resv.ent.ReservedBlueprintE;
import net.es.oscars.resv.ent.RequestedBlueprintE;
import net.es.oscars.st.resv.ResvState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Component
public class ResvStartGrabber {
    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private TopPCE topPCE;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void getSubmitted() {
        // time out old HELD reservations
        Integer timeoutMs = 30000;

        connRepo.findAll().stream()
                .filter(c -> c.getStates().getResv().equals(ResvState.HELD))
                .filter(c -> (c.getSchedule().getSubmitted().getTime() + timeoutMs < new Date().getTime()))
                .forEach(c -> {
                    log.info("reservation "+c.getConnectionId()+" timing out from HELD");

                    c.getStates().setResv(ResvState.IDLE_WAIT);

                });

        // process newly submitted reservations
        connRepo.findAll().stream()
                .filter(c -> c.getStates().getResv().equals(ResvState.SUBMITTED))
                .forEach(c -> {
                    log.info("detected submitted connection " + c.getConnectionId());
                    RequestedBlueprintE req = c.getSpecification().getRequested();

                    try {
                        ReservedBlueprintE res = topPCE.makeReserved(req, c.getSpecification().getScheduleSpec());
                        c.setReserved(res);
                        c.getStates().setResv(ResvState.HELD);
                        c = connRepo.save(c);

                        String pretty = null;
                        try {
                            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(c);
                            log.info(pretty);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }


                    } catch (PSSException ex) {
                        log.error("PSS Exception", ex);

                    } catch (PCEException ex) {
                        log.error("PCE Exception", ex);
                    }


                });

    }
}