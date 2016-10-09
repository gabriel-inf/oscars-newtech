package net.es.oscars.webui.ipc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.auth.User;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.viz.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public Map<String, Set<String>> getHubs() {
        Map<String, Set<String>> result = new HashMap<>();
        return result;
    }

    public Map<String, Position> getPositions() {
        Map<String, Position> positionMap = new HashMap<>();
        return positionMap;
    }


}
