package net.es.oscars.resv.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.BasicCircuitSpecification;
import net.es.oscars.dto.resv.CircuitSpecification;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ReservationDetails;
import net.es.oscars.pce.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.ConnectionE;
import net.es.oscars.resv.svc.ConnectionGenerationService;
import net.es.oscars.resv.svc.ConnectionSimplificationService;
import net.es.oscars.st.resv.ResvState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@Controller
/**
 * Provides a simplified endpoint for submitting and committing circuit reservations.
 */
public class SimpleResvController {

    @Autowired
    private ConnectionSimplificationService connectionSimplificationService;

    @Autowired
    private ConnectionGenerationService connectionGenerationService;

    @Autowired
    private ResvController resvController;

    @RequestMapping(value = "/resv/get_simple/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public ReservationDetails getDetails(@PathVariable("connectionId") String connectionId) throws JsonProcessingException {
        log.info("Retrieving reservation information...");

        Connection conn = resvController.getResv(connectionId);
        return simplifyResponse(conn);
    }

    @RequestMapping(value = "/resv/connection/add_simple", method = RequestMethod.POST)
    @ResponseBody
    public ReservationDetails submitSpec(@RequestBody CircuitSpecification spec) throws PSSException, PCEException {

        log.info("Received Specification from Client. Submitting (must commit later).");
        log.info("Specification Params: " + spec);

        Connection conn = connectionGenerationService.generateConnection(spec);
        // Submit, do not commit
        conn = resvController.submitConnection(conn);
        return simplifyResponse(conn);
    }

    @RequestMapping(value = "/resv/commit_simple/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public ReservationDetails commit(@PathVariable("connectionId") String connectionId) {
        log.info("Committing Reservation " + connectionId);

        // Commit
        Connection conn = resvController.commit(connectionId);
        return simplifyResponse(conn);
    }

    @RequestMapping(value = "/resv/connection/add_commit_simple", method = RequestMethod.POST)
    @ResponseBody
    public ReservationDetails submitCommitSpec(@RequestBody CircuitSpecification spec) throws PSSException, PCEException {

        log.info("Received Specification from Client. Submitting and committing (on success).");
        log.info("Specification Params: " + spec);

        Connection conn = connectionGenerationService.generateConnection(spec);
        // Submit
        conn = resvController.submitConnection(conn);
        // Commit if submit was successful
        if(conn.getStates().getResv().equals(ResvState.HELD)) {
            conn = resvController.commit(conn.getConnectionId());
        }
        return simplifyResponse(conn);
    }

    @RequestMapping(value = "/resv/connection/add_commit_b_simple/", method = RequestMethod.POST)
    @ResponseBody
    public ReservationDetails submitCommitBasicSpec(@RequestBody BasicCircuitSpecification spec) throws PSSException, PCEException {

        log.info("Received Basic Specification from Client. Submitting and committing (on success).");
        log.info("Specification Params: " + spec);

        Connection conn = connectionGenerationService.generateConnection(spec);
        // Submit
        conn = resvController.submitConnection(conn);
        // Commit if submit was successful
        if(conn.getStates().getResv().equals(ResvState.HELD)) {
            conn = resvController.commit(conn.getConnectionId());
        }
        return simplifyResponse(conn);
    }

    @RequestMapping(value = "/resv/abort_simple/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public ReservationDetails abort(@PathVariable("connectionId") String connectionId) {
        log.info("Aborting Reservation " + connectionId);

        // Abort
        Connection conn = resvController.abort(connectionId);
        return simplifyResponse(conn);

    }

    private ReservationDetails simplifyResponse(Connection conn){
        ReservationDetails resDetails = connectionSimplificationService.simplifyConnection(conn);
        resDetails.setConnectionId(conn.getConnectionId());
        log.info("Response Details: " + resDetails.toString());
        return resDetails;
    }


}
