package net.es.oscars.dto.pss.params.ex;

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
