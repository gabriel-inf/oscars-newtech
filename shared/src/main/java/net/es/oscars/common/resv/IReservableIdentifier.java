package net.es.oscars.common.resv;

public interface IReservableIdentifier extends IReservable {
    void accept(IReservableVisitor visitor);

}
