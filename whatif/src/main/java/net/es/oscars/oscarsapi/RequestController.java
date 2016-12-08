package net.es.oscars.oscarsapi;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Controller
public class RequestController {

    @Autowired
    private RestTemplate restTemplate;

    private final String oscarsUrl = "https://localhost:8000";

    public Connection submit(@ModelAttribute Connection conn){
        String restPath = oscarsUrl + "/resv/connection/add";
        log.info("Submitting a circuit request");

        Connection c;
        try {
            c = restTemplate.postForObject(restPath, conn, Connection.class);
            log.info("Received Connection Response from OSCARS");
        } catch(Exception e){
            c = handleException(e, "Submission", conn.getConnectionId());
        }

        return c;
    }

    public Connection commit(@PathVariable String connectionId){
        String restPath = oscarsUrl + "/resv/commit/" + connectionId;
        log.info("Committing circuit request: " + connectionId);

        Connection c;
        try {
            c = restTemplate.getForObject(restPath, Connection.class);
            log.info("Received Connection Response from OSCARS");
        } catch(Exception e){
            c = handleException(e, "Commit", connectionId);
        }
        return c;
    }

    public Connection abort(@PathVariable String connectionId){
        String restPath = oscarsUrl + "/resv/abort/" + connectionId;
        log.info("Aborting Submitted Request");

        Connection c;
        try {
            c = restTemplate.getForObject(restPath, Connection.class);
            log.info("Received Connection Response from OSCARS");
        } catch(Exception e){
            c = handleException(e, "Abort", connectionId);
        }
        return c;
    }

    public Connection query(@PathVariable String connectionId){
        String restPath = oscarsUrl + "/resv/get/" + connectionId;
        log.info("Querying for Connection details");

        Connection c;
        try {
            c = restTemplate.getForObject(restPath, Connection.class);
            log.info("Received Connection Response from OSCARS");
        } catch(Exception e){
            log.info("Query Failed");
            log.info("Exception: " + e.getMessage());
            return null;
        }
        return c;
    }

    public Connection handleException(Exception e, String action, String connectionId){
        log.info(action + " Failed");
        log.info("Exception: " + e.getMessage());
        return query(connectionId);
    }


}
