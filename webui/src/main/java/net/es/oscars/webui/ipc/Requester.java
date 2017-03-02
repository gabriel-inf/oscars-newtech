package net.es.oscars.webui.ipc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.webui.dto.AdvancedRequest;
import net.es.oscars.webui.dto.ConnectionBuilder;
import net.es.oscars.webui.dto.MinimalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
public class Requester {
    @Autowired
    private RestTemplate restTemplate;

    private final String oscarsUrl = "https://localhost:8000";

    public Connection holdMinimal(MinimalRequest minimalRequest) {
        log.info("holding minimal " + minimalRequest.toString());

        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        Connection c = connectionBuilder.buildConnectionFromMinimalRequest(minimalRequest);
        return hold(c);
    }

    public Connection holdAdvanced(AdvancedRequest advancedRequest){
        log.info("Holding advanced " + advancedRequest.toString());

        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        Connection c = connectionBuilder.buildConnectionFromAdvancedRequest(advancedRequest);
        return hold(c);
    }

    public Connection hold(Connection c){

        String submitUrl = "/resv/connection/add";
        String restPath = oscarsUrl + submitUrl;
        log.info("sending connection " + c.toString());
        Connection resultC = restTemplate.postForObject(restPath, c, Connection.class);
        log.info("got connection " + resultC.toString());
        return c;
    }


}
