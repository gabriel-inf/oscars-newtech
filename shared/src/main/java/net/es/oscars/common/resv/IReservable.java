package net.es.oscars.common.resv;

public interface IReservable {
    void accept(IReservableVisitor visitor);
}
