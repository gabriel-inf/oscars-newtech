package net.es.oscars.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.ReservableVlanE;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.IfceType;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.prop.TopoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;


@Slf4j
@Service
public class TopoFileImporter implements TopoImporter {

    @Autowired
    private UrnRepository urnRepo;

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


        List<Device> devices = importDevicesFromFile(devicesFilename);
        devices.stream().forEach(t -> log.info(t.toString()));

        List<UrnAdjcyE> newAdjcies = importAdjciesFromFile(adjciesFilename);
        newAdjcies.stream().forEach(t -> log.info(t.toString()));

        List<UrnAdjcyE> deviceAdjcies = adjciesFromDevices(devices);
        deviceAdjcies.stream().forEach(t -> log.info(t.toString()));

        if (overwrite) {
            log.info("Overwrite set; deleting topology DB entries.");
            urnRepo.deleteAll();
            adjcyRepo.deleteAll();
        }

        List<UrnE> urns = urnRepo.findAll();

        if (urns.isEmpty()) {
            log.info("URN DB empty. Will replace with input from devices file "+devicesFilename);
            List<UrnE> newUrns = this.urnsFromDevices(devices);

            urnRepo.save(newUrns);
        } else {
            log.info("Devices DB is not empty; skipping import");
        }

        List<UrnAdjcyE> adjcies = adjcyRepo.findAll();
        if (adjcies.isEmpty()) {
            log.info("Adjacencies DB empty. Will replace with input from adjacencies file "+adjciesFilename);
            adjcyRepo.save(newAdjcies);
            adjcyRepo.save(deviceAdjcies);
        } else {
            log.info("Adjacencies DB is not empty; skipping import");
        }

    }


    private List<UrnAdjcyE> adjciesFromDevices(List<Device> devices) {
        List<UrnAdjcyE> adjcies = new ArrayList<>();
        devices.stream().forEach(d -> {
            UrnE deviceUrn = urnRepo.findByUrn(d.getUrn()).get();
            d.getIfces().stream().forEach(i -> {
                UrnE ifceUrn = urnRepo.findByUrn(i.getUrn()).get();
                UrnAdjcyE azAdjcy = UrnAdjcyE.builder()
                        .a(deviceUrn)
                        .z(ifceUrn)
                        .metrics(new HashMap<>())
                        .build();

                UrnAdjcyE zaAdjcy = UrnAdjcyE.builder()
                        .a(deviceUrn)
                        .z(ifceUrn)
                        .metrics(new HashMap<>())
                        .build();

                azAdjcy.getMetrics().put(Layer.INTERNAL, 1L);
                zaAdjcy.getMetrics().put(Layer.INTERNAL, 1L);
                adjcies.add(azAdjcy);
                adjcies.add(zaAdjcy);
            });

        });

        return adjcies;
    }

    private List<UrnE> urnsFromDevices(List<Device> devices) {
        List<UrnE> urns = new ArrayList<>();

        devices.stream().forEach(d -> {
            UrnE deviceUrn = UrnE.builder()
                    .valid(true)
                    .urn(d.getUrn())
                    .deviceModel(d.getModel())
                    .deviceType(d.getType())
                    .urnType(UrnType.DEVICE)
                    .capabilities(d.getCapabilities())
                    .build();

            if (null != d.getReservableVlans() && !d.getReservableVlans().isEmpty()) {

                ReservableVlanE resvVlan = ReservableVlanE.builder()
                        .vlanRanges(d.getReservableVlans())
                        .urn(deviceUrn)
                        .build();
                deviceUrn.setReservableVlans(resvVlan);
            }
            urns.add(deviceUrn);


            d.getIfces().stream().forEach(i -> {
                UrnE ifceUrn = UrnE.builder()
                        .valid(true)
                        .urn(i.getUrn())
                        .urnType(UrnType.IFCE)
                        .capabilities(i.getCapabilities())
                        .ifceType(IfceType.PORT)
                        .build();

                if (null != i.getReservableBw()) {
                    ReservableBandwidthE rbw = ReservableBandwidthE.builder()
                            .bandwidth(i.getReservableBw())
                            .ingressBw(i.getReservableBw())
                            .egressBw(i.getReservableBw())
                            .urn(ifceUrn)
                            .build();
                    ifceUrn.setReservableBandwidth(rbw);
                }

                if (null != i.getReservableVlans() && !i.getReservableVlans().isEmpty()) {
                    ReservableVlanE resvVlan = ReservableVlanE.builder()
                            .vlanRanges(i.getReservableVlans())
                            .urn(ifceUrn)
                            .build();
                    ifceUrn.setReservableVlans(resvVlan);
                }
                urns.add(ifceUrn);
            });
        });

        return urns;

    }


    private List<Device> importDevicesFromFile(String filename) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(jsonFile, Device[].class));
    }

    private List<UrnAdjcyE> importAdjciesFromFile(String filename) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(jsonFile, UrnAdjcyE[].class));
    }

}
