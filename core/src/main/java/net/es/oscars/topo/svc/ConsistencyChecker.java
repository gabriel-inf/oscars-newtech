package net.es.oscars.topo.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.pss.ent.UrnAddressE;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.topo.dao.ReservableBandwidthRepository;
import net.es.oscars.topo.dao.ReservableVlanRepository;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.ui.pop.UIPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        checks.put("positions", this.checkPositions());

        boolean result = true;
        for (String key : checks.keySet()) {
            if (!checks.get(key)) {
                log.error("consistency check "+key+" failed");
                result = false;
            }
        }

        return result;
    }

    public boolean checkPositions() {
        log.debug("checking positions consistency..");
        Set<String> devicesInPositions = this.ui.getPositions().keySet();
        Set<String> devicesInUrns = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());

        Set<String> devicesWithoutPositions = new HashSet<>();
        for (String device : devicesInUrns) {
            if (!devicesInPositions.contains(device)) {
                devicesWithoutPositions.add(device);
            }
        }
        Set<String> positionsWithoutDevices = new HashSet<>();
        for (String device : devicesInPositions) {
            if (!devicesInUrns.contains(device)) {
                positionsWithoutDevices.add(device);
            }
        }
        if (positionsWithoutDevices.isEmpty() && devicesWithoutPositions.isEmpty()) {
            return true;
        }
        return false;
    }
    public boolean checkDeviceAddresses() {
        log.debug("checking address consistency..");
        Set<String> devicesInUrns = this.urnRepo.findAll().stream()
                .filter(t -> t.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toSet());
        Set<String> urnsWithAddresses = this.urnAddrRepo.findAll().stream()
                .map(UrnAddressE::getUrn).collect(Collectors.toSet());

        Set<String> devicesWithoutAddrs = new HashSet<>();
        for (String device : devicesInUrns) {
            if (!urnsWithAddresses.contains(device)) {
                devicesWithoutAddrs.add(device);
            }
        }

        return devicesWithoutAddrs.isEmpty();

    }

}
