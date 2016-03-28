package net.es.oscars.resv.svc;

import net.es.oscars.resv.dao.ConnectionRepository;
import net.es.oscars.resv.ent.EConnection;
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

    public void delete(EConnection resv) {
        resvRepo.delete(resv);
    }

    public List<EConnection> findAll() {
        return resvRepo.findAll();
    }

    public Optional<EConnection> findByGri(String connectionId) {
        return resvRepo.findByConnectionId(connectionId);
    }


    public EConnection save(EConnection resv) {
        return resvRepo.save(resv);
    }

}
