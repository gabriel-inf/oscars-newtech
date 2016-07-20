package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.TopologyBuilder;
import net.es.oscars.topo.VlanTranslationTopologyBuilder;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.PalindromicType;
import org.hibernate.criterion.Junction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
@Transactional
public class VlanTranslationTest {

    @Autowired
    private TopPCE topPCE;

    @Autowired
    private TestEntityBuilder testBuilder;

    @Autowired
    private VlanTranslationTopologyBuilder vlanTopologyBuilder;

    @Test
    public void vlanTransTest1(){

        log.info("Initializing test: 'vlanTransTest1'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String srcVlan = "3";
        String dstVlan = "3";

        vlanTopologyBuilder.buildVlanTransTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, srcVlan, dstVlan);

        log.info("Beginning test: 'vlanTransTest1'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedBlueprintE resBlueprint  = reservedBlueprint.get();
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();
        Set<ReservedVlanJunctionE> junctions = resBlueprint.getVlanFlow().getJunctions();

        assert(ethPipes.size() == 2);
        assert(mplsPipes.size() == 0);
        assert(junctions.size() == 0);

        Map<String, UrnE> urnMap = new HashMap<>();
        Map<UrnE, Integer> reservedVlanMap = new HashMap<>();
        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes);

        UrnE a0 = urnMap.getOrDefault("A:0", null);
        UrnE a1 = urnMap.getOrDefault("A:1", null);
        UrnE a2 = urnMap.getOrDefault("A:2", null);
        UrnE b0 = urnMap.getOrDefault("B:0", null);
        UrnE b1 = urnMap.getOrDefault("B:1", null);
        UrnE b2 = urnMap.getOrDefault("B:2", null);
        UrnE c0 = urnMap.getOrDefault("C:0", null);
        UrnE c1 = urnMap.getOrDefault("C:1", null);
        UrnE c2 = urnMap.getOrDefault("C:2", null);

        // Unused
        assert(a0 == null);
        // Intermediate
        assert(reservedVlanMap.get(a1) == 3);
        // Src Fixture
        assert(reservedVlanMap.get(a2) == 3);
        // Intermediate
        assert(reservedVlanMap.get(b0) == 3);
        // Intermediate
        assert(reservedVlanMap.get(b1) == 3);
        // Unused
        assert(b2 == null);
        // Intermediate
        assert(reservedVlanMap.get(c0) == 3);
        // Unused
        assert(c1 == null);
        // Src Fixture
        assert(reservedVlanMap.get(c2) == 3);

        assert(reservedVlanMap.get(a1).equals(reservedVlanMap.get(b0)));
        assert(reservedVlanMap.get(b0).equals(reservedVlanMap.get(b1)));
        assert(reservedVlanMap.get(b1).equals(reservedVlanMap.get(c0)));

        log.info("Finished test: vlanTransTest1");
    }

    @Test
    public void vlanTransTest2(){

        log.info("Initializing test: 'vlanTransTest2'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String srcVlan = "2";
        String dstVlan = "3";

        vlanTopologyBuilder.buildVlanTransTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, srcVlan, dstVlan);

        log.info("Beginning test: 'vlanTransTest2'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedBlueprintE resBlueprint  = reservedBlueprint.get();
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();
        Set<ReservedVlanJunctionE> junctions = resBlueprint.getVlanFlow().getJunctions();

        assert(ethPipes.size() == 2);
        assert(mplsPipes.size() == 0);
        assert(junctions.size() == 0);

        Map<String, UrnE> urnMap = new HashMap<>();
        Map<UrnE, Integer> reservedVlanMap = new HashMap<>();
        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes);

        UrnE a0 = urnMap.getOrDefault("A:0", null);
        UrnE a1 = urnMap.getOrDefault("A:1", null);
        UrnE a2 = urnMap.getOrDefault("A:2", null);
        UrnE b0 = urnMap.getOrDefault("B:0", null);
        UrnE b1 = urnMap.getOrDefault("B:1", null);
        UrnE b2 = urnMap.getOrDefault("B:2", null);
        UrnE c0 = urnMap.getOrDefault("C:0", null);
        UrnE c1 = urnMap.getOrDefault("C:1", null);
        UrnE c2 = urnMap.getOrDefault("C:2", null);

        // Unused
        assert(a0 == null);
        // Intermediate
        assert(reservedVlanMap.get(a1) == 2 || reservedVlanMap.get(a1) == 3);
        // Src Fixture
        assert(reservedVlanMap.get(a2) == 2);
        // Intermediate
        assert(reservedVlanMap.get(b0) == 2 || reservedVlanMap.get(b0) == 3);
        // Intermediate
        assert(reservedVlanMap.get(b1) == 2 || reservedVlanMap.get(b1) == 3);
        // Unused
        assert(b2 == null);
        // Intermediate
        assert(reservedVlanMap.get(c0) == 2 || reservedVlanMap.get(c0) == 3);
        // Unused
        assert(c1 == null);
        // Dst Fixture
        assert(reservedVlanMap.get(c2) == 3);

        assert(reservedVlanMap.get(a1).equals(reservedVlanMap.get(b0)));
        assert(reservedVlanMap.get(b0).equals(reservedVlanMap.get(b1)));
        assert(reservedVlanMap.get(b1).equals(reservedVlanMap.get(c0)));

        log.info("Finished test: vlanTransTest2");
    }

