package net.es.oscars.ds.conf;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.conf.dao.ConfigRepository;
import net.es.oscars.ds.conf.ent.EStartupConfig;
import net.es.oscars.dto.cfg.StartupConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

        EStartupConfig configEnt = repository.findByName(startupConfig.getName()).orElseThrow(NoSuchElementException::new);


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