package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.resv.ResourceType;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReservedPssResourceE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String urn;

    @NonNull
    private ResourceType resourceType;

    @NonNull
    private Integer resource;

    private Instant beginning;

    private Instant ending;


    public static ReservedPssResourceE makeSvcIdResource(String deviceUrn, Integer svcId,
                                                   Instant beginning, Instant ending) {

        ReservedPssResourceE svcIdResource = ReservedPssResourceE.builder()
                .resource(svcId)
                .urn(deviceUrn)
                .resourceType(ResourceType.ALU_SVC_ID)
                .beginning(beginning)
                .ending(ending)
                .build();
        return svcIdResource;
    }
    public static ReservedPssResourceE makeQosIdResource(String deviceUrn, Integer qosId, ResourceType rt,
                                                   Instant beginning, Instant ending) {
        ReservedPssResourceE qosIdResource = ReservedPssResourceE.builder()
                .resource(qosId)
                .urn(deviceUrn)
                .resourceType(rt)
                .beginning(beginning)
                .ending(ending)
                .build();
        return qosIdResource;
    }

    public static ReservedPssResourceE makeVcIdResource(Integer vcId,
                                                  Instant beginning, Instant ending) {

        ReservedPssResourceE vcIdResource = ReservedPssResourceE.builder()
                .resource(vcId)
                .urn(ResourceType.GLOBAL)
                .resourceType(ResourceType.VC_ID)
                .beginning(beginning)
                .ending(ending)
                .build();
        return vcIdResource;
    }

    public static ReservedPssResourceE makeSdpIdResource(String deviceUrn, Integer sdpId,
                                                   Instant beginning, Instant ending) {
        ReservedPssResourceE sdpIdResource = ReservedPssResourceE.builder()
                .urn(deviceUrn)
                .resource(sdpId)
                .resourceType(ResourceType.ALU_SDP_ID)
                .beginning(beginning)
                .ending(ending)
                .build();

        return sdpIdResource;
    }


}
