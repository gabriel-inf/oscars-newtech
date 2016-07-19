package net.es.oscars.pss;

import net.es.oscars.pss.cmd.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;


@Component
public class ExParamsBuilder {

    public ExGenerationParams sampleParams() {
        ExVlan vlan = ExVlan.builder()
                .vlanId(333)
                .name("vlan_333")
                .description("it's 333!")
                .build();

        ExGenerationParams params = ExGenerationParams.builder()
                .exVlan(vlan)
                .ifces(new ArrayList<>())
                .build();

        ExIfce myIfce = ExIfce.builder()
                .port("xe-1/1/1")
                .vlan_name(vlan.getName())
                .build();

        params.getIfces().add(myIfce);


        return params;
    }
}
