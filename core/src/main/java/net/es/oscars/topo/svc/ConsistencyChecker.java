package net.es.oscars.topo.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.pss.ent.UrnAddressE;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.ui.pop.UIPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Component
@Transactional
public class ConsistencyChecker {
    private UrnAdjcyRepository adjcyRepo;

    private UrnRepository urnRepo;

    private UrnAddressRepository urnAddrRepo;

    private UIPopulator ui;


    @Autowired
    public ConsistencyChecker(UrnAdjcyRepository adjcyRepo, UrnRepository urnRepo,
                              UrnAddressRepository urnAddrRepo, UIPopulator ui) {
        this.adjcyRepo = adjcyRepo;
        this.urnRepo = urnRepo;
        this.urnAddrRepo = urnAddrRepo;
        this.ui = ui;
    }

    public boolean checkConsistency() {
        log.debug("checking data consistency..");

        Map<String, Boolean> checks = new HashMap<>();
        checks.put("device addresses", this.checkDeviceAddresses());
        checks.put("mpls ifce addresses", this.checkMplsIfceAddresses());
        checks.put("address urns exist", this.checkAddressUrnsExist());
        checks.put("inverse adjacencies", this.checkInverseAdjacencies());

        checks.put("positions match devices", this.checkAllPositionsHaveDevices());
        checks.put("devices have positions", this.checkDevicePositions());

        boolean result = true;
        for (String key : checks.keySet()) {
            if (!checks.get(key)) {
                log.error("consistency check "+key+" failed");
                result = false;
            }
        }

        return result;
    }

    public boolean checkDevicePositions() {
        log.debug("checking that all devices have positions ..");
        Set<String> devicesInPositions = this.ui.getPositions().keySet();
        Set<String> devicesInUrns = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());

        Set<String> devicesWithoutPositions = new HashSet<>();
        for (String device : devicesInUrns) {
            if (!devicesInPositions.contains(device)) {
                log.error("position entry missing for device "+device);
                devicesWithoutPositions.add(device);
            }
        }
        return devicesWithoutPositions.isEmpty();
    }
    public boolean checkAllPositionsHaveDevices() {
        log.debug("checking that all position entries match a device Urn..");
        Set<String> devicesInPositions = this.ui.getPositions().keySet();
        Set<String> devicesInUrns = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());
        Set<String> positionsWithoutDevices = new HashSet<>();

        for (String device : devicesInPositions) {
            if (!devicesInUrns.contains(device)) {
                positionsWithoutDevices.add(device);
                log.error("urn in position entry not found: "+device);
            }
        }
        return positionsWithoutDevices.isEmpty();

    }

    public boolean checkDeviceAddresses() {
        log.debug("checking device addresses ..");
        Set<String> urnsThatShouldHaveAddresses = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());
        return this.checkIfAddressesExist(urnsThatShouldHaveAddresses);
    }

    public boolean checkAddressUrnsExist() {
        log.debug("checking if address URNs all exist..");
        Set<String> allUrnsWithAddresses = this.urnAddrRepo.findAll().stream()
                .map(UrnAddressE::getUrn).collect(Collectors.toSet());
        Set<String> allKnownUrns = this.urnRepo.findAll().stream()
                .map(UrnE::getUrn).collect(Collectors.toSet());

        boolean result = true;
        for (String urn : allUrnsWithAddresses) {
            if (!allKnownUrns.contains(urn)) {
                log.error("have address entry for non-existent "+urn);
                result = false;
            }
        }
        return result;
    }

    public boolean checkMplsIfceAddresses() {
        log.debug("checking mpls ifce addresses..");
        Set<String> urnsThatShouldHaveAddresses = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.IFCE) && t.getCapabilities().contains(Layer.MPLS))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());
        return this.checkIfAddressesExist(urnsThatShouldHaveAddresses);
    }
    private boolean checkIfAddressesExist(Set<String> urnsThatShouldHaveAddresses) {

        Set<String> allUrnsWithAddresses = this.urnAddrRepo.findAll().stream()
                .map(UrnAddressE::getUrn).collect(Collectors.toSet());

        Set<String> urnsThatShouldHaveAddressesButDoNot = new HashSet<>();
        for (String urn : urnsThatShouldHaveAddresses) {
            if (!allUrnsWithAddresses.contains(urn)) {
                log.error("ip address missing for urn "+urn);
                urnsThatShouldHaveAddressesButDoNot.add(urn);
            }
        }
        return urnsThatShouldHaveAddressesButDoNot.isEmpty();
    }


    public boolean checkInverseAdjacencies() {
        log.debug("checking inverse adjacencies..");
        List<UrnAdjcyE> adjcies = this.adjcyRepo.findAll();
        boolean result = true;
        for (UrnAdjcyE adjcy : adjcies) {
            UrnE a = adjcy.getA();
            UrnE z = adjcy.getZ();
            Map<Layer, Long> metrics = adjcy.getMetrics();
            boolean inverseFound = false;
            boolean inverseMetricsOk = false;
            for (UrnAdjcyE maybeInverse : adjcies) {
                if (maybeInverse.getZ().equals(a) && maybeInverse.getA().equals(z)) {
                    inverseFound = true;
                    inverseMetricsOk = true;

                    boolean allMetricsFound = true;
                    for (Layer l : metrics.keySet()) {
                        if (!maybeInverse.getMetrics().keySet().contains(l)) {
                            allMetricsFound = false;
                        }
                    }
                    if (!allMetricsFound) {
                        inverseMetricsOk = false;
                    }
                }
            }

            if (!inverseFound) {
                log.error("could not find inverse for "+a.getUrn()+" -- "+z.getUrn());
                result = false;
            } else if (!inverseMetricsOk) {
                log.error("could not find matching metrics for "+a.getUrn()+" -- "+z.getUrn());
                result = false;
            }
        }
        return result;



    }

}
