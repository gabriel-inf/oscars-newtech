package net.es.oscars.dto.rsrc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.resv.ResourceType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservableQty {
    protected IntRange range;
    protected ResourceType type;

}
