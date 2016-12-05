package net.es.oscars.webui.ipc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.webui.dto.MinimalConnectionBuilder;
import net.es.oscars.webui.dto.MinimalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
public class MinimalRequester {
    @Autowired
    private RestTemplate restTemplate;

    private final String oscarsUrl = "https://localhost:8000";

    public Connection holdMinimal(MinimalRequest minimalRequest) {
        log.info("holding minimal " + minimalRequest.toString());

        MinimalConnectionBuilder minimalConnectionBuilder = new MinimalConnectionBuilder();
        Connection c = minimalConnectionBuilder.buildMinimalConnectionFromRequest(minimalRequest);

        String submitUrl = "/resv/connection/add";
        String restPath = oscarsUrl + submitUrl;
        log.info("sending connection " + c.toString());
        Connection resultC = restTemplate.postForObject(restPath, c, Connection.class);
        log.info("got connection " + resultC.toString());
        return c;
    }


}
