package net.es.oscars.dto.pss.params;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MplsHop {

    @NonNull
    private Integer order;

    @NonNull
    private String address;

}
