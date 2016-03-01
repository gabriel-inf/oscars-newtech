package net.es.oscars.dto.rsrc;

import lombok.Data;
import net.es.oscars.common.resv.IReservableQty;
import net.es.oscars.common.resv.IReservableVisitor;
import org.apache.commons.lang3.Range;

@Data
public class ReservableBW implements IReservableQty {
    protected Range<Long> range;
    public ReservableBW() {}

    public ReservableBW(Range<Long> range) {
        this.range = range;
    }


    public void accept(IReservableVisitor visitor) {
        visitor.visit(this);
    }

}
