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
            log.info("No users in database; adding an admin user from authnz.username / .password properties.");
            if (properties == null) {
                log.info("No authnz application property set!");
                return;
            }

            String username = properties.getUsername();
            String password = properties.getPassword();
            if (username == null) {
                log.info("Null authnz.username application property!");
                return;
            }
            if (password == null) {
                log.info("Null authnz.password application property!");
                return;
            }

            String encoded = new BCryptPasswordEncoder().encode(password);
            EUser admin = EUser.builder()
                    .username(username)
                    .password(encoded)
                    .permissions(new EPermissions())
                    .build();
            admin.getPermissions().setAdminAllowed(true);
            userRepo.save(admin);

        } else {
            log.debug("User db not empty; no action needed");

        }
    }





}
