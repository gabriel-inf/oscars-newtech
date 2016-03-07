package net.es.oscars.core.tasks;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.st.resv.ResvState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ResvStartGrabber {

    @Autowired
    private RestTemplate restTemplate;


    @Scheduled(fixedDelay = 30000)
    public void getSubmitted() {

        String restPath = "https://localhost:8000/resvs/r_state/" + ResvState.SUBMITTED;
        Connection[] resvs = restTemplate.getForObject(restPath, Connection[].class);

        for (Connection resv : resvs) {
            log.info(resv.toString());
        }

    }

}
