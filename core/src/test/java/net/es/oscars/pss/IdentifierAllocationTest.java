package net.es.oscars.pss;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;

import net.es.oscars.QuickTests;
import net.es.oscars.pss.svc.PssResourceService;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=CoreUnitTestConfiguration.class)
@Transactional
public class IdentifierAllocationTest
{
    @Autowired
    private PssResourceService resourceService;

    @Test
    @Category(QuickTests.class)
    public void testSingleJunction() {

    }
}
