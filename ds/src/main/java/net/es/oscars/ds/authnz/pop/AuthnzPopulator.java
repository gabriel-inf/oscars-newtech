package net.es.oscars.ds.authnz.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.authnz.dao.UserRepository;
import net.es.oscars.ds.authnz.ent.EPermissions;
import net.es.oscars.ds.authnz.ent.EUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class AuthnzPopulator {

    @Autowired
    private UserRepository userRepo;

    @PostConstruct
    public void populate() {

        List<EUser> users = userRepo.findAll();
        if (users.isEmpty()) {
            String encoded = new BCryptPasswordEncoder().encode("oscars");
            EUser admin = EUser.builder()
                    .username("admin")
                    .password(encoded)
                    .permissions(new EPermissions())
                    .build();
            admin.getPermissions().setAdminAllowed(true);
            userRepo.save(admin);

            log.info("added default admin");
        } else {
            log.info("user db not empty");

        }
    }





}
