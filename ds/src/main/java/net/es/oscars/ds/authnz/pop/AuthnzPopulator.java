package net.es.oscars.ds.authnz.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.authnz.dao.UserRepository;
import net.es.oscars.ds.authnz.ent.EPermissions;
import net.es.oscars.ds.authnz.ent.EUser;
import net.es.oscars.ds.authnz.prop.AuthnzProperties;
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

    @Autowired
    private AuthnzProperties properties;

    @PostConstruct
    public void initializeUserDb() {

        List<EUser> users = userRepo.findAll();
        if (users.isEmpty()) {
            log.info("No users set; adding an admin user from application properties.");
            String username = properties.getUsername();
            String password = properties.getPassword();

            String encoded = new BCryptPasswordEncoder().encode(password);
            EUser admin = EUser.builder()
                    .username(username)
                    .password(encoded)
                    .permissions(new EPermissions())
                    .build();
            admin.getPermissions().setAdminAllowed(true);
            userRepo.save(admin);

        } else {
            log.info("user db not empty");

        }
    }





}
