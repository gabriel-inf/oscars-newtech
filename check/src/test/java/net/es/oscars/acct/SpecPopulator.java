package net.es.oscars.acct;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.spec.ent.VlanFlowE;
import net.es.oscars.spec.ent.VlanFixtureE;
import net.es.oscars.spec.ent.VlanJunctionE;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.spec.ent.BlueprintE;

import java.util.HashSet;

@Slf4j
public class SpecPopulator {

    public BlueprintE aBlueprint() {
        BlueprintE bp = BlueprintE.builder()
                .vlanFlows(new HashSet<>())
                .layer3Flows(new HashSet<>())
                .build();
        VlanFlowE fl = VlanFlowE.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();
        bp.getVlanFlows().add(fl);

        VlanJunctionE j = VlanJunctionE.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .fixtures(new HashSet<>())
                .deviceUrn("albq-cr5")
                .resourceIds(new HashSet<>())
                .build();

        fl.getJunctions().add(j);

        VlanFixtureE fa = VlanFixtureE.builder()
                .vlanExpression("2-100")
                .inMbps(100)
                .egMbps(100)
                .portUrn("albq-cr5:1/0/0")
                .build();

        VlanFixtureE fb = VlanFixtureE.builder()
                .vlanExpression("2-100")
                .inMbps(100)
                .egMbps(100)
                .portUrn("albq-cr5:2/0/0")
                .build();

        j.getFixtures().add(fa);
        j.getFixtures().add(fb);

        return bp;





    }


}
