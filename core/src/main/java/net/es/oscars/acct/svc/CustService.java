package net.es.oscars.acct.svc;

import net.es.oscars.acct.dao.CustomerRepository;
import net.es.oscars.acct.ent.CustomerE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustService {
    @Autowired
    public CustService(CustomerRepository custRepo) {
        this.custRepo = custRepo;
    }

    private CustomerRepository custRepo;

    public void delete(CustomerE customer) {
        custRepo.delete(customer);
    }

    public List<CustomerE> findAll() {
        return custRepo.findAll();
    }

    public Optional<CustomerE> findByName(String name) {
        return custRepo.findByName(name);
    }


    public CustomerE save(CustomerE customer) {
        return custRepo.save(customer);
    }

}
