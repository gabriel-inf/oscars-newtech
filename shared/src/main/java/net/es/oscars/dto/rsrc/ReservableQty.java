package net.es.oscars.dto.rsrc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.common.IntRange;
import net.es.oscars.common.resv.IReservableQty;
import net.es.oscars.common.resv.IReservableVisitor;
import net.es.oscars.common.resv.ResourceType;
import org.apache.commons.lang3.Range;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservableQty {
    protected IntRange range;
    protected ResourceType type;

}
