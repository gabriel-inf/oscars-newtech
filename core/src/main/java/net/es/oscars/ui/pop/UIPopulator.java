package net.es.oscars.ui.pop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.viz.Position;
import net.es.oscars.helpers.JsonHelper;
import net.es.oscars.topo.prop.TopoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@Data
public class UIPopulator {
    @Autowired
    private TopoProperties topoProperties;

    @Autowired
    private JsonHelper jsonHelper;

    private Map<String, Position> positions;

    public void startup() throws IOException {
        ObjectMapper mapper = jsonHelper.mapper();

        String filename = "./config/topo/"+topoProperties.getPrefix()+"-positions.json";
        File jsonFile = new File(filename);

        positions = mapper.readValue(jsonFile, new TypeReference< Map<String, Position>>() {});
        log.info("positions imported for devices: " + positions.size());

    }





}
