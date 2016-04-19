package net.es.oscars.dto.rsrc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.resv.ResourceType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservableRanges {
    protected List<IntRange> ranges;
    protected ResourceType type;


}
