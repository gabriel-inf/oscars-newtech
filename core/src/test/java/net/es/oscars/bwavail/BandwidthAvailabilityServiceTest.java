package net.es.oscars.bwavail;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.bwavail.enums.BandwidthAvailabilityRequest;
import net.es.oscars.bwavail.enums.BandwidthAvailabilityResponse;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.UrnE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class BandwidthAvailabilityServiceTest {

    @Autowired
    private BandwidthAvailabilityService bwAvailService;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private UrnRepository urnRepo;

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
                .palindromicType(PalindromicType.PALINDROME)
                .survivabilityType(SurvivabilityType.SURVIVABILITY_NONE)
                .build();
        BandwidthAvailabilityResponse response = bwAvailService.getBandwidthAvailabilityMap(request);

        List<UrnE> urns = urnRepo.findAll();
        List<ReservableBandwidthE> reservableBandwidths = urns
                .stream()
                .filter(u -> u.getReservableBandwidth() != null)
                .map(UrnE::getReservableBandwidth)
                .collect(Collectors.toList());
        List<Integer> inReservableBws = reservableBandwidths.stream().map(ReservableBandwidthE::getIngressBw).collect(Collectors.toList());
        List<Integer> egReservableBws = reservableBandwidths.stream().map(ReservableBandwidthE::getEgressBw).collect(Collectors.toList());

        Map<Instant, Integer> azBwMap = response.getAzBwAvailMap();
        Map<Instant, Integer> zaBwMap = response.getZaBwAvailMap();
        Integer minAzBw = response.getMinAvailableAzBandwidth();
        Integer maxAzBw = response.getMaxAvailableAzBandwidth();
        Integer minZaBw = response.getMinAvailableZaBandwidth();
        Integer maxZaBw = response.getMaxAvailableZaBandwidth();
        assert(minAzBw.equals(maxAzBw));
        assert(maxAzBw.equals(minZaBw));
        assert(minZaBw.equals(maxZaBw));
        assert(azBwMap.values().stream().allMatch(inReservableBws::contains));
        assert(zaBwMap.values().stream().allMatch(inReservableBws::contains));
        assert(azBwMap.values().stream().allMatch(egReservableBws::contains));
        assert(zaBwMap.values().stream().allMatch(egReservableBws::contains));
    }
}
