package net.es.oscars.ds.resv.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.resv.ResourceType;
import net.es.oscars.ds.resv.dao.ReservedStrRepository;
import net.es.oscars.ds.resv.dao.ConnectionRepository;
import net.es.oscars.ds.resv.dao.ReservedIntRepository;
import net.es.oscars.ds.resv.ent.*;
import net.es.oscars.st.resv.ResvState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

@Slf4j
@Component
public class ResvPopulator {


    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private ReservedIntRepository intRepo;

    @Autowired
    private ReservedStrRepository strRepo;

    @PostConstruct
    public void fill() {

        if (connRepo.findAll().isEmpty()) {
            EConnection resv = new EConnection("es.net-1");

            EStates states = new EStates();
            states.setResv(ResvState.SUBMITTED.toString());
            resv.setStates(states);
            connRepo.save(resv);
        } else {
            log.info("db not empty");
        }


        if (intRepo.findAll().isEmpty()) {
            log.info("adding some urn reservations");
            String gri = "es.net-123";
            Instant now = Instant.now();
            Instant later = now.plus(1L, ChronoUnit.DAYS);


            String globalUrn = "net";

            EReservedInteger vcResv = EReservedInteger.builder()
                    .validFrom(now)
                    .validUntil(later)
                    .resource(6111)
                    .urn(globalUrn)
                    .resourceType(ResourceType.VC_ID)
                    .build();

            intRepo.save(vcResv);


            Long bw = 100L;
            String[] edges = {"nersc-tb1:3/1/1", "star-tb1:1/1/1"};
            for (String urn : edges) {
                EReservedInteger vlanResv = EReservedInteger.builder()
                        .validFrom(now)
                        .validUntil(later)
                        .resource(123)
                        .urn(globalUrn)
                        .resourceType(ResourceType.VLAN)
                        .build();

                intRepo.save(vlanResv);
            }
            String[] devices = {"nersc-tb1", "star-tb1"};
            HashMap<ResourceType, String> strIdents = new HashMap<>();
            strIdents.put(ResourceType.ALU_MPLS_LSP_NAME, "es.net-123_lsp");
            strIdents.put(ResourceType.ALU_MPLS_PATH_NAME, "es.net-123_path");

            HashMap<ResourceType, Integer> intIdents = new HashMap<>();
            intIdents.put(ResourceType.ALU_EGRESS_POLICY_ID, 6100);
            intIdents.put(ResourceType.ALU_INGRESS_POLICY_ID, 6100);
            intIdents.put(ResourceType.ALU_SDP_ID, 99);

            for (String urn : devices) {
                for (ResourceType idType : strIdents.keySet()) {
                    String value = strIdents.get(idType);
                    EReservedString strResv = EReservedString.builder()
                            .validFrom(now)
                            .validUntil(later)
                            .resource(value)
                            .urn(globalUrn)
                            .resourceType(idType)
                            .build();

                    strRepo.save(strResv);
                }
                for (ResourceType idType : intIdents.keySet()) {
                    Integer value = intIdents.get(idType);
                    EReservedInteger intResv = EReservedInteger.builder()
                            .validFrom(now)
                            .validUntil(later)
                            .resource(value)
                            .urn(globalUrn)
                            .resourceType(idType)
                            .build();
                    intRepo.save(intResv);
                }
            }



        } else {
            log.info("db not empty");
        }


    }


}
