package net.es.oscars.ds.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.topo.dao.DeviceRepository;
import net.es.oscars.ds.topo.dao.UrnAdjcyRepository;
import net.es.oscars.ds.topo.ent.EDevice;
import net.es.oscars.ds.topo.ent.EUrnAdjcy;
import net.es.oscars.ds.topo.prop.TopoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Service
public class TopoFileImporter implements TopoImporter {

    @Autowired
    private DeviceRepository deviceRepo;

    @Autowired
    private UrnAdjcyRepository adjcyRepo;

    @Autowired
    private TopoProperties topoProperties;

    @PostConstruct
    public void startup() {
        log.info("Startup. Will attempt import from files set in topo.[devices|adcjies].filename properties.");
        if (topoProperties == null) {
            log.error("No 'topo' stanza in application properties! Skipping topology import.");
            return;
        }

        String devicesFilename = topoProperties.getDevicesFilename();
        if (devicesFilename == null) {
            log.error("No 'topo.devices-filename' entry in application properties! Skipping topology import.");
            return;
        }
        String adjciesFilename = topoProperties.getAdjciesFilename();
        if (adjciesFilename == null) {
            log.error("No 'topo.adjcies-filename' entry in application properties! Skipping topology import.");
            return;
        }

        try {
            this.importFromFile(false, devicesFilename, adjciesFilename);
        } catch (IOException ex) {
            log.error("Import failed! " + ex.getMessage());
        }
    }

    public void importFromFile(boolean overwrite, String devicesFilename, String adjciesFilename) throws IOException {
        List<EDevice> newDevices = importDevicesFromFile(devicesFilename);
        newDevices.stream().forEach(t -> log.info(t.toString()));

        List<EUrnAdjcy> newAdjcies = importAdjciesFromFile(adjciesFilename);
        newAdjcies.stream().forEach(t -> log.info(t.toString()));

        if (overwrite) {
            log.info("Overwrite set; deleting topology DB entries.");
            deviceRepo.deleteAll();
            adjcyRepo.deleteAll();
        }

        List<EDevice> devices = deviceRepo.findAll();

        if (devices.isEmpty()) {
            log.info("Devices DB empty. Will replace with input from devices file "+devicesFilename);
            deviceRepo.save(newDevices);
        } else {
            log.info("Devices DB is not empty; skipping import");
        }


        List<EUrnAdjcy> adjcies = adjcyRepo.findAll();
        if (adjcies.isEmpty()) {
            log.info("Adjacencies DB empty. Will replace with input from adjacencies file "+adjciesFilename);
            adjcyRepo.save(newAdjcies);
        } else {
            log.info("Adjacencies DB is not empty; skipping import");
        }

    }

    private List<EDevice> importDevicesFromFile(String filename) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(jsonFile, EDevice[].class));
    }

    private List<EUrnAdjcy> importAdjciesFromFile(String filename) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(jsonFile, EUrnAdjcy[].class));
    }

}
