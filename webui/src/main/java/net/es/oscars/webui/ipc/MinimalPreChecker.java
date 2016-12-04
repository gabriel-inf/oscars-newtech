package net.es.oscars.webui.ipc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.webui.dto.MinimalConnectionBuilder;
import net.es.oscars.webui.dto.MinimalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by jeremy on 11/1/16.
 */
@Slf4j
@Component
public class MinimalPreChecker
{
    @Autowired
    private RestTemplate restTemplate;

    private final String oscarsUrl = "https://localhost:8000";


    public Connection preCheckMinimal(MinimalRequest minimalRequest)
    {
        log.info("Pre-checking minimal " + minimalRequest.getConnectionId());

        MinimalConnectionBuilder minimalConnectionBuilder = new MinimalConnectionBuilder();
        Connection c = minimalConnectionBuilder.buildMinimalConnectionFromRequest(minimalRequest);

        String submitUrl = "/resv/connection/precheck";
        String restPath = oscarsUrl + submitUrl;

        Connection resultC = restTemplate.postForObject(restPath, c, Connection.class);

        if(resultC == null)
            log.info("Pre-Check result: UNSUCCESSFUL");
        else
            log.info("Pre-Check result: SUCCESS");

        //return c;
        return resultC;
    }
}
