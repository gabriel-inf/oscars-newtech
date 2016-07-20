package net.es.oscars.pss.cmd;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;


@Data
@Builder
public class ExVlan {

    @NonNull
    private Integer vlanId;

    @NonNull
    private String description;

    @NonNull
    private String name;

}
