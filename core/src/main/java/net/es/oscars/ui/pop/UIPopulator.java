package net.es.oscars.ui.pop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.authnz.dao.UserRepository;
import net.es.oscars.authnz.ent.EPermissions;
import net.es.oscars.authnz.ent.EUser;
import net.es.oscars.authnz.prop.AuthnzProperties;
import net.es.oscars.dto.viz.Position;
import net.es.oscars.ui.prop.UIProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Data
public class UIPopulator {
    @Autowired
    private UIProperties properties;

    private Map<String, Position> positions;


    @PostConstruct
    public void loadPositions() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File positionsFile = properties.getPositionsFile();
        positions = mapper.readValue(positionsFile, new TypeReference< Map<String, Position>>() {});
        log.info("positions imported for devices: " + positions.size());

    }





}
