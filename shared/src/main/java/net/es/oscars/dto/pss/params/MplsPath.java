package net.es.oscars.dto.pss.params;

import lombok.*;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MplsPath {

    private String name;

    private List<MplsHop> hops;
}
