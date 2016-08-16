package net.es.oscars.bwavail;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.bwavail.enums.BandwidthAvailabilityRequest;
import net.es.oscars.bwavail.enums.BandwidthAvailabilityResponse;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.topo.TopologyBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class BandwidthAvailabilityServiceTest {

    @Autowired
    private BandwidthAvailabilityService bwAvailService;

    @Autowired
    private TopologyBuilder topologyBuilder;


    @Test
    public void noReservationsTest(){

        topologyBuilder.buildTopoFourPaths();

        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .requestID(1L)
                .srcPort("portA")
                .srcDevice("nodeK")
                .dstPort("portZ")
                .dstDevice("nodeQ")
                .startDate(new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond()))
                .endDate(new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .pathType(PalindromicType.PALINDROME)
                .survivabilityType(SurvivabilityType.SURVIVABILITY_NONE)
                .build();
        BandwidthAvailabilityResponse response = bwAvailService.getBandwidthAvailabilityMap(request);
        log.info(response.toString());
    }
}
