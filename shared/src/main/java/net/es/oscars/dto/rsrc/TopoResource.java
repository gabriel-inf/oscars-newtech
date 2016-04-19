package net.es.oscars.dto.rsrc;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopoResource {
    private List<String> topoVertexUrns;
    private Set<ReservableQty> reservableQties;
    private Set<ReservableRanges> reservableRanges;

}
