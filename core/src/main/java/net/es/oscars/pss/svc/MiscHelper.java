package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.params.MplsHop;
import net.es.oscars.dto.pss.params.MplsPath;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.pss.ent.UrnAddressE;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MiscHelper {
    @Autowired
    private UrnAddressRepository addrRepo;

    @Autowired
    private TopoService topoService;


    public String vlanString(ReservedVlanFixtureE f) {
        List<String> vlans = f.getReservedVlans()
                .stream()
                .map(ReservedVlanE::getVlan)
                .map(Object::toString)
                .collect(Collectors.toList());
        return String.join(",", vlans);
    }


    public Optional<Integer> junctionVcId(ReservedVlanJunctionE rvj) {
        return pssResourceOfType(rvj.getReservedPssResources(), ResourceType.VC_ID);
    }

    public Optional<Integer> junctionSdpId(ReservedVlanJunctionE rvj) {
        return pssResourceOfType(rvj.getReservedPssResources(), ResourceType.ALU_SDP_ID);
    }

    private Optional<Integer> pssResourceOfType(Set<ReservedPssResourceE> resources, ResourceType rt) {
        Optional<Integer> resource = Optional.empty();

        for (ReservedPssResourceE rps : resources) {
            if (rps.getResourceType().equals(rt)) {
                resource = Optional.of(rps.getResource());
            }
        }
        return resource;


    }

    private List<String> filterEro(List<String> ero) throws PSSException {
        List<String> filteredEro = new ArrayList<>();

        if (ero.size() < 2) {
            throw new PSSException("invalid ERO size < 2");
        } else if (ero.size() % 3 != 2) {
            throw new PSSException("invalid ERO size (should be 2 modulo 3)"+ ero.size());
        }
        for (int i = 0; i < ero.size() - 1; i++) {
            if (i % 3 == 1) {
                log.info("passed filtering "+ero.get(i));
                filteredEro.add(ero.get(i));
            }
        }
        return filteredEro;
    }

    public MplsPath mplsPathBuilder(ConnectionE conn, List<String> ero) throws PSSException {
        String name = "oscars-path-" + conn.getConnectionId();
        List<MplsHop> hops = new ArrayList<>();
        Integer order = 0;
        List<String> filteredEro = filterEro(ero);

        for (String urn : filteredEro) {

            String addr = urn;
            Optional<UrnAddressE> maybeAddr = addrRepo.findByUrn(urn);
            if (maybeAddr.isPresent()) {
                addr = maybeAddr.get().getIpv4Address();
            } else {
                throw new PSSException("could not locate address for "+urn);
            }


            MplsHop hop = MplsHop.builder()
                    .address(addr)
                    .order(order)
                    .build();

            order += 1;
            hops.add(hop);
        }

        return MplsPath.builder()
                .name(name)
                .hops(hops)
                .build();
    }
}
