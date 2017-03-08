package net.es.oscars.pss;

import net.es.oscars.dto.pss.params.Lsp;
import net.es.oscars.dto.pss.params.MplsHop;
import net.es.oscars.dto.pss.params.MplsPath;
import net.es.oscars.dto.pss.params.Policing;
import net.es.oscars.dto.pss.params.alu.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;


@Component
public class AluParamsBuilder {

    public AluParams sampleParams() {
        AluParams params = AluParams.builder()
                .applyQos(true)
                .loopbackAddress("134.55.99.11")
                .loopbackInterface("es.net-1234_loopback")
                .lsps(new ArrayList<>())
                .qoses(new ArrayList<>())
                .paths(new ArrayList<>())
                .sdps(new ArrayList<>())
                .build();

        Lsp lsp = Lsp.builder()
                .metric(65100)
                .holdPriority(5)
                .setupPriority(5)
                .name("es.net-1234")
                .pathName("es.net-1234_pri")
                .to("134.55.200.1")
                .build();

        params.getLsps().add(lsp);


        AluQos qos = AluQos.builder().
                type(AluQosType.SAP_INGRESS)
                .description("es.net-1234")
                .mbps(100)
                .policing(Policing.STRICT)
                .policyId(6511)
                .policyName("es.net-1234").build();

        params.getQoses().add(qos);

        MplsPath mplsPath = MplsPath.builder()
                .name("es.net-1234_pri").hops(new ArrayList<>()).build();

        MplsHop hop_a = MplsHop.builder()
                .address("134.55.11.1").order(1).build();
        MplsHop hop_b = MplsHop.builder()
                .address("134.55.22.2").order(2).build();

        mplsPath.getHops().add(hop_a);
        mplsPath.getHops().add(hop_b);

        params.getPaths().add(mplsPath);


        AluSdp sdp = AluSdp.builder()
                .description("es.net-1234_sdp")
                .farEnd("134.55.200.99")
                .lspName("es.net-1234")
                .sdpId(6511)
                .build();

        params.getSdps().add(sdp);


        AluVpls vpls = AluVpls.builder()
                .serviceName("es.net-1234")
                .description("es.net-1234, to someplace")
                .endpointName("es.net-1234_endpoint")
                .sdp(sdp)
                .vcId(6011)
                .saps(new ArrayList<>())
                .build();



        AluSap mySap = AluSap.builder()
                .description("sap description")
                .ingressQosId(6011)
                .egressQosId(6011)
                .port("1/1/1")
                .vlan(333)
                .build();

        vpls.getSaps().add(mySap);

        params.setAluVpls(vpls);

        return params;
    }
}
