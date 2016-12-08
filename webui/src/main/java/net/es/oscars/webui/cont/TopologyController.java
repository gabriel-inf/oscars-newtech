package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.spec.ReservedBandwidth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Controller
public class TopologyController
{
    @Autowired
    private RestTemplate restTemplate;

    private final String oscarsUrl = "https://localhost:8000";

    @RequestMapping(value = "/topology/reservedbw", method = RequestMethod.POST)
    @ResponseBody
    public List<ReservedBandwidth> get_reserved_bw(@RequestBody List<String> resUrns)
    {
        String restPath = oscarsUrl + "/topo/reservedbw";

        HttpEntity<List<String>> requestEntity = new HttpEntity<>(resUrns);
        ParameterizedTypeReference<List<ReservedBandwidth>> typeRef = new ParameterizedTypeReference<List<ReservedBandwidth>>() {};
        ResponseEntity<List<ReservedBandwidth>> response = restTemplate.exchange(restPath, HttpMethod.POST, requestEntity, typeRef);

        List<ReservedBandwidth> relevantBwItems = response.getBody();

        return relevantBwItems;
    }
}