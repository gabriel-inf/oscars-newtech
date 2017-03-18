package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.helpers.test.RepoEntityBuilder;
import net.es.oscars.helpers.test.TopologyBuilder;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.RequestedBlueprintE;
import net.es.oscars.resv.ent.ReservedBlueprintE;
import net.es.oscars.resv.ent.ScheduleSpecificationE;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.BidirectionalPathE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=CoreUnitTestConfiguration.class)
@Transactional
public class TopPceTestNonPalindromicESnet {

    @Autowired
    private TopPCE topPCE;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ReservedBandwidthRepository reservedBandwidthRepo;

    @Autowired
    private RepoEntityBuilder repoEntityBuilder;

    @Test
    public void nonPalNoFixturesTest()
    {

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());


        String srcPort = "";
        String srcDevice = "wash-cr5";
        String dstPort = "";
        String dstDevice = "denv-cr5";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopoEsnet();
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);
        RequestedBlueprintE requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "survTest");

        evaluate(requestedBlueprint, requestedSched);

    }

    @Test
    public void nonPalOneFixtureTest()
    {

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());


        String srcPort = "ga-rt2:ge-1/0/5";
        String srcDevice = "ga-rt2";
        String dstPort = "amst-cr5:1/2/1";
        String dstDevice = "amst-cr5";
        Integer azBW = 500;
        Integer zaBW = 500;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopoEsnet();
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);
        RequestedBlueprintE requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "survTest");

        evaluate(requestedBlueprint, requestedSched);

    }

    @Test
    public void nonPalTwoFixturesTest()
    {

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());


        List<String> srcPorts = Arrays.asList("wash-cr5:10/1/10", "wash-cr5:10/1/8");
        String srcDevice = "wash-cr5";
        List<String> dstPorts = Arrays.asList("denv-cr5:10/1/11", "denv-cr5:10/1/12");
        String dstDevice = "denv-cr5";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopoEsnet();
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);
        RequestedBlueprintE requestedBlueprint = testBuilder.buildRequest(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "survTest");

        evaluate(requestedBlueprint, requestedSched);

    }

    @Test
    public void nonPalEnforced()
    {

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());


        List<String> srcPorts = Collections.singletonList("denv-cr5:10/1/11");
        String srcDevice = "denv-cr5";
        List<String> dstPorts = Collections.singletonList("kans-cr5:10/1/1");
        String dstDevice = "kans-cr5";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.NON_PALINDROME;
        SurvivabilityType survivability = SurvivabilityType.SURVIVABILITY_NONE;
        String vlan = "any";

        topologyBuilder.buildTopoEsnet();
        ScheduleSpecificationE requestedSched = testBuilder.buildSchedule(startDate, endDate);
        RequestedBlueprintE requestedBlueprint = testBuilder.buildRequest(srcPorts, srcDevice, dstPorts, dstDevice, azBW, zaBW, palindrome, survivability, vlan, 2, 1, 1, "survTest");


        // Reserve bandwidth on the return path
        List<String> intermediatePorts = Arrays.asList("denv-cr5:1/1/1", "kans-cr5:2/1/1");
        List<Boolean> isIngress = Arrays.asList(false, true);
        reserveBandwidth(intermediatePorts, isIngress, 100000);
        evaluate(requestedBlueprint, requestedSched);

    }

    public void evaluate(RequestedBlueprintE reqBlueprint, ScheduleSpecificationE reqSchedule){
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        try
        {
            reservedBlueprint = topPCE.makeReserved(reqBlueprint, reqSchedule, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE)
        {
            log.error("", pceE);
        }

        assert (reservedBlueprint.isPresent());
        Set<BidirectionalPathE> paths = reservedBlueprint.get().getVlanFlow().getAllPaths();
        assert(paths.size() == 1);
        assert(paths.stream().allMatch(path -> path.getAzPath().size() > 0 && path.getZaPath().size() > 0));

    }

    private void reserveBandwidth(List<String> ports, List<Boolean> ingress, Integer bwAmount) {

        List<String> reservedPortNames = new ArrayList<>();
        List<Instant> reservedStartTimes = new ArrayList<>();
        List<Instant> reservedEndTimes = new ArrayList<>();
        List<Integer> inBandwidths = new ArrayList<>();
        List<Integer> egBandwidths = new ArrayList<>();

        for(Integer i = 0; i < ports.size(); i++){

            String port = ports.get(i);
            Boolean isIngress = ingress.get(i);

            reservedPortNames.add(port);
            reservedStartTimes.add(Instant.now().minus(100L, ChronoUnit.DAYS));
            reservedEndTimes.add(Instant.now().plus(100L, ChronoUnit.DAYS));
            if(isIngress) {
                inBandwidths.add(bwAmount);
                egBandwidths.add(0);
            }
            else{
                inBandwidths.add(0);
                egBandwidths.add(bwAmount);
            }
        }

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
    }
}
