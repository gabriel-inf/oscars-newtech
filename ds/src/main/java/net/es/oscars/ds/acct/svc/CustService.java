package net.es.oscars.ds.acct.svc;

import net.es.oscars.ds.acct.dao.CustomerRepository;
import net.es.oscars.ds.acct.ent.ECustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustService {

    @Autowired
    private CustomerRepository custRepo;

    public void delete(ECustomer customer) {
        custRepo.delete(customer);
    }

    public List<ECustomer> findAll() {
        return custRepo.findAll();
    }

    public Optional<ECustomer> findByName(String name) {
        return custRepo.findByName(name);
    }


    public ECustomer save(ECustomer customer) {
        return custRepo.save(customer);
    }

}