    @Test
    public void vlanTransTest3(){

        log.info("Initializing test: 'vlanTransTest3'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String srcVlan = "3";
        String dstVlan = "2";

        vlanTopologyBuilder.buildVlanTransTopo1();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, srcVlan, dstVlan);

        log.info("Beginning test: 'vlanTransTest3'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedBlueprintE resBlueprint  = reservedBlueprint.get();
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();
        Set<ReservedVlanJunctionE> junctions = resBlueprint.getVlanFlow().getJunctions();

        assert(ethPipes.size() == 2);
        assert(mplsPipes.size() == 0);
        assert(junctions.size() == 0);

        Map<String, UrnE> urnMap = new HashMap<>();
        Map<UrnE, Integer> reservedVlanMap = new HashMap<>();
        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes);

        UrnE a0 = urnMap.getOrDefault("A:0", null);
        UrnE a1 = urnMap.getOrDefault("A:1", null);
        UrnE a2 = urnMap.getOrDefault("A:2", null);
        UrnE b0 = urnMap.getOrDefault("B:0", null);
        UrnE b1 = urnMap.getOrDefault("B:1", null);
        UrnE b2 = urnMap.getOrDefault("B:2", null);
        UrnE c0 = urnMap.getOrDefault("C:0", null);
        UrnE c1 = urnMap.getOrDefault("C:1", null);
        UrnE c2 = urnMap.getOrDefault("C:2", null);

        // Unused
        assert(a0 == null);
        // Intermediate
        assert(reservedVlanMap.get(a1) == 2 || reservedVlanMap.get(a1) == 3);
        // Src Fixture
        assert(reservedVlanMap.get(a2) == 3);
        // Intermediate
        assert(reservedVlanMap.get(b0) == 2 || reservedVlanMap.get(b0) == 3);
        // Intermediate
        assert(reservedVlanMap.get(b1) == 2 || reservedVlanMap.get(b1) == 3);
        // Unused
        assert(b2 == null);
        // Intermediate
        assert(reservedVlanMap.get(c0) == 2 || reservedVlanMap.get(c0) == 3);
        // Unused
        assert(c1 == null);
        // Dst Fixture
        assert(reservedVlanMap.get(c2) == 2);

        assert(reservedVlanMap.get(a1).equals(reservedVlanMap.get(b0)));
        assert(reservedVlanMap.get(b0).equals(reservedVlanMap.get(b1)));
        assert(reservedVlanMap.get(b1).equals(reservedVlanMap.get(c0)));

        log.info("Finished test: vlanTransTest3");
    }

