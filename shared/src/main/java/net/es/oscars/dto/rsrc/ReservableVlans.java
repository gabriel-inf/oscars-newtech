package net.es.oscars.dto.rsrc;

import lombok.Data;
import net.es.oscars.common.resv.IReservableIdentifier;
import net.es.oscars.common.resv.IReservableVisitor;
import org.apache.commons.lang3.Range;

import java.util.List;

@Data
public class ReservableVlans implements IReservableIdentifier {
    protected List<Range<Integer>> vlans;

    public ReservableVlans() {
    }
    public ReservableVlans(List<Range<Integer>> vlans) {
        this.vlans = vlans;
    }

    public void accept(IReservableVisitor visitor) {
        visitor.visit(this);
    }
}
