package net.es.oscars.webui.ipc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ConnectionFilter;
import net.es.oscars.dto.resv.Schedule;
import net.es.oscars.dto.resv.States;
import net.es.oscars.dto.spec.*;
import net.es.oscars.st.oper.OperState;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;
import net.es.oscars.webui.dto.MinimalFixture;
import net.es.oscars.webui.dto.MinimalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Slf4j
@Component
public class ConnectionProvider {
    @Autowired
    private RestTemplate restTemplate;


    public Set<Connection> filtered(ConnectionFilter filter) {
        log.info("Listing connections");

        String submitUrl = "resv/filter";
        String restPath = "https://localhost:8000/" + submitUrl;
        log.info("sending filter " + filter.toString());
        Set<Connection> result = restTemplate.postForObject(restPath, filter, Set.class);
        return result;
    }


}