    @Test
    public void vlanTransTest4(){

        log.info("Initializing test: 'vlanTransTest4'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String srcVlan = "3";
        String dstVlan = "3";

        vlanTopologyBuilder.buildVlanTransTopo2();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, srcVlan, dstVlan);

        log.info("Beginning test: 'vlanTransTest4'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(reservedBlueprint.isPresent());

        ReservedBlueprintE resBlueprint  = reservedBlueprint.get();
        Set<ReservedEthPipeE> ethPipes = resBlueprint.getVlanFlow().getEthPipes();
        Set<ReservedMplsPipeE> mplsPipes = resBlueprint.getVlanFlow().getMplsPipes();
        Set<ReservedVlanJunctionE> junctions = resBlueprint.getVlanFlow().getJunctions();

        assert(ethPipes.size() == 2);
        assert(mplsPipes.size() == 0);
        assert(junctions.size() == 0);

        Map<String, UrnE> urnMap = new HashMap<>();
        Map<UrnE, Integer> reservedVlanMap = new HashMap<>();
        buildUrnAndReservedVlanMap(urnMap, reservedVlanMap, ethPipes);

        UrnE a0 = urnMap.getOrDefault("A:0", null);
        UrnE a1 = urnMap.getOrDefault("A:1", null);
        UrnE a2 = urnMap.getOrDefault("A:2", null);
        UrnE b0 = urnMap.getOrDefault("B:0", null);
        UrnE b1 = urnMap.getOrDefault("B:1", null);
        UrnE b2 = urnMap.getOrDefault("B:2", null);
        UrnE c0 = urnMap.getOrDefault("C:0", null);
        UrnE c1 = urnMap.getOrDefault("C:1", null);
        UrnE c2 = urnMap.getOrDefault("C:2", null);

        // Unused
        assert(a0 == null);
        // Intermediate
        assert(reservedVlanMap.get(a1) == 4 || reservedVlanMap.get(a1) == 5);
        // Src Fixture
        assert(reservedVlanMap.get(a2) == 3);
        // Intermediate
        assert(reservedVlanMap.get(b0) == 4 || reservedVlanMap.get(b0) == 5);
        // Intermediate
        assert(reservedVlanMap.get(b1) == 4 || reservedVlanMap.get(b1) == 5);
        // Unused
        assert(b2 == null);
        // Intermediate
        assert(reservedVlanMap.get(c0) == 4 || reservedVlanMap.get(c0) == 5);
        // Unused
        assert(c1 == null);
        // Dst Fixture
        assert(reservedVlanMap.get(c2) == 3);

        assert(reservedVlanMap.get(a1).equals(reservedVlanMap.get(b0)));
        assert(reservedVlanMap.get(b0).equals(reservedVlanMap.get(b1)));
        assert(reservedVlanMap.get(b1).equals(reservedVlanMap.get(c0)));

        log.info("Finished test: vlanTransTest4");
    }

    @Test
    public void vlanTransTest5(){

        log.info("Initializing test: 'vlanTransTest5'.");

        RequestedBlueprintE requestedBlueprint;
        Optional<ReservedBlueprintE> reservedBlueprint = Optional.empty();
        ScheduleSpecificationE requestedSched;

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());

        String srcPort = "A:2";
        String srcDevice = "A";
        String dstPort = "C:2";
        String dstDevice = "C";
        Integer azBW = 25;
        Integer zaBW = 25;
        PalindromicType palindrome = PalindromicType.PALINDROME;
        String srcVlan = "3";
        String dstVlan = "3";

        vlanTopologyBuilder.buildVlanTransTopo3();
        requestedSched = testBuilder.buildSchedule(startDate, endDate);
        requestedBlueprint = testBuilder.buildRequest(srcPort, srcDevice, dstPort, dstDevice, azBW, zaBW, palindrome, srcVlan, dstVlan);

        log.info("Beginning test: 'vlanTransTest5'.");

        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched);
        }
        catch(PCEException | PSSException pceE){ log.error("", pceE); }

        assert(!reservedBlueprint.isPresent());

        log.info("Finished test: vlanTransTest5");
    }

    private void buildUrnAndReservedVlanMap(Map<String, UrnE> urnMap, Map<UrnE, Integer> reservedVlanMap,
                                            Set<ReservedEthPipeE> ethPipes){
        for(ReservedEthPipeE pipe : ethPipes){
            assert(pipe.getReservedVlans().stream().map(ReservedVlanE::getVlan).distinct().count() == 1);

            ReservedVlanJunctionE aJunction = pipe.getAJunction();
            ReservedVlanJunctionE zJunction = pipe.getZJunction();

            for(ReservedVlanFixtureE fix: aJunction.getFixtures()){
                urnMap.putIfAbsent(fix.getIfceUrn().getUrn(), fix.getIfceUrn());
                reservedVlanMap.putIfAbsent(fix.getIfceUrn(), fix.getReservedVlan().getVlan());
            }

            for(ReservedVlanFixtureE fix: zJunction.getFixtures()){
                urnMap.putIfAbsent(fix.getIfceUrn().getUrn(), fix.getIfceUrn());
                reservedVlanMap.putIfAbsent(fix.getIfceUrn(), fix.getReservedVlan().getVlan());
            }

            Set<ReservedVlanE> reservedVlans = pipe.getReservedVlans();
            for(ReservedVlanE resVlan : reservedVlans){
                urnMap.putIfAbsent(resVlan.getUrn().getUrn(), resVlan.getUrn());
                reservedVlanMap.putIfAbsent(resVlan.getUrn(), resVlan.getVlan());
            }
        }
    }
}
