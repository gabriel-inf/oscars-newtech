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

    private Topology topology = null;
    private Map<String, Position> positions = null;
    private Map<String, Set<String>> devicePortMap = null;

    private final String oscarsUrl = "https://localhost:8000";

    public Topology getTopology() {
        if (topology == null) {
            String restPath = oscarsUrl + "/topo/multilayer";
            topology = restTemplate.getForObject(restPath, Topology.class);
        }

        return topology;
    }

    public Map<String, Set<String>> devicePortMap() {
        if (devicePortMap == null) {
            devicePortMap = new HashMap<>();
            String restPath = oscarsUrl + "/topo/device_port_map";

            Map<String, List<String>> receivedMap = restTemplate.getForObject(restPath, Map.class);
            receivedMap.keySet().forEach(d -> {
                devicePortMap.put(d, new HashSet<>());
                devicePortMap.get(d).addAll(receivedMap.get(d));

            });

        }

        return devicePortMap;
    }
    public Map<String, Set<String>> getHubs() {
        Map<String, Set<String>> result = new HashMap<>();
        return result;
    }

    public Map<String, Position> getPositions() {
        if (positions == null) {
            positions = new HashMap<>();
            String restPath = oscarsUrl + "/ui/positions";
            Map<String, Map<String,Integer>> rcvd = restTemplate.getForObject(restPath, Map.class);

            rcvd.keySet().forEach(d -> {
                Integer x = rcvd.get(d).get("x");
                Integer y = rcvd.get(d).get("y");
                Position p = Position.builder().x(x).y(y).build();
                positions.put(d, p);
            });
        }

        return positions;
    }


}
