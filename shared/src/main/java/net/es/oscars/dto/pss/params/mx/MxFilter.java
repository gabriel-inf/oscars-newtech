package net.es.oscars.dto.pss.params.mx;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class MxFilter {

    @NonNull
    private String name;

}
