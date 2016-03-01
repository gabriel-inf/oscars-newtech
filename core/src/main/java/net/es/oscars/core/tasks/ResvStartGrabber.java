package net.es.oscars.core.tasks;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Reservation;
import net.es.oscars.rest.RestTemplateBuilder;
import net.es.oscars.st.resv.ResvState;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ResvStartGrabber {

    @Scheduled(fixedDelay = 5000)
    public void listResvs() {

        try {
            RestTemplate restTemplate = new RestTemplateBuilder().build();
            String restPath = "https://localhost:8000/resvs/r_state/" + ResvState.SUBMITTED;
            Reservation[] resvs = restTemplate.getForObject(restPath, Reservation[].class);

            for (Reservation resv : resvs) {
                log.info(resv.toString());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}
