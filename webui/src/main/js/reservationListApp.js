const React = require('react');
const ReservationHeatMap = require('./reservationHeatMap');
const NavBar = require('./navbar');
const client = require('./client');
const connHelper = require('./connectionHelper');
const networkVis = require('./networkVis');
const vis = require('../../../node_modules/vis/dist/vis');
const ReservationList = require("./reservationList");

let timeoutId = -1;
class ReservationListApp extends React.Component{

    constructor(props){
        super(props);
        this.state = {reservations: [], timeoutId: -1};
        this.setState = this.setState.bind(this);
        this.componentDidMount = this.componentDidMount.bind(this);
        this.updateReservations = this.updateReservations.bind(this);
        this.listHasChanged = this.listHasChanged.bind(this);
        this.evaluateReservationList = this.evaluateReservationList.bind(this);
        this.componentWillUnmount = this.componentWillUnmount.bind(this);
    }

    componentDidMount(){
        return this.updateReservations();
    }

    componentWillUnmount(){
        clearTimeout(timeoutId);
    }

    updateReservations(){
        client.loadJSON({method: "GET", url: '/resv/list/allconnections'})
            .then(
                (successResponse) => {
                    this.evaluateReservationList(successResponse);
                },
                (failResponse) => {
                    console.log("Error: " + failResponse.status + " - " + failResponse.statusText);
                }
        );
        timeoutId = setTimeout(this.updateReservations, 15000);   // Updates every 15 seconds
    }

    evaluateReservationList(response){
        let resvs = JSON.parse(response);
        if(this.listHasChanged(this.state.reservations, resvs)){
            this.setState({reservations: resvs});
        }
    }

    listHasChanged(oldConnectionList, newConnectionList) {
        // If new list is empty, list has only changed if old was not empty
        if($.isEmptyObject(newConnectionList)){
            return !$.isEmptyObject(oldConnectionList);
        }

        // Same size
        if(oldConnectionList.length !== newConnectionList.length)
            return true;

        // Same Reservations - All properties unchanged
        for(let o = 0; o < oldConnectionList.length; o++)
        {
            let oldConn = oldConnectionList[o];

            let newIndex = connHelper.connectionIndex(oldConn, newConnectionList);

            if(newIndex === -1)
                return true;
        }

        return false;
    }

    render() {
        return (
            <div>
                <NavBar isAuthenticated={this.props.route.isAuthenticated} isAdmin={this.props.route.isAdmin}/>
                <ReservationHeatMap reservations={this.state.reservations}/>
                <p style={{marginLeft: '40px', color: '#2c5699'}}> Select a connection to view additional reservation details.</p>
                <ReservationList reservations={this.state.reservations} alwaysExpanded={false}/>
            </div>
        );
    }
}


module.exports = ReservationListApp;