package net.es.oscars.webui.ipc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ConnectionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Set;


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
        //Set<Connection> result = restTemplate.postForObject(restPath, filter, Set.class);

        HttpEntity<ConnectionFilter> requestEntity = new HttpEntity<>(filter);
        ParameterizedTypeReference<Set<Connection>> typeRef = new ParameterizedTypeReference<Set<Connection>>() {};
        ResponseEntity<Set<Connection>> response = restTemplate.exchange(restPath, HttpMethod.POST, requestEntity, typeRef);

        Set<Connection> result = response.getBody();

        return result;
    }


}
