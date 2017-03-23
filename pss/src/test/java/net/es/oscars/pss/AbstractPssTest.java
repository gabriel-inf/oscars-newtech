package net.es.oscars.pss;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PssTestConfiguration.class)
@TestPropertySource(locations = "file:config/test/application.properties")
public abstract class AbstractPssTest {


}