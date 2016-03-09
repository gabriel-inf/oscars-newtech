package net.es.oscars.ds.conf.pop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.conf.dao.ConfigRepository;
import net.es.oscars.ds.conf.ent.EStartupConfig;
import net.es.oscars.ds.conf.prop.StartupConfigContainer;
import net.es.oscars.ds.conf.prop.StartupConfigEntry;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Component
public class ConfigPopulator {
    @Autowired
    private StartupConfigContainer startup;

    @Autowired
    private ConfigRepository repository;


    @PostConstruct
    public void initDefaults() throws JsonProcessingException {
        StartupConfigEntry defaultCfg = startup.getDefaults();
        if (startup.getModules() == null) {
            log.error("Could not retrieve module configurations from application properties!");
            return;
        }

        for (StartupConfigEntry cfg : startup.getModules()) {

            String name = cfg.getName();
            Integer port = cfg.getServer_port();
            cfg = SerializationUtils.clone(defaultCfg);
            cfg.setName(name);
            cfg.setServer_port(port);


            Optional<EStartupConfig> maybeConfig = repository.findByName(name);

            if (maybeConfig.isPresent()) {
                log.info("config for " + name + " already in db, skipping");

            } else {
                log.info("saving a new default JSON config for service " + name);
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(cfg);
                log.info("json string: " + jsonString);

                EStartupConfig configEnt = new EStartupConfig();
                configEnt.setName(name);
                configEnt.setConfigJson(jsonString);

                repository.save(configEnt);
            }

        }

    }
}
