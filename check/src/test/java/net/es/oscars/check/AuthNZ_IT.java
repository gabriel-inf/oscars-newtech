package net.es.oscars.check;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.DatastoreApp;
import net.es.oscars.ds.authnz.ent.EPermissions;
import net.es.oscars.ds.authnz.ent.EUser;
import net.es.oscars.ds.authnz.svc.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@IntegrationTest("server.port:0")
@SpringApplicationConfiguration(DatastoreApp.class)
@WebAppConfiguration
public class AuthNZ_IT {

    @Autowired
    private UserService userService;

    @Before
    public void setUp() {
        EUser user = EUser.builder()
                .email("abc.es.f")
                .username("someuser")
                .password("somepass")
                .permissions(new EPermissions())
                .build();

        userService.save(user);
        log.info("saved a user!");
    }

    @Test
    public void testHello() {

    }
}
