package net.es.oscars.webui.ipc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.viz.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
public class TopologyProvider {
    @Autowired
    private RestTemplate restTemplate;


    public Topology getTopology() {

        String restPath = "https://localhost:8000/topo/multilayer";
        Topology topology = restTemplate.getForObject(restPath, Topology.class);

        return topology;
    }

    public Map<String, Set<String>> devicePortMap() {
        String restPath = "https://localhost:8000/topo/device_port_map";

        Map<String, List<String>> receivedMap = restTemplate.getForObject(restPath, Map.class);
        Map<String, Set<String>> portMap = new HashMap<>();
        receivedMap.keySet().forEach(d -> {
            portMap.put(d, new HashSet<>());
            portMap.get(d).addAll(receivedMap.get(d));

        });

        return portMap;
    }
    public Map<String, Set<String>> getHubs() {
        Map<String, Set<String>> result = new HashMap<>();
        return result;
    }

    public Map<String, Position> getPositions() {
        Map<String, Position> positionMap = new HashMap<>();
        return positionMap;
    }


}
