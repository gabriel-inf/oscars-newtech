package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.rsrc.ReservableBandwidth;
import net.es.oscars.dto.spec.ReservedBandwidth;
import net.es.oscars.webui.ipc.TopologyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class TopologyController
{
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TopologyProvider topologyProvider;

    private final String oscarsUrl = "https://localhost:8000";

    @RequestMapping(value = "/topology/reservedbw", method = RequestMethod.POST)
    @ResponseBody
    public List<ReservedBandwidth> get_reserved_bw(@RequestBody List<String> resUrns)
    {
        String restPath = oscarsUrl + "/topo/reservedbw";

        HttpEntity<List<String>> requestEntity = new HttpEntity<>(resUrns);
        ParameterizedTypeReference<List<ReservedBandwidth>> typeRef = new ParameterizedTypeReference<List<ReservedBandwidth>>() {};
        ResponseEntity<List<ReservedBandwidth>> response = restTemplate.exchange(restPath, HttpMethod.POST, requestEntity, typeRef);

        List<ReservedBandwidth> relevantBwItems = response.getBody();

        return relevantBwItems;
    }

    @RequestMapping(value = "/topology/bwcapacity", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> get_port_capacity(@RequestBody List<String> ports)
    {
        Map urn2CapMap = new HashMap<>();

        List<ReservableBandwidth> bwCapList = topologyProvider.getPortCapacities();

        bwCapList = bwCapList.stream()
                .filter(bwCap -> ports.contains(bwCap.getTopoVertexUrn()))
                .collect(Collectors.toList());

        for(int p = 0; p < bwCapList.size(); p++)
        {
            ReservableBandwidth oneBW = bwCapList.get(p);
            Integer minCap = Math.min(oneBW.getIngressBw(), oneBW.getEgressBw());
            urn2CapMap.put(oneBW.getTopoVertexUrn(), minCap);
        }

        return urn2CapMap;
    }



    @RequestMapping( value = "/topology/getbwavl" , method = RequestMethod.POST)

    @ResponseBody
    public BandwidthAvailabilityResponse getBwdata(@RequestBody List<String> theERO, List<String> therERO, Date startTime, Date endTime)
    {

        String restPath = oscarsUrl +  "/bwavail/path" ;
        BandwidthAvailabilityRequest bwRequest = new BandwidthAvailabilityRequest();
        {
            bwRequest.setStartDate(startTime);
            bwRequest.setEndDate(endTime);
            bwRequest.setMinAzBandwidth(0);
            bwRequest.setMinAzBandwidth(0);

            List<List<String>> eroList = new ArrayList<>();
            eroList.add(theERO);
            bwRequest.setAzEros(eroList);

            List<List<String>> eroListr = new ArrayList<>();
            eroListr.add(therERO);
            bwRequest.setZaEros(eroListr);

        }

        BandwidthAvailabilityResponse bwResponse = restTemplate.postForObject(restPath, bwRequest, BandwidthAvailabilityResponse.class);

        return bwResponse;

    }


    @RequestMapping(value = "/topology/deviceportmap/full", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Set<String>> get_device2port_map()
    {
        Map<String, Set<String>> fullPortMap = topologyProvider.devicePortMap();

        return fullPortMap;
    }


    @RequestMapping(value = "/topology/deviceportmap/{deviceURN}", method = RequestMethod.GET)
    @ResponseBody
    public Set<String> get_single_port_set(@PathVariable String deviceURN)
    {
        Map<String, Set<String>> fullPortMap = topologyProvider.devicePortMap();

        Set<String> portMap = fullPortMap.get(deviceURN);

        if(portMap == null)
            portMap = new HashSet<>();

        return portMap;
    }

    @RequestMapping(value = "/topology/portdevicemap/full", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> get_port2device_map()
    {
        Map<String, String> fullDeviceMap = topologyProvider.portDeviceMap();

        return fullDeviceMap;
    }
}
