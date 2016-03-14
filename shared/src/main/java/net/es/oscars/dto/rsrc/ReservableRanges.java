package net.es.oscars.dto.rsrc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.common.IntRange;
import net.es.oscars.common.resv.IReservable;
import net.es.oscars.common.resv.IReservableIdentifier;
import net.es.oscars.common.resv.IReservableVisitor;
import net.es.oscars.common.resv.ResourceType;
import org.apache.commons.lang3.Range;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservableRanges {
    protected List<IntRange> ranges;
    protected ResourceType type;


}
