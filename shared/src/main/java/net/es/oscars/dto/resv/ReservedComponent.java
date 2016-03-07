package net.es.oscars.dto.resv;

import lombok.Data;
import lombok.NonNull;
import net.es.oscars.common.resv.ComponentType;
import net.es.oscars.common.resv.IReservable;
import net.es.oscars.common.resv.IReservableVisitor;
import net.es.oscars.common.resv.IReserved;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReservedComponent {
    public ReservedComponent() {

    }
    private ComponentType componentType;

    private List<IReserved> resources = new ArrayList<>();

}
