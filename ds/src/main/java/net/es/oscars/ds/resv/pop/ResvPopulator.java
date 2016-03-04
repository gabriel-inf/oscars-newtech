package net.es.oscars.ds.resv.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.resv.IdentifierType;
import net.es.oscars.ds.resv.dao.ResvRepository;
import net.es.oscars.ds.resv.dao.UrnReservedRepository;
import net.es.oscars.ds.resv.ent.EReservation;
import net.es.oscars.ds.resv.ent.EReservedIdentifier;
import net.es.oscars.ds.resv.ent.EStates;
import net.es.oscars.ds.resv.ent.EUrnReserved;
import net.es.oscars.st.resv.ResvState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Component

public class ResvPopulator {


    @Autowired
    private ResvRepository resvRepo;

    @Autowired
    private UrnReservedRepository urnRepo;


    @PostConstruct
    public void fill() {

        if (resvRepo.findAll().isEmpty()) {
            EReservation resv = new EReservation("es.net-1");

            EStates states = new EStates();
            states.setResv(ResvState.SUBMITTED.toString());
            resv.setStates(states);
            resvRepo.save(resv);
        } else {
            log.info("db not empty");
        }


        if (urnRepo.findAll().isEmpty()) {
            log.info("adding some urn reservations");
            String gri = "es.net-123";

            String globalUrn = "net";
            EUrnReserved vcIdResv = EUrnReserved.builder()
                    .identifiers(new HashSet<>())
                    .gri(gri)
                    .urn(globalUrn)
                    .build();
            EReservedIdentifier vcId = EReservedIdentifier.builder()
                    .type(IdentifierType.VC_ID)
                    .identifier("6211")
                    .build();
            vcIdResv.getIdentifiers().add(vcId);
            urnRepo.save(vcIdResv);


            Long bw = 100L;
            String[] edges = {"nersc-tb1:3/1/1", "star-tb1:1/1/1"};
            for (String urn : edges) {
                EUrnReserved urnResv = EUrnReserved.builder()
                        .bandwidth(bw)
                        .gri(gri)
                        .urn(urn)
                        .identifiers(new HashSet<>())
                        .build();
                EReservedIdentifier vlan = EReservedIdentifier.builder()
                        .identifier("100")
                        .type(IdentifierType.VLAN).build();
                urnResv.getIdentifiers().add(vlan);
                urnRepo.save(urnResv);
            }
            String[] devices = {"nersc-tb1", "star-tb1"};
            HashMap<IdentifierType, String> deviceIdents = new HashMap<>();
            deviceIdents.put(IdentifierType.ALU_EGRESS_POLICY_ID, "6100");
            deviceIdents.put(IdentifierType.ALU_INGRESS_POLICY_ID, "6100");
            deviceIdents.put(IdentifierType.ALU_MPLS_LSP_NAME, "es.net-123_lsp");
            deviceIdents.put(IdentifierType.ALU_MPLS_PATH_NAME, "es.net-123_path");
            deviceIdents.put(IdentifierType.ALU_SDP_ID, "99");

            for (String urn : devices) {
                EUrnReserved urnResv = EUrnReserved.builder()
                        .gri(gri)
                        .urn(urn)
                        .identifiers(new HashSet<>())
                        .build();
                for (IdentifierType idType : deviceIdents.keySet()) {
                    EReservedIdentifier ident = EReservedIdentifier.builder()
                            .identifier(deviceIdents.get(idType))
                            .type(idType).build();
                    urnResv.getIdentifiers().add(ident);
                }
                urnRepo.save(urnResv);
            }



        } else {
            log.info("db not empty");
        }


    }


}
