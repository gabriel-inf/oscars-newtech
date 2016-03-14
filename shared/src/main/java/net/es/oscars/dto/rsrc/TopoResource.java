package net.es.oscars.dto.rsrc;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.common.resv.IReservable;
import net.es.oscars.common.resv.IReservableQty;
import net.es.oscars.common.resv.IReservableVisitor;

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
