package net.es.oscars.ds.spec.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.spec.dao.SpecificationRepository;
import net.es.oscars.ds.spec.ent.ESpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class SpecPopulator {

    @Autowired
    private SpecificationRepository specRepo;

    @PostConstruct
    public void fill() {

        if (specRepo.findAll().isEmpty()) {
            Instant now = Instant.now();
            Instant begin = now.plus(15L, ChronoUnit.MINUTES);
            Instant end = now.plus(1L, ChronoUnit.DAYS);

            ESpecification spec = ESpecification.builder()
                    .mbps(100)
                    .submitted(now)
                    .reserveBegin(begin)
                    .reserveEnd(end)
                    .aUrn("star-cr5:1/1/1")
                    .zUrn("star-cr5:2/1/1")
                    .vlanId(1234)
                    .username("some user")
                    .specificationId("UANS8A")
                    .build();

            specRepo.save(spec);

        } else {
            log.info("db not empty");
        }

    }
}
