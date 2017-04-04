package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.GenerateResponse;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ConnectionFilter;
import net.es.oscars.pss.prop.PssConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;


@Component
@Slf4j
public class RestPssServer implements PSSProxy {
    private PssConfig pssConfig;
    private RestTemplate restTemplate;

    @Autowired
    public RestPssServer(PssConfig pssConfig, RestTemplate restTemplate) {

        this.pssConfig = pssConfig;
        this.restTemplate = restTemplate;
    }

    public CommandResponse submitCommand(Command cmd) {
        String pssUrl = pssConfig.getUrl();
        String submitUrl = "/command";
        String restPath = pssUrl + submitUrl;
        return restTemplate.postForObject(restPath, cmd, CommandResponse.class);

    }

    public GenerateResponse generate(Command cmd) {
        String pssUrl = pssConfig.getUrl();
        String submitUrl = "/generate";
        String restPath = pssUrl + submitUrl;
        return restTemplate.postForObject(restPath, cmd, GenerateResponse.class);
    }

    public CommandStatus status(String commandId) {
        String pssUrl = pssConfig.getUrl();
        String submitUrl = "/status?commandId="+commandId;
        String restPath = pssUrl + submitUrl;
        return restTemplate.getForObject(restPath, CommandStatus.class);
    }

}
