package net.es.oscars.dto.spec;

import lombok.*;

import java.util.Date;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Blueprint {


    @NonNull
    private Set<String> vlanFlows;

    @NonNull
    private Set<String> layer3Flows;
}