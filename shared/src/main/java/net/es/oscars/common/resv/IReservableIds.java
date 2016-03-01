package net.es.oscars.common.resv;

public interface IReservableIds extends IReservable {
    void accept(IReservableVisitor visitor);

}
