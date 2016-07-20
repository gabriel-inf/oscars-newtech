package net.es.oscars.pss;

import net.es.oscars.pss.cmd.*;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;


@Component
public class MxParamsBuilder {

    public MxGenerationParams sampleParams() {
        MxGenerationParams params = MxGenerationParams.builder()
                .applyQos(true)
                .loopbackAddress("134.55.99.11")
                .loopbackInterface("es.net-1234_loopback")
                .lsps(new HashMap<>())
                .paths(new ArrayList<>())
                .policing(new HashMap<>())
                .build();

        MxPolicer policer = MxPolicer.builder()
                .mbps(100)
                .name("policer-name")
                .policing(Policing.STRICT)
                .build();

        MxFilter pipeFilter = MxFilter.builder().name("main").build();

        params.getPolicing().put(pipeFilter, policer);


        MxVpls vpls = MxVpls.builder()
                .serviceName("es.net-1234")
                .description("es.net-1234, to someplace")
                .vcId(6011)
                .ifces(new ArrayList<>())
                .communityName("myCommName")
                .lspNeighbors(new HashMap<>())
                .policyName("somePolicyName")
                .build();


        MxIfce myIfce = MxIfce.builder()
                .description("ifce description")
                .port("xe-1/1/1")
                .vlan(333)
                .build();

        vpls.getIfces().add(myIfce);

        params.setMxVpls(vpls);


        Lsp lsp = Lsp.builder()
                .metric(65100)
                .holdPriority(5)
                .setupPriority(5)
                .name("es.net-1234")
                .pathName("es.net-1234_pri")
                .to("134.55.200.1")
                .build();

        MxFilter lspFilter = MxFilter.builder().name("es.net-1234-pri-filter").build();
        params.getLsps().put(lsp, lspFilter);

        MplsPath mplsPath = MplsPath.builder()
                .name("es.net-1234_pri").hops(new ArrayList<>()).build();

        MplsHop hop_a = MplsHop.builder()
                .address("134.55.11.1").order(1).build();
        MplsHop hop_b = MplsHop.builder()
                .address("134.55.22.2").order(2).build();

        mplsPath.getHops().add(hop_a);
        mplsPath.getHops().add(hop_b);

        params.getPaths().add(mplsPath);

        vpls.getLspNeighbors().put("134.55.200.1", "134.55.200.1");



        return params;
    }
}
