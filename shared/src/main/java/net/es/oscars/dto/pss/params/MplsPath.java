package net.es.oscars.dto.pss.params;

import lombok.*;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MplsPath {

    @NonNull
    private String name;

    @NonNull
    private List<MplsHop> hops;
}
