package net.es.oscars.pss.cmd;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExGenerationParams {

    @NonNull
    private List<ExIfce> ifces;

    @NonNull
    private ExVlan exVlan;


}
