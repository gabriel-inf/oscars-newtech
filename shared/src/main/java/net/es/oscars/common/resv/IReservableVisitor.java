package net.es.oscars.common.resv;



public interface IReservableVisitor {
    void visit(IReservableQty quantity);
    void visit(IReservableIds ids);


}
