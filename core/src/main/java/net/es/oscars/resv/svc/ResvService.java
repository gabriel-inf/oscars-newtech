package net.es.oscars.resv.svc;

import net.es.oscars.resv.dao.ConnectionRepository;
import net.es.oscars.resv.ent.ConnectionE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ResvService {

    @Autowired
    private ConnectionRepository resvRepo;

    public void delete(ConnectionE resv) {
        resvRepo.delete(resv);
    }

    public List<ConnectionE> findAll() {
        return resvRepo.findAll();
    }

    public Optional<ConnectionE> findByConnectionId(String connectionId) {
        return resvRepo.findByConnectionId(connectionId);
    }


    public ConnectionE save(ConnectionE resv) {
        return resvRepo.save(resv);
    }

}
