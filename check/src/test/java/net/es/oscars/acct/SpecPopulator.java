package net.es.oscars.acct;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.spec.ent.EFlow;
import net.es.oscars.spec.ent.EVlanFixture;
import net.es.oscars.spec.ent.EVlanJunction;
import net.es.oscars.pss.enums.EthJunctionType;
import net.es.oscars.spec.ent.EBlueprint;

import java.util.HashSet;

@Slf4j
public class SpecPopulator {

    public EBlueprint aBlueprint() {
        EBlueprint bp = EBlueprint.builder()
                .flows(new HashSet<>())
                .build();
        EFlow fl = EFlow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();
        bp.getFlows().add(fl);

        EVlanJunction j = EVlanJunction.builder()
                .junctionType(EthJunctionType.REQUESTED)
                .fixtures(new HashSet<>())
                .deviceUrn("albq-cr5")
                .resourceIds(new HashSet<>())
                .build();

        fl.getJunctions().add(j);

        EVlanFixture fa = EVlanFixture.builder()
                .vlanExpression("2-100")
                .inMbps(100)
                .egMbps(100)
                .portUrn("albq-cr5:1/0/0")
                .build();

        EVlanFixture fb = EVlanFixture.builder()
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
