package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.ConnectionE;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@Slf4j
public class PssResourceService {
    public void reserve(ConnectionE conn) {

    }

    public void release(ConnectionE conn) {

    }
}
