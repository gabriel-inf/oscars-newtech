package net.es.oscars.pss.help;

import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.params.Lsp;
import net.es.oscars.dto.pss.params.MplsPath;
import net.es.oscars.dto.pss.params.Policing;
import net.es.oscars.dto.pss.params.alu.*;
import net.es.oscars.dto.topo.enums.DeviceModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class CommandHelper {

    public Command getAlu() {

        AluParams aluParams = this.sampleAluParams();

        return Command.builder()
                .connectionId("someId")
                .device("someDevice")
                .model(DeviceModel.ALCATEL_SR7750)
                .alu(aluParams)
                .type(CommandType.BUILD)
                .build();
    }

    public AluParams sampleAluParams() {
        AluSap sap_1 = AluSap.builder()
                .description("some desc")
                .port("1/1/1")
                .ingressQosId(11)
                .egressQosId(22)
                .vlan(100)
                .build();
        AluSap sap_2 = AluSap.builder()
                .description("some desc")
                .port("1/2/1")
                .ingressQosId(33)
                .egressQosId(44)
                .vlan(100)
                .build();
        List<AluSap> saps = new ArrayList<>();
        saps.add(sap_1);
        saps.add(sap_2);

        AluSdpToVcId sdpToVcId = AluSdpToVcId.builder()
                .vcId(200)
                .sdpId(100)
                .build();

        List<AluSdpToVcId> sdpToVcIds = new ArrayList<>();
        sdpToVcIds.add(sdpToVcId);
        AluVpls vpls = AluVpls.builder()
                .saps(saps)
                .description("some desc")
                .endpointName("endpoint")
                .sdpToVcIds(sdpToVcIds)
                .serviceName("service")
                .svcId(100)
                .build();

        AluQos qos_1_in = AluQos.builder()
                .mbps(100)
                .description("desc")
                .policyId(11)
                .policyName("qos_1_in")
                .policing(Policing.STRICT)
                .build();
        AluQos qos_1_eg = AluQos.builder()
                .mbps(100)
                .description("desc")
                .policyId(22)
                .policyName("qos_1_eg")
                .policing(Policing.STRICT)
                .build();


        AluQos qos_2_in = AluQos.builder()
                .mbps(100)
                .description("desc")
                .policyId(33)
                .policyName("qos_2_in")
                .policing(Policing.STRICT)
                .build();

        AluQos qos_2_eg = AluQos.builder()
                .mbps(100)
                .description("desc")
                .policyId(44)
                .policyName("qos_2_eg")
                .policing(Policing.STRICT)
                .build();

        List<AluQos> qoses = new ArrayList<>();
        qoses.add(qos_1_in);
        qoses.add(qos_1_eg);
        qoses.add(qos_2_in);
        qoses.add(qos_2_eg);

        List<Lsp> lsps = new ArrayList<>();
        List<MplsPath> paths = new ArrayList<>();
        List<AluSdp> sdps = new ArrayList<>();

        return AluParams.builder()
                .aluVpls(vpls)
                .applyQos(true)
                .loopbackAddress("10.1.1.1")
                .loopbackInterface("foobar")
                .lsps(lsps)
                .qoses(qoses)
                .paths(paths)
                .sdps(sdps)
                .build();

    }
}
