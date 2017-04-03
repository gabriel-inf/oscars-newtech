package net.es.oscars.whatif;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.bwavail.svc.BandwidthAvailabilityService;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.spec.ReservedBandwidth;
import net.es.oscars.dto.spec.ReservedEthPipe;
import net.es.oscars.dto.spec.ReservedMplsPipe;
import net.es.oscars.dto.spec.ReservedVlanJunction;
import net.es.oscars.dto.topo.BidirectionalPath;
import net.es.oscars.dto.topo.Edge;
import net.es.oscars.pce.helpers.RepoEntityBuilder;
import net.es.oscars.pce.helpers.TopologyBuilder;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.svc.DateService;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.whatif.dto.WhatifSpecification;
import net.es.oscars.whatif.svc.SuggestionGenerator;
import net.es.oscars.whatif.svc.SuggestionService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional
public class SuggestionGeneratorTest  extends AbstractCoreTest {

    @Autowired
    private SuggestionGenerator suggestionGenerator;

    @Autowired
    private BandwidthAvailabilityService bandwidthAvailabilityService;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private DateService dateService;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private ReservedBandwidthRepository reservedBandwidthRepo;

    @Autowired
    private RepoEntityBuilder repoEntityBuilder;


    @Test
    public void startEndVolumeWithOneConnection(){
        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopo2();

        reserveBandwidth();

        String startDate = "01 01 2020 00:00";
        String endDate = "01 02 2020 00:00";
        Set<String> srcPorts = Collections.singleton("portA");
        String srcDevice = "nodeP";
        Set<String> dstPorts = Collections.singleton("portZ");
        String dstDevice = "nodeM";
        Integer volume = 100000;
        Integer bandwidthMbps = null;
        Long durationMinutes = null;
        WhatifSpecification spec = WhatifSpecification.builder()
                .startDate(startDate)
                .endDate(endDate)
                .srcPorts(srcPorts)
                .srcDevice(srcDevice)
                .dstPorts(dstPorts)
                .dstDevice(dstDevice)
                .volume(volume)
                .bandwidthMbps(bandwidthMbps)
                .durationMinutes(durationMinutes)
                .build();


        BandwidthAvailabilityResponse bwResponse = getBwMap(srcDevice, srcPorts, dstDevice, dstPorts, startDate, endDate);

        List<Connection> suggestions = suggestionGenerator.generateWithStartEndVolume(spec, bwResponse);
        List<Integer> bws = Arrays.asList(2);

        assert(suggestions.size() == 1);
        for(Integer i = 0; i < suggestions.size(); i++){
            Connection conn = suggestions.get(i);
            Integer bw = bws.get(i);
            Set<ReservedEthPipe> ethPipes = conn.getReserved().getVlanFlow().getEthPipes();
            Set<ReservedMplsPipe> mplsPipes = conn.getReserved().getVlanFlow().getMplsPipes();
            assert(mplsPipes.isEmpty());
            assert(ethPipes.size() == 2);

            for(ReservedEthPipe pipe : ethPipes){
                Set<ReservedBandwidth> reservedBandwidths = pipe.getReservedBandwidths();
                assert(reservedBandwidths.stream().allMatch(rsvBw -> rsvBw.getInBandwidth().equals(bw) && rsvBw.getEgBandwidth().equals(bw)));

                ReservedVlanJunction junctionA = pipe.getAJunction();
                ReservedVlanJunction junctionZ = pipe.getZJunction();
                assert(junctionA.getFixtures().stream().allMatch(fix -> fix.getReservedBandwidth().getInBandwidth().equals(bw)));
                assert(junctionZ.getFixtures().stream().allMatch(fix -> fix.getReservedBandwidth().getInBandwidth().equals(bw)));
            }
        }
    }

    private void reserveBandwidth(){

        List<String> reservedPortNames = new ArrayList<>();
        List<Instant> reservedStartTimes = new ArrayList<>();
        List<Instant> reservedEndTimes = new ArrayList<>();
        List<Integer> inBandwidths = new ArrayList<>();
        List<Integer> egBandwidths = new ArrayList<>();

        reservedPortNames.add("portA");
        reservedStartTimes.add(dateService.parseDate("01 01 2020 05:00").toInstant());
        reservedEndTimes.add(dateService.parseDate("01 01 2020 10:00").toInstant());
        inBandwidths.add(998);
        egBandwidths.add(998);

        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
    }


    private BandwidthAvailabilityResponse getBwMap(String srcDevice, Set<String> srcPorts, String dstDevice, Set<String> dstPorts,
                                                   String startDate, String endDate){
        BandwidthAvailabilityRequest bwRequest = suggestionService.createBwAvailRequest(srcDevice, srcPorts, dstDevice,
                dstPorts, 0, 0, dateService.parseDate(startDate), dateService.parseDate(endDate));
        return bandwidthAvailabilityService.getBandwidthAvailabilityMap(bwRequest);
    }
}
