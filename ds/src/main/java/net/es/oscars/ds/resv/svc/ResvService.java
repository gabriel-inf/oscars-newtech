package net.es.oscars.ds.resv.svc;

import net.es.oscars.ds.resv.dao.ResvRepository;
import net.es.oscars.ds.resv.ent.EReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ResvService {

    @Autowired
    private ResvRepository resvRepo;

    public void delete(EReservation resv) {
        resvRepo.delete(resv);
    }

    public List<EReservation> findAll() {
        return resvRepo.findAll();
    }

    public Optional<EReservation> findByGri(String gri) {
        return resvRepo.findByGri(gri);
    }

    public List<EReservation> byResvState(String resvState) {
        return resvRepo.findByStatesResv(resvState);
    }

    public EReservation save(EReservation resv) {
        return resvRepo.save(resv);
    }

}
