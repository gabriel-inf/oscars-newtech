package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.topo.enums.DeviceModel;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class QueuingTest {
    @Test
    @Category(UnitTests.class)
    public void hello() {
        log.info("all is good");


    }

}
