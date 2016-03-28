package net.es.oscars.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResvStartGrabber {



    @Scheduled(fixedDelay = 30000)
    public void getSubmitted() {
        /*
        String restPath = "https://localhost:8000/resvs/";
        Connection[] resvs = restTemplate.getForObject(restPath, Connection[].class);

        for (Connection resv : resvs) {
            log.info(resv.toString());
        }
        */

    }

}
