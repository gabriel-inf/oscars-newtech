package net.es.oscars.ds.authnz.svc;

import net.es.oscars.ds.authnz.dao.UserRepository;
import net.es.oscars.ds.authnz.ent.EUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepo;


    public EUser save(EUser user) {
        return userRepo.save(user);
    }

}
