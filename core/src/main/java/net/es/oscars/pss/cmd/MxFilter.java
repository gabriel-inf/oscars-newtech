package net.es.oscars.pss.cmd;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class MxFilter {

    @NonNull
    private String name;

}
