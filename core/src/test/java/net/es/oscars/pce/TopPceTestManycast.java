package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.helpers.test.TopologyBuilder;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.BidirectionalPathE;
import net.es.oscars.topo.svc.TopoService;
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
public class TopPceTestManycast {

    @Autowired
    TopologyBuilder topologyBuilder;

    @Autowired
    UrnRepository urnRepo;

    @Autowired
    TopPCE topPCE;

    @Autowired
    RequestedEntityBuilder entityBuilder;

    @Autowired
    TopoService topoService;

    @Test
    public void zeroMinZeroMaxOfTwo(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 25);
        List<Integer> zaMbpsList = Arrays.asList(25, 25);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1);
        Integer minPipes = 0;
        Integer maxPipes = 0;
        String connectionId = "test";
        Integer numPaths = 0;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);

    }

    @Test
    public void oneMinOneMaxOfTwo(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 25);
        List<Integer> zaMbpsList = Arrays.asList(25, 25);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1);
        Integer minPipes = 1;
        Integer maxPipes = 1;
        String connectionId = "test";
        Integer numPaths = 1;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);

    }

    @Test
    public void oneMinTwoMaxOfTwo(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 25);
        List<Integer> zaMbpsList = Arrays.asList(25, 25);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1);
        Integer minPipes = 1;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 2;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);

    }

    @Test
    public void twoMinTwoMaxOfTwo(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 25);
        List<Integer> zaMbpsList = Arrays.asList(25, 25);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1);
        Integer minPipes = 2;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 2;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void oneMinOneMaxOfThree(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6", "sacr-cr5:10/1/1");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5", "sacr-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1", "bost-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5", "bost-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 25, 25);
        List<Integer> zaMbpsList = Arrays.asList(25, 25, 25);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 1;
        Integer maxPipes = 1;
        String connectionId = "test";
        Integer numPaths = 1;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void oneMinTwoMaxOfThree(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6", "sacr-cr5:10/1/1");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5", "sacr-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1", "bost-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5", "bost-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 25, 25);
        List<Integer> zaMbpsList = Arrays.asList(25, 25, 25);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 1;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 2;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void twoMinTwoMaxOfThree(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6", "sacr-cr5:10/1/1");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5", "sacr-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1", "bost-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5", "bost-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 25, 25);
        List<Integer> zaMbpsList = Arrays.asList(25, 25, 25);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 2;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 2;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void twoMinTwoMaxOfThreeOneFails(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6", "sacr-cr5:10/1/1");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5", "sacr-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1", "bost-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5", "bost-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 25, 1000000);
        List<Integer> zaMbpsList = Arrays.asList(25, 25, 1000000);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 2;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 2;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void twoMinTwoMaxOfThreeTwoFail(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6", "sacr-cr5:10/1/1");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5", "sacr-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1", "bost-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5", "bost-cr5");
        List<Integer> azMbpsList = Arrays.asList(1000000, 25, 1000000);
        List<Integer> zaMbpsList = Arrays.asList(1000000, 25, 1000000);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 2;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 0;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, false);
    }

    @Test
    public void oneMinTwoMaxOfThreeTwoFail(){

        List<String> aPorts = Arrays.asList("wash-cr5:10/1/10", "sunn-cr5:10/1/6", "sacr-cr5:10/1/1");
        List<String> aDevices = Arrays.asList("wash-cr5", "sunn-cr5", "sacr-cr5");
        List<String> zPorts = Arrays.asList("denv-cr5:10/1/11", "aofa-cr5:10/1/1", "bost-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("denv-cr5", "aofa-cr5", "bost-cr5");
        List<Integer> azMbpsList = Arrays.asList(1000000, 25, 1000000);
        List<Integer> zaMbpsList = Arrays.asList(1000000, 25, 1000000);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 1;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 1;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void twoMinTwoMaxOfThreeEqualDistance(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 2;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 2;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void twoMinThreeMaxOfThree(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 2;
        Integer maxPipes = 3;
        String connectionId = "test";
        Integer numPaths = 3;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void threeMinThreeMaxOfThree(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 3;
        Integer maxPipes = 3;
        String connectionId = "test";
        Integer numPaths = 3;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void fourMinSixMaxOfEightThreeFail(){

        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5", "aofa-cr5",
                "albq-cr5", "eqx-ash-rt1", "sacr-cr5", "atla-cr5");
        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1", "aofa-cr5:10/1/1",
                "albq-cr5:10/1/4", "eqx-ash-rt1:ae0", "sacr-cr5:10/1/1", "atla-cr5:10/1/1");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5", "hous-cr5",
                "cern-272-cr5", "lsvn-cr1", "lond-cr5", "sdsc-sdn2");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2", "hous-cr5:10/1/3",
                "cern-272-cr5:10/1/1", "lsvn-cr1:ge-1/0/0", "lond-cr5:1/2/1", "sdsc-sdn2:xe-0/0/0");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 10000000, 100, 10000000, 150, 10000000, 200);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 10000000, 100, 10000000, 150, 10000000, 200);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME, PalindromicType.PALINDROME, PalindromicType.PALINDROME,
                PalindromicType.PALINDROME, PalindromicType.PALINDROME, PalindromicType.PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_PARTIAL,
                SurvivabilityType.SURVIVABILITY_PARTIAL, SurvivabilityType.SURVIVABILITY_PARTIAL,
                SurvivabilityType.SURVIVABILITY_PARTIAL, SurvivabilityType.SURVIVABILITY_PARTIAL,
                SurvivabilityType.SURVIVABILITY_PARTIAL, SurvivabilityType.SURVIVABILITY_PARTIAL,
                SurvivabilityType.SURVIVABILITY_PARTIAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any", "any", "any", "any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(2,2,2,2,2,2,2,2);
        Integer minPipes = 4;
        Integer maxPipes = 6;
        String connectionId = "test";
        Integer numPaths = 10;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void oneMinOneMaxOfThreeNonPalindromic(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 1;
        Integer maxPipes = 1;
        String connectionId = "test";
        Integer numPaths = 1;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void oneMinTwoMaxOfThreeNonPalindromic(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_NONE,
                SurvivabilityType.SURVIVABILITY_NONE, SurvivabilityType.SURVIVABILITY_NONE);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 1);
        Integer minPipes = 1;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 2;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void oneMinOneMaxOfThreeOneSurvivableTwoNot(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_TOTAL,
                SurvivabilityType.SURVIVABILITY_TOTAL, SurvivabilityType.SURVIVABILITY_TOTAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 1, 2);
        Integer minPipes = 1;
        Integer maxPipes = 1;
        String connectionId = "test";
        Integer numPaths = 1;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void oneMinOneMaxOfThreeTwoSurvivableOneNot(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_TOTAL,
                SurvivabilityType.SURVIVABILITY_TOTAL, SurvivabilityType.SURVIVABILITY_TOTAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(1, 2, 2);
        Integer minPipes = 1;
        Integer maxPipes = 1;
        String connectionId = "test";
        Integer numPaths = 1;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void oneMinOneMaxOfThreeThreeSurvivable(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_TOTAL,
                SurvivabilityType.SURVIVABILITY_TOTAL, SurvivabilityType.SURVIVABILITY_TOTAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(2, 2, 2);
        Integer minPipes = 1;
        Integer maxPipes = 1;
        String connectionId = "test";
        Integer numPaths = 2;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void oneMinTwoMaxOfThreeTwoSurvivableOneNot(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_TOTAL,
                SurvivabilityType.SURVIVABILITY_TOTAL, SurvivabilityType.SURVIVABILITY_TOTAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(2, 1, 2);
        Integer minPipes = 1;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 3;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void oneMinTwoMaxOfThreeThreeSurvivable(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_TOTAL,
                SurvivabilityType.SURVIVABILITY_TOTAL, SurvivabilityType.SURVIVABILITY_TOTAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(2, 2, 2);
        Integer minPipes = 1;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 4;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void twoMinTwoMaxOfThreeTwoSurvivableOneNot(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_TOTAL,
                SurvivabilityType.SURVIVABILITY_TOTAL, SurvivabilityType.SURVIVABILITY_TOTAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(2, 1, 2);
        Integer minPipes = 2;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 3;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void twoMinTwoMaxOfThreeThreeSurvivable(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_TOTAL,
                SurvivabilityType.SURVIVABILITY_TOTAL, SurvivabilityType.SURVIVABILITY_TOTAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(2, 2, 2);
        Integer minPipes = 2;
        Integer maxPipes = 2;
        String connectionId = "test";
        Integer numPaths = 4;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void twoMinThreeMaxOfThreeThreeSurvivable(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_TOTAL,
                SurvivabilityType.SURVIVABILITY_TOTAL, SurvivabilityType.SURVIVABILITY_TOTAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(2, 2, 2);
        Integer minPipes = 2;
        Integer maxPipes = 3;
        String connectionId = "test";
        Integer numPaths = 6;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    @Test
    public void twoMinThreeMaxOfThreeThreeSurvivableOneFails(){

        List<String> aPorts = Arrays.asList("kans-cr5:10/1/1", "elpa-cr5:10/1/3", "star-cr5:1/2/1");
        List<String> aDevices = Arrays.asList("kans-cr5", "elpa-cr5", "star-cr5");
        List<String> zPorts = Arrays.asList("wash-cr5:10/1/10", "nash-cr5:10/1/3", "newy-cr5:10/1/2");
        List<String> zDevices = Arrays.asList("wash-cr5", "nash-cr5", "newy-cr5");
        List<Integer> azMbpsList = Arrays.asList(25, 50, 75);
        List<Integer> zaMbpsList = Arrays.asList(25, 50, 75);
        List<PalindromicType> palindromicList = Arrays.asList(PalindromicType.NON_PALINDROME, PalindromicType.NON_PALINDROME,
                PalindromicType.NON_PALINDROME);
        List<SurvivabilityType> survivableList = Arrays.asList(SurvivabilityType.SURVIVABILITY_TOTAL,
                SurvivabilityType.SURVIVABILITY_TOTAL, SurvivabilityType.SURVIVABILITY_TOTAL);
        List<String> vlanExps = Arrays.asList("any", "any", "any");
        List<Integer> numDisjoints = Arrays.asList(5, 2, 2);
        Integer minPipes = 2;
        Integer maxPipes = 3;
        String connectionId = "test";
        Integer numPaths = 4;

        buildThenEvaluate(aPorts, aDevices, zPorts, zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList,
                vlanExps, numDisjoints, new ArrayList<>(), minPipes, maxPipes, connectionId, numPaths, true);
    }

    private void buildThenEvaluate(List<String> aPorts, List<String> aDevices, List<String> zPorts,
                                   List<String> zDevices, List<Integer> azMbpsList, List<Integer> zaMbpsList,
                                   List<PalindromicType> palindromicList, List<SurvivabilityType> survivableList,
                                   List<String> vlanExps, List<Integer> numDisjoints, List<Integer> priorities,
                                   Integer minPipes, Integer maxPipes, String connectionId, Integer numPaths,
                                   Boolean shouldSucceed){
        topologyBuilder.buildTopoEsnet();

        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.DAYS).getEpochSecond());
        ScheduleSpecificationE requestedSched = entityBuilder.buildSchedule(startDate, endDate);
        RequestedBlueprintE requestedBlueprint = entityBuilder.buildRequest(aPorts, aDevices, zPorts,
                zDevices, azMbpsList, zaMbpsList, palindromicList, survivableList, vlanExps,
                numDisjoints, priorities, minPipes, maxPipes, connectionId);
        evaluateSuccess(requestedBlueprint, requestedSched, minPipes, numPaths, shouldSucceed);
    }

    private void evaluateSuccess(RequestedBlueprintE requestedBlueprint, ScheduleSpecificationE requestedSched,
                                 Integer minPipes, Integer numPaths, Boolean shouldSucceed){


        Optional<ReservedBlueprintE> reservedBlueprint;
        try
        {
            reservedBlueprint = topPCE.makeReserved(requestedBlueprint, requestedSched, new ArrayList<>());
        }
        catch(PCEException | PSSException pceE){
            log.error("", pceE);
            reservedBlueprint = Optional.empty();
        }

        // If reservation was not successful: Return true if it shouldn't have succeeded, False if it should have worked.
        if(!reservedBlueprint.isPresent()) {
            assert(!shouldSucceed);
        }
        else{
            ReservedVlanFlowE flow = reservedBlueprint.get().getVlanFlow();
            Set<BidirectionalPathE> paths = flow.getAllPaths();
            Set<ReservedMplsPipeE> mplsPipes = flow.getMplsPipes();
            assert(mplsPipes.size() >= minPipes);
            assert(mplsPipes.size() <= numPaths);
            assert(paths.size() == numPaths);
        }
    }

    private void testTopology(){
        Topology ethernetTopo = topoService.layer(Layer.ETHERNET);
        Topology mplsTopo = topoService.layer(Layer.MPLS);
        Topology internalTopo = topoService.layer(Layer.INTERNAL);

        Set<TopoEdge> internalEdges = internalTopo.getEdges();
        Set<TopoEdge> mplsEdges = mplsTopo.getEdges();
        Set<TopoEdge> ethernetEdges = ethernetTopo.getEdges();

        Set<TopoEdge> internalMplsEdges = mplsEdges.stream().filter(e -> e.getLayer().equals(Layer.INTERNAL)).collect(Collectors.toSet());
        Set<TopoEdge> internalEthernetEdges = ethernetEdges.stream().filter(e -> e.getLayer().equals(Layer.INTERNAL)).collect(Collectors.toSet());

        Set<TopoEdge> overlapInternalMplsEdges = internalEdges.stream()
                .filter(eI -> mplsEdges.stream().anyMatch(eM -> eI.getA().equals(eM.getA()) && eI.getZ().equals(eM.getZ())))
                .collect(Collectors.toSet());

        Set<TopoEdge> overlapInternalEthernetEdges = internalEdges.stream()
                .filter(eI -> ethernetEdges.stream().anyMatch(eE -> eI.getA().equals(eE.getA()) && eI.getZ().equals(eE.getZ())))
                .collect(Collectors.toSet());

        Set<TopoVertex> internalAndMpls = internalTopo.getVertices().stream()
                .filter(vI -> mplsTopo.getVertices().stream().anyMatch(vM -> vI.getUrn().equals(vM.getUrn())))
                .collect(Collectors.toSet());

        Set<TopoVertex> internalAndEthernet = internalTopo.getVertices().stream()
                .filter(vI -> ethernetTopo.getVertices().stream().anyMatch(vE -> vI.getUrn().equals(vE.getUrn())))
                .collect(Collectors.toSet());

        Set<TopoVertex> ethernetAndMpls = mplsTopo.getVertices().stream()
                .filter(vM -> ethernetTopo.getVertices().stream().anyMatch(vE -> vM.getUrn().equals(vE.getUrn())))
                .collect(Collectors.toSet());

        Set<TopoVertex> internalOnly = internalTopo.getVertices().stream()
                .filter(vI -> ethernetTopo.getVertices().stream().noneMatch(vE -> vI.getUrn().equals(vE.getUrn()))
                        && mplsTopo.getVertices().stream().noneMatch(vM -> vI.getUrn().equals(vM.getUrn())))
                .collect(Collectors.toSet());

        log.info("Yo");
    }

}
