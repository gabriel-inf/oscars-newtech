package net.es.oscars.common.resv;

public interface IReservableQty extends IReservable {
    void accept(IReservableVisitor visitor);
}
