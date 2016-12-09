package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        log.info("HERE IN TOPOLOGY CONTROLLER!!!!");
        String restPath = oscarsUrl + "/topo/reservedbw";

        HttpEntity<List<String>> requestEntity = new HttpEntity<>(resUrns);
        ParameterizedTypeReference<List<ReservedBandwidth>> typeRef = new ParameterizedTypeReference<List<ReservedBandwidth>>() {};
        ResponseEntity<List<ReservedBandwidth>> response = restTemplate.exchange(restPath, HttpMethod.POST, requestEntity, typeRef);

        List<ReservedBandwidth> relevantBwItems = response.getBody();

        return relevantBwItems;
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