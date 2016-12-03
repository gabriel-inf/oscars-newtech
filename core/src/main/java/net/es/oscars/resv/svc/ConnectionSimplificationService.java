package net.es.oscars.resv.svc;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.*;
import net.es.oscars.dto.spec.ReservedVlanFlow;
import net.es.oscars.dto.topo.BidirectionalPath;
import net.es.oscars.helpers.DateService;
import net.es.oscars.st.oper.OperState;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class ConnectionSimplificationService {

    @Autowired
    private DateService dateService;

    public ReservationDetails simplifyConnection(Connection conn) {

        String connectionId = "";
        Status status = Status.FAILED_TO_RETRIEVE;
        String start = "";
        String end = "";
        Set<BidirectionalPath> paths = new HashSet<>();

        if (conn != null) {
            connectionId = conn.getConnectionId();
            if(conn.getReservedSchedule().size() > 1){
                start = dateService.convertDateToString(conn.getReservedSchedule().get(0));
                end = dateService.convertDateToString(conn.getReservedSchedule().get(1));
            }
            status = parseStates(conn.getStates());
            ReservedVlanFlow rvf = conn.getReserved().getVlanFlow();
            paths = rvf.getAllPaths() != null? rvf.getAllPaths() : new HashSet<>();
        }


        return ReservationDetails.builder()
                .connectionId(connectionId)
                .status(status)
                .start(start)
                .end(end)
                .paths(paths)
                .build();
    }

    public Status parseStates(States states) {
        ResvState resv = states.getResv();
        ProvState prov = states.getProv();
        OperState oper = states.getOper();

        if (resv.equals(ResvState.SUBMITTED)) {
            return Status.SUBMITTED;
        }
        if (resv.equals(ResvState.HELD)) {
            return Status.HELD;
        }
        //TODO: Change in future, currently IDLE_WAIT is default state after committing OR aborting
        if (resv.equals(ResvState.COMMITTING) || resv.equals(ResvState.IDLE_WAIT)) {
            return Status.COMMITTED;
        } else {
            return Status.ABORTED;
        }
    }
}



