package net.es.oscars.ds.conf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.conf.dao.ConfigRepository;
import net.es.oscars.ds.conf.ent.EStartupConfig;
import net.es.oscars.ds.conf.props.StartupConfigContainer;
import net.es.oscars.ds.conf.props.StartupConfigEntry;
import net.es.oscars.dto.cfg.StartupConfig;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class ConfigController {
    @Autowired
    private ConfigRepository repository;

    @Autowired
    private StartupConfigContainer startup;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a resource which didn't exist", ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // LOG.warn("user requested a resource which didn't exist", ex);
    }


    @PostConstruct
    public void initDefaults() throws JsonProcessingException {
        StartupConfigEntry defaultCfg = startup.getDefaults();

        for (StartupConfigEntry cfg : startup.getStartupConfigs()) {

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


    @RequestMapping(value = "/configs/all", method = RequestMethod.GET)
    @ResponseBody
    public List<String> listComponents() {
        log.info("listing all");

        return repository.findAll().stream().map(
                EStartupConfig::getName).collect(Collectors.toCollection(ArrayList::new));
    }


    @RequestMapping(value = "/configs/update", method = RequestMethod.POST)
    @ResponseBody
    public StartupConfig update(@RequestBody StartupConfig startupConfig) {
        log.info("updating " + startupConfig.getName());

        Optional<EStartupConfig> maybeConfig = repository.findByName(startupConfig.getName());
        maybeConfig.orElseThrow(NoSuchElementException::new);
        EStartupConfig configEnt = maybeConfig.get();


        configEnt.setConfigJson(startupConfig.getConfigJson());
        repository.save(configEnt);
        return startupConfig;
    }


    @RequestMapping(value = "/configs/get/{component}", method = RequestMethod.GET)
    @ResponseBody
    public String getConfig(@PathVariable("component") String component) {
        log.info("retrieving " + component);
        Optional<EStartupConfig> maybeConfig = repository.findByName(component);
        maybeConfig.orElseThrow(NoSuchElementException::new);
        EStartupConfig configEnt = maybeConfig.get();

        return configEnt.getConfigJson();
    }

    @RequestMapping(value = "/configs/delete/{component}", method = RequestMethod.GET)
    @ResponseBody
    public String delConfig(@PathVariable("component") String component) {
        log.info("deleting " + component);

        Optional<EStartupConfig> maybeConfig = repository.findByName(component);
        maybeConfig.orElseThrow(NoSuchElementException::new);
        EStartupConfig configEnt = maybeConfig.get();

        repository.delete(configEnt);
        return "deleted";
    }

}