package net.es.oscars.dto.rsrc;


import lombok.Data;
import net.es.oscars.common.resv.IReservable;
import net.es.oscars.common.resv.IReservableVisitor;

import java.util.List;

@Data
public class Allocatable implements IReservable {
    public Allocatable() {

    }
    private String allocationScope;
    private List<IReservable> reservables;

    public void accept(IReservableVisitor visitor) {

        for (IReservable reservable : reservables) {
            reservable.accept(visitor);
        }
    }
}
