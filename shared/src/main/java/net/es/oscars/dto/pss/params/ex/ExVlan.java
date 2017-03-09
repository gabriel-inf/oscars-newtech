package net.es.oscars.dto.pss.params.ex;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExVlan {

    private Integer vlanId;

    private String description;

    private String name;

}
