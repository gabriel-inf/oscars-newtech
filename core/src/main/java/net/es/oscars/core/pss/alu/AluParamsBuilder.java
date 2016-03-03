package net.es.oscars.core.pss.alu;

import net.es.oscars.core.pss.ftl.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AluParamsBuilder {

    public AluGenerationParams sampleParams() {
        AluGenerationParams params = new AluGenerationParams();

        params.setApplyQos(true);
        params.setLoopbackAddress("134.55.99.11");
        params.setLoopbackInterface("es.net-1234_loopback");

        Lsp lsp = new Lsp();
        lsp.setMetric(65100);
        lsp.setHoldPriority(5);
        lsp.setSetupPriority(5);
        lsp.setName("es.net-1234");
        lsp.setPathName("es.net-1234_pri");
        lsp.setTo("134.55.200.1");
        params.getLsps().add(lsp);


        AluQos qos = new AluQos();
        qos.setType(AluQosType.SAP_INGRESS);
        qos.setDescription("es.net-1234");
        qos.setMbps(100);
        qos.setPolicing(Policing.STRICT);
        qos.setPolicyId(1);
        qos.setPolicyName("es.net-1234");
        params.getQosList().add(qos);


        MplsPath mplsPath = new MplsPath();
        mplsPath.setName("es.net-1234_pri");
        MplsHop hop_a = new MplsHop();
        hop_a.setAddress("134.55.11.1");
        hop_a.setOrder(1);
        MplsHop hop_b = new MplsHop();
        hop_b.setAddress("134.55.22.2");
        hop_b.setOrder(2);
        mplsPath.getHops().add(hop_b);

        params.getPaths().add(mplsPath);



        AluSdp sdp = new AluSdp();
        sdp.setDescription("es.net-1234_sdp");
        sdp.setFarEnd("134.55.200.99");
        sdp.setLspName("es.net-1234");
        sdp.setSdpId(6011);
        params.getSdps().add(sdp);



        AluVpls vpls = new AluVpls();
        vpls.setServiceName("es.net-1234");
        vpls.setHasProtect(false);
        vpls.setDescription("es.net-1234, to someplace");
        vpls.setEndpoint(true);
        vpls.setEndpointName("es.net-1234_endpoint");
        vpls.setProtectSdp(null);
        vpls.setProtectVcId(null);
        vpls.setSdp(sdp);
        vpls.setVcId(6011);
        vpls.setProtectVcId(null);
        vpls.setProtectSdp(null);

        AluSap mySap = new AluSap();
        mySap.setDescription("foobar");
        mySap.setIngressQosId(6011);
        mySap.setEgressQosId(6011);
        mySap.setPort("1/1/1");
        mySap.setVlan(333);

        vpls.getSaps().add(mySap);

        params.setAluVpls(vpls);

        return params;
    }
}
