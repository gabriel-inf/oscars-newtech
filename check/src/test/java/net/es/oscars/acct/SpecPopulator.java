package net.es.oscars.acct;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.*;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.dto.topo.enums.DeviceType;
import net.es.oscars.dto.topo.enums.UrnType;

import java.util.HashSet;

@Slf4j
public class SpecPopulator {

    public RequestedBlueprintE aBlueprint() {
        RequestedVlanFlowE fl = RequestedVlanFlowE.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .minPipes(0)
                .maxPipes(0)
                .build();

        RequestedBlueprintE bp = RequestedBlueprintE.builder()
                .vlanFlow(fl)
                .layer3Flow(Layer3FlowE.builder().build())
                .build();


        UrnE albqcr5 = UrnE.builder()
                .deviceModel(DeviceModel.JUNIPER_EX)
                .capabilities(new HashSet<>())
                .deviceType(DeviceType.SWITCH)
                .urnType(UrnType.DEVICE)
                .urn("albq-cr5")
                .valid(true)
                .build();
        albqcr5.getCapabilities().add(Layer.ETHERNET);


        UrnE albqcr5_1_0_0 = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("albq-cr5:1/0/0")
                .valid(true)
                .build();
        albqcr5_1_0_0.getCapabilities().add(Layer.ETHERNET);

        UrnE albqcr5_2_0_0 = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("albq-cr5:2/0/0")
                .valid(true)
                .build();
        albqcr5_2_0_0.getCapabilities().add(Layer.ETHERNET);




        RequestedVlanJunctionE j = RequestedVlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .fixtures(new HashSet<>())
                .deviceUrn(albqcr5)
                .build();

        fl.getJunctions().add(j);

        RequestedVlanFixtureE fa = RequestedVlanFixtureE.builder()
                .vlanExpression("2-100")
                .inMbps(100)
                .egMbps(100)
                .portUrn(albqcr5_1_0_0)
                .build();

        RequestedVlanFixtureE fb = RequestedVlanFixtureE.builder()
                .vlanExpression("2-100")
                .inMbps(100)
                .egMbps(100)
                .portUrn(albqcr5_2_0_0)
                .build();

        j.getFixtures().add(fa);
        j.getFixtures().add(fb);

        return bp;





    }


}
