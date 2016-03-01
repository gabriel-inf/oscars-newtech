package net.es.oscars.ds.resv.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.resv.dao.ResvRepository;
import net.es.oscars.ds.resv.ent.EReservation;
import net.es.oscars.ds.resv.ent.EStates;
import net.es.oscars.st.resv.ResvState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component

public class ResvPopulator {


    @Autowired
    private ResvRepository repository;

    @PostConstruct
    public void fill() {

        List<EReservation> resvs = repository.findAll();

        if (resvs.isEmpty()) {
            EReservation resv = new EReservation("es.net-1");

            EStates states = new EStates();
            states.setResv(ResvState.SUBMITTED.toString());
            resv.setStates(states);
            repository.save(resv);
        } else {
            log.info("db not empty");

        }
    }


}
