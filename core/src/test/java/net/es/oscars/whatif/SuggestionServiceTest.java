package net.es.oscars.whatif;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.pce.helpers.TopologyBuilder;
import net.es.oscars.whatif.dto.WhatifSpecification;
import net.es.oscars.whatif.svc.SuggestionService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Transactional
public class SuggestionServiceTest extends AbstractCoreTest{

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Test
    public void basicVolumeTest(){
        topologyBuilder.buildTopo2();
        String startDate = "01 01 2020 00:00";
        String endDate = "01 01 2022 00:00";
        Set<String> srcPorts = Collections.singleton("portA");
        String srcDevice = "nodeP";
        Set<String> dstPorts = Collections.singleton("portZ");
        String dstDevice = "nodeM";
        Integer volume = 1000;
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
        List<Connection> suggestions = suggestionService.generateSuggestions(spec);
        //assert(suggestions.size() > 0);
    }
}
